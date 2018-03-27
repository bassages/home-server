import "rxjs/Rx";
import {Moment} from "moment";
import {ChartConfiguration} from "c3";

export interface EnergieVerbruikChartService {

  getVerbruik(selectedDate: Moment);

  getEmptyChartConfig(): ChartConfiguration;

  getChartConfig(selectedDate: Moment,
                 verbruiksoort: string,
                 energiesoorten: string[],
                 verbruiken: any[],
                 onDataClick: ((date: Moment) => void)): ChartConfiguration;

  getEnergiesoortenAfterToggle(verbruiksoort: string, energiesoorten: string[], energiesoortToToggle: string): string[];
}
