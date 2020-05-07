import {Component, HostListener, OnInit} from '@angular/core';
import {KlimaatSensor} from '../klimaatSensor';
import {KlimaatService} from '../klimaat.service';
import {LoadingIndicatorService} from '../../loading-indicator/loading-indicator.service';
import {ErrorHandingService} from '../../error-handling/error-handing.service';
import sortBy from 'lodash/sortBy';
import map from 'lodash/map';
import uniq from 'lodash/uniq';
import mean from 'lodash/mean';
import min from 'lodash/min';
import max from 'lodash/max';
import filter from 'lodash/filter';
import {ActivatedRoute, Router} from '@angular/router';
import * as moment from 'moment';
import {Moment} from 'moment';
import * as c3 from 'c3';
import {ChartAPI, ChartConfiguration} from 'c3';
import {ChartService} from '../../chart/chart.service';
import {Klimaat} from '../klimaat';
import {DecimalPipe} from '@angular/common';
import {Statistics} from '../../statistics';
import {ChartStatisticsService} from '../../chart/statistics/chart-statistics.service';
import * as chroma from 'chroma-js';
import {KlimaatSensorService} from '../klimaatsensor.service';

@Component({
  selector: 'home-klimaat-historie',
  templateUrl: './klimaat-historie.component.html',
  styleUrls: ['./klimaat-historie.component.scss']
})
export class KlimaatHistorieComponent implements OnInit {

  public sensorCode: string;
  public sensorType: string;
  public date: Moment;

  public sensors: KlimaatSensor[];

  private sortedUniqueValues: number[];
  private colorScale: string[];

  public showTable = false;
  public showChart = false;
  public klimaats: Klimaat[] = [];
  public statistics: Statistics;
  private chart: ChartAPI;

  constructor(private klimaatService: KlimaatService,
              private klimaatSensorService: KlimaatSensorService,
              private chartService: ChartService,
              private chartStatisticsService: ChartStatisticsService,
              private loadingIndicatorService: LoadingIndicatorService,
              private errorHandlingService: ErrorHandingService,
              private activatedRoute: ActivatedRoute,
              private router: Router,
              private decimalPipe: DecimalPipe) {
  }

  @HostListener('window:resize') onResize() {
    this.determineChartOrTable();
    if (this.showChart) {
      this.chartService.adjustChartHeightToAvailableWindowHeight(this.chart);
    }
  }

  public ngOnInit(): void {
    this.activatedRoute.queryParamMap.subscribe(queryParams => {
      const sensorCodeParam = queryParams.get('sensorCode');
      const sensorTypeParam = queryParams.get('sensorType');

      if (queryParams.has('datum')) {
        this.date = moment(queryParams.get('datum'), 'DD-MM-YYYY');
      } else {
        return this.navigateTo(sensorCodeParam, sensorTypeParam, moment());
      }

      if (!queryParams.has('sensorType')) {
        return this.navigateTo(sensorCodeParam, 'temperatuur', this.date);
      }

      this.sensorCode = sensorCodeParam;
      this.sensorType = sensorTypeParam;

      this.determineChartOrTable();

      setTimeout(() => this.getKlimaatSensors());
    });
  }

  private getKlimaatSensors(): void {
    this.loadingIndicatorService.open();

    this.klimaatSensorService.list().subscribe(
      response => {
          this.sensors = sortBy<KlimaatSensor>(response, ['omschrijving']);

          if (!this.sensorCode && this.sensors.length > 0) {
            return this.navigateTo(this.sensors[0].code, this.sensorType, this.date);
          }

          if (this.sensorCode && this.sensorType && this.date) {
            this.getAndLoadData();
          } else {
            this.loadData([]);
            this.loadingIndicatorService.close();
          }
      },
      error => this.errorHandlingService.handleError('De klimaat sensors konden nu niet worden opgehaald', error),
    );
  }

  private loadData(klimaats: Klimaat[]) {
    this.klimaats = klimaats;
    this.statistics = this.getStatistics(klimaats);
    if (this.showChart) {
      this.loadDataIntoChart(this.klimaats);
    } else if (this.showTable) {
      this.loadDataIntoTable(this.klimaats);
    }
  }

  public onDateNavigate(selectedDate: Moment): void {
    this.navigateTo(this.sensorCode, this.sensorType, selectedDate);
  }

  public setSensorType(sensorType: string): void {
    this.navigateTo(this.sensorCode, sensorType, this.date);
  }

  public determineChartOrTable(): void {
    const autoChartOrTableThreshold = 500;
    if (window.innerWidth >= autoChartOrTableThreshold) {
      this.doShowChart();
    } else {
      this.doShowTable();
    }
  }

  private doShowChart(): void {
    if (!this.showChart) {
      this.showTable = false;
      this.showChart = true;
      this.loadDataIntoChart(this.klimaats);
    }
  }

  private doShowTable(): void {
    if (!this.showTable) {
      this.showChart = false;
      this.showTable = true;
      this.loadDataIntoTable(this.klimaats);
    }
  }

  private navigateTo(sensorCode: string, sensorType: string, datum: Moment): void {
    const commands = ['/klimaat/historie'];
    const extras = { queryParams: { sensorCode: sensorCode, sensorType: sensorType, datum: datum.format('DD-MM-YYYY') }, replaceUrl: true };
    this.router.navigate(commands, extras);
  }

  private loadDataIntoChart(klimaat: Klimaat[]): void {
    const chartConfiguration: ChartConfiguration = this.getChartConfig(klimaat);
    this.chart = c3.generate(chartConfiguration);
    this.chartService.adjustChartHeightToAvailableWindowHeight(this.chart);
  }

