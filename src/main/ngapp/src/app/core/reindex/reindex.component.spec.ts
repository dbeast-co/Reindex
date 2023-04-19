import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ReindexComponent } from './reindex.component';

describe('ReindexComponent', () => {
  let component: ReindexComponent;
  let fixture: ComponentFixture<ReindexComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ReindexComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ReindexComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
