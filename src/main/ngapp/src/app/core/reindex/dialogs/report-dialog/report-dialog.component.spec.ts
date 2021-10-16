import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ReportDialogComponent } from './report-dialog.component';

describe('ReportDialogComponent', () => {
  let component: ReportDialogComponent;
  let fixture: ComponentFixture<ReportDialogComponent>;

  beforeEach(async(() => {
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
