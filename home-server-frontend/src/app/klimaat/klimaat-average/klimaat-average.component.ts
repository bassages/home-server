import {Component, OnInit} from '@angular/core';
import * as moment from 'moment';
import {Moment} from 'moment';
import {ErrorHandingService} from '../../error-handling/error-handing.service';
import {KlimaatService} from '../klimaat.service';
import {LoadingIndicatorService} from '../../loading-indicator/loading-indicator.service';
import {KlimaatSensor} from '../klimaatSensor';
import {GemiddeldeKlimaatPerMaand} from '../gemiddeldeKlimaatPerMaand';
import * as _ from 'lodash';

@Component({
  selector: 'home-klimaat-average',
  templateUrl: './klimaat-average.component.html',
  styleUrls: ['./klimaat-average.component.scss']
})
export class KlimaatAverageComponent implements OnInit {

  public sensors: KlimaatSensor[];
  public sensorType = 'temperatuur';
  public sensorCode: string;
  public year: Moment = moment();

  public gemiddeldeKlimaatPerMaand: GemiddeldeKlimaatPerMaand[];

  constructor(private klimaatService: KlimaatService,
              private loadingIndicatorService: LoadingIndicatorService,
              private errorHandlingService: ErrorHandingService) {
  }

  public ngOnInit(): void {
    setTimeout(() => this.getKlimaatSensors());
  }

  private getKlimaatSensors(): void {
    this.loadingIndicatorService.open();

    this.klimaatService.getKlimaatSensors().subscribe(
      response => {
        this.sensors = _.sortBy<KlimaatSensor>(response, ['omschrijving']);

        if (this.sensors.length > 0) {
          this.sensorCode = this.sensors[0].code;
        }
        this.getAndLoadData();
      },
      error => this.errorHandlingService.handleError('De klimaat sensors konden nu niet worden opgehaald', error),
    );
  }

  private getAndLoadData() {
    this.loadingIndicatorService.open();

    this.klimaatService.getGemiddeldeKlimaatPerMaand(this.sensorCode, this.sensorType, this.year.year()).subscribe(
      gemiddeldeKlimaatPerMaand => { this.gemiddeldeKlimaatPerMaand = gemiddeldeKlimaatPerMaand; },
      error => this.errorHandlingService.handleError('Gemiddelde klimaat kon niet worden opgehaald', error),
      () => this.loadingIndicatorService.close()
    );
  }

  public getValuePostFix(sensorType: string): string {
    return this.klimaatService.getValuePostFix(sensorType);
  }

  public getDecimalFormat(sensorType: string): string {
    return this.klimaatService.getDecimalFormat(sensorType);
  }

  public setSensorCode(sensorCode: string): void {
    this.sensorCode = sensorCode;
    this.getAndLoadData();
  }

  public yearPickerChanged(selectedYear: Moment): void {
    this.year = selectedYear;
    this.getAndLoadData();
  }

  public setSensorType(sensorType: string): void {
    this.sensorType = sensorType;
    this.getAndLoadData();
  }
}
