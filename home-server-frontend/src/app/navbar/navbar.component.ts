import {Component, OnDestroy, OnInit} from '@angular/core';
import {AuthService} from '../auth.service';
import {Subscription} from 'rxjs';

@Component({
  selector: 'home-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit, OnDestroy {
  public isCollapsed = true;
  public isVisible = false;

  private authenticated: Subscription;

  constructor(private authenticationService: AuthService) {
  }

  public ngOnInit(): void {
    this.authenticated = this.authenticationService.authenticatedSubject
      .subscribe(authenticated => this.isVisible = authenticated);
  }

  public ngOnDestroy(): void {
    this.authenticated.unsubscribe();
  }

  public collapse() {
    this.isCollapsed = true;
  }

  public logout() {
    this.authenticationService.logout();
  }
}
