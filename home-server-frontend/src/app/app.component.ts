import {Component, OnInit} from '@angular/core';
import * as moment from 'moment';
import 'moment/locale/nl';
import {AuthService} from './auth.service';

@Component({
  selector: 'home-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {


  constructor(private authService: AuthService) {
  }

  public ngOnInit(): void {
    moment.locale('nl');
    this.authService.determineCurrentLoginStatus().subscribe();
  }
}
