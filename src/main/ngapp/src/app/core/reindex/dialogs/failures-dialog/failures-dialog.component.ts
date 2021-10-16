import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {IMonitoringProjectModel} from '../../models/monitoring_project.model';
import {SelectedFailedTask} from '../../components/progress/progress.component';

@Component({
  selector: 'yl-failures-dialog',
  templateUrl: './failures-dialog.component.html',
  styleUrls: ['./failures-dialog.component.scss']
})
export class FailuresDialogComponent implements OnInit {
  @Input() selectedFailedTask: SelectedFailedTask;
  @Output() isCloseDialog: EventEmitter<boolean> = new EventEmitter<boolean>();

  constructor() {
  }

  ngOnInit() {
  }
  onCloseDialog() {
    this.isCloseDialog.emit(false);
  }

}
