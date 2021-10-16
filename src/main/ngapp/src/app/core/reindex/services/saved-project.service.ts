import {Injectable} from '@angular/core';
import {Observable, Subject} from 'rxjs';
import {IProjectModel} from '../models/project.model';
import {ISavedProject} from '../models/saved_project.model';
import {ProjectService} from './project.service';

@Injectable({
  providedIn: 'root'
})
export class SavedProjectService  {

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