  private loadDataIntoTable(klimaat: Klimaat[]) {
    this.sortedUniqueValues = sortBy(uniq(map(this.klimaats, this.sensorType)));
    this.colorScale = chroma.scale(['#3e7fcd', 'white', '#e83b26'])
                            .mode('lch')
                            .colors(this.sortedUniqueValues.length);
  }

  private getAndLoadData(): void {
    this.loadingIndicatorService.open();
    this.klimaatService.getKlimaat(this.sensorCode, this.date, this.date.clone().add(1, 'days')).subscribe(
      klimaat => this.loadData(klimaat),
      error => this.errorHandlingService.handleError('Klimaat historie kon niet worden opgehaald', error),
      () => this.loadingIndicatorService.close()
    );
  }

  public sensorChanged(): void {
    this.navigateTo(this.sensorCode, this.sensorType, this.date);
  }

  private getChartConfig(klimaat: Klimaat[]): ChartConfiguration {
    if (klimaat.length === 0) {
      return this.chartService.getEmptyChartConfig();
    }

    const that = this;

    const tickValues = this.getTicksForEveryHourInDay();

    const values: any = [];
    // for (var i = 0; i < vm.selection.length; i++) {
      values.push(this.date.format('DD-MM-YYYY'));
      // values.push(d3.time.format('%d-%m-%Y')(vm.selection[i]));
    // }

    return {
      bindto: '#chart',
      data: {
        type: 'spline',
        json: this.transformServerdata([{data: klimaat}]),
        keys: {
          x: 'datumtijd',
          value: values
        }
      },
      line: { connectNull: true },
      axis: {
        x: {
          type: 'timeseries',
          tick: { format: '%H:%M', values: tickValues, rotate: -45 },
          min: this.getFixedDate(), max: this.getTo(this.getFixedDate()),
          padding: { left: 0, right: 10 }
        },
        y: {
          tick: {
            format: (value: number) => this.formatWithoutUnitLabel(this.sensorType, value)
          }
        }
      },
      legend: { show: false },
      bar: {
        width: { ratio: 1 }
      },
      transition: { duration: 0 },
      padding: this.chartService.getDefaultChartPadding(),
      grid: {
        y: {
          show: true,
          lines: this.chartStatisticsService.createStatisticsChartLines(this.statistics)
        }
      },
      tooltip: {
        format: {
          name: (name: string, ratio: number, id: string, index: number) => moment(name, 'DD-MM-YYYY').format('DD-MM-YYYY'),
          value: (value: number) => this.formatWithUnitLabel(that.sensorType, value)
        }
      }
    };
  }

  // noinspection JSMethodCanBeStatic
  private getTo(from: Date): Date {
    return moment(from).add(1, 'days').toDate();
  }

  private getTicksForEveryHourInDay() {
    const from: Date = this.getFixedDate();
    const to: Date = this.getTo(from);

    const numberOfHoursInDay: number = ((to.getTime() - from.getTime()) / 1000) / 60 / 60;

    const tickValues = [];
    for (let i = 0; i <= numberOfHoursInDay; i++) {
      const tickValue = from.getTime() + (i * 60 * 60 * 1000);
      tickValues.push(tickValue);
    }
    return tickValues;
  }

  private getFixedDate(): Date {
    return this.date.toDate();
    // return new Date('01-01-2016');
  }

  public formatWithoutUnitLabel(sensorType: string, value: number): string {
    return this.decimalPipe.transform(value, this.getDecimalFormat(this.sensorType));
  }

  public formatWithUnitLabel(sensorType: string, value: number): string {
    return this.formatWithoutUnitLabel(sensorType, value) + this.getValuePostFix(sensorType);
  }

  public getDecimalFormat(sensorType: string) {
    return this.klimaatService.getDecimalFormat(sensorType);
  }

  public getValuePostFix(sensorType: string) {
    return this.klimaatService.getValuePostFix(this.sensorType);
  }

  private transformServerdata(serverresponses) {
    const result = [];

    for (let i = 0; i < serverresponses.length; i++) {
      const serverresponse = serverresponses[i]; // Values on a specific date

      for (let j = 0; j < serverresponse.data.length; j++) {
        const datumtijd = serverresponse.data[j].dateTime;

        const datumtijdKey = datumtijd.format('DD-MM-YYYY');
        const datumtijdValue = serverresponse.data[j][this.sensorType];

        const date: Date = datumtijd.clone().toDate();
        date.setDate(this.getFixedDate().getDate());
        date.setMonth(this.getFixedDate().getMonth());
        date.setFullYear(this.getFixedDate().getFullYear());
        const row = this.getOrCreateCombinedRow(result, date);

        row[datumtijdKey] = datumtijdValue;
      }
    }
    return result;
  }

  // noinspection JSMethodCanBeStatic
  private getOrCreateCombinedRow(currentRows, datumtijd) {
    let row = null;

    for (let i = 0; i < currentRows.length; i++) {
      if (currentRows[i].datumtijd.getTime() === datumtijd.getTime()) {
        row = currentRows[i];
        break;
      }
    }
    if (row === null) {
      row = {};
      row.datumtijd = datumtijd;
      currentRows.push(row);
    }
    return row;
  }

  private getStatistics(klimaats: Klimaat[]): Statistics {
    const values: number[] = filter(map(klimaats, this.sensorType), (value: number) => value !== null && value > 0);
    return new Statistics(min(values), max(values), mean(values));
  }

  // noinspection JSMethodCanBeStatic
  public getFormattedTime(klimaat: Klimaat) {
    return klimaat.dateTime.format('HH:mm');
  }

  // noinspection JSMethodCanBeStatic
  public getColor(value: number) {
    const indexOfValue = this.sortedUniqueValues.indexOf(value);
    return this.colorScale[indexOfValue];
  }
}
