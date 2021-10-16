import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {ReindexModule} from './core/reindex/reindex.module';
import {ReindexRoutingModule} from './core/reindex/reindex-routing.module';
import {ToastrModule} from 'ngx-toastr';
import {HashLocationStrategy, JsonPipe, LocationStrategy} from '@angular/common';
import {SafeJsonPipe} from 'angular2-prettyjson/src/json.pipe';
import {PrettyJsonModule} from 'angular2-prettyjson';
import {AboutModule} from './about/about.module';
import {HTTP_INTERCEPTORS} from '@angular/common/http';
import {ErrorInterceptor} from './core/reindex/interceptors/error-interceptor';

@NgModule({
  declarations: [
    AppComponent,

  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    ReindexModule,
    ToastrModule.forRoot({
      positionClass: 'toast-bottom-right'
    }),
    AboutModule,


  ],
  providers: [
    {provide: LocationStrategy, useClass: HashLocationStrategy},
    // {provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true}
  ],

  bootstrap: [AppComponent]
})
export class AppModule {
}
