import {Component, OnDestroy} from '@angular/core';
import {StompService} from "@stomp/ng2-stompjs";
import {Subscription} from "rxjs/Subscription";
import {Observable} from "rxjs/Observable";
import {Message} from '@stomp/stompjs';
import {Meterstand} from "../meterstand/meterstand";
import {Klimaat} from "../klimaat/klimaat";

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnDestroy {

  private meterstandObserver: Observable<Message>;
  private meterstandSubscription: Subscription;

  private opgenomenVermogenObserver: Observable<Message>;
  private opgenomenVermogenSubscription: Subscription;

  private klimaatObserver: Observable<Message>;
  private klimaatSubscription: Subscription;

  constructor(private stompService: StompService) {
    this.subscribeToKlimaatUpdates();
    this.subscribeToMeterstandUpdates();
    this.subscribeToOpgenomenVermogenUpdates();
  }

  public ngOnDestroy() {
    this.meterstandSubscription.unsubscribe();
    this.opgenomenVermogenSubscription.unsubscribe();
    this.klimaatSubscription.unsubscribe();
  }

  public subscribeToKlimaatUpdates() {
    this.klimaatObserver = this.stompService.subscribe('/topic/klimaat');
    this.klimaatSubscription = this.klimaatObserver.subscribe(this.onKlimaatMessage);
  }

  private subscribeToMeterstandUpdates() {
    this.meterstandObserver = this.stompService.subscribe('/topic/meterstand');
    this.meterstandSubscription = this.meterstandObserver.subscribe(this.onMeterstandMessage);
  }

  private subscribeToOpgenomenVermogenUpdates() {
    this.opgenomenVermogenObserver = this.stompService.subscribe('/topic/opgenomen-vermogen');
    this.opgenomenVermogenSubscription = this.opgenomenVermogenObserver.subscribe(this.onOpgenomenVermogenMessage);
  }

  private onMeterstandMessage(message: Message) {
    console.log('Meterstand', new Meterstand(message.body));
  }

  private onKlimaatMessage(message: Message) {
    console.log('Klimaat', new Klimaat(message.body));
  }

  private onOpgenomenVermogenMessage(message: Message) {
    console.log('Opgenomen vermogen', message);
  }
}
