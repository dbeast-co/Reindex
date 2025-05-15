import {
  AfterContentInit,
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  ElementRef,
  NgZone,
  OnDestroy,
  OnInit,
  Renderer2,
  ViewChild,
  ViewContainerRef
} from '@angular/core';
import {ProjectService} from '../../services/project.service';
import {
  IAlgorithmParams,
  IProjectModel,
  IReindexAlgorithm,
  ISourceIndexList,
  ISourceTemplateList
} from '../../models/project.model';
import {
  AbstractControl,
  FormControl,
  UntypedFormArray,
  UntypedFormBuilder,
  UntypedFormGroup,
  Validators
} from '@angular/forms';
import {ProjectFormService} from '../../services/project-form.service';
import {ApiService} from '../../services/api.service';
import {
  MatLegacyCheckbox,
  MatLegacyCheckbox as MatCheckbox,
  MatLegacyCheckboxChange as MatCheckboxChange
} from '@angular/material/legacy-checkbox';
import {MatLegacyDialog as MatDialog} from '@angular/material/legacy-dialog';
import {MatLegacySelectChange as MatSelectChange} from '@angular/material/legacy-select';
import {MatSort} from '@angular/material/sort';
import {MatLegacyTableDataSource as MatTableDataSource} from '@angular/material/legacy-table';
import {SelectionModel} from '@angular/cdk/collections';
import {ToastrService} from 'ngx-toastr';
import {mergeMap} from 'rxjs/operators';
import {IReportModel} from '../../models/report.model';
import {ClickService, IClickEvent} from '../../services/click.service';
import {ActivatedRoute, Router} from '@angular/router';
import {SubSink} from 'subsink';
import {SavedProjectService} from '../../services/saved-project.service';
import {ProjectIdService} from '../../services/project-id.service';
import {HeaderService} from '../../services/header.service';
import {IError} from '../../models/error.model';
import {FloatLabelType} from '@angular/material/form-field';

@Component({
  selector: 'yl-main',
  templateUrl: './main.component.html',
  styleUrls: ['./main.component.scss'],

})
export class MainComponent implements OnInit, OnDestroy, AfterViewInit, AfterContentInit {
  projectForm: UntypedFormGroup;
  project: IProjectModel = null;
  displayedColumnsForIndexPatternNamesTable: string[] = ['checkboxIndexPattern', 'index_name', 'viewIndexPattern'];
  displayedColumnsForTemplatesTable: string[] = ['checkboxTemplate', 'template_name', 'viewTemplate'];
  sourceIndexListMatSource: MatTableDataSource<ISourceIndexList>;
  sourceIndexTemplatesMatSource: MatTableDataSource<ISourceTemplateList>;
  selectionIndexList = new SelectionModel<ISourceIndexList>(true, []);
  selectionTemplateList = new SelectionModel<ISourceTemplateList>(true, []);
  checkedCheckboxes: ICheckedSettings[] = [];
  selectedAlgorithmParams: IAlgorithmParams[] = [];
  selectedAlgorithm: IReindexAlgorithm;
  showSpinner: boolean = false;
  spinnerColor: string = '#afbecf';
  notSelectedAlgorithms: AbstractControl[];
  defaultAlgorithm: IReindexAlgorithm;
  defaultName: any;
  isActiveOverlay: boolean = false;
  startStopBtn: string = 'Start';
  showYesNoDialog: boolean = false;
  playIcon: string = 'play_arrow-black-18dp.svg';
  source_cluster_status: string = '';
  destination_cluster_status: string = '';
  timer: number;
  showReportDialog: boolean;
  projectReport: IReportModel;
  maxEmptyRows: number = 12;
  showViewButtonInTables: boolean = false;
  emptyRowForSourceIndexList: ISourceIndexList = {
    is_checked: false,
    index_name: ''
  };
  emptyRowForSourceTemplateList: ISourceTemplateList = {
    is_checked: false,
    template_name: ''
  };
  radioBtns: IRadioBtn[] = [
    {
      index: 0,
      label: 'Local reindex'
    },
    {
      index: 1,
      label: 'Remote reindex'
    }
  ];
  selectedRadioBtn: number;
  showSourceSSLBtn: boolean = false;
  showDestSSLBtn: boolean = false;
  private subs = new SubSink();
  event: IClickEvent;
  isHideDestinationForm: boolean = true;
  isUploadSSLFileForSource: boolean = false;
  isUploadSSLFileForDestination: boolean = false;
  isSuccessForSource: boolean = false;
  isSuccessForDestination: boolean = false;
  isErrorForSource: boolean = false;
  isErrorForDestination: boolean = false;
  @ViewChild('fileSourceRef') fileSourceRef: ElementRef;
  @ViewChild('fileDestinationRef') fileDestinationRef: ElementRef;
  @ViewChild('mainCheckboxForIndexList') mainCheckboxForIndexList: ElementRef;
  isShowViewDialog: boolean = false;
  viewResult: Object;
  selectedIndex: string = '';
  selectedTemplate: string = '';
  isPrepareProjectClicked: boolean = false;
  isTestSourceClicked: boolean = false;
  isGettingSettingsClicked: boolean;
  isDisableSaveBtn: boolean;
  isSaveBtnClicked: boolean = false;
  isDisableStartStopBtn: boolean = false;
  restoreIcon: string = 'restore-black-18dp.svg';
  isTestDestinationClicked: boolean = false;
  showValidationNameError: boolean = false;
  source_status_on_reload_page: string = '';
  destination_status_on_reload_page: string = '';
  isOnFilter: boolean = false;
  isShowErrorDialog: boolean = false;
  errorMessage: string = '';
  @ViewChild('sourceClusterStatus') sourceClusterStatusRef: ElementRef;
  @ViewChild('destinationClusterStatusRef') destinationClusterStatus: ElementRef;
  @ViewChild(MatSort) sort: MatSort;
  showValidationNameErrorRequired: boolean = false;
  isDisableSourceTestButton: boolean = false;
  isDisableDestinationTestButton: boolean = false;
  isOnLoading: boolean = false;
  isOnTest: boolean = false;
  isDisableSaveBtnOnRunningProject: boolean;
  isDisablePrepareProjectBtn: boolean = false;
  isDisableDeleteProjectBtn: boolean = false;
  showRequiredMessage: boolean = true;
  showtimeFrameTooltip: boolean = true;
  filterTableValue: string = '';
  isDisableSort: boolean = false;
  floatLabelControl = new FormControl('auto' as FloatLabelType);

  constructor(private projectService: ProjectService,
              private savedProjectService: SavedProjectService,
              private projectFormService: ProjectFormService,
              private apiService: ApiService,
              private toastr: ToastrService,
              private cdr: ChangeDetectorRef,
              private dialog: MatDialog,
              private clickService: ClickService,
              private activatedRoute: ActivatedRoute,
              private zone: NgZone,
              private router: Router,
              private projectIdService: ProjectIdService,
              private viewContainer: ViewContainerRef,
              private headerService: HeaderService,
              private render: Renderer2,
              private fb: UntypedFormBuilder) {

  }


  ngOnInit() {
    this.sourceIndexListMatSource = new MatTableDataSource<ISourceIndexList>([]);
    this.sourceIndexTemplatesMatSource = new MatTableDataSource<ISourceTemplateList>([]);
    this.source_cluster_status = '';
    this.source_status_on_reload_page = '';
    this.destination_cluster_status = '';
    this.destination_status_on_reload_page = '';
    this.isOnLoading = true;


    this.onChangeRoute();
    this.onClickInMenu();
    this.cdr.markForCheck();

  }

  getFloatLabelValue(): FloatLabelType {
    return this.floatLabelControl.value || 'auto';
  }

  onChangeForm() {
    // this.projectForm.valueChanges.subscribe(change => {
    //   console.log('changez');
    // });
  }

