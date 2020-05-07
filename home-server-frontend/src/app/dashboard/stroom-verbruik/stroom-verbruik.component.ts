import {Component, OnDestroy, OnInit} from '@angular/core';
import {RxStompService} from '@stomp/ng2-stompjs';
import {Observable, Subscription} from 'rxjs';
import {Message} from '@stomp/stompjs';
import {Meterstand} from '../../meterstand/meterstand';
import {OpgenomenVermogen} from '../../opgenomen-vermogen/opgenomen-vermogen';
import {OpgenomenVermogenService} from '../../opgenomen-vermogen/opgenomen-vermogen.service';
import {Led, LedState} from '../led';
import {MeterstandService} from '../../meterstand/meterstand.service';
import {Router} from '@angular/router';

@Component({
  selector: 'home-stroom-verbruik',
  templateUrl: './stroom-verbruik.component.html',
  styleUrls: ['../ledbar.scss', './stroom-verbruik.component.scss']
})
export class StroomVerbruikComponent implements OnInit, OnDestroy {

  public LedState = LedState;

  public opgenomenVermogenLeds: Led[] = [];
  public opgenomenVermogen: OpgenomenVermogen;
  public meterstand: Meterstand;

  private meterstandObserver: Observable<Message>;
  private meterstandSubscription: Subscription;

  public opgenomenVermogenObserver: Observable<Message>;
  public opgenomenVermogenSubscription: Subscription;

  constructor(private opgenomenVermogenService: OpgenomenVermogenService,
              private meterstandService: MeterstandService,
              private stompService: RxStompService,
              private router: Router) { }

  public ngOnInit(): void {
    this.subscribeToMeterstandUpdates();
    this.subscribeToOpgenomenVermogenUpdates();
    this.getMostRecentOpgenomenVermogen();
    this.getMostRecentMeterstand();
  }

  private getMostRecentOpgenomenVermogen() {
    this.opgenomenVermogenService.getMostRecent().subscribe(mostRecentOpgenomenVermogen => {
      this.setOpgenomenVermogen(mostRecentOpgenomenVermogen);
    });
  }

  private getMostRecentMeterstand() {
    this.meterstandService.getMostRecent().subscribe(mostRecentMeterstand => this.meterstand = mostRecentMeterstand);
  }

  public ngOnDestroy() {
    this.meterstandSubscription.unsubscribe();
    this.opgenomenVermogenSubscription.unsubscribe();
  }

  private subscribeToMeterstandUpdates() {
    this.meterstandObserver = this.stompService.watch('/topic/meterstand');
    this.meterstandSubscription = this.meterstandObserver.subscribe((message) => this.meterstand = new Meterstand(message.body));
  }

  private subscribeToOpgenomenVermogenUpdates() {
    this.opgenomenVermogenObserver = this.stompService.watch('/topic/opgenomen-vermogen');
    this.opgenomenVermogenSubscription = this.opgenomenVermogenObserver.subscribe(
      (message) => this.setOpgenomenVermogen(new OpgenomenVermogen(message.body))
    );
  }

  private setOpgenomenVermogen(opgenomenVermogen: OpgenomenVermogen) {
    this.opgenomenVermogen = opgenomenVermogen;
    this.setOpgenomenVermogenLeds(opgenomenVermogen);
  }

  private setOpgenomenVermogenLeds(opgenomenVermogen: OpgenomenVermogen) {
    const opgenomenVermogenLeds: Led[] = [];
    opgenomenVermogenLeds.push(new Led(opgenomenVermogen.watt > 0 ? LedState.ON : LedState.OFF));
    for (let i = 1; i <= 9; i++) {
      opgenomenVermogenLeds.push(new Led(opgenomenVermogen.watt >= (i * 150) ? LedState.ON : LedState.OFF));
    }
    this.opgenomenVermogenLeds = opgenomenVermogenLeds;
  }

  public navigateToVerbruikDetails() {
    this.router.navigate(['/energie', 'verbruik', 'uur'], {queryParams: { energiesoort: 'stroom' }});
  }
}
