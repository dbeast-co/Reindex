export interface IProjectMonitoring {
  end_time: string;
  estimated_docs: number;
  execution_progress: number;
  failed_tasks: number;
  project_id: string;
  project_name: string;
  project_status: string;
  start_time: string;
  succeeded_tasks: number;
  tasks_number: number;
  transferred_docs: number;
}
