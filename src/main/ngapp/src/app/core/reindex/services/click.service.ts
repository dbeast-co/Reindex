import {ChangeDetectorRef, Injectable} from '@angular/core';
import {Observable, Subject} from 'rxjs';
import {IProjectModel} from '../models/project.model';

@Injectable({
  providedIn: 'root'
})
export class ClickService {
  private subject = new Subject<IClickEvent>();

  constructor() {
  }


  sendEvent(event: IClickEvent) {
    this.subject.next(event);
  }

  clearEvent() {
    this.subject.next();
  }

  getEvent(): Observable<IClickEvent> {
    return this.subject.asObservable();
  }
}

export interface IClickEvent {
  id: number;
  type: string;
}
