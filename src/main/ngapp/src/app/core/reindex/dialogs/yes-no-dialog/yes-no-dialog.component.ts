import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import { MatLegacyDialogRef as MatDialogRef } from '@angular/material/legacy-dialog';
import {ISavedProject} from '../../models/saved_project.model';

@Component({
  selector: 'yl-yes-no-dialog',
  templateUrl: './yes-no-dialog.component.html',
  styleUrls: ['./yes-no-dialog.component.scss'],
})
export class YesNoDialogComponent implements OnInit {
  @Output() isYesEmit: EventEmitter<boolean> = new EventEmitter<boolean>();
  @Output() isYesWithProject: EventEmitter<ISavedProject> = new EventEmitter<ISavedProject>();
  @Output() isNoEmit: EventEmitter<boolean> = new EventEmitter<boolean>();
  @Output() isCloseDialog: EventEmitter<boolean> = new EventEmitter<boolean>();
  @Input() flowCardForm;
  @Input() savedProject: ISavedProject;

  constructor() {
  }

  ngOnInit() {
  }

  onYes() {
    this.isYesEmit.emit(false);
  }

  onYesWithSavedProject() {
    this.isYesWithProject.emit(this.savedProject);
  }

  onNo() {
    this.isNoEmit.emit(false);
  }

  onCloseDialog() {
    this.isCloseDialog.emit(false);
  }


}
