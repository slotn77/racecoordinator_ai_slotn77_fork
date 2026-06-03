import { TestbedHarnessEnvironment } from "@angular/cdk/testing/testbed";
import { Component, CUSTOM_ELEMENTS_SCHEMA } from "@angular/core";
import {
  ComponentFixture,
  fakeAsync,
  TestBed,
  tick,
} from "@angular/core/testing";
import { ActivatedRoute } from "@angular/router";
import { BehaviorSubject, of } from "rxjs";
import { AcknowledgementModalComponent } from "@app/components/shared/acknowledgement-modal/acknowledgement-modal.component";
import { UndoManager } from "@app/components/shared/undo-redo-controls/undo-manager";
import { Settings } from "@app/models/settings";
import { TranslatePipe } from "@app/pipes/translate.pipe";
import { HelpService } from "@app/services/help.service";
import { ReportingService } from "@app/services/reporting.service";
import { SettingsService } from "@app/services/settings.service";
import { TranslationService } from "@app/services/translation.service";
import { createTestSettings } from "@app/testing/unit-test-mocks";

import { ToolbarHarness } from "./testing/toolbar.harness";
import { ToolbarComponent } from "./toolbar.component";

@Component({
  standalone: true,selector: "app-help-overlay",
  template: "",
})
class MockHelpOverlayComponent {}

