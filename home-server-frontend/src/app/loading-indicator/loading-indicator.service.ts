import {Injectable} from '@angular/core';
import {Observable} from "rxjs/Observable";
import {Subject} from "rxjs/Subject";

@Injectable()
export class LoadingIndicatorService {
  private openSubject = new Subject();
  private closeSubject = new Subject();

  constructor() { }

  public onOpen(): Observable<any> {
    return this.openSubject.asObservable();
  }

  public onClose(): Observable<any> {
    return this.closeSubject.asObservable();
  }

  public open() {
    this.openSubject.next();
  }

  public close() {
    this.closeSubject.next();
  }
}
