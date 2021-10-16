import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {IReportModel} from '../../models/report.model';

@Component({
  selector: 'yl-error',
  templateUrl: './error.component.html',
  styleUrls: ['./error.component.scss']
})
export class ErrorComponent implements OnInit {
  @Input() errorMessage: string = '';
  @Output() isCloseEmit: EventEmitter<boolean> = new EventEmitter<boolean>();

  constructor() {
  }

  ngOnInit() {
  }

  onCloseDialog() {
    this.isCloseEmit.emit(false);
  }
}
