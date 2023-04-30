import { Injectable } from '@angular/core';
import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import {catchError, finalize, Observable, tap, throwError} from 'rxjs';
import { AuthService } from 'app/core/auth/auth.service';
import { AuthUtils } from 'app/core/auth/auth.utils';
import { Router, NavigationEnd} from "@angular/router";
import { filter } from 'rxjs/operators';



@Injectable()
export class AuthInterceptor implements HttpInterceptor
{
    /**
     * Constructor
     */
    constructor(private _authService: AuthService,
                private _router: Router)
    {
    }

    /**
     * Intercept
     *
     * @param req
     * @param next
     */
    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>>
    {
        // Clone the request object
        let newReq = req.clone();
        // Request
        //
        // If the access token didn't expire, add the Authorization header.
        // We won't add the Authorization header if the access token expired.
        // This will force the server to return a "401 Unauthorized" response
        // for the protected API routes which our response interceptor will
        // catch and delete the access token from the local storage while logging
        // the user out from the app.
        let redirectUser = false;
        if ( this._authService.accessToken && !AuthUtils.isTokenExpired(this._authService.accessToken) )
        {
            if(this._router.url == "/") {
                console.log("I think you're on the Base URL, so I'm going to redirect you.");
                redirectUser = true;
            } else {
                redirectUser = false;
            }
            newReq = req.clone({
                headers: req.headers.set('Authorization', 'Bearer ' + this._authService.accessToken)
            });
        }
        // Response
        return next.handle(newReq).pipe(
            catchError((error) => {

                // Catch "401 Unauthorized" responses
                if ( error instanceof HttpErrorResponse && error.status === 401 )
                {
                    console.log("401 error caught!  Signing you out...");
                    // Sign out
                    this._authService.signOut();

                    // Reload the app
                    location.reload();
                }

                console.log("I'm throwing an error because I don't recognize it:");
                console.log(error);
                return throwError(error);
            }),
            finalize(() => {
                if(redirectUser) {
                    // Sign in
                    console.log(this._authService.signInUsingToken());

                    console.log("now what?  Let's redirect you...");

                    const redirectURL = '/signed-in-redirect';

                    // Navigate to the redirect url
                    this._router.navigateByUrl(redirectURL);
                }
            })
        );
    }
}