describe("ToolbarComponent", () => {
  let component: ToolbarComponent;
  let fixture: ComponentFixture<ToolbarComponent>;
  let harness: ToolbarHarness;
  let translationServiceSpy: jasmine.SpyObj<TranslationService>;
  let ReportingServiceSpy: jasmine.SpyObj<ReportingService>;
  let helpServiceSpy: jasmine.SpyObj<HelpService>;
  let settingsServiceSpy: jasmine.SpyObj<SettingsService>;
  let mockActivatedRoute: any;
  let queryParamsSubject: BehaviorSubject<any>;

  beforeEach(async () => {
    translationServiceSpy = jasmine.createSpyObj("TranslationService", [
      "translate",
    ]);
    translationServiceSpy.translate.and.callFake((key: string) => key);

    ReportingServiceSpy = jasmine.createSpyObj("ReportingService", [
      "isEnabled",
      "toggleReporting",
      "trackClick",
    ]);
    ReportingServiceSpy.isEnabled.and.returnValue(true);
    ReportingServiceSpy.toggleReporting.and.returnValue(of({ success: true }));

    helpServiceSpy = jasmine.createSpyObj("HelpService", ["startGuide"]);
    helpServiceSpy.isVisible$ = of(false);
    helpServiceSpy.currentStep$ = of(null);
    helpServiceSpy.hasNext$ = of(false);
    helpServiceSpy.hasPrevious$ = of(false);

    settingsServiceSpy = jasmine.createSpyObj("SettingsService", [
      "getSettings",
      "saveSettings",
    ]);
    settingsServiceSpy.getSettings.and.returnValue(createTestSettings());

    queryParamsSubject = new BehaviorSubject({});
    mockActivatedRoute = {
      queryParams: queryParamsSubject.asObservable(),
    };

    await TestBed.configureTestingModule({
      imports: [
        ToolbarComponent,
        TranslatePipe,
        AcknowledgementModalComponent,
        MockHelpOverlayComponent,
      ],
      providers: [
        { provide: TranslationService, useValue: translationServiceSpy },
        { provide: ReportingService, useValue: ReportingServiceSpy },
        { provide: HelpService, useValue: helpServiceSpy },
        { provide: SettingsService, useValue: settingsServiceSpy },
        { provide: ActivatedRoute, useValue: mockActivatedRoute },
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(ToolbarComponent);
    component = fixture.componentInstance;
    harness = await TestbedHarnessEnvironment.harnessForFixture(
      fixture,
      ToolbarHarness,
    );
    fixture.detectChanges();
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });

  it("should show edit button when showEdit is true", async () => {
    fixture.componentRef.setInput("showEdit", true);
    fixture.detectChanges();
    expect(await harness.isEditVisible()).toBeTrue();
  });

  it("should emit edit event when edit button is clicked", async () => {
    spyOn(component.edit, "emit");
    fixture.componentRef.setInput("showEdit", true);
    fixture.detectChanges();
    await harness.clickEdit();
    expect(component.edit.emit).toHaveBeenCalled();
  });

  it("should show help button when showHelp is true", async () => {
    fixture.componentRef.setInput("showHelp", true);
    fixture.detectChanges();
    expect(await harness.isHelpVisible()).toBeTrue();
  });

  it("should emit help event when help button is clicked", async () => {
    spyOn(component.help, "emit");
    fixture.componentRef.setInput("showHelp", true);
    fixture.detectChanges();
    await harness.clickHelp();
    expect(component.help.emit).toHaveBeenCalled();
  });

  it("should show delete button when showDelete is true", async () => {
    fixture.componentRef.setInput("showDelete", true);
    fixture.detectChanges();
    expect(await harness.isDeleteVisible()).toBeTrue();
  });

  it("should emit delete event when delete button is clicked", async () => {
    spyOn(component.delete, "emit");
    fixture.componentRef.setInput("showDelete", true);
    fixture.detectChanges();
    await harness.clickDelete();
    expect(component.delete.emit).toHaveBeenCalled();
  });

  it("should show undo/redo when showUndo/showRedo are true", async () => {
    fixture.componentRef.setInput("showUndo", true);
    fixture.componentRef.setInput("showRedo", true);
    fixture.detectChanges();
    expect(await harness.isUndoVisible()).toBeTrue();
    expect(await harness.isRedoVisible()).toBeTrue();
  });

  it("should call undoManager.undo() when undo button is clicked", async () => {
    const config = {
      clonner: (item: any) => ({ ...item }),
      equalizer: (a: any, b: any) => JSON.stringify(a) === JSON.stringify(b),
      applier: () => {},
    };
    let state = { foo: "bar" };
    const manager = new UndoManager<any>(config, () => state);
    spyOn(manager, "undo");

    fixture.componentRef.setInput("showUndo", true);
    fixture.componentRef.setInput("undoManager", manager);
    fixture.detectChanges();

    // First commit captures initial snapshot
    manager.commitState();

    // Second commit after change pushes to undo stack
    state = { foo: "baz" };
    manager.commitState();

    expect(await harness.isUndoDisabled()).toBeFalse();
    await harness.clickUndo();
    expect(manager.undo).toHaveBeenCalled();
  });

  it("should call undoManager.redo() when redo button is clicked", async () => {
    const config = {
      clonner: (item: any) => ({ ...item }),
      equalizer: (a: any, b: any) => JSON.stringify(a) === JSON.stringify(b),
      applier: () => {},
    };
    let state = { foo: "bar" };
    const manager = new UndoManager<any>(config, () => state);
    spyOn(manager, "redo");

    fixture.componentRef.setInput("showRedo", true);
    fixture.componentRef.setInput("undoManager", manager);
    fixture.detectChanges();

    manager.commitState();
    state = { foo: "baz" };
    manager.commitState();
    manager.undo(); // Now redoStackCount is 1

    expect(await harness.isRedoDisabled()).toBeFalse();
    await harness.clickRedo();
    expect(manager.redo).toHaveBeenCalled();
  });

  it("should disable buttons when isSaving is true", async () => {
    fixture.componentRef.setInput("showEdit", true);
    fixture.componentRef.setInput("showDelete", true);
    fixture.componentRef.setInput("isSaving", true);
    fixture.detectChanges();

    expect(await harness.isEditDisabled()).toBeTrue();
    expect(await harness.isDeleteDisabled()).toBeTrue();
  });

  describe("Reporting", () => {
    it("should show Reporting icon", async () => {
      expect(await harness.isReportingVisible()).toBeTrue();
    });

    it("should NOT show modal on successful toggle (localhost or remote)", async () => {
      ReportingServiceSpy.toggleReporting.and.returnValue(
        of({ success: true }),
      );

      await harness.clickReporting();
      fixture.detectChanges();

      expect(ReportingServiceSpy.toggleReporting).toHaveBeenCalled();
      expect(component.showReportingModal).toBeFalse();
    });

    it("should show error modal on synchronization failure (localhost)", async () => {
      const errorResult = {
        success: false,
        titleKey: "RDS_Reporting_ENABLED_TITLE",
        messageKey: "RDS_Reporting_SYNC_ERROR",
      };
      ReportingServiceSpy.toggleReporting.and.returnValue(of(errorResult));

      await harness.clickReporting();
      fixture.detectChanges();

      expect(ReportingServiceSpy.toggleReporting).toHaveBeenCalled();
      expect(component.showReportingModal).toBeTrue();
      expect(component.ReportingModalTitle).toBe("RDS_Reporting_ENABLED_TITLE");
      expect(component.ReportingModalMessage).toBe("RDS_Reporting_SYNC_ERROR");
    });

    it("should close Reporting modal on acknowledge", async () => {
      component.showReportingModal = true;
      fixture.detectChanges();

      component.onReportingModalAcknowledge();
      fixture.detectChanges();

      expect(component.showReportingModal).toBeFalse();
    });
  });

  describe("Automatic Guided Help", () => {
    it("should trigger help automatically when helpRecordName is provided and help has not been shown", fakeAsync(() => {
      spyOn(component, "onHelp").and.callThrough();
      const settings = new Settings();
      settings.trackManagerHelpShown = false;
      settingsServiceSpy.getSettings.and.returnValue(settings);

      fixture.componentRef.setInput("helpRecordName", "trackManagerHelpShown");
      component.ngOnInit();
      tick(600); // Wait for the 500ms delay in ngOnInit

      expect(component.onHelp).toHaveBeenCalled();
      expect(helpServiceSpy.startGuide).toHaveBeenCalled();
      expect(settingsServiceSpy.saveSettings).toHaveBeenCalled();
      expect(settings.trackManagerHelpShown).toBeTrue();
    }));

    it("should NOT trigger help automatically if it has already been shown", fakeAsync(() => {
      spyOn(component, "onHelp").and.callThrough();
      const settings = new Settings();
      settings.trackManagerHelpShown = true;
      settingsServiceSpy.getSettings.and.returnValue(settings);

      fixture.componentRef.setInput("helpRecordName", "trackManagerHelpShown");
      component.ngOnInit();
      tick(600);

      expect(component.onHelp).not.toHaveBeenCalled();
    }));

    it("should trigger help regardless of setting if help=true query param is present", fakeAsync(() => {
      spyOn(component, "onHelp").and.callThrough();
      const settings = new Settings();
      settings.trackManagerHelpShown = true;
      settingsServiceSpy.getSettings.and.returnValue(settings);
      queryParamsSubject.next({ help: "true" });

      fixture.componentRef.setInput("helpRecordName", "trackManagerHelpShown");
      component.ngOnInit();
      tick(600);

      expect(component.onHelp).toHaveBeenCalled();
      // Should NOT save settings as marked shown because it was already shown
      expect(settingsServiceSpy.saveSettings).not.toHaveBeenCalled();
    }));
  });
});
