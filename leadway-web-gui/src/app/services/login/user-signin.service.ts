import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';

export const USER_REGISTRATION_ENDPOINT = 'register';
export const USER_SIGNIN_ENDPOINT = 'login';

@Injectable({
  providedIn: 'root'
})
export class UserSigninService {

  constructor(private http: HttpClient) { }

  public registerUser(registrationForm: object): void {
    console.log(registrationForm);

    const requestUrl = `${environment.apiUrl}/${USER_REGISTRATION_ENDPOINT}`;
    this.http.post(
      requestUrl,
      JSON.stringify(registrationForm),
      {
        headers: {
          'Content-Type': 'application/json'
        }
      }
    ).subscribe(
      res => console.log(res),
      error => console.log(error)
    );
  }

  public signInUser(signInForm: object): void {
    console.log(signInForm);

    const requestUrl = `${environment.apiUrl}/${USER_SIGNIN_ENDPOINT}`;
    this.http.post(
      requestUrl,
      JSON.stringify(signInForm),
      {
        headers: {
          'Content-Type': 'application/json'
        }
      }
    ).subscribe(
      res => console.log(res),
      error => console.log(error)
    );
  }
}