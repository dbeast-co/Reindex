import {AfterViewInit, ChangeDetectorRef, Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {ApiService} from '../../services/api.service';
import {IProjectMonitoring} from '../../models/project-monitoring';
import {MatTableDataSource} from '@angular/material';
import {ToastrService} from 'ngx-toastr';
import {ProjectMonitoringService} from '../../services/project-monitoring.service';
import {Router} from '@angular/router';
import {SubSink} from 'subsink';
import {HeaderService} from '../../services/header.service';
import {MatSort, Sort, SortDirection} from '@angular/material/sort';

@Component({
  selector: 'yl-about',
  templateUrl: './project-monitoring.component.html',
  styleUrls: ['./project-monitoring.component.scss'],
})
export class ProjectMonitoringComponent implements OnInit, OnDestroy, AfterViewInit {
  displayedColumnsForSourceProjectMonitoringTable: string[] = ['project_name', 'start_time', 'end_time', 'tasks_number', 'estimated_docs', 'transferred_docs', 'succeeded_tasks', 'failed_tasks', 'progress', 'project_status', 'buttons'];

  sourceProjectMonitoring: MatTableDataSource<IProjectMonitoring> = new MatTableDataSource<IProjectMonitoring>();
  isShowYesNoDialog: boolean = false;
  selected_project: IProjectMonitoring = {
    project_id: '',
    end_time: '',
    estimated_docs: null,
    execution_progress: null,
    failed_tasks: null,
    project_name: '',
    project_status: '',
    start_time: null,
    succeeded_tasks: null,
    tasks_number: null,
    transferred_docs: null
  };
  private subs = new SubSink();
  @ViewChild(MatSort) sort: MatSort;
  private columnToSort: string;
  private sortDirection: SortDirection;
  private sourceInterval: NodeJS.Timer;

  constructor(private api: ApiService,
              private toastr: ToastrService,
              private cdr: ChangeDetectorRef,
              private router: Router,
              private headerService: HeaderService,
              private projectService: ProjectMonitoringService) {
  }

  ngOnInit() {

    this.getSourceProjectMonitoringData();
    this.sourceInterval = setInterval(() => {
      let i = 0;
      this.getSourceProjectMonitoringData();
      i++;
    }, 10000);
  }

  ngAfterViewInit() {
    this.sourceProjectMonitoring.sort = this.sort;

    this.cdr.markForCheck();
  }

  /**
   * get data for Monitoring table
   */
  getSourceProjectMonitoringData() {
    this.subs.add(this.api.getSavedProjectsForMonitoring().subscribe(projects => {
      projects.sort((a, b) => {
        switch (this.columnToSort) {
          case 'project_name':
            return this.onSortColumn(a.project_name, b.project_name);
          case 'project_status':
            return this.onSortColumn(a.project_status, b.project_status);
          case 'start_time':
            return this.onSortColumn(a.start_time, b.start_time);
          case 'end_time':
            return this.onSortColumn(a.end_time, b.end_time);
          case 'progress':
            return this.onSortColumn(a.execution_progress, b.execution_progress);
        }


      });
      this.sourceProjectMonitoring = new MatTableDataSource<IProjectMonitoring>(projects);
    }));
  }

  onSortColumn(aProperty, bProperty): number {
    if (aProperty < bProperty) {
      return -1;
    } else if (aProperty > bProperty) {
      return 1;
    }
    return 0;
  }


  /**
   * Click on delete Icon
   * @param project: IProjectMonitoring
   */

  onDeleteProject(project: IProjectMonitoring) {
    this.selected_project = project;
    this.isShowYesNoDialog = true;
    this.projectService.sendProjectMonitoring(project);
  }

  /**
   * Click on 'Yes' button in dialog
   * @param event: boolean
   */
  onYes(event: boolean) {
    this.subs.add(this.api.deleteProject(this.selected_project.project_id).subscribe(res => {
      if (res) {
        this.isShowYesNoDialog = false;
        this.sourceProjectMonitoring.data = this.sourceProjectMonitoring.data.filter(data => data.project_id !== this.selected_project.project_id);
        this.toastr.success('Project was successfully deleted!');
      } else {
        this.toastr.error('Something went wrong!');
      }
    }));
  }

  /**
   * Click on 'No' button in dialog
   * @param event: boolean
   */
  onNo(event: boolean) {
    this.isShowYesNoDialog = event;
  }

  /**
   * Click on 'Close' icon in dialog
   * @param event: boolean
   */
  onClose(event: boolean) {
    this.isShowYesNoDialog = event;
  }

  /**
   * Click on 'Edit' icon in table
   * @param project: IProjectMonitoring
   */
  onEditProject(project: IProjectMonitoring) {
    this.selected_project = project;
    this.headerService.setHeaderTitle('Project settings');
    this.projectService.sendProjectMonitoring(this.selected_project);
    this.router.navigate(['/project_configuration'], {
      queryParams: {
        project_status: this.selected_project.project_status,
        project_id: project.project_id,
        event: '2'
      }
    });
  }

  ngOnDestroy() {
    clearInterval(this.sourceInterval);
    this.subs.unsubscribe();
  }

  /**
   * Click on 'Eye' icon to navigate to 'Progress' page
   * @param project: IProjectMonitoring
   */
  onMonitoring(project: IProjectMonitoring) {
    this.headerService.setHeaderTitle('Project monitoring');
    this.router.navigate(['/progress'], {queryParams: {id: project.project_id}});
  }


  onSort(event: Sort) {
    this.sourceProjectMonitoring.sort = this.sort;
    this.columnToSort = event.active;
  }
}
