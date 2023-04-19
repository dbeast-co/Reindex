import {NgModule} from '@angular/core';
import {Routes, RouterModule} from '@angular/router';
import {HeaderComponent} from './core/reindex/components/header/header.component';

const routes: Routes = [


  {
    path: '',
    loadChildren: () => import('./core/reindex/reindex.module').then(m => m.ReindexModule)
  },
  // {
  //   path: '',
  //   loadChildren: () => import('./core/reindex/reindex.module').then(m => m.ReindexModule)
  // }
  {
    path: 'about',
    loadChildren: () => import('./about/about.module').then(m => m.AboutModule)

  }

];

@NgModule({
  imports: [RouterModule.forRoot(routes, { useHash: true })],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
