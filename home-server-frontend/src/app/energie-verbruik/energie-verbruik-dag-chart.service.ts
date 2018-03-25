import {Injectable} from '@angular/core';
import "rxjs/Rx";
import {EnergieVerbruikChartService} from "./energie-verbruik-chart.service";
import * as moment from "moment";
import {Moment} from "moment";
import {EnergieVerbruikService} from "./energie-verbruik.service";
import {Observable} from "rxjs/Observable";
import {VerbruikOpDag} from "./verbruikOpDag";
import {EnergieVerbruikBaseChartService} from "./energie-verbruik-base-chart.service";
import {ChartConfiguration} from "c3";
import {DecimalPipe} from "@angular/common";
import * as _ from "lodash";

@Injectable()
export class EnergieVerbruikDagChartService extends EnergieVerbruikBaseChartService implements EnergieVerbruikChartService {

  constructor(private energieVerbruikService: EnergieVerbruikService,
              protected decimalPipe: DecimalPipe) {
    super(decimalPipe);
  }

  public getVerbruik(selectedDate: Moment): Observable<VerbruikOpDag[]> {
    const from = selectedDate.clone().date(1);
    const to = from.clone().add(1, 'months');
    return this.energieVerbruikService.getVerbruikPerDag(from, to);
  }

  public getChartConfig(selectedDate: moment.Moment, verbruiksoort: string, energiesoorten: string[], verbruiken: any[]): ChartConfiguration {
    const that = this;
    const chartConfiguration = super.getDefaultBarChartConfig(verbruiken);
    const keysGroups = super.getKeysGroups(verbruiksoort, energiesoorten);

    chartConfiguration.data.groups = [keysGroups];
    chartConfiguration.data.keys = { x: 'dag', value: keysGroups };
    chartConfiguration.data.json = verbruiken;
    chartConfiguration.axis = {
      x: {
        type: 'timeseries',
        tick: {
          format: (x: Date) => _.capitalize(moment(x).format('ddd DD')),
          values: this.getTicksForEveryDayInMonth(selectedDate), centered: true, multiline: true, width: 25
        },
        min: this.getPeriodStartDate(selectedDate),
        max: this.getPeriodEndDate(selectedDate),
        padding: { left: 0, right: 0 }
      },
      y: {
        tick: {
          format: (x: number) => super.formatWithoutUnitLabel(verbruiksoort, x)
        }
      }
    };
    chartConfiguration.tooltip = {
      contents: function (data, defaultTitleFormat, defaultValueFormat, color) {
        const titleFormat = (x: Date) => _.capitalize(moment(x).format('ddd DD-MM'));
        return that.getTooltipContent(this, data, titleFormat, defaultValueFormat, color, verbruiksoort, energiesoorten)
      }
    };

    return chartConfiguration;
  }

  private getPeriodStartDate(selectedDate: Moment): Date {
    return selectedDate.clone().subtract(12, 'hours').toDate();
  }

  private getPeriodEndDate(selectedDate: Moment): Date {
    return selectedDate.clone().add(1, 'months').subtract(1, 'milliseconds').subtract(12, 'hours').toDate();
  }

  private getTicksForEveryDayInMonth(selectedDate: Moment): number[] {
    const date = selectedDate.clone();
    const numberOfDaysInMonth = selectedDate.daysInMonth();
    const tickValues: number[] = [];

    for (let i = 0; i < numberOfDaysInMonth; i++) {
      tickValues.push(date.toDate().getTime());
      date.add(1, 'days');
    }
    return tickValues;
  }
}
