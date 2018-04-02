import {Component, OnInit} from '@angular/core';

@Component({
  selector: 'navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit {
  isCollapsed: boolean = true;

  constructor() { }

  public collapse() {
    this.isCollapsed = true;
  }

  ngOnInit() {
  }
}
