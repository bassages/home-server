import {Component, OnInit, ViewChild} from '@angular/core';
import {MeterstandService} from "./meterstand.service";
import {MeterstandOpDag} from "./meterstandOpDag";

import {DatePickerComponent, IDatePickerConfig} from 'ng2-date-picker';
import * as _ from "lodash";
import * as moment from "moment";
import {Moment} from "moment";
import {LoadingIndicatorService} from "../loading-indicator/loading-indicator.service";
import {ErrorHandingService} from "../error-handling/error-handing.service";

@Component({
  selector: 'meterstand',
  templateUrl: './meterstand.component.html',
  styleUrls: ['./meterstand.component.scss'],
})
export class MeterstandComponent implements OnInit {
  @ViewChild('monthPicker') monthPicker: DatePickerComponent;

  private selectedMonthFormat = "MMMM YYYY";

  monthPickerConfig: IDatePickerConfig;
  monthPickerModel: String;
  selectedMonth: Moment;

  sortedMeterstandenPerDag: MeterstandOpDag[] = [];

  constructor(private meterstandService: MeterstandService,
              private loadingIndicatorService: LoadingIndicatorService,
              private errorHandlingService: ErrorHandingService) {
    }

  public ngOnInit() {
    this.initMonthPicker();
  }

  private initMonthPicker() {
    this.monthPickerConfig = {
      format: this.selectedMonthFormat,
      max: MeterstandComponent.getStartOfCurrentMonth()
    };
    this.setSelectedMonth(MeterstandComponent.getStartOfCurrentMonth());
  }

  private static getStartOfCurrentMonth(): Moment {
    return moment().startOf('month');
  }

  private setSelectedMonth(month: Moment) {
    this.selectedMonth = month;
    this.monthPickerModel = month.format(this.selectedMonthFormat);
  }

  private getMeterstanden(): void {
    const from = this.selectedMonth.clone().startOf('month');
    const to = from.clone().add(1, 'month');

    this.loadingIndicatorService.open();
    this.sortedMeterstandenPerDag = [];

    this.meterstandService.getMeterstanden(from, to).subscribe(
      response => this.sortedMeterstandenPerDag = _.sortBy<MeterstandOpDag>(response, ['dag']),
      error => this.errorHandlingService.handleError("De meterstanden konden nu niet worden opgehaald", error),
      () => this.loadingIndicatorService.close()
    );
  }

  monthPickerChanged(selectedMonth: Moment): void {
    if (!_.isUndefined(selectedMonth)) {
      this.setSelectedMonth(selectedMonth);
      this.getMeterstanden();
    }
  }

  navigate(numberOfMonths: number): void {
    this.setSelectedMonth(this.selectedMonth.add(numberOfMonths, 'months'));
  }

  isMaxSelected(): boolean {
    const now: Moment = moment();
    return now.month() === this.selectedMonth.month() && now.year() === this.selectedMonth.year();
  }
}

