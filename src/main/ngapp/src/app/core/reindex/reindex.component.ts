import { Component, OnInit } from '@angular/core';
import {IButtonModel} from './models/button.model';

@Component({
  selector: 'yl-reindex',
  templateUrl: './reindex.component.html',
  styleUrls: ['./reindex.component.scss']
})
export class ReindexComponent implements OnInit {
  buttons: Array<IButtonModel> = [
    {
      id: 0,
      text: 'Start'
    },
    {
      id: 1,
      text: 'Pause'
    },
    {
      id: 2,
      text: 'Save'
    },
    {
      id: 3,
      text: 'Delete'
    }
  ];
  constructor() { }

  ngOnInit() {
  }

}
