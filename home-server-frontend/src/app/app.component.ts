import {Component, OnInit} from '@angular/core';
import * as moment from 'moment';

@Component({
  selector: 'home-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {

  constructor() { }

  ngOnInit() {
    moment.locale('nl');
  }
}
