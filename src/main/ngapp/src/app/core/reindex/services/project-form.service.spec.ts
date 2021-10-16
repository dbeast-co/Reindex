import { TestBed } from '@angular/core/testing';

import { ProjectFormService } from './project-form.service';

describe('ProjectFormService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: ProjectFormService = TestBed.get(ProjectFormService);
    expect(service).toBeTruthy();
  });
});
