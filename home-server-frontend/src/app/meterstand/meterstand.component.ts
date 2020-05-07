import {Component, OnInit} from '@angular/core';
import {MeterstandService} from './meterstand.service';
import {MeterstandOpDag} from './meterstandOpDag';
import sortBy from 'lodash/sortBy';
import * as moment from 'moment';
import {Moment} from 'moment';
import {LoadingIndicatorService} from '../loading-indicator/loading-indicator.service';
import {ErrorHandingService} from '../error-handling/error-handing.service';

@Component({
  selector: 'home-meterstand',
  templateUrl: './meterstand.component.html',
  styleUrls: ['./meterstand.component.scss'],
})
export class MeterstandComponent implements OnInit {

  public selectedYearMonth: Moment;

  public sortedMeterstandenPerDag: MeterstandOpDag[] = [];

  constructor(private meterstandService: MeterstandService,
              private loadingIndicatorService: LoadingIndicatorService,
              private errorHandlingService: ErrorHandingService) {
  }

  public ngOnInit(): void {
    this.selectedYearMonth = this.getStartOfCurrentMonth();
    setTimeout(() => this.getMeterstanden());
  }

  // noinspection JSMethodCanBeStatic
  private getStartOfCurrentMonth(): Moment {
    return moment().startOf('month');
  }

  private getMeterstanden(): void {
    const from = this.selectedYearMonth.clone().startOf('month');
    const to = from.clone().add(1, 'month');

    this.loadingIndicatorService.open();

    this.meterstandService.getMeterstanden(from, to).subscribe(
      response => this.sortedMeterstandenPerDag = sortBy<MeterstandOpDag>(response, ['dag']),
      error => this.errorHandlingService.handleError('De meterstanden konden nu niet worden opgehaald', error),
      () => this.loadingIndicatorService.close()
    );
  }

  public yearMonthChanged(selectedYearMonth: Moment): void {
    this.selectedYearMonth = selectedYearMonth;
    this.getMeterstanden();
  }
}

