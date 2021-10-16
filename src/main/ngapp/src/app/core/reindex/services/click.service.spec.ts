import { TestBed } from '@angular/core/testing';

import { ClickService } from './click.service';

describe('ClickService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: ClickService = TestBed.get(ClickService);
    expect(service).toBeTruthy();
  });
});
