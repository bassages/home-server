import {Injectable} from '@angular/core';
import "rxjs/Rx";
import {EnergieVerbruikChartService} from "./energie-verbruik-chart.service";
import * as moment from "moment";
import {Moment} from "moment";
import {EnergieVerbruikService} from "./energie-verbruik.service";
import {Observable} from "rxjs/Observable";
import {EnergieVerbruikBaseChartService} from "./energie-verbruik-base-chart.service";
import {ChartConfiguration} from "c3";
import {DecimalPipe} from "@angular/common";
import {VerbruikInMaand} from "./verbruikInMaand";

const shortMonthNames = ["Jan.", "Feb.", "Maa.", "Apr.", "Mei.", "Jun.", "Jul.", "Aug.", "Sep.", "Okt.", "Nov.", "Dec."];
const fullMonthNames = ["Januari", "Februari", "Maart", "April", "Mei", "Juni", "Juli", "Augustus", "September", "Oktober", "November", "December"];

@Injectable()
export class EnergieVerbruikMaandChartService extends EnergieVerbruikBaseChartService implements EnergieVerbruikChartService {

  constructor(private energieVerbruikService: EnergieVerbruikService,
              protected decimalPipe: DecimalPipe) {
    super(decimalPipe);
  }

  public getVerbruik(selectedDate: Moment): Observable<VerbruikInMaand[]> {
    return this.energieVerbruikService.getVerbruikPerMaandInJaar(selectedDate.year());
  }

  public getChartConfig(selectedDate: Moment,
                        verbruiksoort: string,
                        energiesoorten: string[],
                        verbruiken: any[],
                        onDataClick: ((date: Moment) => void)): ChartConfiguration {
    const that = this;

    const chartConfiguration = super.getDefaultBarChartConfig(verbruiken);
    const keysGroups = super.getKeysGroups(verbruiksoort, energiesoorten);

    chartConfiguration.data.groups = [keysGroups];
    chartConfiguration.data.keys = { x: 'maand', value: keysGroups };
    chartConfiguration.data.json = verbruiken;
    chartConfiguration.data.onclick = (data => onDataClick(moment(selectedDate.format('YYYY') + '-' + this.decimalPipe.transform(data.x, '2.0-0') + '-' + selectedDate.format('DD'))));
    chartConfiguration.axis = {
      x: {
        tick: {
          format: (month: number) => shortMonthNames[month - 1],
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
        const titleFormatter = (month: number) => fullMonthNames[month - 1];
        return that.getTooltipContent(this, data, titleFormatter, valueFormatter, color, verbruiksoort, energiesoorten)
      }
    };
    return chartConfiguration;
  }
}
