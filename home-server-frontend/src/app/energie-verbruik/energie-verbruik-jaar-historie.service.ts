import {Injectable} from '@angular/core';
import {EnergieVerbruikHistorieService} from './energie-verbruik-historie.service';
import * as moment from 'moment';
import {Moment} from 'moment';
import {EnergieVerbruikService} from './energie-verbruik.service';
import {Observable} from 'rxjs';
import {AbstractEnergieVerbruikHistorieService} from './energie-verbruik-base-chart.service';
import {ChartConfiguration} from 'c3';
import {DecimalPipe} from '@angular/common';
import {VerbruikInJaar} from './verbruikInJaar';

@Injectable()
export class EnergieVerbruikJaarHistorieService extends AbstractEnergieVerbruikHistorieService
                                                implements EnergieVerbruikHistorieService<VerbruikInJaar> {

  constructor(private energieVerbruikService: EnergieVerbruikService,
              protected decimalPipe: DecimalPipe) {
    super(decimalPipe);
  }

  public getVerbruiken(selectedDate: Moment): Observable<VerbruikInJaar[]> {
    return this.energieVerbruikService.getVerbruikPerJaar();
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
    chartConfiguration.data.keys = { x: 'jaar', value: keysGroups };
    chartConfiguration.data.json = verbruiken;
    chartConfiguration.data.onclick = (data => onDataClick(this.toMoment(selectedDate, data.x)));
    chartConfiguration.axis = {
      y: {
        tick: {
          format: (value: number) => super.formatWithoutUnitLabel(verbruiksoort, value)
        }
      }
    };
    chartConfiguration.tooltip = {
      contents: function (data, defaultTitleFormat, valueFormatter, color) {
        const titleFormatter = (year: number) => year;
        return that.getTooltipContent(this, data, titleFormatter, valueFormatter, color, verbruiksoort, energiesoorten);
      }
    };
    return chartConfiguration;
  }

  public getFormattedDate(verbruikInJaar: VerbruikInJaar): string {
    return !(verbruikInJaar.jaar === null || verbruikInJaar.jaar === undefined) ? verbruikInJaar.jaar.toString() : '';
  }

  public getMoment(selectedDate: Moment, verbruikInjaar: VerbruikInJaar): Moment {
    return this.toMoment(selectedDate, verbruikInjaar.jaar);
  }

  // noinspection JSMethodCanBeStatic
  private toMoment(selectedDate: Moment, value: number): Moment {
    return moment(value + '-' + selectedDate.format('MM') + '-' + selectedDate.format('DD'));
  }
}
