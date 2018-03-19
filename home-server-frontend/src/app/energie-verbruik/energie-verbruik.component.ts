import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import * as c3 from 'c3';
import {ChartAPI, ChartConfiguration} from 'c3';
import {Moment} from "moment";
import {IDatePickerConfig} from "ng2-date-picker";
import * as _ from "lodash";
import {EnergieVerbruikService} from "./energie-verbruik.service";
import {VerbruikInUur} from "./verbruikInUur";
import {ErrorHandingService} from "../error-handling/error-handing.service";
import {LoadingIndicatorService} from "../loading-indicator/loading-indicator.service";
import {DecimalPipe} from "@angular/common";
import moment = require("moment");

@Component({
  selector: 'app-energie-verbruik',
  templateUrl: './energie-verbruik.component.html',
  styleUrls: ['./energie-verbruik.component.scss']
})
export class EnergieVerbruikComponent implements OnInit {

  public dayPickerConfiguration: IDatePickerConfig;
  public dayPickerModel: String;
  public selectedDay: Moment;
  public verbruiksoort: string = 'verbruik';
  public energiesoorten: string[] = ['gas'];

  private verbruikPerUurOpDag: VerbruikInUur[];

  private selectedDayFormat = 'DD-MM-YYYY';
  private chart: ChartAPI;

  constructor(private energieVerbruikService: EnergieVerbruikService,
              private loadingIndicatorService: LoadingIndicatorService,
              private errorHandlingService: ErrorHandingService,
              private decimalPipe: DecimalPipe,
              private route: ActivatedRoute,
              private router: Router) { }

  ngOnInit() {
    this.initDayPicker();

    this.chart = c3.generate(this.getDefaultBarChartConfig());
    this.chart.resize({height: 500});
  }

  private initDayPicker() {
    this.dayPickerConfiguration = {
      format: this.selectedDayFormat,
      max: this.getToday()
    };
    this.setSelectedDay(this.getToday());
  }

  private getDefaultBarChartConfig(): ChartConfiguration {
    let that = this;

    return <ChartConfiguration>{
      bindto: '#chart',
      data: {
        type: 'bar',
        json: {},
        colors: this.getDataColors(),
        order: (data1: any, data2: any) => data2.id.localeCompare(data1.id)
      },
      legend: {
        show: false
      },
      bar: {
        width: {
          ratio: 0.8
        }
      },
      transition: {
        duration: 0
      },
      padding: this.getChartPadding(),
      grid: {
        y: {
          show: true
        }
      },
      axis: {
        y: {
          tick: {
            format: (x: number) => this.formatWithoutUnitLabel(this.verbruiksoort, x)}
        }
      },
      tooltip: {
        contents: function (data, defaultTitleFormat, defaultValueFormat, color) {
          return that.getTooltipContent(this, data, defaultTitleFormat, defaultValueFormat, color, that.verbruiksoort, that.energiesoorten)
        }
      }
    };
  };

  private getDataColors () {
    return {
      'stroomVerbruikDal': '#4575b3',
      'stroomVerbruikNormaal': '#f4b649',
      'stroomKostenDal': '#4575b3',
      'stroomKostenNormaal': '#f4b649',
      'stroomVerbruik': '#4575b3',
      'stroomKosten': '#4575b3',
      'gasVerbruik': '#2ca02c',
      'gasKosten': '#2ca02c'
    };
  };

  private getChartPadding() {
    return {
      top: 10,
      bottom: 25,
      left: 55,
      right: 20
    };
  };

  public dayPickerChanged(selectedDay: Moment): void {
    if (!_.isUndefined(selectedDay)) {
      this.setSelectedDay(selectedDay);
      this.getVerbruik();
    }
  }

  private getVerbruik() {
    this.loadingIndicatorService.open();

    this.energieVerbruikService.getVerbruikPerUurOpDag(this.selectedDay).subscribe(
      response => {
        this.verbruikPerUurOpDag = response;
        this.initGraph();
      },
      error => this.errorHandlingService.handleError("De meterstanden konden nu niet worden opgehaald", error),
      () => this.loadingIndicatorService.close()
    );
  }

  public navigate(numberOfDays: number): void {
    this.setSelectedDay(this.selectedDay.add(numberOfDays, 'days'));
  }

  public isMaxSelected(): boolean {
    const now: Moment = moment();
    return now.date() === this.selectedDay.date() && now.month() === this.selectedDay.month() && now.year() === this.selectedDay.year();
  }

  private getToday(): Moment {
    return moment();
  }

