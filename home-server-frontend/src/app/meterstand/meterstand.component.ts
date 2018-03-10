import {Component, OnInit, ViewChild} from '@angular/core';
import {MeterstandService} from "./meterstand.service";
import {MeterstandOpDag} from "./meterstandOpDag";

import {DatePickerComponent, IDatePickerConfig} from 'ng2-date-picker';
import * as _ from "lodash";
import * as moment from "moment";
import {Moment} from "moment";
import {LoaderService} from "../loader/loader.service";

const selectedMonthFormat = "MMMM YYYY";

@Component({
  selector: 'meterstand',
  templateUrl: './meterstand.component.html',
  styleUrls: ['./meterstand.component.scss'],
})
export class MeterstandComponent implements OnInit {
  @ViewChild('monthPicker') monthPicker: DatePickerComponent;

  monthPickerConfig: IDatePickerConfig;
  monthPickerModel: String;
  selectedMonth: Moment;

  sortedMeterstandenPerDag: MeterstandOpDag[] = [];

  constructor(private meterstandService: MeterstandService, private loaderService: LoaderService) {
    this.monthPickerConfig = {
      format: selectedMonthFormat,
      max: this.getStartOfCurrentMonth()
    };
  }

  ngOnInit() {
    this.setSelectedMonth(this.getStartOfCurrentMonth());
  }

  private getStartOfCurrentMonth() {
    return moment().startOf('month');
  }

  private setSelectedMonth(month: Moment) {
    this.selectedMonth = month;
    this.monthPickerModel = month.format(selectedMonthFormat);
  }

  getMeterstanden(): void {
    const from = this.selectedMonth.clone().startOf('month');
    const to = from.clone().add(1, 'month');

    this.loaderService.open();
    this.sortedMeterstandenPerDag = [];

    this.meterstandService.getMeterstanden(from, to).subscribe(
        resp => {
          const unsortedMeterstandenPerDag: MeterstandOpDag[] = {...resp.body};
          this.sortedMeterstandenPerDag = _.sortBy<MeterstandOpDag>(unsortedMeterstandenPerDag, ['dag']);
        },
        error  => console.error(error),
        () => this.loaderService.close()
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
