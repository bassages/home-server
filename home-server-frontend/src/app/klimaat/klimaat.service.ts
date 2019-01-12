import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {filter, map} from 'rxjs/operators';
import * as moment from 'moment';
import {Moment} from 'moment';
import {Klimaat} from './klimaat';
import {RealtimeKlimaat} from './realtimeKlimaat';
import {Trend} from './trend';
import {GemiddeldeKlimaatPerMaand} from './gemiddeldeKlimaatPerMaand';

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

  public static mapToRealtimeKlimaat(source: any): RealtimeKlimaat {
    const realtimeKlimaat: RealtimeKlimaat = new RealtimeKlimaat();
    realtimeKlimaat.dateTime = moment(source.datumtijd);
    realtimeKlimaat.temperatuur = source.temperatuur;
    realtimeKlimaat.luchtvochtigheid = source.luchtvochtigheid;
    realtimeKlimaat.temperatuurTrend = Trend[source.temperatuurTrend as string];
    realtimeKlimaat.luchtvochtigheidTrend = Trend[source.luchtvochtigheidTrend as string];
    realtimeKlimaat.sensorCode = source.sensorCode;
    return realtimeKlimaat;
  }

  private static mapAllToGemiddeldeKlimaatPerMaand(backendGemiddeldeKlimaatPerMaand: BackendGemiddeldeKlimaatPerMaand[][])
                 : GemiddeldeKlimaatPerMaand[] {
    return backendGemiddeldeKlimaatPerMaand[0].map(KlimaatService.mapToGemiddeldeKlimaatPerMaand);
  }

  public static mapToGemiddeldeKlimaatPerMaand(backendGemiddeldeKlimaatPerMaand: BackendGemiddeldeKlimaatPerMaand)
                : GemiddeldeKlimaatPerMaand {
    const gemiddeldeKlimaatPerMaand: GemiddeldeKlimaatPerMaand = new GemiddeldeKlimaatPerMaand();
    gemiddeldeKlimaatPerMaand.maand = moment(backendGemiddeldeKlimaatPerMaand.maand);
    gemiddeldeKlimaatPerMaand.gemiddelde = backendGemiddeldeKlimaatPerMaand.gemiddelde;
    return gemiddeldeKlimaatPerMaand;
  }

  public getKlimaat(sensorCode: string, from: Moment, to: Moment): Observable<Klimaat[]> {
    const url = `/api/klimaat/${sensorCode}?from=${from.format('YYYY-MM-DD')}&to=${to.format('YYYY-MM-DD')}`;
    return this.http.get<BackendKlimaat[]>(url).pipe(map(KlimaatService.mapAllToKlimaat));
  }

  public getMostRecent(sensorCode: string): Observable<RealtimeKlimaat> {
    return this.http.get<BackendRealtimeKlimaat>(`api/klimaat/${sensorCode}/meest-recente`)
                    .pipe(filter(value => value !== undefined && value !== null))
                    .pipe(map(KlimaatService.mapToRealtimeKlimaat));
  }

  public getTop(sensorCode: string, sensorType: string, topType: string, from: Moment, to: Moment, limit: number): Observable<Klimaat[]> {
    const formattedTo = to.format('YYYY-MM-DD');
    const formattedForm = from.format('YYYY-MM-DD');
    const url = `api/klimaat/${sensorCode}/${topType}?from=${formattedForm}&to=${formattedTo}&sensorType=${sensorType}&limit=${limit}`;
    return this.http.get<BackendKlimaat[]>(url)
                    .pipe(map(KlimaatService.mapAllToKlimaat));
  }

  public getGemiddeldeKlimaatPerMaand(sensorCode: string, sensorType: string, year: number): Observable<GemiddeldeKlimaatPerMaand[]> {
    const url = `api/klimaat/${sensorCode}/gemiddeld-per-maand-in-jaar?jaar=${year}&sensorType=${sensorType}`;
    return this.http.get<BackendGemiddeldeKlimaatPerMaand[][]>(url)
                    .pipe(map(KlimaatService.mapAllToGemiddeldeKlimaatPerMaand));
  }

  // noinspection JSMethodCanBeStatic
  public getValuePostFix(sensorType: string) {
    return sensorTypeToPostfixMapping.has(sensorType) ? sensorTypeToPostfixMapping.get(sensorType) : '';
  }

  // noinspection JSMethodCanBeStatic
  public getDecimalFormat(sensorType: string) {
    return sensorTypeToDecimalFormatMapping.has(sensorType) ? sensorTypeToDecimalFormatMapping.get(sensorType) : '0.0-0';
  }
}

class BackendKlimaat {
  datumtijd: string;
  temperatuur: number;
  luchtvochtigheid: number;
}

class BackendRealtimeKlimaat extends BackendKlimaat {
  temperatuurTrend: string;
  luchtvochtigheidTrend: string;
}

class BackendGemiddeldeKlimaatPerMaand {
  maand: string;
  gemiddelde: number;
}
