import {Injectable} from '@angular/core';
import {EnergieVerbruikHistorieService} from './energie-verbruik-historie.service';
import {Moment} from 'moment';
import {EnergieVerbruikService} from './energie-verbruik.service';
import {VerbruikInUur} from './verbruikInUur';
import {Observable} from 'rxjs';
import {AbstractEnergieVerbruikHistorieService} from './energie-verbruik-base-chart.service';
import {ChartConfiguration} from 'c3';
import {DecimalPipe} from '@angular/common';

@Injectable()
export class EnergieVerbruikUurHistorieService extends AbstractEnergieVerbruikHistorieService
                                               implements EnergieVerbruikHistorieService<VerbruikInUur> {

  constructor(private energieVerbruikService: EnergieVerbruikService,
              protected decimalPipe: DecimalPipe) {
    super(decimalPipe);
  }

  public getVerbruiken(selectedDate: Moment): Observable<VerbruikInUur[]> {
    return this.energieVerbruikService.getVerbruikPerUurOpDag(selectedDate);
  }

  public getChartConfig(selectedDate: Moment,
                        verbruiksoort: string,
                        energiesoorten: string[],
                        verbruiken: any[],
                        onDataClick: ((date: Moment) => void)): ChartConfiguration {
    const that = this;

    const chartConfiguration: ChartConfiguration = super.getDefaultBarChartConfig();
    const keysGroups = super.getKeysGroups(verbruiksoort, energiesoorten);

    chartConfiguration.data.groups = [keysGroups];
    chartConfiguration.data.keys = { x: 'uur', value: keysGroups };
    chartConfiguration.data.json = verbruiken;
    chartConfiguration.data.onclick = (data => onDataClick(selectedDate));
    chartConfiguration.axis = {
      x: {
        type: 'category',
        tick: {
          format: (uur: number) => this.formatUur(uur)
        }
      },
      y: {
        tick: {
          format: (value: number) => super.formatWithoutUnitLabel(verbruiksoort, value)
        },
      }
    };
    chartConfiguration.tooltip = {
      contents: function (data, titleFormatter, valueFormatter, color) {
        return that.getTooltipContent(this, data, titleFormatter, valueFormatter, color, verbruiksoort, energiesoorten);
      }
    };
    return chartConfiguration;
  }

  public getFormattedDate(verbruikInUur: VerbruikInUur): string {
    return this.formatUur(verbruikInUur.uur);
  }

  private formatUur(uur: number): string {
    return `${this.decimalPipe.transform(uur, '2.0-0')}:00 - ${this.decimalPipe.transform(uur + 1, '2.0-0')}:00`;
  }

  public getMoment(selectedDate: Moment, verbruikInUur: VerbruikInUur): Moment {
    return selectedDate.clone();
  }
}
