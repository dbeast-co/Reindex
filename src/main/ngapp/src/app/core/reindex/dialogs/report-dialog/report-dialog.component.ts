import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {YesNoDialogComponent} from '../yes-no-dialog/yes-no-dialog.component';
import {IProjectModel} from '../../models/project.model';
import { MatLegacyTableDataSource as MatTableDataSource } from '@angular/material/legacy-table';
import {IReportModel, IValidationResult} from '../../models/report.model';
import {ApiService} from '../../services/api.service';

@Component({
  selector: 'yl-report-dialog',
  templateUrl: './report-dialog.component.html',
  styleUrls: ['./report-dialog.component.scss']
})
export class ReportDialogComponent extends YesNoDialogComponent implements OnInit {
  @Output() isRetryEmit: EventEmitter<IReportModel> = new EventEmitter<IReportModel>();
  @Input() projectReport: IReportModel;
  @Input()project_id: string;
  projectReportDataSource: MatTableDataSource<Array<IValidationResult>> = new MatTableDataSource<Array<IValidationResult>>();
  displayedColumnsForReportDialogTable: Array<string> = ['task', 'status', 'notes'];
  status: string[] = [];

  constructor(private apiService: ApiService) {
    super();
  }

  ngOnInit() {
    this.projectReportDataSource = new MatTableDataSource<any>(this.projectReport.validation_results);
    this.status = this.projectReport.validation_results.map(report => report.status);

  }

  onYesReport() {
    this.isYesEmit.emit(false);
  }

  onNoReport() {
    this.isNoEmit.emit(false);
  }

  onRetryReport() {
    this.apiService.prepareProject(this.project_id).subscribe((project) => {
      if (project) {
        this.projectReport = project;
        this.projectReportDataSource = new MatTableDataSource<any>(this.projectReport.validation_results);
        this.isRetryEmit.emit(project);
      }
    });

  }
}
