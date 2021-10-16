import {Injectable} from '@angular/core';
import {Observable, Subject} from 'rxjs';
import {IProjectModel} from '../models/project.model';
import {IProjectMonitoring} from '../models/project-monitoring';



@Injectable({
  providedIn: 'root'
})
export class ProjectService {
  private subject = new Subject<IProjectModel>();

  sendProject(project: IProjectModel) {
    this.subject.next(project);
  }

  clearProject() {
    this.subject.next();
  }

  getProject(): Observable<IProjectModel> {
    return this.subject.asObservable();
  }
}
