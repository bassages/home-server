import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {OpgenomenVermogen} from './opgenomen-vermogen';
import {Moment} from 'moment';

@Injectable()
export class OpgenomenVermogenService {

  constructor(private http: HttpClient) { }

  public getMostRecent(): Observable<OpgenomenVermogen> {
    const url = '/api/opgenomen-vermogen/meest-recente';
    return this.http.get<OpgenomenVermogen>(url);
  }

  public getHistory(from: Moment, to: Moment, periodLengthInMilliseconds: number): Observable<OpgenomenVermogen[]> {
    const formattedFrom = from.format('YYYY-MM-DD');
    const formattedTo = to.format('YYYY-MM-DD');
    const url = `/api/opgenomen-vermogen/historie/${formattedFrom}/${formattedTo}?subPeriodLength=${periodLengthInMilliseconds}`;
    return this.http.get<OpgenomenVermogen[]>(url);
  }
}
