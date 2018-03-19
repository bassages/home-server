import {Injectable} from '@angular/core';
import {Moment} from "moment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs/Observable";
import "rxjs/Rx";
import {VerbruikOpDag} from "./verbruikOpDag";
import {GemiddeldVerbruikInPeriod} from "./gemiddeldVerbruikInPeriod";
import {VerbruikInUur} from "./verbruikInUur";

@Injectable()
export class EnergieVerbruikService {

  constructor(private http: HttpClient) { }

  public getVerbruikPerDag(from: Moment, to: Moment): Observable<VerbruikOpDag[]> {
    const url = 'api/energie/verbruik-per-dag/' + from.format('YYYY-MM-DD') + '/' + to.format('YYYY-MM-DD');
    return this.http.get<VerbruikOpDag[]>(url, { observe: 'response' }).map(response => response.body);
  }

  public getGemiddeldVerbruikPerDag(from: Moment, to: Moment): Observable<GemiddeldVerbruikInPeriod> {
    const url = 'api/energie/gemiddelde-per-dag/' + from.format('YYYY-MM-DD') + '/' + to.format('YYYY-MM-DD');
    return this.http.get<GemiddeldVerbruikInPeriod>(url, { observe: 'response' }).map(response => response.body);
  }

  public getVerbruikPerUurOpDag(dag: Moment): Observable<VerbruikInUur[]> {
    const url = `api/energie/verbruik-per-uur-op-dag/${dag.format('YYYY-MM-DD')}`;
    return this.http.get<VerbruikInUur[]>(url, { observe: 'response' }).map(response => response.body);
  }
}
