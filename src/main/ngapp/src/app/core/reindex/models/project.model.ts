export class ISourceIndexList {
  index_name: string;
  is_checked: boolean;
}

export class ISourceTemplateList {
  template_name: string;
  is_checked: boolean;
}

export class IStatus {
  execution_progress: number;
  project_status: string;
  is_done: boolean;
  is_failed: boolean;
  is_in_active_process: boolean;
}

export interface IProjectModel {
  project_id: string;
  project_name: string;
  connection_settings: IConnectionSettings;
  reindex_settings: IReindexSettings;
  source_index_list: Array<ISourceIndexList>;
  source_template_list: Array<ISourceTemplateList>;
  status: IStatus;
}

interface IConnectionSettings {
  source: ISettings;
  destination: ISettings;
}

export interface ISettings {
  authentication_enabled: boolean;
  es_host: string;
  password: string;
  ssl_enabled: boolean;
  ssl_file: object;
  status: string;
  username: string;
}

export class IReindexAlgorithm {
  reindex_algorithm_name: string;
  is_selected: boolean;
  algorithm_params: Array<IAlgorithmParams>;
}

export class IAlgorithmParams {
  actual_value: number;
  default_value: string;
  field_type: string;
  label: string;
}

interface IReindexSettings {
  add_prefix_prefix: string;
  add_suffix_suffix: string;
  associate_with_ilm_policy_policy_name: string;
  create_first_index_of_rollover_index_name: string;
  remove_suffix_suffix: string;
  is_add_prefix: boolean;
  is_add_suffix: boolean;
  is_associate_with_ilm_policy: boolean;
  is_continue_on_failure: boolean;
  is_create_first_index_of_rollover: boolean;
  is_merge_to_one_index: boolean;
  is_send_to_alias: boolean;
  is_send_to_pipeline: boolean;
  is_send_to_rollover_alias: boolean;
  is_transfer_index_settings_from_source_index: boolean;
  is_send_to_data_stream: boolean;
  is_use_ilm: boolean;
  is_use_same_index_name: boolean;
  is_remove_suffix: boolean;
  merge_to_one_index_index_name: string;
  number_of_concurrent_processed_indices: number;
  reindex_algorithms: Array<IReindexAlgorithm>;
  reindex_type: string;
  send_to_alias_alias: string;
  send_to_pipeline_pipeline_name: string;
  send_to_rollover_alias_alias: string;
  send_to_data_stream_stream_name: string;
  total_number_of_threads: number;
  useIlm: boolean;

}

export interface IClusterStatus {
  cluster_status: string;
}


