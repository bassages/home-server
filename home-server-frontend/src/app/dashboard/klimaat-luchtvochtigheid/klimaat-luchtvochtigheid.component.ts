import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {KlimaatService} from "../../klimaat/klimaat.service";
import {Led, LedState} from "../led";
import {RealtimeKlimaat} from "../../klimaat/realtimeKlimaat";
import {Observable} from "rxjs/Observable";
import {Router} from "@angular/router";
import {Subscription} from "rxjs/Subscription";
import {StompService} from "@stomp/ng2-stompjs";
import {Message} from '@stomp/stompjs';

@Component({
  selector: 'klimaat-luchtvochtigheid',
  templateUrl: './klimaat-luchtvochtigheid.component.html',
  styleUrls: ['./klimaat-luchtvochtigheid.component.scss']
})
export class KlimaatLuchtvochtigheidComponent implements  OnInit, OnDestroy {

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
    this.klimaatSubscription = this.klimaatObserver.subscribe((message) => this.setKlimaat(KlimaatService.toRealtimeKlimaat(JSON.parse(message.body))));
  }

  private setKlimaat(klimaat: RealtimeKlimaat): void {
    if (klimaat.sensorCode === this.sensorCode) {
      this.klimaat = klimaat;
      this.setLeds(this.klimaat.luchtvochtigheid);
    }
  }

  private setLeds(luchtvochtigheid): void {
    const leds: Led[] = [];
    leds.push(new Led(LedState.ON));
    for (let i = 1; i < 10; i++) {
      leds.push(new Led(luchtvochtigheid >= (i * 10) ? LedState.ON : LedState.OFF));
    }
    this.leds = leds;
  }

  private getMostRecentKlimaat(): void {
    this.klimaatService.getMostRecent(this.sensorCode).subscribe(mostRecent => this.setKlimaat(mostRecent));
  }

  public navigateToKlimaatHistory(): void {
    this.router.navigate(['/klimaat/historie'], {queryParams: { 'sensorType': 'luchtvochtigheid' }});
  }
}
