import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule, Routes} from '@angular/router';
import {MainComponent} from './components/main/main.component';
import {ProjectMonitoringComponent} from './components/projects-monitoring/project-monitoring.component';
import {ProgressComponent} from './components/progress/progress.component';

const routes: Routes = [
  {
    path: '',
    redirectTo: 'project_monitoring',
    pathMatch: 'full'
  },
  {
    path: 'project_configuration',
    component: MainComponent
  },
  {
    path: 'project_configuration/:id',
    component: MainComponent
  },
  {
    path: 'project_monitoring',
    component: ProjectMonitoringComponent
  },
  {
    path: 'progress',
    component: ProgressComponent
  },
  {
    path: '**',
    redirectTo: 'project_monitoring',
    pathMatch: 'full'
  },
];

@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    RouterModule.forChild(routes)
  ],
  exports: [
    RouterModule
  ]
})
export class ReindexRoutingModule {
}
