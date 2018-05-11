import {Component} from '@angular/core';

@Component({
  selector: 'home-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent {
  public isCollapsed = true;

  constructor() { }

  public collapse() {
    this.isCollapsed = true;
  }
}
