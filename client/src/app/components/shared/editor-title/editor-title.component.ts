import { Component, input, output, ViewChild } from "@angular/core";
import { Router } from "@angular/router";
import { BackButtonComponent } from "@app/components/shared/back-button/back-button.component";
import { ToolbarComponent } from "@app/components/shared/toolbar/toolbar.component";
import { ToolbarComponent as ToolbarComponent_1 } from "@app/components/shared/toolbar/toolbar.component";
import { UndoManager } from "@app/components/shared/undo-redo-controls/undo-manager";
import { Settings } from "@app/models/settings";
import { TranslatePipe } from "@app/pipes/translate.pipe";
import { GuideStep } from "@app/services/help.service";

@Component({
  standalone: true,
  selector: "app-editor-title",
  templateUrl: "./editor-title.component.html",
  styleUrls: ["./editor-title.component.css"],
  imports: [BackButtonComponent, ToolbarComponent_1, TranslatePipe],
})
export class EditorTitleComponent {
  @ViewChild(ToolbarComponent) toolbar!: ToolbarComponent;
  titleKey = input("");
  backRoute = input("");
  backQueryParams = input<any>({});
  backConfirm = input(false);
  backConfirmTitle = input("");
  backConfirmMessage = input("");
  undoManager = input<UndoManager<any>>();
  showUndo = input(true);
  showRedo = input(true);
  showHelp = input(true);
  showCopy = input(false);
  showAdd = input(false);
  showDelete = input(false);
  isSaving = input(false);
  helpSteps = input<GuideStep[]>([]);
  helpTitle = input("");
  helpRecordName = input<keyof Settings>();

  back = output<void>();
  help = output<void>();
  copy = output<void>();
  add = output<void>();
  delete = output<void>();

  constructor(private router: Router) {}

  onHelp() {
    this.help.emit();
  }

  onCopy() {
    this.copy.emit();
  }

  onAdd() {
    this.add.emit();
  }

  onDelete() {
    this.delete.emit();
  }

  onBack() {
    this.back.emit();
    if (this.backRoute() && this.backRoute() !== "") {
      this.router.navigate([this.backRoute()], {
        queryParams: this.backQueryParams(),
      });
    }
  }
}
