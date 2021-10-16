import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { FailuresDialogComponent } from './failures-dialog.component';

describe('FailuresDialogComponent', () => {
  let component: FailuresDialogComponent;
  let fixture: ComponentFixture<FailuresDialogComponent>;

  beforeEach(async(() => {
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
