import {ChartConfiguration} from 'c3';
import { Injectable } from '@angular/core';

const defaultChartPadding = { top: 10, bottom: 25, left: 55, right: 20 };

const minimumChartHeight = 220;
const maximumChartHeight = 500;

@Injectable()
export class ChartService {

  // noinspection JSMethodCanBeStatic
  public getEmptyChartConfig(): ChartConfiguration {
    return {
      data: { json: {} },
      legend: { show: false },
      axis: {
        x: { tick: { values: [] } },
        y: { tick: { values: [] } }
      },
      padding: defaultChartPadding
    };
  }

  // noinspection JSMethodCanBeStatic
  public adjustChartHeightToAvailableWindowHeight(chart: any): void {
    const rect = chart.element.getBoundingClientRect();

    let height = window.innerHeight - rect.top - 10;

    if (height < minimumChartHeight) {
      height = minimumChartHeight;
    } else if (height > maximumChartHeight) {
      height = maximumChartHeight;
    }
    chart.resize({height: height});
  }

  // noinspection JSMethodCanBeStatic
  public getDefaultChartPadding() {
    return defaultChartPadding;
  }
}
