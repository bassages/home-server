import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs/Observable";
import {KlimaatSensor} from "./klimaatSensor";
import * as moment from "moment";
import {Moment} from "moment";
import {Klimaat} from "./klimaat";

@Injectable()
export class KlimaatService {

  constructor(private http: HttpClient) { }

  public getKlimaatSensors(): Observable<KlimaatSensor[]> {
    const url: string = '/api/klimaat/sensors';
    return this.http.get<KlimaatSensor[]>(url);
  }

  public getKlimaat(sensorCode: string, from: Moment, to: Moment): Observable<Klimaat[]> {
    const url: string = `/api/klimaat/${sensorCode}?from=${from.format('YYYY-MM-DD')}&to=${to.format('YYYY-MM-DD')}`;
    return this.http.get<BackendKlimaat[]>(url).map(this.mapToKlimaat);
  }

  private mapToKlimaat(backendKlimaats: BackendKlimaat[]): Klimaat[] {
    let klimaats: Klimaat[] = [];

    for (let backendKlimaat of backendKlimaats) {
      const klimaat: Klimaat = new Klimaat();
      klimaat.dateTime = moment(backendKlimaat.datumtijd);
      klimaat.temperatuur = backendKlimaat.temperatuur;
      klimaat.luchtvochtigheid = backendKlimaat.luchtvochtigheid;
      klimaats.push(klimaat);
    }

    return klimaats;
  }
}

class BackendKlimaat {
  datumtijd: string;
  temperatuur: number;
  luchtvochtigheid: number;
}