  private setSelectedDay(day: Moment) {
    this.selectedDay = day;
    this.dayPickerModel = day.format(this.selectedDayFormat);
  }

  private getKeysGroups(): string[] {
    let keysGroups: string[] = [];
    if (this.energiesoorten.indexOf('gas') > -1) {
      keysGroups.push('gasVerbruik')
    }
    if (this.energiesoorten.indexOf('stroom') > -1) {
      keysGroups.push('stroomVerbruikDal');
      keysGroups.push('stroomVerbruikNormaal');
    }
    return keysGroups;
  }

  private initGraph() {
    const chartConfiguration: ChartConfiguration = this.getDefaultBarChartConfig();
    const keysGroups = this.getKeysGroups();
    chartConfiguration.data.groups = [keysGroups];
    chartConfiguration.data.keys = {x: 'uur', value: keysGroups};
    chartConfiguration.data.json = this.verbruikPerUurOpDag;
    chartConfiguration.axis.x = {
        type: 'category',
        tick: {
          format: (uur: number) => `${this.decimalPipe.transform(uur, '2.0-0')}:00 - ${this.decimalPipe.transform(uur + 1, '2.0-0')}:00`
        }
    };
    c3.generate(chartConfiguration);
  }

  public allowMultpleEnergiesoorten(): boolean {
    return this.verbruiksoort === 'kosten';
  }

  public formatWithoutUnitLabel = function(soort: string, value: any) {
    return this.decimalPipe.transform(value, '1.3-3');
  };

  private formatWithUnitLabel(soort: string, energieSoorten: string[], value) {
    const withoutUnitLabel = this.formatWithoutUnitLabel(soort, value);
    if (soort === 'verbruik') {
      return withoutUnitLabel + ' ' + this.getVerbruikLabel(energieSoorten[0]);
    } else if (soort === 'kosten') {
      return '\u20AC ' + withoutUnitLabel;
    }
  };

  private getTooltipContent(c3, data, defaultTitleFormat, defaultValueFormat, color, soort, energiesoorten) {
    let tooltipContents: string = '';

    data = _.sortBy(data, 'id');

    if (data.length > 0) {
      tooltipContents += `<table class='${c3.CLASS.tooltip}'><tr><th colspan='2'>${defaultTitleFormat(data[0].x)}</th></tr>`;
    }

    for (let i = 0; i < data.length; i++) {
      if (!(data[i] && (data[i].value || data[i].value === 0))) {
        continue;
      }

      const bgcolor = c3.levelColor ? c3.levelColor(data[i].value) : color(data[i].id);

      tooltipContents += '<tr>';
      tooltipContents += `<td class='name'><span style='background-color:${bgcolor}'></span>${this.getTooltipLabel(data[i].id)}</td>`;
      tooltipContents += `<td class='value'>${this.formatWithUnitLabel(soort, this.energiesoorten, data[i].value)}</td>`;
      tooltipContents += '</tr>';
    }

    if (data.length > 1) {
      let total: number = _.sumBy(data, 'value');
      tooltipContents += '<tr>';
      tooltipContents += '<td class=\'name\'><strong>Totaal</strong></td>';
      tooltipContents += `<td class='value'><strong>${this.formatWithUnitLabel(soort, energiesoorten, total)}</strong></td>`;
      tooltipContents += "</tr>";
    }
    tooltipContents += "</table>";

    return tooltipContents;
  }

  private getTooltipLabel(id) {
    if (_.endsWith(id, 'Dal')) {
      return 'Stroom - Daltarief';
    } else if (_.endsWith(id, 'Normaal')) {
      return 'Stroom - Normaaltarief';
    } else if (_.startsWith(id, 'gas')) {
      return 'Gas';
    }
  }

  private getVerbruikLabel(energiesoort: string) {
    if (energiesoort === 'stroom') {
      return 'kWh';
    } else if (energiesoort === 'gas') {
      return 'm\u00B3';
    } else {
      return '?';
    }
  };

  public toggleEnergiesoort(energiesoortToToggle) {
    const index = this.energiesoorten.indexOf(energiesoortToToggle);

    if (this.verbruiksoort === 'kosten') {
      if (index < 0) {
        this.energiesoorten.push(energiesoortToToggle);
      } else {
        this.energiesoorten.splice(index, 1);
      }
    } else {
      if (this.energiesoorten[0] !== energiesoortToToggle) {
        this.energiesoorten.splice(0, this.energiesoorten.length);
        this.energiesoorten.push(energiesoortToToggle);
      }
    }
    this.getVerbruik();
  }

}
