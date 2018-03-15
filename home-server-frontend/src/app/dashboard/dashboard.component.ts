import {Component, OnDestroy, OnInit} from '@angular/core';
import {StompService} from "@stomp/ng2-stompjs";
import {Subscription} from "rxjs/Subscription";
import {Observable} from "rxjs/Observable";
import {Message} from '@stomp/stompjs';
import {Meterstand} from "../meterstand/meterstand";
import {Klimaat} from "../klimaat/klimaat";
import {OpgenomenVermogen} from "../opgenomen-vermogen/opgenomenVermogen";
import {OpgenomenVermogenService} from "../opgenomen-vermogen/opgenomenVermogen.service";
import {Led, LedState} from "./led";
import {MeterstandService} from "../meterstand/meterstand.service";
import {EnergieVerbruikService} from "../verbruik/verbruik.service";
import * as _ from "lodash";
import {VerbruikOpDag} from "../verbruik/verbruikOpDag";
import {GemiddeldVerbruikInPeriod} from "../verbruik/gemiddeldVerbruikInPeriod";
import {TariefIndicator} from "../opgenomen-vermogen/tariefIndicator";
import moment = require("moment");

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit, OnDestroy {

  public LedState = LedState;
  public tariefIndicator = TariefIndicator;

  public opgenomenVermogenLeds: Led[] = [];
  public opgenomenVermogen: OpgenomenVermogen;

  public meterstand: Meterstand;

  public verbruikVandaag: VerbruikOpDag;
  public gemiddeldVerbruikPerDagInAfgelopenWeek: GemiddeldVerbruikInPeriod;

  public gasLeds: Led[] = [];

  private meterstandObserver: Observable<Message>;
  private meterstandSubscription: Subscription;

  public opgenomenVermogenObserver: Observable<Message>;
  public opgenomenVermogenSubscription: Subscription;

  private klimaatObserver: Observable<Message>;
  private klimaatSubscription: Subscription;

  constructor(private stompService: StompService,
              private opgenomenVermogenService: OpgenomenVermogenService,
              private meterstandService: MeterstandService,
              private energieVerbruikService: EnergieVerbruikService) { }

  ngOnInit(): void {
    this.subscribeToKlimaatUpdates();
    this.subscribeToMeterstandUpdates();
    this.subscribeToOpgenomenVermogenUpdates();

    this.getMostRecentOpgenomenVermogen();
    this.getGemiddeldVerbruikAfgelopenWeek();
    this.getVerbruikVandaag();
    this.getMostRecentMeterstand();
  }

  private getMostRecentOpgenomenVermogen() {
    this.opgenomenVermogenService.getMostRecent().subscribe(
      httpResponse => {
        this.setOpgenomenVermogen({...httpResponse.body});
      }
    );
  }

  private getMostRecentMeterstand() {
    this.meterstandService.getMostRecent().subscribe(
      httpResponse => {
        this.setMeterstand({...httpResponse.body});
      }
    );
  }

  public ngOnDestroy() {
    this.meterstandSubscription.unsubscribe();
    this.opgenomenVermogenSubscription.unsubscribe();
    this.klimaatSubscription.unsubscribe();
  }

  public subscribeToKlimaatUpdates() {
    this.klimaatObserver = this.stompService.subscribe('/topic/klimaat');
    this.klimaatSubscription = this.klimaatObserver.subscribe(
      (message) => console.info(new Klimaat(message.body))
    );
  }

  private subscribeToMeterstandUpdates() {
    this.meterstandObserver = this.stompService.subscribe('/topic/meterstand');
    this.meterstandSubscription = this.meterstandObserver.subscribe(
      (message) => console.info(new Klimaat(message.body))
    );
  }

  private subscribeToOpgenomenVermogenUpdates() {
    this.opgenomenVermogenObserver = this.stompService.subscribe('/topic/opgenomen-vermogen');
    this.opgenomenVermogenSubscription = this.opgenomenVermogenObserver.subscribe(
      (message) => this.setOpgenomenVermogen(new OpgenomenVermogen(message.body))
    );
  }

  private setOpgenomenVermogen(opgenomenVermogen: OpgenomenVermogen) {
    this.opgenomenVermogen = opgenomenVermogen;
    this.setOpgenomenVermogenLeds(opgenomenVermogen);
  }

  private setMeterstand(meterstand: Meterstand) {
    this.meterstand = meterstand;
  }

  private getVerbruikVandaag() {
    const from = moment().startOf('day');
    const to = from.clone().add(1, 'day');

    this.energieVerbruikService.getVerbruikPerDag(from, to).subscribe(
      (httpResponse) => {
        let verbruikPerDag: VerbruikOpDag[] = {...httpResponse.body};
        if (verbruikPerDag) {
          this.verbruikVandaag = verbruikPerDag[0];
          this.setGasVerbruikVandaagLeds();
        }
      }
    );
  }

  private getGemiddeldVerbruikAfgelopenWeek() {
    const to = moment().startOf('day');
    const from = to.clone().subtract(6, 'day');

    this.energieVerbruikService.getGemmiddeldVerbruikPerDag(from, to).subscribe(
      (httpResponse) => {
        this.gemiddeldVerbruikPerDagInAfgelopenWeek = {...httpResponse.body};
        this.setGasVerbruikVandaagLeds();
      }
    );
  }

  private setOpgenomenVermogenLeds(opgenomenVermogen: OpgenomenVermogen) {
    let opgenomenVermogenLeds: Led[] = [];
      opgenomenVermogenLeds.push(new Led(opgenomenVermogen.watt > 0 ? LedState.ON : LedState.OFF));
      for (let i = 1; i <= 9; i++) {
        opgenomenVermogenLeds.push(new Led(opgenomenVermogen.watt >= (i * 150) ? LedState.ON : LedState.OFF));
    }
    this.opgenomenVermogenLeds = opgenomenVermogenLeds;
  }

  private setGasVerbruikVandaagLeds() {
    if (   this.verbruikVandaag && _.isNumber(this.verbruikVandaag.gasVerbruik)
        && this.gemiddeldVerbruikPerDagInAfgelopenWeek && _.isNumber(this.gemiddeldVerbruikPerDagInAfgelopenWeek.gasVerbruik)) {
      const procentueleVeranderingTovAfgelopenWeek: number = ((this.verbruikVandaag.gasVerbruik - this.gemiddeldVerbruikPerDagInAfgelopenWeek.gasVerbruik) / this.gemiddeldVerbruikPerDagInAfgelopenWeek.gasVerbruik) * 100;

      let gasLeds: Led[] = new Array<Led>(10);

      gasLeds[9] = new Led(procentueleVeranderingTovAfgelopenWeek >= 50 ? LedState.ON : LedState.OFF);
      gasLeds[8] = new Led(procentueleVeranderingTovAfgelopenWeek >= 40 ? LedState.ON : LedState.OFF);
      gasLeds[7] = new Led(procentueleVeranderingTovAfgelopenWeek >= 30 ? LedState.ON : LedState.OFF);
      gasLeds[6] = new Led(procentueleVeranderingTovAfgelopenWeek >= 20 ? LedState.ON : LedState.OFF);
      gasLeds[5] = new Led(procentueleVeranderingTovAfgelopenWeek >= 10 ? LedState.ON : LedState.OFF);
      gasLeds[4] = new Led(procentueleVeranderingTovAfgelopenWeek >= 0 ? LedState.ON : LedState.OFF);
      gasLeds[3] = new Led(procentueleVeranderingTovAfgelopenWeek >= -10 ? LedState.ON : LedState.OFF);
      gasLeds[2] = new Led(procentueleVeranderingTovAfgelopenWeek >= -20 ? LedState.ON : LedState.OFF);
      gasLeds[1] = new Led(procentueleVeranderingTovAfgelopenWeek >= -30 ? LedState.ON : LedState.OFF);
      gasLeds[0] = new Led(LedState.ON);

      this.gasLeds = gasLeds;
    }
  }

}
