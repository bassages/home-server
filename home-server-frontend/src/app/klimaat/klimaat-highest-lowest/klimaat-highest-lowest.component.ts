import {Component, OnInit} from '@angular/core';
import {KlimaatService} from '../klimaat.service';
import {LoadingIndicatorService} from '../../loading-indicator/loading-indicator.service';
import {ErrorHandingService} from '../../error-handling/error-handing.service';
import * as moment from 'moment';
import {Moment} from 'moment';
import {Klimaat} from '../klimaat';
import {KlimaatSensor} from '../klimaatSensor';
import sortBy from 'lodash/sortBy';
import {zip} from 'rxjs';
import {Router} from '@angular/router';
import {KlimaatSensorService} from '../klimaatsensor.service';

@Component({
  selector: 'home-klimaat-highest-lowest',
  templateUrl: './klimaat-highest-lowest.component.html',
  styleUrls: ['./klimaat-highest-lowest.component.scss']
})
export class KlimaatHighestLowestComponent implements OnInit {
  public sensors: KlimaatSensor[];
  public sensorCode;
  public sensorType = 'temperatuur';
  public limit = 10;
  public year: Moment = moment();

  public highestKlimaats: Klimaat[];
  public lowestKlimaats: Klimaat[];

  constructor(private klimaatService: KlimaatService,
              private klimaatSensorService: KlimaatSensorService,
              private loadingIndicatorService: LoadingIndicatorService,
              private errorHandlingService: ErrorHandingService,
              private router: Router) {
  }

  public ngOnInit(): void {
    setTimeout(() => this.getKlimaatSensors());
  }

  private getKlimaatSensors(): void {
    this.loadingIndicatorService.open();

    this.klimaatSensorService.list().subscribe(
      response => {
        this.sensors = sortBy<KlimaatSensor>(response, ['omschrijving']);

        if (this.sensors.length > 0) {
          this.sensorCode = this.sensors[0].code;
        }
        this.getAndLoadData();
      },
      error => this.errorHandlingService.handleError('De klimaat sensors konden nu niet worden opgehaald', error),
    );
  }

  private getAndLoadData(): void {
    this.loadingIndicatorService.open();

    const from: Moment = this.getFrom();
    const to: Moment = this.getTo();

    const getLowest = this.klimaatService.getTop(this.sensorCode, this.sensorType, 'laagste', from, to, this.limit);
    const getHighest = this.klimaatService.getTop(this.sensorCode, this.sensorType, 'hoogste', from, to, this.limit);

    zip(getLowest, getHighest).subscribe(klimaats => {
        this.lowestKlimaats = klimaats[0];
        this.highestKlimaats = klimaats[1];
      },
      error => this.errorHandlingService.handleError('Hoogste/laagste klimaat kon niet worden opgehaald', error),
      () => this.loadingIndicatorService.close()
    );
  }

  private getFrom(): Moment {
    return this.year.clone().month(0).date(1);
  }

  private getTo(): Moment {
    return this.year.clone().month(11).date(31);
  }

  public getValuePostFix(sensorType: string): string {
    return this.klimaatService.getValuePostFix(sensorType);
  }

  public getDecimalFormat(sensorType: string): string {
    return this.klimaatService.getDecimalFormat(sensorType);
  }

  public limitChanged(): void {
    this.getAndLoadData();
  }

  public sensorChanged(): void {
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

  public navigateToDetailsOfDate(dateTime: Moment): void {
    const commands = ['/klimaat/historie'];
    const extras = { queryParams: { sensorCode: this.sensorCode, sensorType: this.sensorType, datum: dateTime.format('DD-MM-YYYY') } };
    this.router.navigate(commands, extras);
  }
}
