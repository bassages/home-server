import {Component, OnInit} from '@angular/core';
import {MeterstandService} from "./meterstand.service";
import {MeterstandOpDag} from "./meterstandOpDag";
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

  selectedMonth: Moment;

  sortedMeterstandenPerDag: MeterstandOpDag[] = [];

  constructor(private meterstandService: MeterstandService,
              private loadingIndicatorService: LoadingIndicatorService,
              private errorHandlingService: ErrorHandingService) {
  }

  public ngOnInit(): void {
    this.selectedMonth = MeterstandComponent.getStartOfCurrentMonth();
    setTimeout(() => { this.getMeterstanden(); },0);
  }

  private static getStartOfCurrentMonth(): Moment {
    return moment().startOf('month');
  }

  private getMeterstanden(): void {
    const from = this.selectedMonth.clone().startOf('month');
    const to = from.clone().add(1, 'month');

    this.loadingIndicatorService.open();

    this.meterstandService.getMeterstanden(from, to).subscribe(
      response => this.sortedMeterstandenPerDag = _.sortBy<MeterstandOpDag>(response, ['dag']),
      error => this.errorHandlingService.handleError("De meterstanden konden nu niet worden opgehaald", error),
      () => this.loadingIndicatorService.close()
    );
  }

  public onMonthNavigate(date: Moment): void {
    this.selectedMonth = date;
    this.getMeterstanden();
  }
}

