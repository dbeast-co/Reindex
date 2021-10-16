import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ReindexComponent } from './reindex.component';

describe('ReindexComponent', () => {
  let component: ReindexComponent;
  let fixture: ComponentFixture<ReindexComponent>;

  beforeEach(async(() => {
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
