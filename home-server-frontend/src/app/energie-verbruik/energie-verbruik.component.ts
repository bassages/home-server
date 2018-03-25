import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, ParamMap, Router} from "@angular/router";
import * as c3 from 'c3';
import {ChartAPI, ChartConfiguration} from 'c3';
import * as moment from "moment";
import {Moment} from "moment";
import * as _ from "lodash";
import {EnergieVerbruikService} from "./energie-verbruik.service";
import {ErrorHandingService} from "../error-handling/error-handing.service";
import {LoadingIndicatorService} from "../loading-indicator/loading-indicator.service";
import {DecimalPipe} from "@angular/common";
import {Observable} from "rxjs/Observable";
import {EnergieVerbruikChartService} from "./energie-verbruik-chart.service";
import {EnergieVerbruikUurChartService} from "./energie-verbruik-uur-chart.service";
import {EnergieVerbruikDagChartService} from "./energie-verbruik-dag-chart.service";

@Component({
  selector: 'energie-verbruik',
  templateUrl: './energie-verbruik.component.html',
  styleUrls: ['./energie-verbruik.component.scss']
})
export class EnergieVerbruikComponent implements OnInit {

  public dateNavigatorMode: string;
  public selectedDate: Moment = moment();
  public verbruiksoort: string = '';
  public energiesoorten: string[] = [];
  public periode: string = '';

  private chart: ChartAPI;

  private energieVerbruikChartService: EnergieVerbruikChartService;

  constructor(private energieVerbruikService: EnergieVerbruikService,
              private energieVerbruikUurChartService: EnergieVerbruikUurChartService,
              private energieVerbruikDagChartService: EnergieVerbruikDagChartService,
              private loadingIndicatorService: LoadingIndicatorService,
              private errorHandlingService: ErrorHandingService,
              private decimalPipe: DecimalPipe,
              private activatedRoute: ActivatedRoute,
              private router: Router) { }

  ngOnInit() {
    Observable.combineLatest([
                this.activatedRoute.paramMap,
                this.activatedRoute.queryParamMap
              ])
              .subscribe(combined => {
                const params: ParamMap = <ParamMap>combined[0];
                const queryParams: ParamMap = <ParamMap>combined[1];

                const verbruiksoortParam = params.get('verbruiksoort');
                const periodeParam = params.get('periode');
                const energiesoortenParam = queryParams.getAll('energiesoort');

                if (!queryParams.has('datum')) {
                  console.info('navigate because of missing datum parameter');
                  this.navigateTo(verbruiksoortParam, energiesoortenParam, periodeParam, moment());
                  return;
                }
                const selectedDayParam = moment(queryParams.get('datum'), "DD-MM-YYYY");

                if (_.isEqual(this.energiesoorten, energiesoortenParam) && this.verbruiksoort == verbruiksoortParam
                           && this.selectedDate.isSame(selectedDayParam) && this.periode == periodeParam) {
                  console.debug('parameters did not change');
                  return;
                }

                if (verbruiksoortParam == 'verbruik' && energiesoortenParam.length > 1) {
                  this.navigateTo(verbruiksoortParam, ['stroom'], periodeParam, selectedDayParam);
                  return;
                }

                this.verbruiksoort = verbruiksoortParam;
                this.energiesoorten = energiesoortenParam;
                this.periode = periodeParam;
                this.selectedDate = selectedDayParam;

                if (this.periode == 'uur') {
                  this.dateNavigatorMode = 'day';
                  this.energieVerbruikChartService = this.energieVerbruikUurChartService;
                } else if (this.periode == 'dag') {
                  this.dateNavigatorMode = 'month';
                  this.energieVerbruikChartService = this.energieVerbruikDagChartService;
                } else if (this.periode == 'maand') {
                  this.dateNavigatorMode = 'year';
                } else {
                  this.dateNavigatorMode = 'off';
                }

                console.info('verbruiksoort=[' + this.verbruiksoort + '], ' +
                             'energiesoorten=[' + this.energiesoorten + '], ' +
                             'periode=[' + this.periode + '], ' +
                             'selectedDate=[' + this.selectedDate.format('DD-MM-YYYY') + '], ' +
                             'dateNavigatorMode=' + this.dateNavigatorMode + ']'
                );

                setTimeout(() => { this.getAndLoadData(); },0);
              });
  }

  private getAndLoadData() {
    if (this.energiesoorten.length === 0) {
      this.loadChartConfiguration(this.energieVerbruikChartService.getEmptyChartConfig());
      return;
    }

    this.loadingIndicatorService.open();

    this.energieVerbruikChartService.getVerbruik(this.selectedDate).subscribe(
      verbruiken => this.loadDataIntoChart(verbruiken),
      error => this.errorHandlingService.handleError("Het verbruik kon niet worden opgehaald", error),
      () => this.loadingIndicatorService.close()
    );
  }

  private loadDataIntoChart(verbruiken: any[]) {
    const chartConfiguration: ChartConfiguration = this.energieVerbruikChartService.getChartConfig(this.selectedDate, this.verbruiksoort, this.energiesoorten, verbruiken);
    this.loadChartConfiguration(chartConfiguration);
  }

  private loadChartConfiguration(chartConfiguration: ChartConfiguration) {
    this.chart = c3.generate(chartConfiguration);
    this.chart.resize({height: 500});
  }

  public toggleEnergiesoort(energiesoortToToggle) {
    const energiesoortenAfterToggle = this.energieVerbruikChartService.getEnergiesoortenAfterToggle(this.verbruiksoort, this.energiesoorten, energiesoortToToggle);
    this.navigateTo(this.verbruiksoort, energiesoortenAfterToggle, this.periode, this.selectedDate);
  }

  private navigateTo(verbruiksoort: string, energiesoorten: string[], periode: string, datum: Moment) {
    const commands = ['/energie', verbruiksoort, periode];
    const extras = { queryParams: { 'energiesoort': energiesoorten, 'datum': datum.format('DD-MM-YYYY') }, replaceUrl: true };
    this.router.navigate(commands, extras);
  }

  public setPeriode(periode: string) {
    let correctedDatum: Moment = this.selectedDate;
    if (periode == 'dag') {
      correctedDatum.date(1);
    }
    this.navigateTo(this.verbruiksoort, this.energiesoorten, periode, correctedDatum);
  }

  public onDateNavigate(selectedDate: Moment) {
    this.navigateTo(this.verbruiksoort, this.energiesoorten, this.periode, selectedDate);
  }
}
