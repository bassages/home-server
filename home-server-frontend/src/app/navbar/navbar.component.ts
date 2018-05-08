import {Component} from '@angular/core';

@Component({
  selector: 'navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent {
  public isCollapsed: boolean = true;

  constructor() { }

  public collapse() {
    this.isCollapsed = true;
  }
}
