import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {StandbyPowerInPeriod} from './standby-power-in-period';

@Injectable()
export class StandbyPowerService {

  constructor(private http: HttpClient) { }

  public get(year: number): Observable<StandbyPowerInPeriod[]> {
    const url = '/api/standby-power/' + year;
    return this.http.get<StandbyPowerInPeriod[]>(url);
  }
}
