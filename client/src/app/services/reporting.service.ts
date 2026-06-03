import { DOCUMENT } from "@angular/common";
import { Inject, Injectable } from "@angular/core";
import { NavigationEnd, Router } from "@angular/router";
import { Observable, of } from "rxjs";
import { catchError, filter, map } from "rxjs/operators";
import { DataService } from "@app/data.service";
import { LoggerService } from "@app/services/logger.service";
import { SettingsService } from "@app/services/settings.service";

// Declare standard gtag function
declare const gtag: Function;

@Injectable({
  providedIn: "root",
})
export class ReportingService {
  private metricsEnabled: boolean = false;
  private measurementId: string = "";
  private scriptLoaded: boolean = false;
  private eventQueue: any[] = [];
  private configLoaded: boolean = false;

  constructor(
    private router: Router,
    private settingsService: SettingsService,
    private dataService: DataService,
    @Inject(DOCUMENT) private document: Document,
    private logger: LoggerService,
  ) {
    this.ensureGtagFallback();
  }

  private ensureGtagFallback() {
    const window = this.document.defaultView as any;
    if (window && typeof window.gtag === "undefined") {
      this.logger.info("Reporting: Defining global gtag fallback function.");
      window.dataLayer = window.dataLayer || [];
      window.gtag = function () {
        window.dataLayer.push(arguments);
      };
    }
  }

  public isEnabled(): boolean {
    return this.settingsService.getSettings().shareReporting;
  }

  public toggleReporting(): Observable<{
    success: boolean;
    titleKey?: string;
    messageKey?: string;
  }> {
    const settings = this.settingsService.getSettings();
    const newValue = !settings.shareReporting;
    settings.shareReporting = newValue;
    this.settingsService.saveSettings(settings);
    this.updateOptOutStatus();

    const serverIp = settings.serverIp?.toLowerCase();
    const isLocal =
      serverIp === "localhost" ||
      serverIp === "127.0.0.1" ||
      serverIp === "0.0.0.0" ||
      serverIp === "::1" ||
      serverIp === "0:0:0:0:0:0:0:1" ||
      !serverIp;

    if (isLocal) {
      return this.dataService.toggleServerReporting(newValue).pipe(
        map(() => ({ success: true })),
        catchError((err) => {
          this.logger.error(
            "Failed to synchronize server Reporting setting",
            err,
          );
          return of({
            success: false,
            titleKey: newValue
              ? "RDS_Reporting_ENABLED_TITLE"
              : "RDS_Reporting_DISABLED_TITLE",
            messageKey: "RDS_Reporting_SYNC_ERROR",
          });
        }),
      );
    } else {
      return of({ success: true });
    }
  }

  public initTracking() {
    this.updateOptOutStatus();

    // 1. Setup Page View tracking on Router changes
    this.router.events
      .pipe(filter((event) => event instanceof NavigationEnd))
      .subscribe((event: any) => {
        this.trackPageView(event.urlAfterRedirects);
      });
  }

  public updateOptOutStatus() {
    const settings = this.settingsService.getSettings();
    this.metricsEnabled = settings.shareReporting;
    this.logger.debug("Reporting: updateOptOutStatus called", {
      metricsEnabled: this.metricsEnabled,
      scriptLoaded: this.scriptLoaded,
    });

    if (this.metricsEnabled && !this.scriptLoaded) {
      this.loadGoogleReportingScript();
    }
  }

