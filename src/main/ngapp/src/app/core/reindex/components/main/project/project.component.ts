import {Component, Input, OnInit} from '@angular/core';
import {AbstractControl, UntypedFormGroup} from '@angular/forms';
import {IAlgorithmParams, IProjectModel, IReindexAlgorithm, ISourceIndexList, ISourceTemplateList} from '../../../models/project.model';
import { MatLegacyTableDataSource as MatTableDataSource } from '@angular/material/legacy-table';
import {SelectionModel} from '@angular/cdk/collections';
import {ICheckedSettings, IRadioBtn} from '../main.component';
import {IReportModel} from '../../../models/report.model';

@Component({
  selector: 'yl-project',
  templateUrl: './project.component.html',
  styleUrls: ['./project.component.scss']
})
export class ProjectComponent implements OnInit {
  @Input()projectForm: UntypedFormGroup;
  @Input()project: IProjectModel;
  @Input() displayedColumnsForIndexPatternNamesTable: string[];
  @Input()  displayedColumnsForTemplatesTable: string[];
  @Input()  sourceIndexListMatSource: MatTableDataSource<ISourceIndexList>;
  @Input()sourceIndexTemplatesMatSource: MatTableDataSource<ISourceTemplateList>;
  @Input()  selectionIndexList = new SelectionModel<ISourceIndexList>(true, []);
  @Input()  selectionTemplateList = new SelectionModel<ISourceTemplateList>(true, []);
  @Input()checkedCheckboxes: ICheckedSettings[];
  @Input() selectedAlgorithmParams: IAlgorithmParams[];
  @Input() selectedAlgorithm: IReindexAlgorithm;
  @Input() showSpinner: boolean;
  @Input()spinnerColor: string;
  @Input()notSelectedAlgorithms: AbstractControl[];
  @Input() defaultAlgorithm: IReindexAlgorithm;
  @Input() isActiveOverlay: boolean;
  @Input()  selectedRowsOfSourceIndexList: any[];
  @Input()searchTerm: string;
  @Input()  startStopBtn: string;
  @Input() showYesNoDialog: boolean;
  @Input()playIcon: string;
  @Input()cluster_status: string;
  @Input()  showReportDialog: boolean;
  @Input()  projectReport: IReportModel;
  @Input() showViewButtonInTables: boolean;
  @Input()  emptyRowForSourceIndexList: ISourceIndexList;
  @Input()  emptyRowForSourceTemplateList: ISourceTemplateList;
  @Input() radioBtns: IRadioBtn[];
  @Input()selectedRadioBtn: number;
  @Input() showSourceSSLBtn: boolean;
  @Input() showDestSSLBtn: boolean;
  constructor() { }

  ngOnInit() {
  }

}
