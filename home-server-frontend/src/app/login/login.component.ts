import {Component, OnInit} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Router} from '@angular/router';
import {AuthService} from '../auth.service';

@Component({
  selector: 'home-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

  public credentials = {username: '', password: ''};

  public loginFailed = false;

  public initialized = false;

  constructor(private authenticationService: AuthService,
              private http: HttpClient,
              private router: Router) {
  }

  public ngOnInit(): void {
    this.authenticationService.determineCurrentLoginStatus().subscribe(() => {}, () => {}, () => this.navigateToRootWhenAlreadyLoggedIn());
  }

  private navigateToRootWhenAlreadyLoggedIn() {
    const alreadyLoggedIn = this.authenticationService.authenticatedSubject.getValue();
    if (alreadyLoggedIn) {
      this.navigateToRoot();
    } else {
      this.initialized = true;
    }
  }

  public login() {
    this.authenticationService.authenticate(this.credentials).subscribe(() => {}, () => {}, () =>  this.processAuthenticationStatus());
  }

  private processAuthenticationStatus() {
    if (this.authenticationService.authenticatedSubject.getValue()) {
      this.navigateToRoot();
    } else {
      this.loginFailed = true;
    }
  }

  private navigateToRoot() {
    this.router.navigate(['/dashboard'], {replaceUrl: true});
  }
}
