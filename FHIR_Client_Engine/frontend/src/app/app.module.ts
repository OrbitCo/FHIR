import { NgModule } from '@angular/core';
import { CommonModule } from "@angular/common";
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ExtraOptions, PreloadAllModules, RouterModule } from '@angular/router';
import { MarkdownModule } from 'ngx-markdown';
import { FuseModule } from '@fuse';
import { FuseConfigModule } from '@fuse/services/config';
import { FuseMockApiModule } from '@fuse/lib/mock-api';
import { CoreModule } from 'app/core/core.module';
import { appConfig } from 'app/core/config/app.config';
import { mockApiServices } from 'app/mock-api';
import { LayoutModule } from 'app/layout/layout.module';
import { AppComponent } from 'app/app.component';
import { appRoutes } from 'app/app.routing';
import { ViewProfilesComponent } from './modules/admin/dashboards/view-profiles/view-profiles.component';
import { ViewProjectsComponent } from './modules/admin/dashboards/view-projects/view-projects.component';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { CreateProfileComponent } from './modules/admin/dashboards/create-profile/create-profile.component';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatMomentDateModule } from '@angular/material-moment-adapter';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatChipsModule } from '@angular/material/chips';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDividerModule } from '@angular/material/divider';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatSortModule } from '@angular/material/sort';
import { MatTabsModule } from "@angular/material/tabs";
import { MatTooltipModule } from '@angular/material/tooltip';
import { SharedModule } from './shared/shared.module';
import { CreateProjectComponent } from './modules/admin/dashboards/create-project/create-project.component';
import { DashboardCommonService } from './modules/admin/services/dashboard-common.service';
import { DeleteConfirmationDailogComponent } from './modules/admin/dashboards/delete-confirmation-dailog/delete-confirmation-dailog.component';
import { ProjectDetailsComponent } from './modules/admin/dashboards/project-details/project-details.component';
import {MatRadioModule} from '@angular/material/radio';
import { MatCheckboxModule } from '@angular/material/checkbox';

const routerConfig: ExtraOptions = {
    preloadingStrategy       : PreloadAllModules,
    scrollPositionRestoration: 'enabled'
};

@NgModule({
    declarations: [
        AppComponent,
        ViewProfilesComponent,
        ViewProjectsComponent,
        CreateProfileComponent,
        CreateProjectComponent,
        DeleteConfirmationDailogComponent,
        ProjectDetailsComponent
    ],
    imports     : [
        BrowserModule,
        BrowserAnimationsModule,
        RouterModule.forRoot(appRoutes, routerConfig),

        // Fuse, FuseConfig & FuseMockAPI
        FuseModule,
        FuseConfigModule.forRoot(appConfig),
        FuseMockApiModule.forRoot(mockApiServices),

        // Core module of your application
        CoreModule,

        CommonModule,

        // Layout module of your application
        LayoutModule,

        // Mat Tabs
        MatTabsModule,

        // Mat Table
        MatTableModule,

        // Mat Icons
        MatIconModule,

        // Mat Dialog
        MatDialogModule,

        //Mat Form
        MatFormFieldModule,

        MatButtonModule,
        MatButtonToggleModule,
        MatDividerModule,
        MatMenuModule,
        MatProgressBarModule,
        MatSortModule,
        MatTooltipModule,
        MatInputModule,
        MatRadioModule,
        MatCheckboxModule,
        MatChipsModule,
        MatDatepickerModule,
        MatMomentDateModule,
        MatSelectModule,
        SharedModule,
        MatSnackBarModule,
        MatRadioModule,
        MatCheckboxModule,
        // 3rd party modules that require global configuration via forRoot
        MarkdownModule.forRoot({}),

    ],
    bootstrap   : [
        AppComponent
    ],
    providers: [
        { provide: MAT_DIALOG_DATA, useValue: {} },
        { provide: MatDialogRef, useValue: {} } ,
       DashboardCommonService
    ],
})
export class AppModule
{
}