  private loadGoogleReportingScript() {
    this.logger.info("Reporting: loadGoogleReportingScript called");
    this.scriptLoaded = true;

    this.dataService.getServerReportingConfig().subscribe({
      next: (config: any) => {
        this.logger.info("Reporting: Received config from server", {
          measurementId: !!config.measurementId,
          clientId: !!config.clientId,
        });
        if (config.measurementId) {
          this.measurementId = config.measurementId;
        }

        if (!this.measurementId) {
          this.logger.warn(
            "Reporting enabled but no Measurement ID is configured on the server. Aborting script injection.",
          );
          return;
        }

        const clientId = config.clientId;
        this.logger.info(
          "Reporting: Configuring GTAG for ID",
          this.measurementId,
        );

        // Push config to dataLayer immediately via fallback
        const window = this.document.defaultView as any;
        if (window && typeof window.gtag === "function") {
          this.logger.info("Reporting: Pushing initial config to dataLayer");
          window.gtag("js", new Date());
          window.gtag("config", this.measurementId, {
            send_page_view: false,
            client_id: clientId, // Linked to the server-generated client ID
          });
        }

        const script1 = this.document.createElement("script");
        script1.async = true;
        script1.src = `https://www.googletagmanager.com/gtag/js?id=${this.measurementId}`;
        script1.onload = () =>
          this.logger.info(
            "Reporting: GTAG library script loaded successfully.",
          );
        script1.onerror = (e) =>
          this.logger.error(
            "Reporting: GTAG library script failed to load.",
            e,
          );
        this.document.head.appendChild(script1);

        this.configLoaded = true;
        this.processQueue();
      },
      error: (err: any) => {
        this.logger.warn(
          "Failed to fetch server tracking ID, falling back to local.",
          err,
        );
        this.configLoaded = true; // Proceed with whatever we have
        this.processQueue();
        if (!this.measurementId) return;

        // Fallback without client_id
        if (!this.measurementId) return;

        const window = this.document.defaultView as any;
        if (window && typeof window.gtag === "function") {
          window.gtag("js", new Date());
          window.gtag("config", this.measurementId, { send_page_view: false });
        }

        const script1 = this.document.createElement("script");
        script1.async = true;
        script1.src = `https://www.googletagmanager.com/gtag/js?id=${this.measurementId}`;
        this.document.head.appendChild(script1);
      },
    });
  }

  private trackPageView(url: string) {
    if (!this.metricsEnabled) {
      this.logger.debug("Reporting: Page view skipped (metrics disabled)");
      return;
    }

    if (!this.configLoaded) {
      this.logger.info(
        "Reporting: Queueing page view event until config is loaded",
        { url },
      );
      this.eventQueue.push({ type: "page_view", url });
      return;
    }

    try {
      const window = this.document.defaultView as any;
      const title = this.document.title || "Race Coordinator AI";
      const fullUrl = window ? window.location.origin + url : url;

      this.logger.info("Reporting: Tracking page view", {
        url,
        title,
        measurementId: this.measurementId,
      });
      gtag("event", "page_view", {
        page_path: url,
        page_location: fullUrl,
        page_title: title,
        send_to: this.measurementId, // Explicitly send to our measurement ID
      });
    } catch (e) {
      this.logger.warn("Reporting: Error in trackPageView", e);
    }
  }

  // Click Event Tracking
  public trackClick(eventName: string, params: Record<string, any> = {}) {
    if (!this.metricsEnabled) return;

    if (!this.configLoaded) {
      this.logger.info(
        "Reporting: Queueing click event until config is loaded",
        {
          eventName,
        },
      );
      this.eventQueue.push({ type: "click", eventName, params });
      return;
    }

    try {
      gtag("event", eventName, {
        ...params,
        event_category: "engagement",
        event_label: "button_click",
        send_to: this.measurementId,
      });
    } catch (e) {
      this.logger.warn("Reporting: Error in trackClick", e);
    }
  }

  private processQueue() {
    if (this.eventQueue.length === 0) return;
    this.logger.info(
      `Reporting: Processing ${this.eventQueue.length} queued events`,
    );
    const queue = [...this.eventQueue];
    this.eventQueue = [];
    queue.forEach((event) => {
      if (event.type === "page_view") {
        this.trackPageView(event.url);
      } else if (event.type === "click") {
        this.trackClick(event.eventName, event.params);
      }
    });
  }
}
