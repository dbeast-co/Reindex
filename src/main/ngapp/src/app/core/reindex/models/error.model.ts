import {HttpHeaders} from '@angular/common/http';

export interface IError {
  error: {
    error: string,
    status: {
      cluster_status: string
    }
  };
  headers: HttpHeaders;
  message: string;
  name: string;
  status: number;
  statusText: string;
}
