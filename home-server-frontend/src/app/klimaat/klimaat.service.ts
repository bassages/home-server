import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs/Observable";
import {KlimaatSensor} from "./klimaatSensor";
import * as moment from "moment";
import {Moment} from "moment";
import {Klimaat} from "./klimaat";
import {RealtimeKlimaat} from "./realtimeKlimaat";
import {isUndefined} from "util";

@Injectable()
export class KlimaatService {

  constructor(private http: HttpClient) { }

  public getKlimaatSensors(): Observable<KlimaatSensor[]> {
    const url: string = '/api/klimaat/sensors';
    return this.http.get<KlimaatSensor[]>(url);
  }

  public getKlimaat(sensorCode: string, from: Moment, to: Moment): Observable<Klimaat[]> {
    const url: string = `/api/klimaat/${sensorCode}?from=${from.format('YYYY-MM-DD')}&to=${to.format('YYYY-MM-DD')}`;
    return this.http.get<BackendKlimaat[]>(url).map(KlimaatService.mapAllToKlimaat);
  }

  public getMostRecent(sensorCode: string): Observable<RealtimeKlimaat> {
    return this.http.get<BackendRealtimeKlimaat>(`api/klimaat/${sensorCode}/meest-recente`)
                    .filter(value => !isUndefined(value) && value !== null)
                    .map(KlimaatService.toRealtimeKlimaat);
  }

  private static mapAllToKlimaat(backendKlimaats: BackendKlimaat[]): Klimaat[] {
    return backendKlimaats.map(KlimaatService.mapToKlimaat);
  }

  private static mapToKlimaat(backendKlimaat: BackendKlimaat): Klimaat {
    const klimaat: Klimaat = new Klimaat();
    klimaat.dateTime = moment(backendKlimaat.datumtijd);
    klimaat.temperatuur = backendKlimaat.temperatuur;
    klimaat.luchtvochtigheid = backendKlimaat.luchtvochtigheid;
    return klimaat;
  }

  public static toRealtimeKlimaat(source: any): RealtimeKlimaat {
    const realtimeKlimaat: RealtimeKlimaat = new RealtimeKlimaat();
    realtimeKlimaat.dateTime = moment(source.datumtijd);
    realtimeKlimaat.temperatuur = source.temperatuur;
    realtimeKlimaat.luchtvochtigheid = source.luchtvochtigheid;
    realtimeKlimaat.temperatuurTrend = source.temperatuurTrend;
    realtimeKlimaat.luchtvochtigheidTrend = source.luchtvochtigheidTrend;
    realtimeKlimaat.sensorCode = source.sensorCode;
    return realtimeKlimaat;
  }
}

class BackendKlimaat {
  datumtijd: string;
  temperatuur: number;
  luchtvochtigheid: number;
}

class BackendRealtimeKlimaat extends BackendKlimaat{
  temperatuurTrend: string;
  luchtvochtigheidTrend: string;
}
