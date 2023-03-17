import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  Input,
  OnDestroy,
  OnInit,
  QueryList,
  ViewChild
} from '@angular/core';
import {IButtonModel} from '../../models/button.model';
import {ApiService} from '../../services/api.service';
import {ProjectService} from '../../services/project.service';
import {ISavedProject} from '../../models/saved_project.model';
import {Router} from '@angular/router';
import {ClickService} from '../../services/click.service';
import {SavedProjectService} from '../../services/saved-project.service';
import {ProjectMonitoringService} from '../../services/project-monitoring.service';
import {HeaderService} from '../../services/header.service';
import {ToastrService} from 'ngx-toastr';

@Component({
  selector: 'yl-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
})
export class HeaderComponent implements OnInit, OnDestroy {
  @Input() buttons: Array<IButtonModel>;
  savedProjects: ISavedProject[] = [];
  savedProject: ISavedProject;
  @ViewChild('savedProjectsRef') savedProjectsRef: QueryList<ElementRef>;
  headerTitle: string = '';
  project_id: string = '';

  constructor(private apiService: ApiService,
              private projectService: ProjectService,
              private savedProjectService: SavedProjectService,
              private projectMonitoringService: ProjectMonitoringService,
              private clickService: ClickService,
              private headerService: HeaderService,
              private cdr: ChangeDetectorRef,
              private toastr: ToastrService,
              private router: Router) {
  }

  ngOnInit() {
    this.projectMonitoringService.getProjectMonitoring().subscribe((project) => {
      if (project) {
        this.savedProject = project;
        this.onOpenSavedProject(this.savedProject.project_id);
        this.cdr.markForCheck();
      }
    }, error => {
      this.toastr.error(`Server not responding!Please try again later`, '', {
        positionClass: 'toast-top-right',
      });
    });
    this.headerService.getHeaderTitle().subscribe(title => this.headerTitle = title);
    if (this.router.url === '/') {
      this.headerTitle = 'Projects monitoring';
    }
  }

  /**
   * Click on 'Hamburger' icon to open the menu
   */
  onOpenMenu() {
    this.apiService.getSavedProjects().subscribe((data) => {
      this.savedProjects = data;
      this.cdr.markForCheck();
    }, error => {
      this.toastr.error(`Server not responding!Please try again later`, '', {
        positionClass: 'toast-top-right',
      });
    });
  }

  /**
   * Click on 'Create new project'
   */
  onCreateNewProject() {
    this.cdr.markForCheck();
    setTimeout(() => {
      this.apiService.createNewProject()
        .subscribe((project) => {
          this.project_id = project.project_id;
          this.projectService.sendProject(project);
          this.headerService.setHeaderTitle('Project settings');
        });
      this.clickService.sendEvent({id: 0, type: 'Click on new selectedFailedTask'});
    }, 300);

  }

  /**
   * Click on 'Open saved project'
   * @param project_id: string
   */
  onOpenSavedProject(project_id: string) {
    setTimeout(() => {
      // this.headerService.setHeaderTitle('Project settings');
      // this.apiService.getSavedProjectById(project_id)
      //   .subscribe((project) => {
      //     this.projectService.sendProject(project);
      //     this.cdr.markForCheck();
      //   });
      this.clickService.sendEvent({id: 1, type: 'Click on open saved selectedFailedTask'});
    }, 300);

  }

  ngOnDestroy() {
    // this.subs.unsubscribe();
  }

  /**
   * Click on project monitoring and navigate to 'Project_monitoring'
   */
  onProjectsMonitoring() {
    this.headerService.setHeaderTitle('Projects monitoring');
  }
}
