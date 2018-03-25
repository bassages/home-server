import {Injectable} from '@angular/core';
import "rxjs/Rx";
import {EnergieVerbruikChartService} from "./energie-verbruik-chart.service";
import {Moment} from "moment";
import {EnergieVerbruikService} from "./energie-verbruik.service";
import {VerbruikInUur} from "./verbruikInUur";
import {Observable} from "rxjs/Observable";
import {EnergieVerbruikBaseChartService} from "./energie-verbruik-base-chart.service";
import {ChartConfiguration} from "c3";
import {DecimalPipe} from "@angular/common";

@Injectable()
export class EnergieVerbruikUurChartService extends EnergieVerbruikBaseChartService implements EnergieVerbruikChartService {

  constructor(private energieVerbruikService: EnergieVerbruikService,
              protected decimalPipe: DecimalPipe) {
    super(decimalPipe);
  }

  public getVerbruik(selectedDate: Moment): Observable<VerbruikInUur[]> {
    return this.energieVerbruikService.getVerbruikPerUurOpDag(selectedDate);
  }

  public getChartConfig(selectedDate: Moment, verbruiksoort: string, energiesoorten: string[], verbruiken: any[]): ChartConfiguration {
    const that = this;
    const chartConfiguration: ChartConfiguration = super.getDefaultBarChartConfig(verbruiken);
    const keysGroups = super.getKeysGroups(verbruiksoort, energiesoorten);

    chartConfiguration.data.groups = [keysGroups];
    chartConfiguration.data.keys = { x: 'uur', value: keysGroups };
    chartConfiguration.data.json = verbruiken;
    chartConfiguration.axis = {
      x: {
        type: 'category',
        tick: {
          format: (uur: number) => `${this.decimalPipe.transform(uur, '2.0-0')}:00 - ${this.decimalPipe.transform(uur + 1, '2.0-0')}:00`
        }
      },
      y: {
        tick: {
          format: (x: number) => super.formatWithoutUnitLabel(verbruiksoort, x)
        }
      }
    };
    chartConfiguration.tooltip = {
      contents: function (data, defaultTitleFormat, defaultValueFormat, color) {
        return that.getTooltipContent(this, data, defaultTitleFormat, defaultValueFormat, color, verbruiksoort, energiesoorten)
      }
    };

    return chartConfiguration;
  }

}
