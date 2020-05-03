import {LineOptions} from 'c3';
import {Statistics} from '../../statistics';
import { Injectable } from '@angular/core';

@Injectable()
export class ChartStatisticsService {

  public createStatisticsChartLines(statistics: Statistics): LineOptions[] {
    if (!statistics) {
      return [];
    }

    const lineOptions: LineOptions[] = [];

    const avg = this.createStatisticChartLine(statistics.avg, 'avg');
    if (avg != null) {
      lineOptions.push(avg);
    }
    const min = this.createStatisticChartLine(statistics.min, 'min');
    if (min != null) {
      lineOptions.push(min);
    }
    const max = this.createStatisticChartLine(statistics.max, 'max');
    if (max != null) {
      lineOptions.push(max);
    }
    return lineOptions;
  }

  // noinspection JSMethodCanBeStatic
  private createStatisticChartLine(value: number, clazz: string): LineOptions {
    if (value) {
      return { value: value, class: clazz };
    }
    return null;
  }
}
