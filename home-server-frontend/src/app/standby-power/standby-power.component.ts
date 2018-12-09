import {Component, OnInit} from '@angular/core';
import {StandbyPowerService} from './standby-power.service';
import {LoadingIndicatorService} from '../loading-indicator/loading-indicator.service';
import {ErrorHandingService} from '../error-handling/error-handing.service';
import {StandbyPowerInPeriod} from './standby-power-in-period';
import * as moment from 'moment';
import {Moment} from 'moment';

@Component({
  selector: 'home-standby-power',
  templateUrl: './standby-power.component.html',
  styleUrls: ['./standby-power.component.scss']
})
export class StandbyPowerComponent implements OnInit {

  public standbyPowerInPeriods: StandbyPowerInPeriod[];

  public selectedYear: Moment;

  constructor(private standbyPowerService: StandbyPowerService,
              private loadingIndicatorService: LoadingIndicatorService,
              private errorHandlingService: ErrorHandingService) { }

  public ngOnInit(): void {
    this.selectedYear = moment();
    setTimeout(() => this.getAndLoadData());
  }

  private getAndLoadData(): void {
    this.loadingIndicatorService.open();

    this.standbyPowerService.get(this.selectedYear.year()).subscribe(
      standbyPower => this.loadData(standbyPower),
      error => this.errorHandlingService.handleError('Basisverbruik kon niet worden opgehaald', error),
      () => this.loadingIndicatorService.close()
    );
  }

  private loadData(standbyPowerInLastQuarters: StandbyPowerInPeriod[]): void {
    this.standbyPowerInPeriods = standbyPowerInLastQuarters;
  }

  public onYearNavigate(selectedYear: Moment): void {
    this.selectedYear = selectedYear;
    this.getAndLoadData();
  }
}
