export interface IMonitoringProjectModel {
  status: string;
  project_id?: string;
  project_name: string;
  execution_progress: number;
  total_tasks: number;
  waiting_tasks: number;
  succeeded_tasks: number;
  failed_tasks: number;
  transferred_docs: number;
  estimated_docs: number;
  index_status: IIndexStatus[];
  on_fly_tasks_status: IOnFlyTask[];
  failed_tasks_status: IFailedTask[];
}

export interface IIndexStatus {
  end_time: number;
  estimated_docs: number;
  execution_progress: number;
  failed_tasks: number;
  index_name: string;
  index_status: string;
  start_time: string;
  succeeded_tasks: number;
  tasks_number: number;
  transferred_docs: number;
}

export interface IOnFlyTask {
  completedDocuments: number;
  estimatedDocuments: number;
  estimated_docs: number;
  executionProgress: number;
  failures: any[];
  index: string;
  is_done: boolean;
  is_failed: boolean;
  is_in_active_process: boolean;
  is_succeeded: boolean;
  params: string;
  self_generated_task_id: string;
  transferred_docs: number;
}

export interface IFailedTask {
  estimated_docs: number;
  execution_progress: number;
  failures: string[];
  index: string;
  is_done: boolean;
  is_failed: boolean;
  is_in_active_process: boolean;
  is_succeeded: boolean;
  params: string;
  self_generated_task_id: string;
  transferred_docs: number;
}

