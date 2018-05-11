import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {MindergasnlSettings} from './mindergasnlSettings';

@Injectable()
export class MindergasnlService {

  constructor(private http: HttpClient) { }

  public get(): Observable<MindergasnlSettings> {
    const url = '/api/mindergasnl';
    return this.http.get<MindergasnlSettings>(url);
  }

  public update(mindergasnlSettings: MindergasnlSettings): Observable<MindergasnlSettings> {
    const url = '/api/mindergasnl';
    return this.http.post<MindergasnlSettings>(url, mindergasnlSettings);
  }
}
