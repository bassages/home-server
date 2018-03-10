import {Injectable} from '@angular/core';
import {Observable} from "rxjs/Observable";
import {Subject} from "rxjs/Subject";

@Injectable()
export class LoaderService {
  private openSubject = new Subject();
  private closeSubject = new Subject();

  constructor() { }

  onOpen(): Observable<any> {
    return this.openSubject.asObservable();
  }

  onClose(): Observable<any> {
    return this.closeSubject.asObservable();
  }

  public open() {
    this.openSubject.next();
  }

  public close() {
    this.closeSubject.next();
  }
}