  /**
   * Check status on change page
   */
  onChangeRoute() {
    this.activatedRoute.queryParams.subscribe((params) => {

      if (!params.project_id) {
        console.log('%c Route 1', 'color: red', this.router.url);
        this.showSpinner = true;
        clearInterval(this.timer);
        this.apiService.createNewProject().subscribe((project) => {
          this.headerService.setHeaderTitle('Project settings');
          this.projectForm = this.initializeForm(project);
          this.showViewButtonInTables = false;
          this.isOnFilter = false;
          this.isDisableSort = true;
          this.showSpinner = false;
          this.is_in_active_process_status.patchValue(false);
          this.source_cluster_status = '';
          this.source_status_on_reload_page = '';
          this.isShowErrorDialog = false;
          this.showRequiredMessage = true;
          this.playIcon = 'play_arrow-black-18dp.svg';
          this.cdr.markForCheck();
        }, (error) => {
          this.toastr.error(`Server not responding! Please try again later`, '', {
            positionClass: 'toast-top-right',
          });
        });


      }

      if (params.project_id && params.event === '1' || params.project_id && params['project_status']) {
        // this.playIcon = 'pause-black-18dp.svg';
        console.log('%c Route 3', 'color: yellow', params);
        // this.playIcon = 'stop_black_24dp.svg';
        // this.
        this.headerService.setHeaderTitle('Project settings');

        clearInterval(this.timer);

        this.showSpinner = true;
        this.apiService.getSavedProjectById(params.project_id).subscribe((project) => {
          this.showRequiredMessage = false;
          console.log(project);
          this.headerService.setHeaderTitle('Project settings');


          if (project.connection_settings.source.status !== 'UNTESTED' || project.connection_settings.destination.status !== 'UNTESTED') {
            this.source_cluster_status = project.connection_settings.source.status;
            this.source_status_on_reload_page = project.connection_settings.source.status;
            this.destination_cluster_status = project.connection_settings.destination.status;
            this.cdr.markForCheck();
          } else {
            this.source_cluster_status = '';
            this.source_status_on_reload_page = '';
            this.destination_status_on_reload_page = '';
            this.destination_cluster_status = '';
            this.cdr.markForCheck();
          }
          // setTimeout(() => {
          this.projectForm = this.initializeForm(project);
          this.isHideDestinationForm = this.reindex_type.value === 'Local reindex';
          if (!this.isHideDestinationForm) {
            this.destination_cluster_status = 'a';
            this.destination_status_on_reload_page = 'a';
            this.destination_cluster_status = project.connection_settings.destination.status;
            this.isDisableSaveBtn = false;
          }
          this.showSpinner = false;
          // this.isHideDestinationForm = !this.reindex_type.value;
          this.onChangeForm();
          if (this.projectForm.get('status').get('is_in_active_process').value) {
            this.isActiveOverlay = true;
            this.showSpinner = true;
            this.isPrepareProjectClicked = true;
            this.startProgressbar();
            this.startStopBtn = 'Stop';
            // this.playIcon = 'pause-black-18dp.svg';
            this.playIcon = 'stop_black_24dp.svg';
            this.isDisableStartStopBtn = false;
          } else {
            this.isActiveOverlay = false;
            this.showSpinner = false;
            this.startStopBtn = 'Start';
            this.isPrepareProjectClicked = false;
            this.isDisableStartStopBtn = true;

            this.playIcon = 'play_arrow-black-18dp.svg';
            this.cdr.markForCheck();
          }

          // }, 500);
        }, error => {
          this.toastr.error(`Server not responding! Please try again later`, '', {
            positionClass: 'toast-top-right',
          });
        });
      }


      if (params.project_id && params.event === '2' || params.project_id && params['project_status']) {
        clearInterval(this.timer);
        console.log('%c Route 2', 'color: green', params);
        console.log('aaaaa');
        this.isDisableSaveBtn = false;
        this.apiService.getSavedProjectById(params.project_id).subscribe((project) => {
          if (this.projectForm && this.projectForm.value) {
            return;
          }
          this.projectForm = this.initializeForm(project);
          this.onChangeForm();
          this.source_cluster_status = project.connection_settings.source.status;
          this.destination_cluster_status = project.connection_settings.destination.status;
          this.source_status_on_reload_page = 'a';
          this.destination_status_on_reload_page = 'a';
          this.cdr.markForCheck();

          if (project.status.is_in_active_process) {
            this.isActiveOverlay = true;
            this.showSpinner = true;
            this.isPrepareProjectClicked = true;
            this.startProgressbar();
            this.startStopBtn = 'Stop';
            // this.playIcon = 'pause-black-18dp.svg';
            this.playIcon = 'stop_black_24dp.svg';
            this.cdr.markForCheck();

          } else {
            this.isActiveOverlay = false;
            this.showSpinner = false;
            this.startStopBtn = 'Start';
            this.isDisableStartStopBtn = true;
            this.isPrepareProjectClicked = false;
            this.playIcon = 'play_arrow-black-18dp.svg';
          }
        });
      }

    });


  }

  /**
   * On click in Menu
   */
  onClickInMenu() {
    this.clickService.getEvent().pipe(
      mergeMap((event) => {
        this.event = event;
        if (event.id === 0) {
          this.resetAllButtons();
          this.isSuccessForSource = false;
          this.filterTableValue = '';

          this.isTestSourceClicked = false;
          this.isSaveBtnClicked = false;
          this.showSpinner = false;
          if (this.startStopBtn === 'Start') {
            this.playIcon = 'play_arrow-black-18dp.svg';
            this.isActiveOverlay = false;
          }
          return this.projectService.getProject();

        } else if (event.id === 1) {
          this.filterTableValue = '';
          // this.resetAllButtons();

          this.isSuccessForSource = false;
          return this.projectService.getProject();
        }
      })
    ).subscribe((project) => {
      setTimeout(() => {

        if (this.event.id === 0) {
          this.cdr.markForCheck();
          this.selectedRadioBtn = this.radioBtns.findIndex(radio => radio.label === project.reindex_settings.reindex_type);
          this.isHideDestinationForm = this.selectedRadioBtn === 0;
          this.isActiveOverlay = false;
          this.showSpinner = false;
          this.startStopBtn = 'Start';
          this.source_cluster_status = '';
          this.destination_cluster_status = '';
          this.source_status_on_reload_page = '';
          this.destination_status_on_reload_page = '';
          this.selectedAlgorithm = null;
          this.showValidationNameErrorRequired = false;
          this.showValidationNameError = false;
          this.isDisableSourceTestButton = false;
          this.isDisableDestinationTestButton = false;
          this.projectForm = this.initializeForm(project);
          this.projectForm.get('connection_settings').get('source').get('authentication_enabled').patchValue(false);
          this.projectForm.clearValidators();
          this.headerService.setHeaderTitle('Project settings');
          this.source_index_list.value.forEach(item => {
            this.isDisableSaveBtn = item.index_name === '';
          });
          if (this.isDisableSaveBtn) {
            this.playIcon = 'play_arrow-black-18dp.svg';
          }
          if (this.is_send_to_rollover_alias.value) {
            this.is_is_use_ilm.patchValue(true);
          }
        } else if (this.event.id === 1) {
          this.selectedRadioBtn = this.radioBtns.findIndex(radio => radio.label === project.reindex_settings.reindex_type);
          this.isHideDestinationForm = this.selectedRadioBtn === 0;
          this.isDisableSaveBtnOnRunningProject = false;
          this.isTestSourceClicked = true;
          this.destination_cluster_status = 'a';
          this.destination_status_on_reload_page = 'a';
          this.source_cluster_status = project.connection_settings.source.status;
          this.destination_cluster_status = project.connection_settings.source.status;
          this.showSpinner = true;


          this.headerService.setHeaderTitle('Project settings');
          this.projectForm = this.initializeForm(project);

          if (this.projectForm) {
            this.showSpinner = false;
          }
          if (this.projectForm.get('status').get('project_status').value === 'ON FLY') {
            this.isActiveOverlay = true;
            this.showSpinner = true;
            this.startStopBtn = 'Stop';
            // this.playIcon = 'pause-black-18dp.svg';
            this.playIcon = 'stop_black_24dp.svg';
            this.isPrepareProjectClicked = true;
            this.isDisableSaveBtnOnRunningProject = false;

          } else {
            this.isActiveOverlay = false;
            this.showSpinner = false;
          }

          if (this.is_send_to_rollover_alias.value) {
            this.is_is_use_ilm.patchValue(true);
          }
        }


      }, 0);

    });
  }

  /**
   * Initialize form
   * @param project: IProjectModel
   */
  initializeForm(project: IProjectModel): UntypedFormGroup {

    const hostRegex = '(http|https):\\/\\/((\\w|-|\\d|_|\\.)+)\\:\\d{2,5}';
    this.project = project;
    this.sourceIndexListMatSource = new MatTableDataSource<ISourceIndexList>(project.source_index_list);
    this.sourceIndexTemplatesMatSource = new MatTableDataSource<ISourceTemplateList>(project.source_template_list);
    if (project.source_index_list.length > 0) {
      this.sourceIndexListMatSource = new MatTableDataSource<ISourceIndexList>(project.source_index_list);
      this.showViewButtonInTables = true;
    } else {
      for (let i = 0; i < this.maxEmptyRows; i++) {
        this.sourceIndexListMatSource.data = [...this.sourceIndexListMatSource.data, this.emptyRowForSourceIndexList];
      }
    }
    if (project.source_template_list.length > 0) {
      this.showViewButtonInTables = true;
      this.sourceIndexTemplatesMatSource = new MatTableDataSource<ISourceTemplateList>(project.source_template_list);

    } else {
      for (let i = 0; i < this.maxEmptyRows; i++) {
        this.sourceIndexTemplatesMatSource.data = [...this.sourceIndexTemplatesMatSource.data, this.emptyRowForSourceTemplateList];
      }
    }

    this.projectForm = this.fb.group({
      project_id: [project.project_id],
      project_name: [project.project_name, Validators.required],
      connection_settings: this.fb.group({
        source: this.fb.group({
          authentication_enabled: [project.connection_settings.source.authentication_enabled],
          es_host: [project.connection_settings.source.es_host, Validators.pattern(hostRegex)],
          password: [project.connection_settings.source.password, Validators.required],
          ssl_enabled: [project.connection_settings.source.ssl_enabled],
          ssl_file: [project.connection_settings.source.ssl_file],
          status: ['' || project.connection_settings.source.status],
          username: [project.connection_settings.source.username, Validators.required]
        }),
        destination: this.fb.group({
          authentication_enabled: [project.connection_settings.destination.authentication_enabled],
          es_host: [project.connection_settings.destination.es_host, Validators.pattern(hostRegex)],
          password: [project.connection_settings.destination.password, Validators.required],
          ssl_enabled: [project.connection_settings.destination.ssl_enabled],
          ssl_file: [project.connection_settings.destination.ssl_file],
          status: [project.connection_settings.destination.status],
          username: [project.connection_settings.destination.username, Validators.required]
        })
      }),
      reindex_settings: this.fb.group({
        add_prefix_prefix: [project.reindex_settings.add_prefix_prefix],
        add_suffix_suffix: [project.reindex_settings.add_suffix_suffix],
        associate_with_ilm_policy_policy_name: [project.reindex_settings.associate_with_ilm_policy_policy_name],
        create_first_index_of_rollover_index_name: [project.reindex_settings.create_first_index_of_rollover_index_name],
        remove_suffix_suffix: [project.reindex_settings.remove_suffix_suffix],
        is_add_prefix: [project.reindex_settings.is_add_prefix],
        is_add_suffix: [project.reindex_settings.is_add_suffix],
        is_associate_with_ilm_policy: [project.reindex_settings.is_associate_with_ilm_policy],
        is_continue_on_failure: [project.reindex_settings.is_continue_on_failure],
        is_create_first_index_of_rollover: [project.reindex_settings.is_create_first_index_of_rollover],
        is_merge_to_one_index: [project.reindex_settings.is_merge_to_one_index],
        is_send_to_alias: [project.reindex_settings.is_send_to_alias],
        is_send_to_pipeline: [project.reindex_settings.is_send_to_pipeline],
        is_send_to_rollover_alias: [project.reindex_settings.is_send_to_rollover_alias],
        is_transfer_index_settings_from_source_index: [project.reindex_settings.is_transfer_index_settings_from_source_index],
        is_send_to_data_stream: [project.reindex_settings.is_send_to_data_stream],
        is_use_ilm: [project.reindex_settings.is_use_ilm],
        is_use_same_index_name: [project.reindex_settings.is_use_same_index_name],
        is_remove_suffix: [project.reindex_settings.is_remove_suffix],
        merge_to_one_index_index_name: [project.reindex_settings.merge_to_one_index_index_name],
        number_of_concurrent_processed_indices: [project.reindex_settings.number_of_concurrent_processed_indices],
        send_to_alias_alias: [project.reindex_settings.send_to_alias_alias],
        send_to_data_stream_stream_name: [project.reindex_settings.send_to_data_stream_stream_name],
        send_to_pipeline_pipeline_name: [project.reindex_settings.send_to_pipeline_pipeline_name],
        send_to_rollover_alias_alias: [project.reindex_settings.send_to_rollover_alias_alias],

        total_number_of_threads: [project.reindex_settings.total_number_of_threads],
        reindex_algorithms: this.fb.array(this.createReindexAlgorithmGroup(project)),
        reindex_type: [project.reindex_settings.reindex_type]
      }),
      source_index_list: this.fb.array(project.source_index_list || []),
      source_template_list: this.fb.array(project.source_template_list || []),
      status: this.fb.group({
        execution_progress: [project.status.execution_progress],
        project_status: [project.status.project_status],
        is_done: [project.status.is_done],
        is_failed: [project.status.is_failed],
        is_in_active_process: [project.status.is_in_active_process]
      })
    });

    this.defaultAlgorithm = this.reindex_algorithms.value.find(control => control.is_selected);
    this.defaultName = this.defaultAlgorithm.reindex_algorithm_name;
    this.disableInputsForUsernameAndPasswordInSource();
    this.disableInputsForUsernameAndPasswordInDestination();

    this.sourceIndexListMatSource.data.forEach(a => {
      if (a.is_checked === false && a.index_name === '') {
        this.showViewButtonInTables = false;
      }
    });

    this.cdr.markForCheck();
    console.log('initialize form', this.projectForm);

    return this.projectForm;
  }

