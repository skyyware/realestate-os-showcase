import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('realestate.token');
  const isApiRequest = req.url.includes('/api/');
  const authRequest = token && isApiRequest
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(authRequest).pipe(catchError((error: unknown) => {
    if (error instanceof HttpErrorResponse && error.status === 401 && isApiRequest && !req.url.includes('/api/auth/')) {
      localStorage.removeItem('realestate.token');
      window.dispatchEvent(new Event('realestate:session-expired'));
    }
    return throwError(() => error);
  }));
};
