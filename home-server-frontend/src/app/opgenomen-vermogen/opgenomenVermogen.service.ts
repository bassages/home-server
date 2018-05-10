import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import "rxjs/Rx";
import {OpgenomenVermogen} from "./opgenomenVermogen";
import {Moment} from "moment";

@Injectable()
export class OpgenomenVermogenService {

  constructor(private http: HttpClient) { }

  public getMostRecent(): Observable<OpgenomenVermogen> {
    const url = '/api/opgenomen-vermogen/meest-recente';
    return this.http.get<OpgenomenVermogen>(url);
  }

  public getHistory(from: Moment, to: Moment, periodLengthInMilliseconds: number): Observable<OpgenomenVermogen[]> {
    const url = `/api/opgenomen-vermogen/historie/${from.format('YYYY-MM-DD')}/${to.format('YYYY-MM-DD')}?subPeriodLength=${periodLengthInMilliseconds}`;
    return this.http.get<OpgenomenVermogen[]>(url);
  }
}
