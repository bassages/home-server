import {Injectable} from '@angular/core';
import "rxjs/Rx";
import {EnergieVerbruikJaarChartService} from "./energie-verbruik-jaar-chart.service";
import {EnergieVerbruikUurChartService} from "./energie-verbruik-uur-chart.service";
import {EnergieVerbruikDagChartService} from "./energie-verbruik-dag-chart.service";
import {EnergieVerbruikMaandChartService} from "./energie-verbruik-maand-chart.service";
import {EnergieVerbruikChartService} from "./energie-verbruik-chart.service";

@Injectable()
export class EnergieVerbruikChartServiceProvider {

  private periodeToChartServiceMapping: Map<string, EnergieVerbruikChartService>;

  constructor(private energieVerbruikUurChartService: EnergieVerbruikUurChartService,
              private energieVerbruikDagChartService: EnergieVerbruikDagChartService,
              private energieVerbruikMaandChartService: EnergieVerbruikMaandChartService,
              private energieVerbruikJaarChartService: EnergieVerbruikJaarChartService) {

    this.periodeToChartServiceMapping = new Map<string, EnergieVerbruikChartService>([
      ['uur', energieVerbruikUurChartService],
      ['dag', energieVerbruikDagChartService],
      ['maand', energieVerbruikMaandChartService],
      ['jaar', energieVerbruikJaarChartService],
    ]);
  }

  public get(periode: string): EnergieVerbruikChartService {
    return this.periodeToChartServiceMapping.get(periode);
  }
}
