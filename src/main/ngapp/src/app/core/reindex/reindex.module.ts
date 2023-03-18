import {APP_INITIALIZER, NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ReindexComponent} from './reindex.component';
import {MaterialModule} from '../../shared/material/material.module';
import {YesNoDialogComponent} from './dialogs/yes-no-dialog/yes-no-dialog.component';
import {HeaderComponent} from './components/header/header.component';
import {MainComponent} from './components/main/main.component';
import {RouterModule} from '@angular/router';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {SavedProjectsComponent} from './components/saved-projects/saved-projects.component';
import {ProjectMonitoringComponent} from './components/projects-monitoring/project-monitoring.component';
import {ReindexRoutingModule} from './reindex-routing.module';
import {HttpClient, HttpClientModule} from '@angular/common/http';
import {CustomSpinnerDirective} from './directives/custom-spinner.directive';
import {SearchInSourceIndexListPipe} from './pipes/search-in-source-index-list.pipe';
import {ReportDialogComponent} from './dialogs/report-dialog/report-dialog.component';
import {ProjectComponent} from './components/main/project/project.component';
import {ViewDialogComponent} from './dialogs/view-dialog/view-dialog.component';
import {PrettyJsonPipe} from './pipes/pretty-json.pipe';
import {PrettyJsonModule} from 'angular2-prettyjson';
import {NgxJsonViewerModule} from 'ngx-json-viewer';
import {ProgressComponent} from './components/progress/progress.component';
import {FailuresDialogComponent} from './dialogs/failures-dialog/failures-dialog.component';
import {AppConfigService} from './services/app-config.service';
import {AppConfig} from './models/app.config';
import {ErrorComponent} from './dialogs/error/error.component';
import { DisableEnterDirective } from './directives/disable-enter.directive';
import { MatSortModule } from '@angular/material/sort';

export function initializeAppFn(appConfigService: AppConfigService) {
  return () => {
    return appConfigService.getAppConfig();
  };
}

// export function getUrl(appConfigService: AppConfigService) {
//   return () => {
//     return appConfigService.getConfigUrl();
//   };
// }

@NgModule({
    declarations: [
        ReindexComponent,
        YesNoDialogComponent,
        HeaderComponent,
        MainComponent,
        SavedProjectsComponent,
        ProjectMonitoringComponent,
        CustomSpinnerDirective,
        SearchInSourceIndexListPipe,
        ReportDialogComponent,
        ProjectComponent,
        ViewDialogComponent,
        PrettyJsonPipe,
        ProgressComponent,
        FailuresDialogComponent,
        ErrorComponent,
        DisableEnterDirective,
    ],
    imports: [
        CommonModule,
        MaterialModule,
        RouterModule,
        FormsModule,
        ReactiveFormsModule,
        ReindexRoutingModule,
        HttpClientModule,
        NgxJsonViewerModule,
        PrettyJsonModule,
    ],
    exports: [
        ReindexComponent,
    ],
    providers: [
        {
            provide: AppConfig,
            deps: [HttpClient],
            useExisting: AppConfigService
        },
        {
            provide: APP_INITIALIZER,
            multi: true,
            deps: [AppConfigService],
            useFactory: initializeAppFn
        },
        // {
        //   provide: APP_INITIALIZER,
        //   multi: true,
        //   deps: [AppConfigService],
        //   useFactory: getUrl
        // }
    ]
})
export class ReindexModule {
}