  disableInputsForUsernameAndPasswordInSource() {
    this.cdr.markForCheck();
    if (this.source_use_authentication_enabled.value === true) {
      this.source_username.enable();
      this.source_password.enable();
    } else {
      this.source_username.disable();
      this.source_password.disable();
    }
  }

  disableInputsForUsernameAndPasswordInDestination() {
    this.cdr.markForCheck();
    if (this.destination_use_authentication_enabled.value === true) {
      this.destination_username.enable();
      this.destination_password.enable();
    } else {
      this.destination_username.disable();
      this.destination_password.disable();
    }
  }

  get is_failed_status() {
    return this.projectForm.get('status').get('is_failed');
  }

  get is_in_active_process_status() {
    return this.projectForm.get('status').get('is_in_active_process');
  }

  get es_source_host() {
    return this.projectForm.get('connection_settings').get('source').get('es_host');
  }

  get source_use_authentication_enabled() {
    return this.projectForm.get('connection_settings').get('source').get('authentication_enabled');
  }

  get destination_use_authentication_enabled() {
    return this.projectForm.get('connection_settings').get('destination').get('authentication_enabled');
  }

  get es_destination_host() {
    return this.projectForm.get('connection_settings').get('destination').get('es_host');
  }

  get source_username() {
    return this.projectForm.get('connection_settings').get('source').get('username');
  }

  get source_password() {
    return this.projectForm.get('connection_settings').get('source').get('password');
  }

  get destination_username() {
    return this.projectForm.get('connection_settings').get('destination').get('username');
  }

  get destination_password() {
    return this.projectForm.get('connection_settings').get('destination').get('password');
  }

  get source_index_list() {
    return this.projectForm.get('source_index_list') as UntypedFormArray;
  }

  get source_template_list() {
    return this.projectForm.get('source_template_list') as UntypedFormArray;
  }

  get execution_progress() {
    return this.projectForm.get('status').get('execution_progress');
  }

  get project_status() {
    return this.projectForm.get('status').get('project_status');
  }

  get is_done() {
    return this.projectForm.get('status').get('is_done');
  }

  get reindex_type() {
    return this.projectForm.get('reindex_settings').get('reindex_type');
  }

  get is_merge_to_one_index() {
    return this.projectForm.get('reindex_settings').get('is_merge_to_one_index');
  }

  get merge_to_one_index_index_nameInput() {
    return this.projectForm.get('reindex_settings').get('merge_to_one_index_index_name');
  }

  get is_send_to_alias_index() {
    return this.projectForm.get('reindex_settings').get('is_send_to_alias');
  }

  get send_to_alias_aliasInput() {
    return this.projectForm.get('reindex_settings').get('send_to_alias_alias');
  }

  get is_send_to_pipeline_index() {
    return this.projectForm.get('reindex_settings').get('is_send_to_pipeline');
  }

  get send_to_pipeline_pipeline_nameInput() {
    return this.projectForm.get('reindex_settings').get('send_to_pipeline_pipeline_name');
  }

  get is_add_prefix() {
    return this.projectForm.get('reindex_settings').get('is_add_prefix');
  }

  get add_prefix_prefixInput() {
    return this.projectForm.get('reindex_settings').get('add_prefix_prefix');
  }

  get is_add_suffix() {
    return this.projectForm.get('reindex_settings').get('is_add_suffix');
  }

  get is_remove_suffix() {
    return this.projectForm.get('reindex_settings').get('is_remove_suffix');
  }

  get add_suffix_suffixInput() {
    return this.projectForm.get('reindex_settings').get('add_suffix_suffix');
  }

  get remove_suffix_suffixInput() {
    return this.projectForm.get('reindex_settings').get('remove_suffix_suffix');
  }

  get is_is_use_ilm() {
    return this.projectForm.get('reindex_settings').get('is_use_ilm');
  }

  get is_send_to_rollover_alias() {
    return this.projectForm.get('reindex_settings').get('is_send_to_rollover_alias');
  }

  get send_to_rollover_alias_aliasInput() {
    return this.projectForm.get('reindex_settings').get('send_to_rollover_alias_alias');
  }

  get is_create_first_index_of_rollover() {
    return this.projectForm.get('reindex_settings').get('is_create_first_index_of_rollover');
  }

  get create_first_index_of_rollover_index_nameInput() {
    return this.projectForm.get('reindex_settings').get('create_first_index_of_rollover_index_name');
  }

  get is_send_to_data_stream() {
    return this.projectForm.get('reindex_settings').get('is_send_to_data_stream');
  }

  get send_to_data_stream_stream_nameInput() {
    return this.projectForm.get('reindex_settings').get('send_to_data_stream_stream_name');
  }

  get reindexSettings() {
    return this.projectForm.get('reindex_settings').value;
  }

  get reindexSettingsMergeToOneIndexInput() {
    return this.projectForm.get('reindex_settings').get('merge_to_one_index_index_name');
  }

  get reindexSettingsSendToAliasInput() {
    return this.projectForm.get('reindex_settings').get('send_to_alias_alias');
  }

  get reindex_algorithms() {
    return this.projectForm.get('reindex_settings').get('reindex_algorithms') as UntypedFormArray;
  }

  get is_use_same_index_name() {
    return this.projectForm.get('reindex_settings').get('is_use_same_index_name');
  }

  /**
   * Reset all buttons variables
   */
  resetAllButtons() {
    this.cdr.markForCheck();
    this.isDisableStartStopBtn = true;
    this.isTestSourceClicked = false;
    this.isTestDestinationClicked = false;
    this.isGettingSettingsClicked = false;
    if (this.projectForm && this.projectForm.get('status').get('is_in_active_process').value) {
      this.isPrepareProjectClicked = this.projectForm.get('status').get('is_in_active_process').value === true;
    }


    this.isSaveBtnClicked = false;
  }

  /**
   * Create 'Reindex algorithm' group for from
   * @param project: IProjectModel
   */
  private createReindexAlgorithmGroup(project: IProjectModel) {
    this.cdr.markForCheck();
    return project.reindex_settings.reindex_algorithms.map((algorithm) => {
      return this.fb.group({
        reindex_algorithm_name: this.fb.control(algorithm.reindex_algorithm_name),
        is_selected: this.fb.control(algorithm.is_selected),
        algorithm_params: this.fb.array(this.createAlgorithmParams(algorithm))
      });
    });

  }

  /**
   * Create 'Reindex algorithm params' group for from
   * @param algorithm: IReindexAlgorithm
   */
  private createAlgorithmParams(algorithm: IReindexAlgorithm) {
    this.cdr.markForCheck();
    return algorithm.algorithm_params.map((param) => {
      return this.fb.group({
        actual_value: this.fb.control(param.actual_value),
        default_value: this.fb.control(param.default_value),
        field_type: this.fb.control(param.field_type),
        label: this.fb.control(param.label)
      });
    });
  }

