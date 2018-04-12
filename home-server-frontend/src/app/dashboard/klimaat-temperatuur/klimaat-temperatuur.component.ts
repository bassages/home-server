import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {Led, LedState} from "../led";
import {Observable} from "rxjs/Observable";
import {Subscription} from "rxjs/Subscription";
import {Message} from '@stomp/stompjs';
import {Router} from "@angular/router";
import {StompService} from "@stomp/ng2-stompjs";
import {KlimaatService} from "../../klimaat/klimaat.service";
import {RealtimeKlimaat} from "../../klimaat/realtimeKlimaat";

@Component({
  selector: 'klimaat-temperatuur',
  templateUrl: './klimaat-temperatuur.component.html',
  styleUrls: ['./klimaat-temperatuur.component.scss']
})
export class KlimaatTemperatuurComponent implements OnInit, OnDestroy {

  public LedState = LedState;

  @Input()
  public sensorCode: string;

  public leds: Led[] = [];
  public klimaat: RealtimeKlimaat;

  private klimaatObserver: Observable<Message>;
  private klimaatSubscription: Subscription;

  constructor(private klimaatService: KlimaatService,
              private stompService: StompService,
              private router: Router) { }

  public ngOnInit(): void {
    this.subscribeToKlimaatUpdates();
    this.getMostRecentKlimaat();
  }

  public ngOnDestroy(): void {
    this.klimaatSubscription.unsubscribe();
  }

  private subscribeToKlimaatUpdates(): void {
    this.klimaatObserver = this.stompService.subscribe('/topic/klimaat');
    this.klimaatSubscription = this.klimaatObserver.subscribe((message) => this.setKlimaat(KlimaatService.toRealTimeKlimaat(JSON.parse(message.body))));
  }

  private setKlimaat(klimaat: RealtimeKlimaat): void {
    this.klimaat = klimaat;
    this.setLeds(this.klimaat.temperatuur);
  }

  private setLeds(temperatuur): void {
    this.leds.push(new Led(LedState.ON));
    for (let i = 1; i < 10; i++) {
      this.leds.push(new Led(temperatuur >= (i + 16) ? LedState.ON : LedState.OFF));
    }
  }

  private getMostRecentKlimaat(): void {
    this.klimaatService.getMostRecent(this.sensorCode).subscribe(mostRecent => this.setKlimaat(mostRecent));
  }

  public navigateToKlimaatHistory(): void {
    this.router.navigate(['/klimaat/historie'], {queryParams: { 'sensorType': 'temperatuur' }});
  }
}
