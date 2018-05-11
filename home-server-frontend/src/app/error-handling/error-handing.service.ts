import {Injectable} from '@angular/core';
import {Observable, Subject} from 'rxjs';
import {LoadingIndicatorService} from '../loading-indicator/loading-indicator.service';
import {Error} from './error';

@Injectable()
export class ErrorHandingService {
  private errorSubject = new Subject<Error>();

  constructor(private loadingIndicatorService: LoadingIndicatorService) { }

  public onError(): Observable<Error> {
    return this.errorSubject.asObservable();
  }

  public handleError(message: String, causedBy: any) {
    console.error(message, causedBy);
    this.loadingIndicatorService.close();
    this.errorSubject.next(new Error(message, causedBy));
  }
}