  private createSourceIndexList(list: ISourceIndexList) {
    this.cdr.markForCheck();
    this.source_index_list.patchValue([]);
    this.source_index_list.push(this.fb.control(list));

  }

  private createTemplateList(list: ISourceTemplateList) {
    this.cdr.markForCheck();
    this.source_template_list.push(this.fb.control(list));
  }

  getProjectStatus() {
    this.cdr.markForCheck();
    if (this.project_status.value === 'NEW' || this.project_status.value === 'STOPPED') {
      return 'new_stopped';
    }
    if (this.project_status.value === 'FAILED') {
      return 'danger';
    }
    if (this.project_status.value === 'SUCCEEDED') {
      return 'success';
    }
    if (this.project_status.value === 'ON FLY') {
      return 'warn';
    }

  }

  onTestSource(projectForm: UntypedFormGroup, target: string) {
    this.cdr.markForCheck();
    this.source_cluster_status = 'a';
    this.source_status_on_reload_page = 'a';
    this.onTestSourceOrDestination(projectForm, target);
    // this.isTestDestinationClicked = true;
    this.isShowErrorDialog = false;


  }

  onTestDestination(projectForm: UntypedFormGroup, target: string) {
    this.cdr.markForCheck();
    this.destination_cluster_status = 'a';
    this.destination_status_on_reload_page = 'a';
    this.onTestSourceOrDestination(projectForm, target);
    this.isDisableSaveBtn = false;
    // this.isTestDestinationClicked = true;
    this.isShowErrorDialog = false;
  }

  onTestSourceOrDestination(projectForm: UntypedFormGroup, target: string) {
    this.cdr.markForCheck();
    const clusterSettings = {
      es_host: projectForm.get('connection_settings').get(`${target}`).get('es_host').value,
      authentication_enabled: projectForm.get('connection_settings').get(`${target}`).get('authentication_enabled').value,
      username: projectForm.get('connection_settings').get(`${target}`).get('username').value,
      password: projectForm.get('connection_settings').get(`${target}`).get('password').value,
      ssl_enabled: projectForm.get('connection_settings').get(`${target}`).get('ssl_enabled').value,
      ssl_file: projectForm.get('connection_settings').get(`${target}`).get('ssl_file').value,
      status: projectForm.get('connection_settings').get(`${target}`).get('status').value
    };
    this.isShowViewDialog = false;
    this.subs.add(this.apiService.getTestCluster(clusterSettings, this.projectForm.get('project_id').value).subscribe((result) => {

      if (result) {
        this.isOnTest = false;
        this.isDisableSaveBtn = false;
        if (target === 'source') {
          this.source_cluster_status = result.cluster_status;
          projectForm.get('connection_settings').get(`${target}`).get('status').patchValue(this.source_cluster_status);
          this.cdr.markForCheck();

        } else {
          this.destination_cluster_status = result.cluster_status;
          projectForm.get('connection_settings').get(`${target}`).get('status').patchValue(this.destination_cluster_status);
          this.cdr.markForCheck();
        }
      }
      this.isTestSourceClicked = true;
    }, (error: IError) => {
      if (error.status === 502) {
        this.cdr.markForCheck();
        this.isShowErrorDialog = true;
        this.errorMessage = error.error.error;
        if (target === 'source') {
          this.source_cluster_status = error.error.status.cluster_status;
        } else {
          this.destination_cluster_status = error.error.status.cluster_status;
        }
        projectForm.get('connection_settings').get(`${target}`).get('status').patchValue(error.error.status.cluster_status);
      } else {
        this.toastr.error(`Server not responding! Please try again later`, '', {
          positionClass: 'toast-top-right',
        });
      }
    }));
  }

  onGetIndices() {
    this.isDisableSort = false;
    this.cdr.markForCheck();
    this.selectionIndexList.selected.map(item => {
      item.is_checked = false;
      this.selectionIndexList.isSelected(item);
      return item;
    });

    this.source_index_list.patchValue([]);


    this.showSpinner = true;
    this.apiService.getSources(this.projectForm.value)
      .pipe(
        // map(value => value.source_template_list.sort((a, b) => {
        //   if (a.template_name < b.template_name) {
        //     return -1;
        //   }
        //   if (a.template_name > b.template_name) {
        //     return 1;
        //   }
        //   return 0;
        // }))
      )
      .subscribe(project => {
        this.cdr.markForCheck();
        this.isGettingSettingsClicked = true;
        const result = project;
        if (result) {
          this.showSpinner = false;
          this.showViewButtonInTables = true;
          this.sortTable(result);
          this.sourceIndexListMatSource = new MatTableDataSource<ISourceIndexList>(result.source_index_list);
          this.sourceIndexTemplatesMatSource = new MatTableDataSource<ISourceTemplateList>(result.source_template_list);
          // this.sourceIndexListMatSource.sort = this.sort;

          result.source_index_list.forEach(source_index => {
            this.createSourceIndexList(source_index);
          });

          result.source_template_list.forEach(source_template => {
            this.createTemplateList(source_template);
          });
          this.isDisableSaveBtn = false;
        }
      }, (error) => {
        this.toastr.error(`Server not responding! Please try again later`, '', {
          positionClass: 'toast-top-right',
        });
      });
  }

  sortTable(result) {
    result.source_index_list.sort((a, b) => {
      if (a.index_name < b.index_name) {
        return -1;
      }
      if (a.index_name > b.index_name) {
        return 1;
      }
      return 0;
    });
  }

  isAllSelectedIndexList() {
    this.cdr.markForCheck();
    const numSelected = this.selectionIndexList.selected.length;
    const numRows = this.sourceIndexListMatSource.data.length;
    if (this.sourceIndexListMatSource.filteredData.length > 0) {
      const numFilterSelected = this.selectionIndexList.selected.length;
      const numFilteredRows = this.sourceIndexListMatSource.filteredData.length;
      return numFilterSelected === numFilteredRows;
    }
    return numSelected === numRows;

  }

  masterToggleIndexList(event: MatCheckboxChange) {
    this.cdr.markForCheck();

    if (this.isAllSelectedIndexList()) {
      this.selectionIndexList.clear();
    } else {
      this.sourceIndexListMatSource.data.forEach(row => this.selectionIndexList.select(row));
    }
    if (this.selectionIndexList.selected.length === this.sourceIndexListMatSource.data.length) {
      const selectedIndexList = this.selectionIndexList.selected.map((item) => {
        item.is_checked = true;
        return item;
      });
      this.source_index_list.patchValue(selectedIndexList);

    }
    if (this.selectionIndexList.selected.length === 0) {
      const unSelectedIndexList = this.sourceIndexListMatSource.data.map((item) => {
        item.is_checked = false;
        return item;
      });
      this.source_index_list.patchValue(unSelectedIndexList);
    }

  }

  isAllSelectedTemplateList() {
    const numSelected = this.selectionTemplateList.selected.length;
    const numRows = this.sourceIndexTemplatesMatSource.data.length;
    if (this.sourceIndexTemplatesMatSource.filteredData.length > 0) {
      const numFilterSelected = this.selectionIndexList.selected.length;
      const numFilteredRows = this.sourceIndexTemplatesMatSource.filteredData.length;
      return numFilterSelected === numFilteredRows;
    }
    this.cdr.markForCheck();
    return numSelected === numRows;
  }

  masterToggleTemplateList() {
    this.isAllSelectedTemplateList() ?
      this.selectionTemplateList.clear() :
      this.sourceIndexTemplatesMatSource.data.forEach(row => this.selectionTemplateList.select(row));
    if (this.selectionTemplateList.selected.length === this.sourceIndexTemplatesMatSource.data.length) {
      const selectedTemplateList = this.selectionTemplateList.selected.map((item) => {
        item.is_checked = true;
        return item;
      });
      this.source_template_list.patchValue(selectedTemplateList);
    }
    if (this.selectionTemplateList.selected.length === 0) {
      const unSelectedTemplateList = this.sourceIndexTemplatesMatSource.data.map((item) => {
        item.is_checked = false;
        return item;
      });
      this.source_template_list.patchValue(unSelectedTemplateList);
      this.cdr.markForCheck();
    }
  }

  onSaveProject() {
    this.source_index_list.patchValue(this.sourceIndexListMatSource.data.filter(el => el.index_name !== ''));
    this.source_template_list.patchValue(this.sourceIndexTemplatesMatSource.data.filter(el => el.template_name !== ''));
    this.isDisableStartStopBtn = true;
    this.playIcon = 'play_arrow-black-18dp.svg';
    this.execution_progress.patchValue(0);
    this.apiService.saveProject(this.projectForm.value).subscribe(res => {
      if (res) {
        this.isSaveBtnClicked = true;
        this.isDisablePrepareProjectBtn = false;
        this.router.navigate(['/project_configuration'], {queryParams: {project_id: this.projectForm.get('project_id').value}});
        this.toastr.success('Project was successfully saved!', '');
      } else {
        this.toastr.error('Something went wrong!', '');
      }
    }, error => {
      this.toastr.error(`Server not responding! Please try again later`, '', {
        positionClass: 'toast-top-right',
      });
    });

  }

  onSelectAlgorithm(event: MatSelectChange) {
    this.cdr.markForCheck();
    this.selectedAlgorithm = this.reindex_algorithms.value.find(control => control.reindex_algorithm_name === event.value);

    this.notSelectedAlgorithms = this.reindex_algorithms.value.filter(control => control.reindex_algorithm_name !== event.value);
    this.reindex_algorithms.value.forEach(alg => {
      alg.is_selected = event.value === alg.reindex_algorithm_name;
      if (alg.reindex_algorithm_name === 'Time oriented') {
      }
    });
  }

