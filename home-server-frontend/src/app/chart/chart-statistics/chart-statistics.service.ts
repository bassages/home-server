import "rxjs/Rx";
import {LineOptions} from "c3";
import * as _ from "lodash";
import {Statistics} from "../../statistics";

export class ChartStatisticsService {

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
