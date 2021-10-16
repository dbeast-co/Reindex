import {Injectable} from '@angular/core';
import {Subject} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class HeaderService {
  private subject: Subject<string> = new Subject<string>();

  constructor() {
  }

  getHeaderTitle() {
    return this.subject.asObservable();
  }

  setHeaderTitle(title: string) {
    this.subject.next(title);
  }
}
