import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { SavedProjectsComponent } from './saved-projects.component';

describe('SavedProjectsComponent', () => {
  let component: SavedProjectsComponent;
  let fixture: ComponentFixture<SavedProjectsComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ SavedProjectsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SavedProjectsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
