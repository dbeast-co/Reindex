import {HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {EMPTY, Observable} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {Injectable} from '@angular/core';
import {ToastrService} from 'ngx-toastr';
import {Router} from '@angular/router';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  constructor(private toastr: ToastrService, private router: Router) {
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req)
      .pipe(catchError((err: HttpErrorResponse) => {
        if (err.status === 404) {
          // do things
          this.toastr.error(`Server not responding!Please try again later`, '', {
            positionClass: 'toast-top-right',
          });
          // this.router.navigate(['/'])
        }
        if (err.status === 502) {
          return EMPTY;
        }
        return EMPTY;
      }));
  }

}
