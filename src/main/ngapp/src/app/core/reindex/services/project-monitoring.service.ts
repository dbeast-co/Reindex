import { Injectable } from '@angular/core';
import {ProjectService} from './project.service';
import {Observable, Subject} from 'rxjs';
import {IProjectModel} from '../models/project.model';
import {IProjectMonitoring} from '../models/project-monitoring';

@Injectable({
  providedIn: 'root'
})
export class ProjectMonitoringService {
  private subject = new Subject<any>();

  sendProjectMonitoring(project: any ) {
    this.subject.next(project);
  }

  clearProjectMonitoring() {
    this.subject.next();
  }

  getProjectMonitoring(): Observable<any> {
    return this.subject.asObservable();
  }
}
