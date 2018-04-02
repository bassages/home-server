import {Component, HostListener, OnInit} from '@angular/core';
import * as moment from "moment";
import {Moment} from "moment";
import {OpgenomenVermogenService} from "./opgenomenVermogen.service";
import {ActivatedRoute, Router} from "@angular/router";
import * as c3 from "c3";
import {ChartAPI, ChartConfiguration} from "c3";
import {LoadingIndicatorService} from "../loading-indicator/loading-indicator.service";
import {ErrorHandingService} from "../error-handling/error-handing.service";
import {OpgenomenVermogen} from "./opgenomenVermogen";
import {ChartService} from "../chart.service";

const periodLengthInMilliseconds = moment.duration(1, 'minutes').asSeconds();

@Component({
  selector: 'app-opgenomen-vermogen',
  templateUrl: './opgenomen-vermogen.component.html',
  styleUrls: ['./opgenomen-vermogen.component.scss']
})
export class OpgenomenVermogenComponent implements OnInit {
  @HostListener('window:resize') onResize() {
    this.chartService.adjustChartHeightToAvailableWindowHeight(this.chart);
  }

  public selectedDate: Moment;

  private chart: ChartAPI;

  constructor(private opgenomenVermogenService: OpgenomenVermogenService,
              private chartService: ChartService,
              private loadingIndicatorService: LoadingIndicatorService,
              private errorHandlingService: ErrorHandingService,
              private router: Router,
              private activatedRoute: ActivatedRoute) { }

  ngOnInit() {
    this.activatedRoute.queryParamMap.subscribe((queryParams) => {
      if (!queryParams.has('datum')) {
        return this.navigateTo(moment());
      }

      this.selectedDate = moment(queryParams.get('datum'), "DD-MM-YYYY");

      setTimeout(() => { this.getAndLoadData(); },0);
    });
  }

  private navigateTo(date: Moment) {
    const commands = ['/energie/opgenomen-vermogen'];
    const extras = {queryParams: {'datum': date.format('DD-MM-YYYY')}, replaceUrl: true};
    this.router.navigate(commands, extras);
  }

  private getAndLoadData() {
    this.loadingIndicatorService.open();

    this.opgenomenVermogenService.getHistory(this.selectedDate, this.selectedDate.clone().add(1, 'days'), periodLengthInMilliseconds).subscribe(
      opgenomenVermogens => this.loadDataIntoChart(opgenomenVermogens),
      error => this.errorHandlingService.handleError("Opgenomen vermogen kon niet worden opgehaald", error),
      () => this.loadingIndicatorService.close()
    );
  }

  public onDateNavigate(selectedDate: Moment) {
    this.navigateTo(selectedDate);
  }

  private loadDataIntoChart(data) {
    let chartData = OpgenomenVermogenComponent.transformData(data);
    this.chart = c3.generate(this.getChartConfiguration(chartData));
    this.chartService.adjustChartHeightToAvailableWindowHeight(this.chart);
  }

  private static transformData(opgenomenVermogens: OpgenomenVermogen[]) {
    let transformedData = [];

    let previousTarief = null;
    for (let i = 0; i < opgenomenVermogens.length; i++) {
      let transformedDataItem: any = {};

      let tarief = opgenomenVermogens[i].tariefIndicator.toLowerCase();
      transformedDataItem.datumtijd = new Date(opgenomenVermogens[i].datumtijd).getTime();
      transformedDataItem['watt-' + tarief] = opgenomenVermogens[i].watt;

      // Fill the "gap" between this row and the previous one
      if (previousTarief && tarief && tarief !== previousTarief) {
        const obj: any = {};
        obj.datumtijd = new Date(opgenomenVermogens[i].datumtijd).getTime() - 1;
        const attribute = 'watt-' + previousTarief;
        obj[attribute] = opgenomenVermogens[i].watt;
        transformedData.push(obj);
      }

      previousTarief = tarief;
      transformedData.push(transformedDataItem);
    }
    return transformedData;
  }

  private getChartConfiguration(chartData): ChartConfiguration {
    if (chartData.length === 0) {
      return this.chartService.getEmptyChartConfig();
    }

    const tickValues = OpgenomenVermogenComponent.getTicksForEveryHourInPeriod(this.selectedDate, this.getTo());

    return {
      bindto: '#chart',
      data: {
        json: chartData,
        keys: {
          x: 'datumtijd',
          value: ['watt-dal', 'watt-normaal']
        },
        types: {'watt-dal': 'area', 'watt-normaal': 'area'}
      },
      axis: {
        x: {
          type: "timeseries",
          tick: { format: "%H:%M", values: tickValues, rotate: -45 },
          min: this.selectedDate.toDate(), max: this.getTo().toDate(),
          padding: {left: 0, right: 10}
        }
      },
      legend: { show: false },
      point: { show: false },
      transition: { duration: 0 },
      tooltip: { show: false },
      padding: this.chartService.getDefaultChartPadding(),
      grid: {
        y: {
          show: true
        }
      }
    }
  }

  private static getTicksForEveryHourInPeriod(from: Moment, to: Moment) {
    const numberOfHoursInDay = ((to.toDate().getTime() - from.toDate().getTime()) / 1000) / 60 / 60;

    let tickValues: number[] = [];
    for (let i = 0; i <= numberOfHoursInDay; i++) {
      let tickValue = from.toDate().getTime() + (i * 60 * 60 * 1000);
      tickValues.push(tickValue);
    }
    return tickValues;
  }

  private getTo() {
    return this.selectedDate.clone().add(1, 'days');
  }
}
