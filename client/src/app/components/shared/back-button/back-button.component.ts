import { ChangeDetectorRef, Component, input, output } from "@angular/core";
import { Router } from "@angular/router";
import {
  ConnectionMonitorService,
  ConnectionState,
} from "src/app/services/connection-monitor.service";
import { ConfirmationModalComponent } from "../confirmation-modal/confirmation-modal.component";
import { TranslatePipe } from "src/app/pipes/translate.pipe";

@Component({
  standalone: true,
  selector: "app-back-button",
  templateUrl: "./back-button.component.html",
  styleUrls: ["./back-button.component.css"],
  imports: [ConfirmationModalComponent, TranslatePipe],
})
export class BackButtonComponent {
  label = input("GEN_BTN_BACK");
  route = input("/raceday-setup");
  queryParams = input<any>({});
  confirm = input(false);
  confirmTitle = input("CD_CONFIRM_EXIT_TITLE");
  confirmMessage = input("CD_CONFIRM_EXIT_MESSAGE");

  back = output<void>();

  showModal = false;

  constructor(
    private router: Router,
    private connectionMonitor: ConnectionMonitorService,
    private cdr: ChangeDetectorRef,
  ) {}

  onBack() {
    if (this.confirm()) {
      this.showModal = true;
    } else {
      this.proceed();
    }
  }

  onModalConfirm() {
    this.showModal = false;
    this.proceed();
  }

  onModalCancel() {
    this.showModal = false;
  }

  private proceed() {
    const isConnected =
      this.connectionMonitor.currentState === ConnectionState.CONNECTED;

    if (!isConnected) {
      // Always go back to splash screen if disconnected
      sessionStorage.removeItem("skipIntro");
      this.router.navigate(["/raceday-setup"]);
      return;
    }

    sessionStorage.setItem("skipIntro", "true");
    this.back.emit();

    // If no one is listening (implied by providing a route and not wanting manual override)
    // we navigate. Since we can't check 'observed', we check if route is provided.
    // However, most components pass a route.
    if (this.route() && this.route() !== "") {
      this.router.navigate([this.route()], { queryParams: this.queryParams() });
    }
  }
}
