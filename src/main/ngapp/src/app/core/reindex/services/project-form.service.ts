import {Injectable} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {from, Observable} from 'rxjs';
import {IProjectModel} from '../models/project.model';
import {ProjectService} from './project.service';

@Injectable({
  providedIn: 'root'
})
export class ProjectFormService {
  private projectForm: FormGroup;
  private projectFromFromService: IProjectModel;

  constructor(private fb: FormBuilder, private projectService: ProjectService) {
    // this.projectService.getProject().subscribe(data => {
    //   this.projectFromFromService = data;
    // });
  }

  getProjectForm(): FormGroup {
    this.projectForm = this.fb.group({
      projectId: [''],
      project_name: [],
      connection_settings: this.fb.group({
        source: this.fb.group({
          authentication_enabled: [null],
          es_host: [''],
          password: [''],
          ssl_enabled: [''],
          ssl_file: [null],
          status: [''],
          username: ['']
        }),
        destination: this.fb.group({
          authentication_enabled: [null],
          es_host: [''],
          password: [''],
          ssl_enabled: [''],
          ssl_file: [null],
          status: [''],
          username: ['']
        })
      }),
      reindex_settings: this.fb.group({}),
      source_index_list: this.fb.array([]),
      source_template_list: this.fb.array([])
    });

    return this.projectForm;
  }
}
