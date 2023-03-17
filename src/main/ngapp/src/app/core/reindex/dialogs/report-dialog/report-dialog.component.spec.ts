import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ReportDialogComponent } from './report-dialog.component';

describe('ReportDialogComponent', () => {
  let component: ReportDialogComponent;
  let fixture: ComponentFixture<ReportDialogComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ReportDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ReportDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
