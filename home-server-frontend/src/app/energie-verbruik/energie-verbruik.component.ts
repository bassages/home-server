import {Component, HostListener, OnInit} from '@angular/core';
import {ActivatedRoute, ParamMap, Router} from "@angular/router";
import * as c3 from 'c3';
import {ChartAPI, ChartConfiguration} from 'c3';
import * as moment from "moment";
import {Moment} from "moment";
import * as _ from "lodash";
import {ErrorHandingService} from "../error-handling/error-handing.service";
import {LoadingIndicatorService} from "../loading-indicator/loading-indicator.service";
import {DecimalPipe} from "@angular/common";
import {Observable} from "rxjs/Observable";
import {EnergieVerbruikHistorieService} from "./energie-verbruik-historie.service";
import {EnergieVerbruikHistorieServiceProvider} from "./energie-verbruik-historie-service-provider";

const periodeToDateNavigatorModeMapping: Map<string, string> =
  new Map<string, string>([
    ['uur', 'day'],
    ['dag', 'month'],
    ['maand', 'year'],
    ['jaar', 'off'],
  ]);

@Component({
  selector: 'energie-verbruik',
  templateUrl: './energie-verbruik.component.html',
  styleUrls: ['./energie-verbruik.component.scss']
})
export class EnergieVerbruikComponent implements OnInit {
  @HostListener('window:resize') onResize() {
    this.determineChartOrTable();
    if (this.showChart) {
      this.energieVerbruikHistorieService.adjustChartHeightToAvailableWindowHeight(this.chart);
    }
  }

  public showChart: boolean = false;
  public showTable: boolean = false;

  public dateNavigatorMode: string;
  public selectedDate: Moment = moment();
  public verbruiksoort: string = '';
  public energiesoorten: string[] = [];
  public periode: string = '';

  private chart: ChartAPI;
  public verbruiken: any[] = [];

  private energieVerbruikHistorieService: EnergieVerbruikHistorieService<any>;

  constructor(private energieVerbruikChartServiceProvider: EnergieVerbruikHistorieServiceProvider,
              private loadingIndicatorService: LoadingIndicatorService,
              private errorHandlingService: ErrorHandingService,
              private decimalPipe: DecimalPipe,
              private activatedRoute: ActivatedRoute,
              private router: Router) { }

  ngOnInit() {
    Observable.combineLatest([ this.activatedRoute.paramMap, this.activatedRoute.queryParamMap ])
              .subscribe(combined => {
                const params: ParamMap = <ParamMap>combined[0];
                const queryParams: ParamMap = <ParamMap>combined[1];

                const verbruiksoortParam = params.get('verbruiksoort');
                const periodeParam = params.get('periode');
                const energiesoortenParam = queryParams.getAll('energiesoort');

                if (!queryParams.has('datum')) {
                  return this.navigateTo(verbruiksoortParam, energiesoortenParam, periodeParam, moment());
                }
                const selectedDayParam = moment(queryParams.get('datum'), "DD-MM-YYYY");

                if (_.isEqual(this.energiesoorten, energiesoortenParam) && this.verbruiksoort == verbruiksoortParam
                           && this.selectedDate.isSame(selectedDayParam) && this.periode == periodeParam) {
                  return;
                }

                if (verbruiksoortParam == 'verbruik' && energiesoortenParam.length > 1) {
                  return this.navigateTo(verbruiksoortParam, ['stroom'], periodeParam, selectedDayParam);
                }

                this.verbruiksoort = verbruiksoortParam;
                this.energiesoorten = energiesoortenParam;
                this.periode = periodeParam;
                this.selectedDate = selectedDayParam;
                this.dateNavigatorMode = periodeToDateNavigatorModeMapping.get(this.periode);
                this.energieVerbruikHistorieService = this.energieVerbruikChartServiceProvider.get(this.periode);

                this.determineChartOrTable();

                setTimeout(() => { this.getAndLoadData(); },0);
              });
  }

  private getAndLoadData() {
    this.verbruiken = [];

    this.loadingIndicatorService.open();

    this.energieVerbruikHistorieService.getVerbruiken(this.selectedDate).subscribe(
      verbruiken => this.loadData(verbruiken),
      error => this.errorHandlingService.handleError("Het verbruik kon niet worden opgehaald", error),
      () => this.loadingIndicatorService.close()
    );
  }

  private loadData(verbruiken: any[]) {
    this.verbruiken = verbruiken;
    if (this.showChart) {
      this.loadDataIntoChart();
    } else if (this.showTable) {
      this.loadDataIntoTable();
    }
  }

  private loadDataIntoTable() {
    // Nothing special to do here
  }

