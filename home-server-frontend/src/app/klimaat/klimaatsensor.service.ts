import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {KlimaatSensor} from './klimaatSensor';
import {tap} from 'rxjs/operators';
import {Observable} from 'rxjs';
import { of } from 'rxjs';

@Injectable()
export class KlimaatSensorService {

  constructor(private http: HttpClient) { }

  // Cache klimaatsensors, they don't update frequently
  private klimaatSensors: KlimaatSensor[];

  public list(): Observable<KlimaatSensor[]> {
    if (this.klimaatSensors) {
      return of(this.klimaatSensors);
    }
    const url = '/api/klimaat/sensors';
    return this.http.get<KlimaatSensor[]>(url).pipe(
      tap(result => this.klimaatSensors = result)
    );
  }

  public update(klimaatSensor: KlimaatSensor): Observable<KlimaatSensor> {
    const url = `/api/klimaat/sensors/${klimaatSensor.code}`;
    return this.http.put<KlimaatSensor>(url, klimaatSensor).pipe(
        tap(() => this.klimaatSensors = null)
    );
  }

  public delete(klimaatSensor: KlimaatSensor) {
    const url = `/api/klimaat/sensors/${klimaatSensor.code}`;
    return this.http.delete(url).pipe(
      tap(() => this.klimaatSensors = null)
    );
  }
}
