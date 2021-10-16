import {Component, OnDestroy, OnInit} from '@angular/core';
import {ApiService} from '../../services/api.service';
import {ProjectIdService} from '../../services/project-id.service';
import {IFailedTask, IIndexStatus, IMonitoringProjectModel, IOnFlyTask} from '../../models/monitoring_project.model';
import {FormBuilder} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {MatTableDataSource} from '@angular/material';
import {ClickService} from '../../services/click.service';
import {ProjectMonitoringService} from '../../services/project-monitoring.service';
import {HeaderService} from '../../services/header.service';

@Component({
  selector: 'yl-progress',
  templateUrl: './progress.component.html',
  styleUrls: ['./progress.component.scss'],
})
export class ProgressComponent implements OnInit, OnDestroy {

  project: IMonitoringProjectModel;
  displayedColumnsForIndexStatusTable: string[] = ['index', 'start_time', 'end_time', 'tasks_number', 'estimated_docs', 'transferred_docs', 'succeeded_tasks', 'failed_tasks', 'progress', 'status'];
  displayedColumnsForOnFlyTaskTable: string[] = ['index', 'params', 'estimated_docs', 'total_failed_docs', 'progress'];
  displayedColumnsForFailedTasksTable: string[] = ['index', 'params', 'estimated_docs', 'transferred_docs', 'view_failures'];
  dataSourceForIndexStatusTable: MatTableDataSource<IIndexStatus>;
  dataSourceForOnFlyTasksTable: MatTableDataSource<IOnFlyTask>;
  dataSourceForFailedTasksTable: MatTableDataSource<IFailedTask>;
  project_id: string = '';
  showFailuresDialog: boolean = false;
  selectedTask: SelectedFailedTask;
  private sourceInterval: NodeJS.Timer;
  private time: number = 10000;

  constructor(private projectIdService: ProjectIdService,
              private projectService: ProjectMonitoringService,
              private api: ApiService,
              private router: ActivatedRoute,
              private route: Router,
              private clickService: ClickService,
              private headerService: HeaderService,
              private fb: FormBuilder) {
  }

  ngOnInit() {
    this.dataSourceForIndexStatusTable = new MatTableDataSource<IIndexStatus>([]);
    this.dataSourceForOnFlyTasksTable = new MatTableDataSource<IOnFlyTask>([]);
    this.dataSourceForFailedTasksTable = new MatTableDataSource<IFailedTask>([]);
    this.headerService.setHeaderTitle('Project monitoring');
    this.getProjectMonitoringData();
    this.sourceInterval = setInterval(() => {
      this.getProjectMonitoringData();
    }, this.time);


  }

  getProjectMonitoringData() {
    this.router.queryParams.subscribe(data => {
      this.project_id = data.id;
      this.api.getProgressProject(data.id).subscribe(res => {
        this.project = res;
        if (this.project) {
          this.dataSourceForIndexStatusTable = new MatTableDataSource<IIndexStatus>(this.project.index_status);
          this.dataSourceForOnFlyTasksTable = new MatTableDataSource<IOnFlyTask>(this.project.on_fly_tasks_status);
          this.dataSourceForFailedTasksTable = new MatTableDataSource<IFailedTask>(this.project.failed_tasks_status);
          if (this.project.status === 'SUCCEEDED') {
            clearInterval(this.sourceInterval);
          }
        }
      });
    });
  }

  getProjectStatus() {
    if (this.project.status === 'NEW' || this.project.status === 'STOPPED') {
      return 'new_stopped';
    }
    if (this.project.status === 'FAILED') {
      return 'danger';
    }
    if (this.project.status === 'SUCCEEDED') {
      return 'success';
    }
    if (this.project.status === 'ON_FLY') {
      return 'warn';
    }

  }

  onEditSettings() {
    // this.api.getSavedProjectById(this.project_id)
    //   .subscribe((project) => {
    //     this.projectService.sendProjectMonitoring(project);
    this.headerService.setHeaderTitle('Project settings');
        this.route.navigate(['/project_configuration'], {queryParams: {project_status: this.project.status, project_id: this.project.project_id}});
    //   });
    this.clickService.sendEvent({id: 1, type: 'Click on open saved selectedFailedTask'});
  }

  onShowFailureMessage(selectedRow: SelectedFailedTask) {
    this.selectedTask = selectedRow;
    this.showFailuresDialog = true;
  }

  onCloseFailuresDialog() {
    this.showFailuresDialog = false;
  }

  ngOnDestroy() {
    clearInterval(this.sourceInterval);
  }
}

export interface SelectedFailedTask {
  index: string;
  params: string;
  self_generated_task_id: string;
  transferred_docs: number;
  estimated_docs: number;
  execution_progress: number;
  failures: string[];
  is_done: boolean;
  is_in_active_process: boolean;
  is_failed: boolean;
  is_succeeded: boolean;
}



