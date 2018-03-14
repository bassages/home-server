import {Injectable} from '@angular/core';
import {Moment} from "moment";
import {HttpClient, HttpResponse} from "@angular/common/http";
import {Observable} from "rxjs/Observable";
import "rxjs/Rx";
import {VerbruikOpDag} from "./verbruikOpDag";
import {GemiddeldVerbruikInPeriod} from "./gemiddeldVerbruikInPeriod";

@Injectable()
export class EnergieVerbruikService {

  constructor(private http: HttpClient) { }

  public getVerbruikPerDag(from: Moment, to: Moment): Observable<HttpResponse<VerbruikOpDag[]>> {
    const url = 'api/energie/verbruik-per-dag/' + from.format('YYYY-MM-DD') + '/' + to.format('YYYY-MM-DD');
    return this.http.get<VerbruikOpDag[]>(url, { observe: 'response' });
  }

  public getGemmiddeldVerbruikPerDag(from: Moment, to: Moment): Observable<HttpResponse<GemiddeldVerbruikInPeriod>> {
    const url = 'api/energie/gemiddelde-per-dag/' + from.format('YYYY-MM-DD') + '/' + to.format('YYYY-MM-DD');
    return this.http.get<GemiddeldVerbruikInPeriod>(url, { observe: 'response' });
  }
}
