import {Component, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {NgbModal, NgbModalRef} from "@ng-bootstrap/ng-bootstrap";
import {LoaderService} from "./loader.service";

@Component({
  selector: 'loader',
  templateUrl: './loader.component.html',
  styleUrls: ['./loader.component.scss']
})
export class LoaderComponent implements OnInit {

  @ViewChild('loaderTemplate')
  private loaderTemplate: TemplateRef<any>;

  modal: NgbModalRef;

  constructor(private modalService: NgbModal, private loaderService: LoaderService) { }

  ngOnInit() {
    this.loaderService.onOpen().subscribe(() => this.open());
    this.loaderService.onClose().subscribe(() => this.close());
  }

  open() {
    this.modal = this.modalService.open(this.loaderTemplate);
  }

  close() {
    if (this.modal) {
      this.modal.close();
    }
  }
}
