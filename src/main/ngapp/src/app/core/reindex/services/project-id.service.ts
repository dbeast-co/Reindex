import { Injectable } from '@angular/core';
import {Observable, Subject} from 'rxjs';
import {IProjectMonitoring} from '../models/project-monitoring';

@Injectable({
  providedIn: 'root'
})
export class ProjectIdService {

  private subject = new Subject<string>();

  sendProjectMonitoring(project_id: string) {
    this.subject.next(project_id);
  }

  clearProjectMonitoring() {
    this.subject.next();
  }

  getProjectMonitoring(): Observable<string> {
    return this.subject.asObservable();
  }
}
