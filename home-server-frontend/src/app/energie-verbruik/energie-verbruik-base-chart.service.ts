import "rxjs/Rx";
import {ChartConfiguration} from "c3";
import * as _ from "lodash";
import {DecimalPipe} from "@angular/common";

export abstract class EnergieVerbruikBaseChartService {

  constructor(protected decimalPipe: DecimalPipe) {};

  public getEmptyChartConfig(): ChartConfiguration {
    return {
      data: { json: {} },
      legend: { show: false },
      axis: {
        x: {
          tick: { values: [] }
        },
        y: {
          tick: { values: [] }
        }
      },
      padding: this.getDefaultChartPadding()
    };
  };

  public getDefaultBarChartConfig(data: any[]): ChartConfiguration {
    return {
      bindto: '#chart',
      data: {
        type: 'bar',
        json: {},
        colors: this.getDataColors(),
        order: (data1: any, data2: any) => data2.id.localeCompare(data1.id)
      },
      legend: { show: false },
      bar: {
        width: { ratio: 0.8 }
      },
      transition: { duration: 0 },
      padding: this.getDefaultChartPadding(),
      grid: {
        y: { show: true }
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

  protected getDefaultChartPadding() {
    return {
      top: 10, bottom: 25, left: 55, right: 20
    };
  };

  protected formatWithoutUnitLabel(verbruiksoort: string, value: any) {
    return this.decimalPipe.transform(value, '1.3-3');
  };

  private formatWithUnitLabel(verbruiksoort: string, energieSoorten: string[], value) {
    const withoutUnitLabel = this.formatWithoutUnitLabel(verbruiksoort, value);
    if (verbruiksoort === 'verbruik') {
      return withoutUnitLabel + ' ' + this.getVerbruikLabel(energieSoorten[0]);
    } else if (verbruiksoort === 'kosten') {
      return '\u20AC ' + withoutUnitLabel;
    }
  };

  protected getTooltipContent(c3, data, titleFormatter, valueFormatter, color, verbruiksoort: string, energiesoorten: string[]) {
    let tooltipContents: string = '';

    data = _.sortBy(data, 'id');

    if (data.length > 0) {
      tooltipContents += `<table class='${c3.CLASS.tooltip}'><tr><th colspan='2'>${titleFormatter(data[0].x)}</th></tr>`;
    }

    for (let i = 0; i < data.length; i++) {
      if (!(data[i] && (data[i].value || data[i].value === 0))) {
        continue;
      }

      const bgcolor = c3.levelColor ? c3.levelColor(data[i].value) : color(data[i].id);

      tooltipContents += '<tr>';
      tooltipContents += `<td class='name'><span style='background-color:${bgcolor}'></span>${this.getTooltipLabel(data[i].id)}</td>`;
      tooltipContents += `<td class='value'>${this.formatWithUnitLabel(verbruiksoort, energiesoorten, data[i].value)}</td>`;
      tooltipContents += '</tr>';
    }

    if (data.length > 1) {
      let total: number = _.sumBy(data, 'value');
      tooltipContents += '<tr>';
      tooltipContents += '<td class=\'name\'><strong>Totaal</strong></td>';
      tooltipContents += `<td class='value'><strong>${this.formatWithUnitLabel(verbruiksoort, energiesoorten, total)}</strong></td>`;
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
  }

  protected getKeysGroups(verbruiksoort: string, energiesoorten: string[]): string[] {
    let keysGroups: string[] = [];
    if (energiesoorten.indexOf('gas') > -1) {
      keysGroups.push(`gas${_.capitalize(verbruiksoort)}`);
    }
    if (energiesoorten.indexOf('stroom') > -1) {
      keysGroups.push(`stroom${_.capitalize(verbruiksoort)}Dal`);
      keysGroups.push(`stroom${_.capitalize(verbruiksoort)}Normaal`);
    }
    return keysGroups;
  }

  public getEnergiesoortenAfterToggle(verbruiksoort: string, currentEnergiesoorten: string[], energiesoortToToggle: string): string[] {
    let newEnergiesoorten = currentEnergiesoorten.slice();

    const indexOfToggledEnergieSoort = newEnergiesoorten.indexOf(energiesoortToToggle);

    if (verbruiksoort == 'kosten') {
      if (indexOfToggledEnergieSoort < 0) {
        newEnergiesoorten.push(energiesoortToToggle);
      } else {
        newEnergiesoorten.splice(indexOfToggledEnergieSoort, 1);
      }
    } else {
      if (newEnergiesoorten[0] !== energiesoortToToggle) {
        newEnergiesoorten.splice(0, newEnergiesoorten.length);
        newEnergiesoorten.push(energiesoortToToggle);
      }
    }
    return newEnergiesoorten;
  }
}
