import {Moment} from 'moment';
import {ChartConfiguration} from 'c3';
import {Observable} from 'rxjs';

export interface EnergieVerbruikHistorieService<T> {

  getVerbruiken(selectedDate: Moment): Observable<T[]>;

  getEmptyChartConfig(): ChartConfiguration;

  getChartConfig(selectedDate: Moment,
                 verbruiksoort: string,
                 energiesoorten: string[],
                 verbruiken: T[],
                 onDataClick: ((date: Moment) => void)): ChartConfiguration;

  toggleEnergiesoort(verbruiksoort: string, energiesoorten: string[], energiesoortToToggle: string): string[];

  getFormattedDate(verbruik: T): string;

  formatWithUnitLabel(verbruiksoort: string, energieSoorten: string[], value: number);

  adjustChartHeightToAvailableWindowHeight(chart: any): void;

  getMoment(selectedDate: Moment, T): Moment;
}
