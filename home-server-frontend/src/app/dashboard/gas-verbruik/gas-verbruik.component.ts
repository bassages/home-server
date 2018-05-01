import {Component, OnDestroy, OnInit} from '@angular/core';
import {StompService} from "@stomp/ng2-stompjs";
import {Subscription} from "rxjs/Subscription";
import {Observable} from "rxjs/Observable";
import {Message} from '@stomp/stompjs';
import {Meterstand} from "../../meterstand/meterstand";
import {Led, LedState} from "../led";
import {MeterstandService} from "../../meterstand/meterstand.service";
import {EnergieVerbruikService} from "../../energie-verbruik/energie-verbruik.service";
import * as _ from "lodash";
import {VerbruikOpDag} from "../../energie-verbruik/verbruikOpDag";
import {GemiddeldVerbruikInPeriod} from "../../energie-verbruik/gemiddeldVerbruikInPeriod";
import {Router} from "@angular/router";
import * as moment from "moment";

@Component({
  selector: 'gas-verbruik',
  templateUrl: './gas-verbruik.component.html',
  styleUrls: ['./gas-verbruik.component.scss']
})
export class GasVerbruikComponent implements OnInit, OnDestroy {

  public LedState = LedState;

  public meterstand: Meterstand;
  public verbruikVandaag: VerbruikOpDag;
  public gemiddeldVerbruikPerDagInAfgelopenWeek: GemiddeldVerbruikInPeriod;

  public gasLeds: Led[] = [];

  private meterstandObserver: Observable<Message>;
  private meterstandSubscription: Subscription;

  constructor(private stompService: StompService,
              private router: Router,
              private meterstandService: MeterstandService,
              private energieVerbruikService: EnergieVerbruikService) { }

  public ngOnInit(): void {
    this.subscribeToMeterstandUpdates();

    this.getGemiddeldVerbruikAfgelopenWeek();
    this.getVerbruikVandaag();
    this.getMostRecentMeterstand();
  }

  private getMostRecentMeterstand() {
    this.meterstandService.getMostRecent().subscribe(mostRecentMeterstand => this.meterstand = mostRecentMeterstand);
  }

  public ngOnDestroy() {
    this.meterstandSubscription.unsubscribe();
  }

  private subscribeToMeterstandUpdates() {
    this.meterstandObserver = this.stompService.subscribe('/topic/meterstand');
    this.meterstandSubscription = this.meterstandObserver.subscribe((message) => this.meterstand = new Meterstand(message.body));
  }

  private getVerbruikVandaag() {
    const from = moment().startOf('day');
    const to = from.clone().add(1, 'day');

    this.energieVerbruikService.getVerbruikPerDag(from, to).subscribe(
      (verbruikPerDag: VerbruikOpDag[]) => {
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

    this.energieVerbruikService.getGemiddeldVerbruikPerDag(from, to).subscribe(
      (gemiddeldVerbruikPerDagInAfgelopenWeek: GemiddeldVerbruikInPeriod) => {
        this.gemiddeldVerbruikPerDagInAfgelopenWeek = gemiddeldVerbruikPerDagInAfgelopenWeek;
        this.setGasVerbruikVandaagLeds();
      }
    );
  }

  private setGasVerbruikVandaagLeds() {
    if (this.verbruikVandaag && _.isNumber(this.verbruikVandaag.gasVerbruik)
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

  public navigateToVerbruikDetails() {
    this.router.navigate(['/energie', 'verbruik', 'uur'], {queryParams: { energiesoort: 'gas' }});
  }
}
