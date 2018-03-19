import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs/Observable";
import "rxjs/Rx";
import {OpgenomenVermogen} from "./opgenomenVermogen";

@Injectable()
export class OpgenomenVermogenService {

  constructor(private http: HttpClient) { }

  public getMostRecent(): Observable<OpgenomenVermogen> {
    const url = 'api/opgenomen-vermogen/meest-recente';
    return this.http.get<OpgenomenVermogen>(url);
  }
}
