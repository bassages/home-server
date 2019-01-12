import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {AppComponent} from './app.component';
import {MeterstandComponent} from './meterstand/meterstand.component';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {MeterstandService} from './meterstand/meterstand.service';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {DpDatePickerModule} from 'ng2-date-picker';
import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
import {NavbarComponent} from './navbar/navbar.component';
import {DashboardComponent} from './dashboard/dashboard.component';
import {LoadingIndicatorComponent} from './loading-indicator/loading-indicator.component';
import {LoadingIndicatorService} from './loading-indicator/loading-indicator.service';
import {ErrorHandlingComponent} from './error-handling/error-handling.component';
import {ErrorHandingService} from './error-handling/error-handing.service';
import { environment } from '../environments/environment';
import * as SockJS from 'sockjs-client';
import {InjectableRxStompConfig, RxStompService, rxStompServiceFactory} from '@stomp/ng2-stompjs';
import {OpgenomenVermogenService} from './opgenomen-vermogen/opgenomen-vermogen.service';
import {EnergieVerbruikService} from './energie-verbruik/energie-verbruik.service';
import {EnergieVerbruikComponent} from './energie-verbruik/energie-verbruik.component';
import {DecimalPipe} from '@angular/common';
import {StroomVerbruikComponent} from './dashboard/stroom-verbruik/stroom-verbruik.component';
import {GasVerbruikComponent} from './dashboard/gas-verbruik/gas-verbruik.component';
import {DateNavigatorComponent} from './date-navigator/date-navigator.component';
import {EnergieVerbruikUurHistorieService} from './energie-verbruik/energie-verbruik-uur-historie.service';
import {EnergieVerbruikDagHistorieService} from './energie-verbruik/energie-verbruik-dag-historie.service';
import {EnergieVerbruikMaandHistorieService} from './energie-verbruik/energie-verbruik-maand-historie.service';
import {EnergieVerbruikJaarHistorieService} from './energie-verbruik/energie-verbruik-jaar-historie.service';
import {EnergieVerbruikHistorieServiceProvider} from './energie-verbruik/energie-verbruik-historie-service-provider';
import {OpgenomenVermogenComponent} from './opgenomen-vermogen/opgenomen-vermogen.component';
import {ChartService} from './chart/chart.service';
import {MindergasnlComponent} from './mindergasnl/mindergasnl.component';
import {MindergasnlService} from './mindergasnl/mindergasnl.service';
import {KlimaatHistorieComponent} from './klimaat/klimaat-historie/klimaat-historie.component';
import {KlimaatService} from './klimaat/klimaat.service';
import {StatisticsComponent} from './chart/statistics/statistics.component';
import {ChartStatisticsService} from './chart/statistics/chart-statistics.service';
import {KlimaatTemperatuurComponent} from './dashboard/klimaat-temperatuur/klimaat-temperatuur.component';
import {KlimaatLuchtvochtigheidComponent} from './dashboard/klimaat-luchtvochtigheid/klimaat-luchtvochtigheid.component';
import {EnergiecontractComponent} from './energiecontract/energiecontract.component';
import {EnergiecontractService} from './energiecontract/energiecontract.service';
import {KlimaatHighestLowestComponent} from './klimaat/klimaat-highest-lowest/klimaat-highest-lowest.component';
import {KlimaatAverageComponent} from './klimaat/klimaat-average/klimaat-average.component';
import {KlimaatSensorsComponent} from './klimaat/klimaat-sensors/klimaat-sensors.component';
import {StandbyPowerComponent} from './standby-power/standby-power.component';
import {StandbyPowerService} from './standby-power/standby-power.service';
import {AuthorizationInterceptor} from './auth/authorization-interceptor';
import {AuthService} from './auth.service';
import {LoginComponent} from './login/login.component';
import {KlimaatSensorService} from './klimaat/klimaatsensor.service';

export function socketProvider() {
  return new SockJS('/ws');
}

const myRxStompConfig: InjectableRxStompConfig = {
  webSocketFactory: socketProvider,
  connectHeaders: {},

  // Heartbeat interval
  // Interval in milliseconds, set to 0 to disable
  heartbeatIncoming: 0, // Typical value 0 - disabled
  heartbeatOutgoing: 60000, // Typical value 20000

  // Wait in milliseconds before attempting auto reconnect
  // Set to 0 to disable
  reconnectDelay: 20000,

  // Will log diagnostics on console (in production environment only)
  debug: (str) => {
    if (!environment.production) {
      console.log(new Date(), str);
    }
  }
};

const appRoutes: Routes = [
  {path: '', pathMatch: 'full', component: LoginComponent},
  {path: 'dashboard', component: DashboardComponent},
  {path: 'meterstand', component: MeterstandComponent},
  {path: 'login', component: LoginComponent},
  {path: 'energie/opgenomen-vermogen', component: OpgenomenVermogenComponent},
  {path: 'energie/:verbruiksoort/:periode', component: EnergieVerbruikComponent},
  {path: 'energie/basisverbruik', component: StandbyPowerComponent},
  {path: 'mindergasnl', component: MindergasnlComponent},
  {path: 'klimaat/sensors', component: KlimaatSensorsComponent},
  {path: 'klimaat/historie', component: KlimaatHistorieComponent},
  {path: 'klimaat/hoogste-laagste', component: KlimaatHighestLowestComponent},
  {path: 'klimaat/gemiddelde', component: KlimaatAverageComponent},
  {path: 'energiecontract', component: EnergiecontractComponent}
];

@NgModule({
  declarations: [
    AppComponent,
    MeterstandComponent,
    NavbarComponent,
    DashboardComponent,
    StroomVerbruikComponent,
    GasVerbruikComponent,
    LoadingIndicatorComponent,
    ErrorHandlingComponent,
    EnergieVerbruikComponent,
    DateNavigatorComponent,
    OpgenomenVermogenComponent,
    LoginComponent,
    MindergasnlComponent,
    KlimaatHistorieComponent,
    StatisticsComponent,
    KlimaatTemperatuurComponent,
    KlimaatLuchtvochtigheidComponent,
    EnergiecontractComponent,
    KlimaatHighestLowestComponent,
    KlimaatAverageComponent,
    KlimaatSensorsComponent,
    StandbyPowerComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule,
    NgbModule,
    DpDatePickerModule,
    RouterModule.forRoot(appRoutes, {enableTracing: false, useHash: true})
  ],
  providers: [
    AuthService,
    DecimalPipe,
    ChartService,
    ChartStatisticsService,
    MeterstandService,
    OpgenomenVermogenService,
    EnergieVerbruikService,
    EnergieVerbruikUurHistorieService,
    EnergieVerbruikDagHistorieService,
    EnergieVerbruikMaandHistorieService,
    EnergieVerbruikJaarHistorieService,
    EnergieVerbruikHistorieServiceProvider,
    EnergiecontractService,
    MindergasnlService,
    KlimaatService,
    KlimaatSensorService,
    LoadingIndicatorService,
    StandbyPowerService,
    ErrorHandingService,
    {
      provide: InjectableRxStompConfig,
      useValue: myRxStompConfig
    },
    {
      provide: RxStompService,
      useFactory: rxStompServiceFactory,
      deps: [InjectableRxStompConfig]
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthorizationInterceptor,
      multi: true
    }
  ],
  bootstrap: [AppComponent]
})

export class AppModule {
}
