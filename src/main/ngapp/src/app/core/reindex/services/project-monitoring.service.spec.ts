import { TestBed } from '@angular/core/testing';

import { ProjectMonitoringService } from './project-monitoring.service';

describe('ProjectMonitoringService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: ProjectMonitoringService = TestBed.get(ProjectMonitoringService);
    expect(service).toBeTruthy();
  });
});
