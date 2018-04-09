import {Component, Input} from '@angular/core';
import {Statistics} from "../../statistics";
import {DecimalPipe} from "@angular/common";
import {isNull, isUndefined} from "util";

@Component({
  selector: 'chart-statistics',
  templateUrl: './chart-statistics.component.html',
  styleUrls: ['./chart-statistics.component.scss']
})
export class ChartStatisticsComponent {

  @Input()
  public statistics: Statistics;

  @Input()
  public decimalFormat: string;

  @Input()
  public valuePrefix: string = '';
  @Input()
  public valuePostfix: string = '';

  constructor(private decimalPipe: DecimalPipe) { }

  public format(value: number): string {
    if (isUndefined(value) || isNull(value) || isNaN(value)) {
      return '-'
    }
    return this.valuePrefix + this.decimalPipe.transform(value, this.decimalFormat) + this.valuePostfix;
  }
}
