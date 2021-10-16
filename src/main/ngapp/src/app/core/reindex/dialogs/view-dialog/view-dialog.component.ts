import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';

@Component({
  selector: 'yl-view-dialog',
  templateUrl: './view-dialog.component.html',
  styleUrls: ['./view-dialog.component.scss']
})
export class ViewDialogComponent implements OnInit {
  @Input() viewJSONResult: any;
  @Input() indexName: string;
  @Input() templateName: string;
  @Output() isCloseDialog: EventEmitter<boolean> = new EventEmitter<boolean>();

  constructor() {
  }

  ngOnInit() {
  }

  onCloseDialog() {
    this.isCloseDialog.emit(false);
  }
}
