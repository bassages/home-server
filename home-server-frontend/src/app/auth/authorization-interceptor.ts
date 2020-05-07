import {Inject, Injectable} from '@angular/core';
import {HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Observable} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {EMPTY, throwError} from 'rxjs';
import {DOCUMENT} from '@angular/common';
import {AuthService} from '../auth.service';

@Injectable()
export class AuthorizationInterceptor implements HttpInterceptor {

  constructor(@Inject(DOCUMENT) private document: any,
              private authService: AuthService) {
  }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const xhr = request.clone({
      // Prevent Basic-auth popup from browser
      headers: request.headers.set('X-Requested-With', 'XMLHttpRequest')
    });
    return next.handle(xhr).pipe(
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
        if (request.url !== '/api/user') {
          this.authService.loggedOut();
        }
        return EMPTY;
      }
    }
    return throwError(httpErrorResponse.message);
  }
}
