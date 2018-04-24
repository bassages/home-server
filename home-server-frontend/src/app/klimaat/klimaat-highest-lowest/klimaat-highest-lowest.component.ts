import {Component, OnInit} from '@angular/core';
import {KlimaatService} from "../klimaat.service";
import {LoadingIndicatorService} from "../../loading-indicator/loading-indicator.service";
import {ErrorHandingService} from "../../error-handling/error-handing.service";
import * as moment from "moment";
import {Moment} from "moment";
import {Klimaat} from "../klimaat";
import {KlimaatSensor} from "../klimaatSensor";
import * as _ from "lodash";

@Component({
  selector: 'klimaat-highest-lowest',
  templateUrl: './klimaat-highest-lowest.component.html',
  styleUrls: ['./klimaat-highest-lowest.component.scss']
})
export class KlimaatHighestLowestComponent implements OnInit {

  public sensors: KlimaatSensor[];
  public sensorCode: string = 'WOONKAMER';
  public sensorType: string = 'temperatuur';
  public limit: number = 25;
  public from: Moment = moment().set('month', 0).set('date', 1);
  public to: Moment = moment().set('month', 11).set('date', 31);

  public highestKlimaats: Klimaat[];
  public lowestKlimaats: Klimaat[];

  constructor(private klimaatService: KlimaatService,
              private loadingIndicatorService: LoadingIndicatorService,
              private errorHandlingService: ErrorHandingService) { }

  public ngOnInit(): void {
    setTimeout(() => { this.getKlimaatSensors(); },0);
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
      error => this.errorHandlingService.handleError("De klimaat sensors konden nu niet worden opgehaald", error),
    );
  }

  private getAndLoadData() {
    this.loadingIndicatorService.open();

    this.klimaatService.getTop(this.sensorCode, this.sensorType, 'laagste', this.from, this.to, this.limit).subscribe(
      klimaats => this.lowestKlimaats = klimaats,
      error => this.errorHandlingService.handleError("Laagste klimaat kon niet worden opgehaald", error),
      () => this.loadingIndicatorService.close()
    );
    this.klimaatService.getTop(this.sensorCode, this.sensorType, 'hoogste', this.from, this.to, this.limit).subscribe(
      klimaats => this.highestKlimaats = klimaats,
      error => this.errorHandlingService.handleError("Hoogste klimaat kon niet worden opgehaald", error),
      () => this.loadingIndicatorService.close()
    );
  }

  public getValuePostFix(sensorType: string) {
    return this.klimaatService.getValuePostFix(this.sensorType);
  }
}
