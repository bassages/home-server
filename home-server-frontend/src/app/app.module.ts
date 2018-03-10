import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {RouterModule} from '@angular/router';
import {AppComponent} from './app.component';
import {MeterstandComponent} from './meterstand/meterstand.component';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {MeterstandService} from "./meterstand/meterstand.service";
import {FormsModule} from "@angular/forms";
import {DpDatePickerModule} from "ng2-date-picker";
import {HttpClientModule} from "@angular/common/http";
import {NavbarComponent} from './navbar/navbar.component';
import {DashboardComponent} from './dashboard/dashboard.component';
import {LoaderComponent} from './loader/loader.component';

@NgModule({
  declarations: [
    AppComponent,
    MeterstandComponent,
    NavbarComponent,
    DashboardComponent,
    LoaderComponent
  ],
  imports: [
    BrowserModule,
    NgbModule.forRoot(),
    DpDatePickerModule,
    HttpClientModule,
    FormsModule,
    RouterModule.forRoot([
        { path: '', pathMatch: 'full', component: DashboardComponent},
        { path: 'meterstand', component: MeterstandComponent }
      ]
    )
  ],
  entryComponents: [
    MeterstandComponent // Don't forget this!!!
  ],
  providers: [
    MeterstandService
  ],
  bootstrap: [AppComponent]
})

export class AppModule {

}
