import "rxjs/Rx";
import {ChartConfiguration, LineOptions} from "c3";
import {Statistics} from "./statistics";
import * as _ from "lodash";

const defaultChartPadding = { top: 10, bottom: 25, left: 55, right: 20 };

const minimumChartHeight: number = 220;
const maximumChartHeight: number = 500;

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
  public adjustChartHeightToAvailableWindowHeight(chart: any) {
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

  public createStatisticsChartLines(statistics: Statistics): LineOptions[] {
    return _.filter( [
      this.createStatisticChartLine(statistics.avg, 'avg'),
      this.createStatisticChartLine(statistics.min, 'min'),
      this.createStatisticChartLine(statistics.max, 'max')
    ], _.isObject);
  }

  // noinspection JSMethodCanBeStatic
  private createStatisticChartLine(value: number, clazz: string): LineOptions {
    if (value) {
      return { value: value, class: clazz };
    }
  }

}
