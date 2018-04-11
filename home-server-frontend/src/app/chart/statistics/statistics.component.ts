import {Component, Input} from '@angular/core';
import {Statistics} from "../../statistics";
import {DecimalPipe} from "@angular/common";
import {isNull, isUndefined} from "util";

@Component({
  selector: 'statistics',
  templateUrl: './statistics.component.html',
  styleUrls: ['./statistics.component.scss']
})
export class StatisticsComponent {

  @Input()
  public statistics: Statistics;

  @Input()
  public decimalFormat: string;

  @Input()
  public valuePrefix: string = '';
  @Input()
  public valuePostfix: string = '';

  @Input()
  public additionalClasses: string = '';

  constructor(private decimalPipe: DecimalPipe) { }

  public format(value: number): string {
    if (isUndefined(value) || isNull(value) || isNaN(value)) {
      return '-'
    }
    return this.valuePrefix + this.decimalPipe.transform(value, this.decimalFormat) + this.valuePostfix;
  }
}