  private loadDataIntoChart(): void {
    const chartConfiguration: ChartConfiguration = this.energieVerbruikHistorieService.getChartConfig(this.selectedDate,
                                                                                                      this.verbruiksoort,
                                                                                                      this.energiesoorten,
                                                                                                      this.verbruiken,
                                                                                           (date: Moment) => this.navigateToDetails(date));
    this.loadChartConfiguration(chartConfiguration);
  }

  private loadChartConfiguration(chartConfiguration: ChartConfiguration) {
    this.chart = c3.generate(chartConfiguration);
    this.energieVerbruikHistorieService.adjustChartHeightToAvailableWindowHeight(this.chart);
  }

  public toggleEnergiesoort(energiesoortToToggle) {
    const energiesoorten = this.energieVerbruikHistorieService.toggleEnergiesoort(this.verbruiksoort, this.energiesoorten, energiesoortToToggle);
    this.navigateTo(this.verbruiksoort, energiesoorten, this.periode, this.selectedDate);
  }

  private navigateTo(verbruiksoort: string, energiesoorten: string[], periode: string, datum: Moment) {
    const commands = ['/energie', verbruiksoort, periode];
    const extras = { queryParams: { 'energiesoort': energiesoorten, 'datum': datum.format('DD-MM-YYYY') }, replaceUrl: true };
    this.router.navigate(commands, extras);
  }

  public setPeriode(periode: string) {
    this.navigateTo(this.verbruiksoort, this.energiesoorten, periode, this.selectedDate);
  }

  public setVerbruikSoort(verbruiksoort: string) {
    this.navigateTo(verbruiksoort, this.energiesoorten, this.periode, this.selectedDate);
  }

  public onDateNavigate(selectedDate: Moment) {
    this.navigateTo(this.verbruiksoort, this.energiesoorten, this.periode, selectedDate);
  }

  private navigateToDetailsOfVerbruik(verbruik: any) {
    this.navigateToDetails(this.energieVerbruikHistorieService.getMoment(this.selectedDate, verbruik));
  }

  private navigateToDetails(date: Moment) {
    if (this.periode == 'uur') {
      this.router.navigate(['/energie/opgenomen-vermogen'], {queryParams: {'datum': date.format('DD-MM-YYYY')}, replaceUrl: false});
    } else if (this.periode == 'dag') {
      this.navigateTo(this.verbruiksoort, this.energiesoorten, 'uur', date);
    } else if (this.periode == 'maand') {
      this.navigateTo(this.verbruiksoort, this.energiesoorten, 'dag', date);
    } else if (this.periode == 'jaar') {
      this.navigateTo(this.verbruiksoort, this.energiesoorten, 'maand', date);
    }
  }

  public determineChartOrTable() {
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
      this.loadDataIntoChart();
    }
  }

  private doShowTable(): void {
    if (!this.showTable) {
      this.showChart = false;
      this.showTable = true;
      this.loadDataIntoTable();
    }
  }

  public isEnergieSoortSelected(energiesoort: string) {
    return this.energiesoorten.indexOf(energiesoort) >= 0;
  }

  public getFormattedTotalCosts(verbruik: any) {
    if (verbruik.stroomKostenDal || verbruik.stroomKostenNormaal || verbruik.gasKosten) {
      return this.energieVerbruikHistorieService.formatWithUnitLabel(this.verbruiksoort, this.energiesoorten, verbruik.stroomKostenDal + verbruik.stroomKostenNormaal + verbruik.gasKosten);
    }
    return '';
  }

  public getFormattedDate(verbruik: any): string {
    return this.energieVerbruikHistorieService.getFormattedDate(verbruik);
  }

  public getFormattedValue(verbruik: any, energiesoort: string) {
    if (energiesoort == 'stroom') {
      const dalValue = verbruik[energiesoort + _.capitalize(this.verbruiksoort) + 'Dal'];
      const normaalValue = verbruik[energiesoort + _.capitalize(this.verbruiksoort) + 'Normaal'];

      if (dalValue || normaalValue) {
        return this.energieVerbruikHistorieService.formatWithUnitLabel(this.verbruiksoort, this.energiesoorten, dalValue + normaalValue);
      }

    } else if (energiesoort == 'gas') {
      const gasValue = verbruik[energiesoort + _.capitalize(this.verbruiksoort)];

      if (gasValue) {
        return this.energieVerbruikHistorieService.formatWithUnitLabel(this.verbruiksoort, this.energiesoorten, gasValue);
      }

    } else {
      throw new Error('Unexpected energiesoort: ' + energiesoort);
    }
    return ''
  }
}
