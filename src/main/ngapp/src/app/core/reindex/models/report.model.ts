export interface IReportModel {
  project_id: string;
  project_name: string;
  user_message: string;
  status: string;
  validation_results: Array<IValidationResult>;
}

export interface IValidationResult {
  notes: string;
  numericStatus: number;
  status: string;
  task: string;
  validation_param: string;
}