  onInputProjectName(project_name) {
    this.showValidationNameErrorRequired = false;
    this.apiService.validateProjectName(project_name).subscribe((result: boolean) => {
      this.showValidationNameError = result;
      this.cdr.markForCheck();

    }, error => {
      this.toastr.error(`Server not responding! Please try again later`, '', {
        positionClass: 'toast-top-right',
      });
    });
    if (this.projectForm.get('project_name').value === '') {
      this.showValidationNameErrorRequired = true;
      this.cdr.markForCheck();
    }
  }

  checkRowOfSourceIndexTable(event: MatCheckboxChange, row: ISourceIndexList) {
    this.cdr.markForCheck();

    const checkedRowIndex = this.source_index_list.value.find((a) => a.index_name === row.index_name);
    return checkedRowIndex.is_checked = event.checked;

    // if (checkedRowIndex.is_checked) {
    //   this.onChangeForm();
    // }
  }

  onSelectRow(event, row) {
    row.is_checked = !row.is_checked;
    const a = this.source_index_list.value.filter((el) => el.index_name !== '');
    const checkedRowIndex = a.indexOf(row);
    a.forEach(el => {

      if (a.indexOf(el) === checkedRowIndex) {
        el.is_checked = row.is_checked;
         this.onChangeForm();
      }
    });

  }

  checkRowOfTemplateTable(event: MatCheckboxChange, row: ISourceTemplateList) {
    row.is_checked = event.checked;
    const checkedRowIndex = this.source_template_list.value.indexOf(row);
    this.source_template_list.value.forEach(el => {
      if (this.source_template_list.value.indexOf(el) === checkedRowIndex) {
        el.is_checked = row.is_checked;
      }
    });
  }

