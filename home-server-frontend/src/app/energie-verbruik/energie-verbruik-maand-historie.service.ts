import {Injectable} from '@angular/core';
import {EnergieVerbruikHistorieService} from './energie-verbruik-historie.service';
import * as moment from 'moment';
import {Moment} from 'moment';
import {EnergieVerbruikService} from './energie-verbruik.service';
import {Observable} from 'rxjs';
import {AbstractEnergieVerbruikHistorieService} from './energie-verbruik-base-chart.service';
import {ChartConfiguration} from 'c3';
import {DecimalPipe} from '@angular/common';
import {VerbruikInMaand} from './verbruikInMaand';

const capitalizedShortMonthNames = ['Jan.', 'Feb.', 'Maa.', 'Apr.', 'Mei', 'Jun.', 'Jul.', 'Aug.', 'Sep.', 'Okt.', 'Nov.', 'Dec.'];
const capitalizedFullMonthNames = ['Januari', 'Februari', 'Maart', 'April', 'Mei', 'Juni',
                                   'Juli', 'Augustus', 'September', 'Oktober', 'November', 'December'];

@Injectable()
export class EnergieVerbruikMaandHistorieService extends AbstractEnergieVerbruikHistorieService
                                                 implements EnergieVerbruikHistorieService<VerbruikInMaand> {

  constructor(private energieVerbruikService: EnergieVerbruikService,
              protected decimalPipe: DecimalPipe) {
    super(decimalPipe);
  }

  public getVerbruiken(selectedDate: Moment): Observable<VerbruikInMaand[]> {
    return this.energieVerbruikService.getVerbruikPerMaandInJaar(selectedDate.year());
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
    chartConfiguration.data.keys = { x: 'maand', value: keysGroups };
    chartConfiguration.data.json = verbruiken;
    chartConfiguration.data.onclick = (data => onDataClick(this.toMoment(selectedDate, data.x)));
    chartConfiguration.axis = {
      x: {
        tick: {
          format: (month: number) => capitalizedShortMonthNames[month - 1],
          values: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12],
          centered: true
        },
      },
      y: {
        tick: {
          format: (value: number) => super.formatWithoutUnitLabel(verbruiksoort, value)
        }
      }
    };
    chartConfiguration.tooltip = {
      contents: function (data, defaultTitleFormat, valueFormatter, color) {
        const titleFormatter = (month: number) => capitalizedFullMonthNames[month - 1];
        return that.getTooltipContent(this, data, titleFormatter, valueFormatter, color, verbruiksoort, energiesoorten);
      }
    };
    return chartConfiguration;
  }

  public getFormattedDate(verbruikInMaand: VerbruikInMaand): string {
    return capitalizedFullMonthNames[verbruikInMaand.maand - 1];
  }

  public getMoment(selectedDate: Moment, verbruikInMaand: VerbruikInMaand): Moment {
    return this.toMoment(selectedDate, verbruikInMaand.maand);
  }

  private toMoment(selectedDate: Moment, value: number): Moment {
    const year = selectedDate.format('YYYY');
    const month = this.decimalPipe.transform(value, '2.0-0');
    const day = selectedDate.format('DD');
    return moment(year + '-' + month + '-' + day);
  }
}
