import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { FailuresDialogComponent } from './failures-dialog.component';

describe('FailuresDialogComponent', () => {
  let component: FailuresDialogComponent;
  let fixture: ComponentFixture<FailuresDialogComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ FailuresDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FailuresDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
