import {Injectable} from '@angular/core';
import {MeterstandOpDag} from "./meterstandOpDag";
import {Moment} from "moment";
import {HttpClient, HttpResponse} from "@angular/common/http";
import {Observable} from "rxjs/Observable";
import "rxjs/Rx";
import {Meterstand} from "./meterstand";

@Injectable()
export class MeterstandService {

  constructor(private http: HttpClient) { }

  public getMeterstanden(from: Moment, to: Moment): Observable<HttpResponse<MeterstandOpDag[]>> {
    const url = 'api/meterstanden/per-dag/' + from.format('YYYY-MM-DD') + '/' + to.format('YYYY-MM-DD');
    return this.http.get<MeterstandOpDag[]>(url, { observe: 'response' });
  }

  public getMostRecent(): Observable<HttpResponse<Meterstand>> {
    const url = 'api/meterstand/meest-recente';
    return this.http.get<Meterstand>(url, { observe: 'response' });
  }
}
