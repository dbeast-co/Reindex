import { TestBed } from '@angular/core/testing';

import { ProjectIdService } from './project-id.service';

describe('ProjectIdService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: ProjectIdService = TestBed.get(ProjectIdService);
    expect(service).toBeTruthy();
  });
});