  applyFilterForSourceIndexTable(event: KeyboardEvent) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.sourceIndexListMatSource.filter = filterValue.trim().toLowerCase();
    if ((event.target as HTMLInputElement).value !== '') {
      this.isOnFilter = true;
    } else {
      this.isOnFilter = false;
    }

  }

  applyFilterIndexTemplatesTable(event: KeyboardEvent) {
    this.cdr.markForCheck();
    const filterValue = (event.target as HTMLInputElement).value;
    this.sourceIndexTemplatesMatSource.filter = filterValue.trim().toLowerCase();
  }

  onStart() {
    if (this.startStopBtn === 'Start') {
      this.startStopBtn = 'Stop';
      // this.playIcon = 'pause-black-18dp.svg';
      this.playIcon = 'stop_black_24dp.svg';
      this.isActiveOverlay = true;
      this.showSpinner = true;
      this.isDisableSaveBtn = true;
      this.isDisablePrepareProjectBtn = true;
      this.isDisableDeleteProjectBtn = true;
      this.cdr.markForCheck();
      this.apiService.startProject(this.projectForm.get('project_id').value)
        .subscribe((res) => {
          if (res) {
            this.project_status.patchValue('ON FLY');

            this.startProgressbar();
            this.cdr.markForCheck();

          }
        }, error => {
          this.toastr.error(`Server not responding! Please try again later`, '', {
            positionClass: 'toast-top-right',
          });
        });
    } else {
      this.startStopBtn = 'Start';
      this.playIcon = 'play_arrow-white-18dp.svg';
      this.isActiveOverlay = false;
      this.showSpinner = false;
      this.isDisableSaveBtn = false;
      this.isDisablePrepareProjectBtn = false;
      this.apiService.stopProject(this.projectForm.get('project_id').value).subscribe(res => {
        this.cdr.markForCheck();

        if (res) {
          this.is_in_active_process_status.patchValue(false);
          this.project_status.patchValue('STOPPED');
          this.isDisableDeleteProjectBtn = false;

          clearInterval(this.timer);
        }

      }, error => {
        this.toastr.error(`Server not responding! Please try again later`, '', {
          positionClass: 'toast-top-right',
        });
      });
    }
  }

  startProgressbar() {
    // this.execution_progress.patchValue(0);
    this.cdr.markForCheck();
    this.timer = window.setInterval(() => {
      this.apiService.getProjectStatus(this.projectForm.get('project_id').value).subscribe((data) => {
        this.execution_progress.patchValue(data.execution_progress);
        this.cdr.markForCheck();
        this.project_status.patchValue(data.project_status);
        this.is_done.patchValue(data.is_done);
        if (this.is_done.value) {
          this.startStopBtn = 'Start';
          this.playIcon = 'play_arrow-white-18dp.svg';
          this.isActiveOverlay = false;
          this.showSpinner = false;
          clearInterval(this.timer);
          this.cdr.markForCheck();
        }
      }, error => {
        this.toastr.error(`Server not responding! Please try again later`, '', {
          positionClass: 'toast-top-right',
        });
      });
    }, 10000);
  }

  onDeleteProject() {
    this.cdr.markForCheck();
    this.showYesNoDialog = true;
  }

  onYesInYesNoDialog(event: boolean) {
    this.cdr.markForCheck();
    this.showYesNoDialog = event;
    this.apiService.deleteProject(this.projectForm.get('project_id').value).subscribe(res => {
      if (res) {
        this.toastr.success('Project was successfully delete!', '');
        this.router.navigate(['/project_monitoring']);
      } else {
        this.toastr.error('Something went wrong!', '');
      }
    }, error => {
      this.toastr.error(`Server not responding! Please try again later`, '', {
        positionClass: 'toast-top-right',
      });
    });
  }

  OnNoInYesNoDialog(event: boolean) {
    this.cdr.markForCheck();
    this.showYesNoDialog = event;
  }

  onCloseInYesNoDialog(event: boolean) {
    this.cdr.markForCheck();
    this.showYesNoDialog = event;
  }

  onValidateSettings(project_id: string) {
    this.showSpinner = true;
    this.apiService.prepareProject(project_id).subscribe((project) => {
      if (project) {
        this.cdr.markForCheck();
        this.showSpinner = false;
        this.restoreIcon = 'restore-white-18dp.svg';
        this.playIcon = 'play_arrow-white-18dp.svg';
        this.projectReport = project;
        this.showReportDialog = true;
        this.isPrepareProjectClicked = true;
        if (project.status === 'ERROR' || project.status === 'WARNING') {
          this.isDisableStartStopBtn = true;
          this.playIcon = 'play_arrow-black-18dp.svg';
        }
        if (project.status === 'PASS') {
          this.isDisableStartStopBtn = false;
        }
      }
    }, error => {
      this.toastr.error(`Server not responding! Please try again later`, '', {
        positionClass: 'toast-top-right',
      });
    });
  }

  onCloseInReportDialog(event: boolean) {
    this.cdr.markForCheck();
    this.showReportDialog = event;
  }

  onNoInYesNoDialog(event: boolean) {
    this.cdr.markForCheck();
    this.showReportDialog = event;
  }

  onYesInReportDialog(event: boolean) {
    this.cdr.markForCheck();
    this.showReportDialog = event;
    this.playIcon = 'play_arrow-white-18dp.svg';
    this.isDisableStartStopBtn = false;
  }

  onImportSSL(event, target: string) {
    this.cdr.markForCheck();
    const formData = new FormData();
    if (event.target.files.length > 0) {
      const pattern = new RegExp('([a-zA-Z0-9\\s_\\\\.\\-:])+(.*)$');
      if (pattern.test(event.target.files[0]['name'])) {
        formData.append('file', event.target.files[0]);
        if (target === 'source') {
          this.isUploadSSLFileForSource = true;
        } else {
          this.isUploadSSLFileForDestination = true;
        }
        this.apiService.sendSSL(formData, this.projectForm.get('project_id').value, target).subscribe((res) => {
          if (res) {
            if (target === 'source') {
              this.isUploadSSLFileForSource = false;
              this.isSuccessForSource = true;
              this.fileSourceRef.nativeElement.value = '';
            } else {
              this.isUploadSSLFileForDestination = false;
              this.isSuccessForDestination = true;
              this.fileDestinationRef.nativeElement.value = '';
            }
            this.toastr.success('File was successfully uploaded!', '');
          } else {
            this.toastr.error('Something went wrong!', '');
          }

        }, (err) => {
          if (target === 'source') {
            this.isSuccessForSource = false;
            this.isUploadSSLFileForSource = false;
            this.isErrorForSource = true;

          } else {
            this.isSuccessForDestination = false;
            this.isUploadSSLFileForDestination = false;
            this.isErrorForDestination = true;
          }
        });
      }
      if (target === 'source') {
        this.projectForm.get('connection_settings').get('source').get('ssl_file').patchValue(target + '_' + event.target.files[0]['name']);
      } else {
        // tslint:disable-next-line:max-line-length
        this.projectForm.get('connection_settings').get('destination').get('ssl_file').patchValue(target + '_' + event.target.files[0]['name']);
      }
    }
  }

  onRadioButton(event, radioEl, radioId: number) {
    this.cdr.markForCheck();
    // this.isDisableSaveBtn = true;
    this.selectedRadioBtn = this.radioBtns.indexOf(radioEl);
    this.isHideDestinationForm = radioEl.label !== 'Remote reindex';
    this.reindex_type.patchValue(radioEl.label);
    if (this.selectedRadioBtn === 0) {
      // this.isDisableSaveBtn = false;

    }
    if (this.selectedRadioBtn === 1 && this.isTestDestinationClicked) {
      // this.isDisableSaveBtn = false;
      this.destination_cluster_status = 'a';
      this.destination_status_on_reload_page = 'a';
    }
  }

  onUseSourceAuth(event: MatCheckboxChange) {
    this.cdr.markForCheck();
    if (event.checked) {
      this.source_username.enable();
      this.source_password.enable();
      this.isDisableSourceTestButton = true;
      if (this.source_password.value !== null && this.source_username.value !== null) {
        this.isDisableSourceTestButton = false;
      } else {
        this.isDisableSourceTestButton = true;
      }
    } else {
      this.source_username.disable();
      this.source_password.disable();
      this.isDisableSourceTestButton = false;
    }
  }

  onUseDestAuth(event: MatCheckboxChange) {
    this.cdr.markForCheck();
    if (event.checked) {
      this.destination_username.enable();
      this.destination_password.enable();
      this.isDisableDestinationTestButton = true;
      if (this.destination_password.value !== null && this.destination_username.value !== null) {
        this.isDisableDestinationTestButton = false;
      } else {
        this.isDisableDestinationTestButton = true;
      }
    } else {
      this.destination_username.disable();
      this.destination_password.disable();
    }
  }

  onSourceSSL(event: MatCheckboxChange) {
    this.cdr.markForCheck();
    this.showSourceSSLBtn = event.checked;
  }

  onDestSSL(event: MatCheckboxChange) {
    this.cdr.markForCheck();
    this.showDestSSLBtn = event.checked;
  }

  ngOnDestroy() {
    clearInterval(this.timer);
  }

  onRetryFailures() {
    this.startStopBtn = 'Stop';
    // this.playIcon = 'pause-black-18dp.svg';
    this.playIcon = 'stop_black_24dp.svg';
    this.isActiveOverlay = true;
    this.showSpinner = true;
    this.restoreIcon = 'restore-white-18dp.svg';
    this.isPrepareProjectClicked = true;
    this.isDisableStartStopBtn = false;
    this.cdr.markForCheck();
    this.apiService.retryFailures(this.projectForm.get('project_id').value)
      .subscribe((res) => {
        if (res) {
          this.startProgressbar();
          this.cdr.markForCheck();
        }
      }, error => {
        this.toastr.error(`Server not responding! Please try again later`, '', {
          positionClass: 'toast-top-right',
        });
      });
  }

  onRetryInReportDialog(project: IReportModel) {
    this.cdr.markForCheck();
    this.showReportDialog = true;
    this.isPrepareProjectClicked = true;
    if (project.status === 'ERROR' || project.status === 'WARNING') {
      this.isDisableStartStopBtn = true;
      this.playIcon = 'play_arrow-black-18dp.svg';
    } else {
      this.isDisableStartStopBtn = false;
      this.playIcon = 'play_arrow-white-18dp.svg';
    }
  }

  onInputParams(event: any, index: number) {
    this.cdr.markForCheck();
    const newAlgorithms = this.reindex_algorithms.value.map((control, i) => {
      control.algorithm_params.map((item, count) => {
        if (index === count) {
          control.algorithm_params[index].actual_value = event;
        }
      });
      return control;
    });
    this.reindex_algorithms.patchValue(newAlgorithms);
  }

  onViewIndex(label: string, table: string) {
    this.cdr.markForCheck();

    const clusterSettings = {
      es_host: this.projectForm.get('connection_settings').get('source').get('es_host').value,
      authentication_enabled: this.projectForm.get('connection_settings').get('source').get('authentication_enabled').value,
      username: this.projectForm.get('connection_settings').get('source').get('username').value,
      password: this.projectForm.get('connection_settings').get('source').get('password').value,
      ssl_enabled: this.projectForm.get('connection_settings').get('source').get('ssl_enabled').value,
      ssl_file: this.projectForm.get('connection_settings').get('source').get('ssl_file').value,
      status: this.projectForm.get('connection_settings').get('source').get('status').value
    };
    if (table === 'index_pattern-table') {
      // this.selectedIndex = label;
      this.selectedTemplate = '';
      this.apiService.getIndexViewResult(label, this.projectForm.get('project_id').value, clusterSettings).subscribe(res => {
        if (res) {
          this.viewResult = res;
          this.isShowViewDialog = true;
        }
      }, error => {
        this.toastr.error(`Server not responding! Please try again later`, '', {
          positionClass: 'toast-top-right',
        });
      });
    } else if (table === 'templates-table') {
      this.selectedTemplate = label;
      this.selectedIndex = '';
      this.apiService.getTemplateViewResult(label, this.projectForm.get('project_id').value, clusterSettings).subscribe(res => {
        if (res) {
          this.viewResult = res;
          this.isShowViewDialog = true;
        }
      }, error => {
        this.toastr.error(`Server not responding! Please try again later`, '', {
          positionClass: 'toast-top-right',
        });
      });
    }


  }

  onCloseInViewDialog(event: boolean) {
    this.cdr.markForCheck();
    this.isShowViewDialog = event;
  }

  emptyButtonStatuses() {
    this.cdr.markForCheck();
    this.is_merge_to_one_index.patchValue(false);
    this.is_send_to_alias_index.patchValue(false);
    this.is_send_to_pipeline_index.patchValue(false);
    this.is_is_use_ilm.patchValue(false);
    this.is_send_to_rollover_alias.patchValue(false);
    this.is_create_first_index_of_rollover.patchValue(false);
    this.is_add_suffix.patchValue(false);
    this.is_add_prefix.patchValue(false);
    this.is_remove_suffix.patchValue(false);
    this.is_use_same_index_name.patchValue(false);
    this.is_send_to_data_stream.patchValue(false);
    this.merge_to_one_index_index_nameInput.patchValue('');
    this.send_to_alias_aliasInput.patchValue('');
    this.send_to_pipeline_pipeline_nameInput.patchValue('');
    this.send_to_rollover_alias_aliasInput.patchValue('');
    this.create_first_index_of_rollover_index_nameInput.patchValue('');
    this.add_suffix_suffixInput.patchValue('');
    this.add_prefix_prefixInput.patchValue('');
    this.remove_suffix_suffixInput.patchValue('');
    this.send_to_data_stream_stream_nameInput.patchValue('');
  }

  emptyButtonStatusesOnUseILM() {
    this.cdr.markForCheck();
    this.is_merge_to_one_index.patchValue(false);
    this.is_send_to_alias_index.patchValue(false);
    this.is_send_to_pipeline_index.patchValue(false);
    this.is_is_use_ilm.patchValue(false);
    this.is_add_suffix.patchValue(false);
    this.is_add_prefix.patchValue(false);
    this.is_send_to_data_stream.patchValue(false);
    this.is_use_same_index_name.patchValue(false);
    this.merge_to_one_index_index_nameInput.patchValue('');
    this.send_to_alias_aliasInput.patchValue('');
    this.send_to_pipeline_pipeline_nameInput.patchValue('');
    this.send_to_rollover_alias_aliasInput.patchValue('');
    this.add_suffix_suffixInput.patchValue('');
    this.add_prefix_prefixInput.patchValue('');
    this.send_to_data_stream_stream_nameInput.patchValue('');
  }

  checkAllCheckboxesAndInputsForSaveButton(): boolean {

    this.cdr.markForCheck();
    if (this.is_merge_to_one_index.value && this.merge_to_one_index_index_nameInput.value !== '' ||
      this.is_send_to_alias_index.value && this.send_to_alias_aliasInput.value !== '' ||
      this.is_send_to_pipeline_index.value && this.send_to_pipeline_pipeline_nameInput.value !== '' ||
      this.is_add_prefix.value && this.add_prefix_prefixInput.value !== '' ||
      this.is_add_suffix.value && this.add_suffix_suffixInput.value !== '' ||
      this.is_send_to_rollover_alias.value && this.send_to_rollover_alias_aliasInput.value !== '' ||
      this.is_create_first_index_of_rollover.value && this.create_first_index_of_rollover_index_nameInput.value !== '' ||
      this.is_remove_suffix.value && this.remove_suffix_suffixInput.value !== '' ||
      this.is_use_same_index_name.value === true ||
      this.is_send_to_data_stream.value === true && this.send_to_data_stream_stream_nameInput.value
    ) {
      return false;
    } else {
      return true;
    }


  }

  addValidatorRequired(abstractControl: AbstractControl) {
    this.cdr.markForCheck();
    abstractControl.setValidators(Validators.required);
  }

  clearValidatorRequired(abstractControl: AbstractControl) {
    this.cdr.markForCheck();
    this.merge_to_one_index_index_nameInput.markAsUntouched();
    this.send_to_alias_aliasInput.markAsUntouched();
    this.send_to_pipeline_pipeline_nameInput.markAsUntouched();
    this.add_prefix_prefixInput.markAsUntouched();
    this.add_suffix_suffixInput.markAsUntouched();
    this.send_to_rollover_alias_aliasInput.markAsUntouched();
    this.create_first_index_of_rollover_index_nameInput.markAsUntouched();
    this.send_to_data_stream_stream_nameInput.markAsUntouched();
    abstractControl.clearValidators();
    abstractControl.setValidators(null);
    abstractControl.updateValueAndValidity();

  }

  onCheckAddPrefix() {
    this.onShowRequiredMessage();
    this.cdr.markForCheck();
    const isCheckedPrefix = this.is_add_prefix.value;
    const isCheckedSuffix = this.is_add_suffix.value;
    const checkSuffixInput = this.add_suffix_suffixInput.value;
    this.emptyButtonStatuses();
    this.is_add_prefix.patchValue(isCheckedPrefix);
    this.is_add_suffix.patchValue(isCheckedSuffix);
    this.is_use_same_index_name.patchValue(false);
    this.add_suffix_suffixInput.patchValue(checkSuffixInput);
    this.add_prefix_prefixInput.setValidators(Validators.required);
    if (this.is_add_prefix.value) {
      this.clearValidatorRequired(this.merge_to_one_index_index_nameInput);
      this.clearValidatorRequired(this.send_to_alias_aliasInput);
      this.clearValidatorRequired(this.send_to_pipeline_pipeline_nameInput);
      this.clearValidatorRequired(this.send_to_rollover_alias_aliasInput);
      this.clearValidatorRequired(this.send_to_data_stream_stream_nameInput);
      this.clearValidatorRequired(this.remove_suffix_suffixInput);
    } else {
      this.clearValidatorRequired(this.add_prefix_prefixInput);
      this.clearValidatorRequired(this.merge_to_one_index_index_nameInput);
      this.clearValidatorRequired(this.send_to_alias_aliasInput);
      this.clearValidatorRequired(this.send_to_pipeline_pipeline_nameInput);
      this.clearValidatorRequired(this.send_to_data_stream_stream_nameInput);
      this.clearValidatorRequired(this.remove_suffix_suffixInput);
    }

  }

  onCheckAddSuffix() {
    this.onShowRequiredMessage();
    // debugger
    const isCheckedPrefix = this.is_add_prefix.value;
    const isCheckedSuffix = this.is_add_suffix.value;
    const checkPrefixInput = this.add_prefix_prefixInput.value;
    this.emptyButtonStatuses();
    this.is_add_prefix.patchValue(isCheckedPrefix);
    this.is_add_suffix.patchValue(isCheckedSuffix);
    this.is_use_same_index_name.patchValue(false);
    this.add_prefix_prefixInput.patchValue(checkPrefixInput);
    this.add_suffix_suffixInput.setValidators(Validators.required);
    if (this.is_add_suffix.value) {
      this.clearValidatorRequired(this.merge_to_one_index_index_nameInput);
      this.clearValidatorRequired(this.send_to_alias_aliasInput);
      this.clearValidatorRequired(this.send_to_pipeline_pipeline_nameInput);
      this.clearValidatorRequired(this.send_to_rollover_alias_aliasInput);
      this.clearValidatorRequired(this.send_to_data_stream_stream_nameInput);
      this.clearValidatorRequired(this.remove_suffix_suffixInput);
      this.cdr.markForCheck();

    } else {
      this.clearValidatorRequired(this.add_suffix_suffixInput);
      this.clearValidatorRequired(this.merge_to_one_index_index_nameInput);
      this.clearValidatorRequired(this.send_to_alias_aliasInput);
      this.clearValidatorRequired(this.send_to_pipeline_pipeline_nameInput);
      this.clearValidatorRequired(this.send_to_data_stream_stream_nameInput);
      this.clearValidatorRequired(this.remove_suffix_suffixInput);
      this.cdr.markForCheck();

    }

  }

  onCheckRemoveSuffix(event: MatCheckboxChange) {
    this.onShowRequiredMessage();
    const isCheckedPrefix = event.checked;
    this.emptyButtonStatuses();
    // this.is_add_prefix.patchValue(isCheckedPrefix);
    // this.is_add_suffix.patchValue(isCheckedSuffix);
    this.is_remove_suffix.patchValue(isCheckedPrefix);
    this.remove_suffix_suffixInput.patchValue('');
    this.remove_suffix_suffixInput.setValidators(Validators.required);
    this.is_use_same_index_name.patchValue(false);

    this.cdr.markForCheck();
    if (this.is_remove_suffix.value) {
      this.clearValidatorRequired(this.merge_to_one_index_index_nameInput);
      this.clearValidatorRequired(this.send_to_alias_aliasInput);
      this.clearValidatorRequired(this.send_to_pipeline_pipeline_nameInput);
      this.addValidatorRequired(this.remove_suffix_suffixInput);
      this.clearValidatorRequired(this.add_suffix_suffixInput);
      this.remove_suffix_suffixInput.markAsUntouched();
      this.clearValidatorRequired(this.send_to_data_stream_stream_nameInput);
    } else {
      this.clearValidatorRequired(this.merge_to_one_index_index_nameInput);
      this.clearValidatorRequired(this.send_to_alias_aliasInput);
      this.clearValidatorRequired(this.send_to_pipeline_pipeline_nameInput);
      this.clearValidatorRequired(this.remove_suffix_suffixInput);
      this.clearValidatorRequired(this.send_to_data_stream_stream_nameInput);
      this.clearValidatorRequired(this.add_suffix_suffixInput);
    }
  }

  onRemoveSuffixInput(event) {
    this.remove_suffix_suffixInput.patchValue(event.target.value);
    // console.log(event.target.value);
  }

  onCheckUseIlm(is_send_to_rollover_aliasRef) {
    this.onShowRequiredMessage();
    this.cdr.markForCheck();
    const checked = is_send_to_rollover_aliasRef.checked;

    this.emptyButtonStatuses();
    if (checked) {
      this.is_is_use_ilm.patchValue(checked);
      this.is_send_to_rollover_alias.patchValue(true);
      this.is_merge_to_one_index.patchValue(false);
      this.is_send_to_alias_index.patchValue(false);
      this.is_send_to_pipeline_index.patchValue(false);
      this.is_add_suffix.patchValue(false);
      this.is_add_prefix.patchValue(false);
      this.is_use_same_index_name.patchValue(false);
      this.clearValidatorRequired(this.remove_suffix_suffixInput);
      this.clearValidatorRequired(this.send_to_data_stream_stream_nameInput);
      this.clearValidatorRequired(this.remove_suffix_suffixInput);
    } else {
      this.is_send_to_rollover_alias.patchValue(false);
      this.is_create_first_index_of_rollover.patchValue(false);
    }

  }

  onShowRequiredMessage() {
    this.showRequiredMessage = !this.showRequiredMessage;
  }

  onCheckIsMergeToOneIndex(is_merge_to_one_indexCheckboxRef) {
    this.onShowRequiredMessage();
    this.cdr.markForCheck();
    const isChecked = is_merge_to_one_indexCheckboxRef.checked;
    this.emptyButtonStatuses();
    this.is_merge_to_one_index.patchValue(isChecked);
    this.is_use_same_index_name.patchValue(false);
    if (this.is_merge_to_one_index.value === true) {
      this.addValidatorRequired(this.merge_to_one_index_index_nameInput);

      this.clearValidatorRequired(this.send_to_alias_aliasInput);
      this.clearValidatorRequired(this.send_to_pipeline_pipeline_nameInput);
      this.clearValidatorRequired(this.add_prefix_prefixInput);
      this.clearValidatorRequired(this.add_suffix_suffixInput);
      this.clearValidatorRequired(this.send_to_rollover_alias_aliasInput);
      this.clearValidatorRequired(this.send_to_data_stream_stream_nameInput);
      this.clearValidatorRequired(this.remove_suffix_suffixInput);
    } else {
      this.clearValidatorRequired(this.merge_to_one_index_index_nameInput);
      this.clearValidatorRequired(this.send_to_alias_aliasInput);
      this.clearValidatorRequired(this.send_to_pipeline_pipeline_nameInput);
      this.clearValidatorRequired(this.add_prefix_prefixInput);
      this.clearValidatorRequired(this.add_suffix_suffixInput);
      this.clearValidatorRequired(this.send_to_data_stream_stream_nameInput);
      this.clearValidatorRequired(this.remove_suffix_suffixInput);
    }
  }

  onCheckIsSendAlias(checkboxRef) {
    this.onShowRequiredMessage();
    this.cdr.markForCheck();
    const isChecked = checkboxRef.checked;
    this.emptyButtonStatuses();
    this.is_send_to_alias_index.patchValue(isChecked);
    this.is_use_same_index_name.patchValue(false);
    if (this.is_send_to_alias_index.value) {
      this.addValidatorRequired(this.send_to_alias_aliasInput);
      this.clearValidatorRequired(this.merge_to_one_index_index_nameInput);
      this.clearValidatorRequired(this.send_to_pipeline_pipeline_nameInput);
      this.clearValidatorRequired(this.add_prefix_prefixInput);
      this.clearValidatorRequired(this.add_suffix_suffixInput);
      this.clearValidatorRequired(this.send_to_rollover_alias_aliasInput);
      this.clearValidatorRequired(this.send_to_data_stream_stream_nameInput);
      this.clearValidatorRequired(this.remove_suffix_suffixInput);
    } else {

      this.clearValidatorRequired(this.merge_to_one_index_index_nameInput);
      this.clearValidatorRequired(this.send_to_alias_aliasInput);
      this.clearValidatorRequired(this.send_to_pipeline_pipeline_nameInput);
      this.clearValidatorRequired(this.add_prefix_prefixInput);
      this.clearValidatorRequired(this.add_suffix_suffixInput);
      this.clearValidatorRequired(this.send_to_data_stream_stream_nameInput);
      this.clearValidatorRequired(this.remove_suffix_suffixInput);
    }
  }

  onCheckSendToDataStream(is_send_to_data_streamRef: MatLegacyCheckbox) {
    this.onShowRequiredMessage();
    this.cdr.markForCheck();
    const isChecked = is_send_to_data_streamRef.checked;
    this.emptyButtonStatuses();
    this.is_send_to_data_stream.patchValue(isChecked);
    this.is_use_same_index_name.patchValue(false);
    if (this.is_send_to_data_stream.value === true) {
      this.addValidatorRequired(this.send_to_data_stream_stream_nameInput);

      this.clearValidatorRequired(this.send_to_alias_aliasInput);
      this.clearValidatorRequired(this.send_to_pipeline_pipeline_nameInput);
      this.clearValidatorRequired(this.add_prefix_prefixInput);
      this.clearValidatorRequired(this.add_suffix_suffixInput);
      this.clearValidatorRequired(this.send_to_rollover_alias_aliasInput);
      this.clearValidatorRequired(this.remove_suffix_suffixInput);
    } else {
      this.clearValidatorRequired(this.merge_to_one_index_index_nameInput);
      this.clearValidatorRequired(this.send_to_alias_aliasInput);
      this.clearValidatorRequired(this.send_to_pipeline_pipeline_nameInput);
      this.clearValidatorRequired(this.add_prefix_prefixInput);
      this.clearValidatorRequired(this.add_suffix_suffixInput);
      this.clearValidatorRequired(this.send_to_data_stream_stream_nameInput);
      this.clearValidatorRequired(this.remove_suffix_suffixInput);
    }
  }

  onCheckIsSendToPipeLine(is_send_to_pipelineRef: MatCheckbox) {
    this.onShowRequiredMessage();
    this.cdr.markForCheck();
    const isChecked = is_send_to_pipelineRef.checked;
    this.emptyButtonStatuses();
    this.is_send_to_pipeline_index.patchValue(isChecked);
    this.is_use_same_index_name.patchValue(false);
    this.is_remove_suffix.patchValue(false);
    if (this.is_send_to_pipeline_index.value) {
      this.addValidatorRequired(this.send_to_pipeline_pipeline_nameInput);
      this.clearValidatorRequired(this.merge_to_one_index_index_nameInput);
      this.clearValidatorRequired(this.send_to_alias_aliasInput);
      this.clearValidatorRequired(this.add_prefix_prefixInput);
      this.clearValidatorRequired(this.add_suffix_suffixInput);
      this.clearValidatorRequired(this.send_to_data_stream_stream_nameInput);
      this.clearValidatorRequired(this.send_to_rollover_alias_aliasInput);
      this.clearValidatorRequired(this.remove_suffix_suffixInput);
    } else {
      this.clearValidatorRequired(this.merge_to_one_index_index_nameInput);
      this.clearValidatorRequired(this.send_to_alias_aliasInput);
      this.clearValidatorRequired(this.send_to_pipeline_pipeline_nameInput);
      this.clearValidatorRequired(this.add_prefix_prefixInput);
      this.clearValidatorRequired(this.add_suffix_suffixInput);
      this.clearValidatorRequired(this.send_to_data_stream_stream_nameInput);
      this.clearValidatorRequired(this.remove_suffix_suffixInput);
    }
  }

  ngAfterViewInit(): void {


    // this.cdr.detectChanges();
  }

  ngAfterContentInit() {

    // this.render.removeClass(this.sourceClusterStatusRef.nativeElement,'new_stopped')

  }

  onCheckSendToRollOverAlias(is_send_to_rollover_alias) {
    this.onShowRequiredMessage();
    this.cdr.markForCheck();
    const checked = is_send_to_rollover_alias.checked;
    this.emptyButtonStatusesOnUseILM();
    if (checked) {
      this.is_is_use_ilm.patchValue(true);
      this.is_send_to_rollover_alias.patchValue(true);
      this.is_use_same_index_name.patchValue(false);
      this.addValidatorRequired(this.send_to_rollover_alias_aliasInput);
      this.clearValidatorRequired(this.merge_to_one_index_index_nameInput);
      this.clearValidatorRequired(this.send_to_alias_aliasInput);
      this.clearValidatorRequired(this.send_to_pipeline_pipeline_nameInput);
      this.clearValidatorRequired(this.add_prefix_prefixInput);
      this.clearValidatorRequired(this.add_suffix_suffixInput);
      this.clearValidatorRequired(this.send_to_data_stream_stream_nameInput);
      this.clearValidatorRequired(this.remove_suffix_suffixInput);
    } else {
      this.clearValidatorRequired(this.merge_to_one_index_index_nameInput);
      this.clearValidatorRequired(this.send_to_alias_aliasInput);
      this.clearValidatorRequired(this.send_to_pipeline_pipeline_nameInput);
      this.clearValidatorRequired(this.add_prefix_prefixInput);
      this.clearValidatorRequired(this.add_suffix_suffixInput);
      this.clearValidatorRequired(this.send_to_rollover_alias_aliasInput);
      this.clearValidatorRequired(this.send_to_data_stream_stream_nameInput);
      this.clearValidatorRequired(this.remove_suffix_suffixInput);
    }

    if (!this.is_send_to_rollover_alias.value && !this.is_create_first_index_of_rollover.value) {
      this.send_to_rollover_alias_aliasInput.patchValue('');
      this.is_is_use_ilm.patchValue(false);
      this.clearValidatorRequired(this.send_to_rollover_alias_aliasInput);
      this.clearValidatorRequired(this.create_first_index_of_rollover_index_nameInput);
    }

  }

  onCheckCreateFirstIndexOfRollOver(is_create_first_index_of_rolloverRef) {
    this.onShowRequiredMessage();
    this.cdr.markForCheck();
    const checked = is_create_first_index_of_rolloverRef.checked;
    this.emptyButtonStatusesOnUseILM();
    if (checked) {
      this.is_send_to_rollover_alias.patchValue(true);
      this.is_is_use_ilm.patchValue(true);
      this.is_use_same_index_name.patchValue(false);
      this.addValidatorRequired(this.create_first_index_of_rollover_index_nameInput);
      this.clearValidatorRequired(this.merge_to_one_index_index_nameInput);
      this.clearValidatorRequired(this.send_to_alias_aliasInput);
      this.clearValidatorRequired(this.send_to_pipeline_pipeline_nameInput);
      this.clearValidatorRequired(this.add_prefix_prefixInput);
      this.clearValidatorRequired(this.add_suffix_suffixInput);
      this.clearValidatorRequired(this.send_to_data_stream_stream_nameInput);
      this.clearValidatorRequired(this.remove_suffix_suffixInput);
    } else {
      this.is_send_to_rollover_alias.patchValue(false);
      this.clearValidatorRequired(this.merge_to_one_index_index_nameInput);
      this.clearValidatorRequired(this.send_to_alias_aliasInput);
      this.clearValidatorRequired(this.send_to_pipeline_pipeline_nameInput);
      this.clearValidatorRequired(this.add_prefix_prefixInput);
      this.clearValidatorRequired(this.add_suffix_suffixInput);
      this.clearValidatorRequired(this.create_first_index_of_rollover_index_nameInput);
      this.clearValidatorRequired(this.send_to_data_stream_stream_nameInput);
      this.clearValidatorRequired(this.remove_suffix_suffixInput);
    }

    if (!this.is_send_to_rollover_alias.value && !this.is_create_first_index_of_rollover.value) {
      this.is_is_use_ilm.patchValue(false);
      this.create_first_index_of_rollover_index_nameInput.patchValue('');
    }
  }

  onUserSameIndexName(is_use_same_index_name: MatCheckboxChange) {
    const checked = is_use_same_index_name.checked;
    if (checked) {
      this.is_merge_to_one_index.patchValue(false);
      this.is_send_to_alias_index.patchValue(false);
      this.is_add_prefix.patchValue(false);
      this.is_add_suffix.patchValue(false);
      this.is_is_use_ilm.patchValue(false);
      this.is_send_to_rollover_alias.patchValue(false);
      this.is_create_first_index_of_rollover.patchValue(false);
      this.is_remove_suffix.patchValue(false);
      this.clearValidatorRequired(this.merge_to_one_index_index_nameInput);
      this.clearValidatorRequired(this.send_to_alias_aliasInput);
      this.clearValidatorRequired(this.send_to_pipeline_pipeline_nameInput);
      this.clearValidatorRequired(this.add_prefix_prefixInput);
      this.clearValidatorRequired(this.add_suffix_suffixInput);
      this.clearValidatorRequired(this.create_first_index_of_rollover_index_nameInput);
      this.clearValidatorRequired(this.send_to_data_stream_stream_nameInput);
      this.clearValidatorRequired(this.remove_suffix_suffixInput);
    }
    this.onShowRequiredMessage();
    this.cdr.markForCheck();
  }

  onMonitoringProject() {
    this.cdr.markForCheck();
    this.headerService.setHeaderTitle('Project monitoring');
    this.router.navigate(['/progress'], {queryParams: {id: this.projectForm.get('project_id').value, event: '2'}});


  }

  onSelectAllAfterFilter(event: MatCheckboxChange) {
    this.cdr.markForCheck();
    if (event.checked) {
      this.selectionIndexList = new SelectionModel<ISourceIndexList>(true, this.sourceIndexListMatSource.filteredData);
      this.selectionIndexList.selected.map(item => {
        item.is_checked = true;
        return item;
      });

      this.source_index_list.patchValue(this.selectionIndexList.selected);
      const filteredSourceIndexList = this.source_index_list.controls.filter(a => a.value !== null);
      const newSourceIndexList: UntypedFormArray = new UntypedFormArray([]);
      filteredSourceIndexList.forEach((item) => {
        newSourceIndexList.push(item);
      });

      this.source_index_list.patchValue(newSourceIndexList.value);
    } else {
      this.selectionIndexList.selected.map(item => {
        item.is_checked = false;
        return item;
      });
      this.source_index_list.patchValue(this.selectionIndexList.selected);
    }
  }

  onCloseErrorDialog(event: boolean) {
    this.cdr.markForCheck();
    this.isShowErrorDialog = false;
  }


  onSourceInput(username: any, password: any = '') {
    this.cdr.markForCheck();
    this.isDisableSourceTestButton = !(username && password);
  }

  onDestinationInput(username: any, password: any = '') {
    this.cdr.markForCheck();
    this.isDisableDestinationTestButton = !(username && password);
  }



}

export interface ICheckedSettings {
  label: string;
  checked: boolean;
}

export interface IRadioBtn {
  index: number;
  label: string;
}


