import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs/Observable";
import {KlimaatSensor} from "./klimaatSensor";

@Injectable()
export class KlimaatService {

  constructor(private http: HttpClient) { }

  public getKlimaatSensors(): Observable<KlimaatSensor[]> {
    const url: string = '/api/klimaat/sensors';
    return this.http.get<KlimaatSensor[]>(url);
  }
}
