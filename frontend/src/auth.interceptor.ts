import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const username = 'root';
  const password = 'example';
  const authHeader = `Basic ${btoa(`${username}:${password}`)}`;

  const clonedRequest = req.clone({
    setHeaders: {
      Authorization: authHeader,
    },
  });

  return next(clonedRequest); // Pass the modified request to the next handler
};
