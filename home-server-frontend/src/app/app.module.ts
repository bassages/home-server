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

import * as SockJS from 'sockjs-client';
import {StompConfig, StompService} from "@stomp/ng2-stompjs";
import {OpgenomenVermogenService} from "./opgenomen-vermogen/opgenomenVermogen.service";
import {EnergieVerbruikService} from "./verbruik/verbruik.service";

export function socketProvider() {
  return new SockJS('/ws');
}

const stompConfig: StompConfig = {
  url: socketProvider,
  headers: { },

  // How often to heartbeat?
  // Interval in milliseconds, set to 0 to disable
  heartbeat_in: 0, // Typical value 0 - disabled
  heartbeat_out: 20000, // Typical value 20000 - every 20 seconds
  // Wait in milliseconds before attempting auto reconnect
  // Set to 0 to disable
  // Typical value 5000 (5 seconds)
  reconnect_delay: 5000,

  // Will log diagnostics on console
  debug: true
};

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
    OpgenomenVermogenService,
    EnergieVerbruikService,
    LoadingIndicatorService,
    ErrorHandingService,
    StompService, { provide: StompConfig, useValue: stompConfig}
  ],
  bootstrap: [AppComponent]
})

export class AppModule {

}
