import {Component, OnInit} from '@angular/core';
import {KlimaatSensor} from "../klimaatSensor";
import {KlimaatService} from "../klimaat.service";
import {LoadingIndicatorService} from "../../loading-indicator/loading-indicator.service";
import {ErrorHandingService} from "../../error-handling/error-handing.service";
import * as _ from "lodash";
import {ActivatedRoute} from "@angular/router";

@Component({
  selector: 'klimaat-historie',
  templateUrl: './klimaat-historie.component.html',
  styleUrls: ['./klimaat-historie.component.scss']
})
export class KlimaatHistorieComponent implements OnInit {

  public sensors: KlimaatSensor[];
  public sensorType: string;

  constructor(private klimaatService: KlimaatService,
              private loadingIndicatorService: LoadingIndicatorService,
              private errorHandlingService: ErrorHandingService,
              private activatedRoute: ActivatedRoute) {

    this.activatedRoute.paramMap.subscribe(params => this.sensorType = params.get('sensorType'));
  }

  ngOnInit() {
    setTimeout(() => { this.getKlimaatSensors(); },0);
  }

  private getKlimaatSensors(): void {
    this.klimaatService.getKlimaatSensors().subscribe(
      response => this.sensors = _.sortBy<KlimaatSensor>(response, ['code']),
      error => this.errorHandlingService.handleError("De klimaat sensors konden nu niet worden opgehaald", error),
      () => this.loadingIndicatorService.close()
    );
  }
}
