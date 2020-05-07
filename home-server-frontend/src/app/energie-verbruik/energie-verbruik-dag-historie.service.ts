import {Injectable} from '@angular/core';
import {EnergieVerbruikHistorieService} from './energie-verbruik-historie.service';
import * as moment from 'moment';
import {Moment} from 'moment';
import {EnergieVerbruikService} from './energie-verbruik.service';
import {Observable} from 'rxjs';
import {VerbruikOpDag} from './verbruikOpDag';
import {AbstractEnergieVerbruikHistorieService} from './energie-verbruik-base-chart.service';
import {ChartConfiguration} from 'c3';
import {DecimalPipe} from '@angular/common';
import capitalize from 'lodash/capitalize';

@Injectable()
export class EnergieVerbruikDagHistorieService extends AbstractEnergieVerbruikHistorieService
                                               implements EnergieVerbruikHistorieService<VerbruikOpDag> {

  constructor(private energieVerbruikService: EnergieVerbruikService,
              protected decimalPipe: DecimalPipe) {
    super(decimalPipe);
  }

  public getVerbruiken(selectedDate: Moment): Observable<VerbruikOpDag[]> {
    const from = selectedDate.clone().date(1);
    const to = from.clone().add(1, 'months');
    return this.energieVerbruikService.getVerbruikPerDag(from, to);
  }

  public getChartConfig(selectedDate: Moment,
                        verbruiksoort: string,
                        energiesoorten: string[],
                        verbruiken: any[],
                        onDataClick: ((date: Moment) => void)): ChartConfiguration {
    const that = this;

    const chartConfiguration = super.getDefaultBarChartConfig();
    const keysGroups = super.getKeysGroups(verbruiksoort, energiesoorten);

    chartConfiguration.data.groups = [keysGroups];
    chartConfiguration.data.keys = { x: 'dag', value: keysGroups };
    chartConfiguration.data.json = verbruiken;
    chartConfiguration.data.onclick = (data => onDataClick(moment(data.x)));
    chartConfiguration.axis = {
      x: {
        type: 'timeseries',
        tick: {
          format: (date: Date) => capitalize(moment(date).format('ddd DD')),
          values: this.getTicksForEveryDayInMonth(selectedDate),
          centered: true,
          multiline: true,
          width: 25
        },
        min: this.getPeriodStart(selectedDate).subtract(12, 'hours').toDate(),
        max: this.getPeriodEnd(selectedDate).subtract(12, 'hours').toDate(),
        padding: { left: 0, right: 0 }
      },
      y: {
        tick: {
          format: (value: number) => super.formatWithoutUnitLabel(verbruiksoort, value)
        }
      }
    };
    chartConfiguration.tooltip = {
      contents: function (data, defaultTitleFormat, defaultValueFormat, color) {
        const titleFormat = (date: any) => that.formatDate(date);
        return that.getTooltipContent(this, data, titleFormat, defaultValueFormat, color, verbruiksoort, energiesoorten);
      }
    };
    return chartConfiguration;
  }

  // noinspection JSMethodCanBeStatic
  private getPeriodStart(selectedDate: Moment): Moment {
    return selectedDate.clone()
                       .date(1);
  }

  // noinspection JSMethodCanBeStatic
  private getPeriodEnd(selectedDate: Moment): Moment {
    return selectedDate.clone()
                       .date(1)
                       .add(1, 'months')
                       .subtract(1, 'milliseconds');
  }

  private getTicksForEveryDayInMonth(selectedMoment: Moment): number[] {
    const date: Moment = this.getPeriodStart(selectedMoment);
    const numberOfDaysInMonth: number = selectedMoment.daysInMonth();
    const tickValues: number[] = [];

    for (let i = 0; i < numberOfDaysInMonth; i++) {
      tickValues.push(date.toDate().getTime());
      date.add(1, 'days');
    }
    return tickValues;
  }

  public getFormattedDate(verbruikOpDag: VerbruikOpDag): string {
    return this.formatDate(verbruikOpDag.dag);
  }

  // noinspection JSMethodCanBeStatic
  private formatDate(date: any): string {
    return capitalize(moment(date).format('ddd DD-MM'));
  }

  public getMoment(selectedDate: Moment, verbruikOpDag: VerbruikOpDag): Moment {
    return moment(verbruikOpDag.dag);
  }
}
