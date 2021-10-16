import { TestBed } from '@angular/core/testing';

import { SavedProjectService } from './saved-project.service';

describe('SavedProjectService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: SavedProjectService = TestBed.get(SavedProjectService);
    expect(service).toBeTruthy();
  });
});
