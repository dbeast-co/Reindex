import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../../environments/environment';
import {Observable} from 'rxjs';
import {ISavedProject} from '../models/saved_project.model';
import {IClusterStatus, IProjectModel, ISettings, IStatus} from '../models/project.model';
import {IReportModel} from '../models/report.model';
import {IProjectMonitoring} from '../models/project-monitoring';
import {IMonitoringProjectModel} from '../models/monitoring_project.model';
import {AppConfig} from '../models/app.config';

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  constructor(private http: HttpClient, private appConfig: AppConfig) {
    environment.apiUrl = appConfig.serverBaseUrl;
  }

  createNewProject(): Observable<IProjectModel> {
    return this.http.get<IProjectModel>(`${environment.apiUrl}/reindexer/reindex_settings/new`);
  }

  getSavedProjectById(project_id): Observable<IProjectModel> {
    return this.http.get<IProjectModel>(`${environment.apiUrl}/reindexer/reindex_settings/get/${project_id}`);

  }

  getSavedProjects(): Observable<Array<ISavedProject>> {
    return this.http.get<Array<ISavedProject>>(`${environment.apiUrl}/reindexer/reindex_settings/list`);
  }

  // Todo: add projectId

  getTestCluster(clusterSettings, project_id: string): Observable<IClusterStatus> {
    return this.http.post<IClusterStatus>(`${environment.apiUrl}/reindexer/reindex_settings/test_cluster/${project_id}`, clusterSettings);
  }

  getSources(project: IProjectModel) {
    return this.http.post<IProjectModel>(`${environment.apiUrl}/reindexer/reindex_settings/get_sources`, project);
  }

  saveProject(project) {
    return this.http.post<IProjectModel>(`${environment.apiUrl}/reindexer/reindex_settings/save`, project);
  }

  deleteProject(projectId) {
    return this.http.delete(`${environment.apiUrl}/reindexer/reindex_settings/delete/${projectId}`);
  }

  getSettings() {
    return this.http.get(`${environment.apiUrl}/`);
  }

  startProject(project_id) {
    return this.http.get<boolean>(`${environment.apiUrl}/reindexer/reindex_settings/start/${project_id}`);
  }

  getProjectStatus(project_id): Observable<IStatus> {
    return this.http.get<IStatus>(`${environment.apiUrl}/reindexer/reindex_settings/get_status/${project_id}`);
  }


  stopProject(project_id) {
    return this.http.get<boolean>(`${environment.apiUrl}/reindexer/reindex_settings/stop/${project_id}`);
  }

  prepareProject(project_id): Observable<IReportModel> {
    return this.http.get<IReportModel>(`${environment.apiUrl}/reindexer/reindex_settings/prepare_project/${project_id}`);
  }

  sendSSL(formData: FormData, project_id: string, source: string): Observable<any> {
    return this.http.post<any>(`${environment.apiUrl}/reindexer/reindex_settings/ssl_cert/${source}/${project_id}`, formData);
  }

  getSavedProjectsForMonitoring(): Observable<IProjectMonitoring[]> {
    return this.http.get<IProjectMonitoring[]>(`${environment.apiUrl}/reindexer/projects_monitoring/projects_status`);
  }

  retryFailures(project_id) {
    return this.http.get<boolean>(`${environment.apiUrl}/reindexer/reindex_settings/retry_failures/${project_id}`);
  }

  getIndexViewResult(label: string, projectId: string, clusterSettings: ISettings) {
    return this.http.post(`${environment.apiUrl}/reindexer/reindex_settings/get_index_parameters/${projectId}/${label}`, clusterSettings);
  }

  getTemplateViewResult(label: string, projectId: string, clusterSettings: ISettings) {
    return this.http.post(`${environment.apiUrl}/reindexer/reindex_settings/get_template_parameters/${projectId}/${label}`, clusterSettings);
  }

  getProgressProject(project_id): Observable<IMonitoringProjectModel> {
    return this.http.get<IMonitoringProjectModel>(`${environment.apiUrl}/reindexer/reindex_monitoring/get/${project_id}`);

  }

  validateProjectName(projectName): Observable<boolean> {
    return this.http.get<boolean>(`${environment.apiUrl}/reindexer/reindex_settings/validate/${projectName}`);
  }

  getUrlForEnvironment() {
    return this.http.get(`${environment.apiUrl}/reindexer/get_url`);
  }
}
