import {Component, Input} from '@angular/core';
import {Statistics} from '../../statistics';
import {DecimalPipe} from '@angular/common';

@Component({
  selector: 'home-statistics',
  templateUrl: './statistics.component.html',
  styleUrls: ['./statistics.component.scss']
})
export class StatisticsComponent {

  @Input()
  public statistics: Statistics;

  @Input()
  public decimalFormat: string;

  @Input()
  public valuePrefix = '';
  @Input()
  public valuePostfix = '';

  @Input()
  public additionalClasses = '';

  constructor(private decimalPipe: DecimalPipe) { }

  public format(value: number): string {
    if (value === undefined || value === null || isNaN(value)) {
      return '-';
    }
    return this.valuePrefix + this.decimalPipe.transform(value, this.decimalFormat) + this.valuePostfix;
  }
}
