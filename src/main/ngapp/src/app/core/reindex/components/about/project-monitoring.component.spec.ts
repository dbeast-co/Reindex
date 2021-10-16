import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjectMonitoringComponent } from './project-monitoring.component';

describe('AboutComponent', () => {
  let component: ProjectMonitoringComponent;
  let fixture: ComponentFixture<ProjectMonitoringComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ProjectMonitoringComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectMonitoringComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
