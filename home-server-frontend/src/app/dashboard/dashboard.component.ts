import {Component, OnDestroy, OnInit} from '@angular/core';
import {StompService} from "@stomp/ng2-stompjs";
import {Subscription} from "rxjs/Subscription";
import {Observable} from "rxjs/Observable";
import {Message} from '@stomp/stompjs';
import {Klimaat} from "../klimaat/klimaat";

@Component({
  selector: 'dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit, OnDestroy {

  private klimaatObserver: Observable<Message>;
  private klimaatSubscription: Subscription;

  constructor(private stompService: StompService) { }

  ngOnInit(): void {
    this.subscribeToKlimaatUpdates();
  }

  public ngOnDestroy() {
    this.klimaatSubscription.unsubscribe();
  }

  public subscribeToKlimaatUpdates() {
    this.klimaatObserver = this.stompService.subscribe('/topic/klimaat');
    this.klimaatSubscription = this.klimaatObserver.subscribe(
      (message) => console.info(new Klimaat(message.body))
    );
  }
}
