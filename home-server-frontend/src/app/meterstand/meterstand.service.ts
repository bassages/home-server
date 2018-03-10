import {Injectable} from '@angular/core';
import {MeterstandOpDag} from "./meterstandOpDag";
import {Moment} from "moment";
import {HttpClient, HttpResponse} from "@angular/common/http";
import {Observable} from "rxjs/Observable";
import "rxjs/Rx";

@Injectable()
export class MeterstandService {

  constructor(private http: HttpClient) { }

  public getMeterstanden(from: Moment, to: Moment): Observable<HttpResponse<MeterstandOpDag[]>> {
    console.log("Get meterstanden from: ", from.toDate(), ', to: ', to.toDate());

    const url = 'api/meterstanden/per-dag/' + from.format('YYYY-MM-DD') + '/' + to.format('YYYY-MM-DD');

    return this.http.get<MeterstandOpDag[]>(url, { observe: 'response' });
  }
}
