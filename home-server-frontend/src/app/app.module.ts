import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {AppComponent} from './app.component';
import {MeterstandComponent} from './meterstand/meterstand.component';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {MeterstandService} from "./meterstand/meterstand.service";
import {FormsModule} from "@angular/forms";
import {DpDatePickerModule} from "ng2-date-picker";
import {HttpClientModule} from "@angular/common/http";
import {NavbarComponent} from './navbar/navbar.component';
import {DashboardComponent} from './dashboard/dashboard.component';
import {LoadingIndicatorComponent} from './loading-indicator/loading-indicator.component';
import {LoadingIndicatorService} from "./loading-indicator/loading-indicator.service";
import {ErrorHandlingComponent} from './error-handling/error-handling.component';
import {ErrorHandingService} from "./error-handling/error-handing.service";

const appRoutes: Routes = [
  { path: '', pathMatch: 'full', component: DashboardComponent},
  { path: 'meterstand', component: MeterstandComponent }
];

@NgModule({
  declarations: [
    AppComponent,
    MeterstandComponent,
    NavbarComponent,
    DashboardComponent,
    LoadingIndicatorComponent,
    ErrorHandlingComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    FormsModule,
    NgbModule.forRoot(),
    DpDatePickerModule,
    RouterModule.forRoot(appRoutes,{enableTracing: true, useHash: true}
    )
  ],
  entryComponents: [
    MeterstandComponent // Don't forget this!!!
  ],
  providers: [
    MeterstandService,
    LoadingIndicatorService,
    ErrorHandingService
  ],
  bootstrap: [AppComponent]
})

export class AppModule {

}
