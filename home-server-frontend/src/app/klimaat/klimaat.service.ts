import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs/Observable";
import {KlimaatSensor} from "./klimaatSensor";
import * as moment from "moment";
import {Moment} from "moment";
import {Klimaat} from "./klimaat";
import {RealtimeKlimaat} from "./realtimeKlimaat";
import {isUndefined} from "util";
import {Trend} from "./trend";

const sensorTypeToPostfixMapping: Map<string, string> =
  new Map<string, string>([
    ['temperatuur', '℃'],
    ['luchtvochtigheid', '%'],
  ]);

const sensorTypeToDecimalFormatMapping: Map<string, string> =
  new Map<string, string>([
    ['temperatuur', '1.2-2'],
    ['luchtvochtigheid', '1.1-1'],
  ]);

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

  public getTop(sensorCode: string, sensorType: string, topType: string, from: Moment, to: Moment, limit: number): Observable<Klimaat[]> {
    const url = `api/klimaat/${sensorCode}/${topType}?from=${from.format('YYYY-MM-DD')}&to=${to.format('YYYY-MM-DD')}&sensorType=${sensorType}&limit=${limit}`;
    return this.http.get<BackendKlimaat[]>(url).map(KlimaatService.mapAllToKlimaat);
  }

  // noinspection JSMethodCanBeStatic
  public getValuePostFix(sensorType: string) {
    return sensorTypeToPostfixMapping.has(sensorType) ? sensorTypeToPostfixMapping.get(sensorType) : '';
  }

  // noinspection JSMethodCanBeStatic
  public getDecimalFormat(sensorType: string) {
    return sensorTypeToDecimalFormatMapping.has(sensorType) ? sensorTypeToDecimalFormatMapping.get(sensorType) : '0.0-0';
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
    realtimeKlimaat.temperatuurTrend = Trend[source.temperatuurTrend as string];
    realtimeKlimaat.luchtvochtigheidTrend = Trend[source.luchtvochtigheidTrend as string];
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
