import {Injectable} from '@angular/core';
import {MeterstandOpDag} from './meterstandOpDag';
import {Moment} from 'moment';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Meterstand} from './meterstand';

@Injectable()
export class MeterstandService {

  constructor(private http: HttpClient) { }

  public getMeterstanden(from: Moment, to: Moment): Observable<MeterstandOpDag[]> {
    const url = `/api/meterstanden/per-dag/${from.format('YYYY-MM-DD')}/${to.format('YYYY-MM-DD')}`;
    return this.http.get<MeterstandOpDag[]>(url);
  }

  public getMostRecent(): Observable<Meterstand> {
    const url = '/api/meterstanden/meest-recente';
    return this.http.get<Meterstand>(url);
  }
}
