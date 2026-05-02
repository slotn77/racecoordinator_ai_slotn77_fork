import { Component, input } from "@angular/core";
import { TranslatePipe } from "@app/pipes/translate.pipe";

import { UndoManager } from "./undo-manager";

@Component({
  standalone: true,
  selector: "app-undo-redo-controls",
  templateUrl: "./undo-redo-controls.component.html",
  styleUrls: ["./undo-redo-controls.component.css"],
  imports: [TranslatePipe],
})
export class UndoRedoControlsComponent {
  manager = input<UndoManager<any>>();

  undo() {
    this.manager()?.undo();
  }

  redo() {
    this.manager()?.redo();
  }

  get canUndo(): boolean {
    return this.manager()?.canUndo() ?? false;
  }

  get canRedo(): boolean {
    return this.manager()?.canRedo() ?? false;
  }
}
