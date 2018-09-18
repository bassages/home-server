import {Inject, Injectable} from '@angular/core';
import {HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Observable} from 'rxjs/Observable';
import {catchError} from 'rxjs/operators';
import {EMPTY, throwError} from 'rxjs';
import {DOCUMENT} from '@angular/common';

@Injectable()
export class AuthorizationInterceptor implements HttpInterceptor {

  constructor(@Inject(DOCUMENT) private document: any) {
  }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(request).pipe(
      catchError(error => this.handleError(request, error))
    );
  }

  // noinspection JSMethodCanBeStatic
  private handleError(request: HttpRequest<any>, httpErrorResponse: HttpErrorResponse) {
    if (httpErrorResponse.error instanceof ErrorEvent) {
      // A client-side or network error occurred. Handle it accordingly.
      console.error('An error occurred:', httpErrorResponse.error.message);
    } else {

      if (httpErrorResponse.status === 401) {
        // Session timed out causing the xhr call to fail.
        // Redirect to root will cause the server to show login page
        this.document.location.href = '/login';
        return EMPTY;
      }
    }
    return throwError(httpErrorResponse.message);
  }
}
