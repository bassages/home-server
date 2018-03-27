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
import {VerbruikInJaar} from "./verbruikInJaar";

@Injectable()
export class EnergieVerbruikJaarChartService extends EnergieVerbruikBaseChartService implements EnergieVerbruikChartService {

  constructor(private energieVerbruikService: EnergieVerbruikService,
              protected decimalPipe: DecimalPipe) {
    super(decimalPipe);
  }

  public getVerbruik(selectedDate: Moment): Observable<VerbruikInJaar[]> {
    return this.energieVerbruikService.getVerbruikPerJaar();
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
    chartConfiguration.data.keys = { x: 'jaar', value: keysGroups };
    chartConfiguration.data.json = verbruiken;
    chartConfiguration.data.onclick = (data => onDataClick(moment(data.x + '-' + selectedDate.format('MM') + '-' + selectedDate.format('DD'))));
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
        return that.getTooltipContent(this, data, titleFormatter, valueFormatter, color, verbruiksoort, energiesoorten)
      }
    };
    return chartConfiguration;
  }
}
