import {Injectable} from '@angular/core';
import {Observable} from "rxjs/Observable";
import {Subject} from "rxjs/Subject";
import {LoadingIndicatorService} from "../loading-indicator/loading-indicator.service";
import {Error} from "./error";

@Injectable()
export class ErrorHandingService {
  private errorSubject = new Subject<Error>();

  constructor(private loadingIndicatorService: LoadingIndicatorService) { }

  onError(): Observable<Error> {
    return this.errorSubject.asObservable();
  }

  public handleError(message: String, causedBy: any) {
    this.loadingIndicatorService.close();
    this.errorSubject.next(new Error(message, causedBy));
  }
}
