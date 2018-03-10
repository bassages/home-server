import {Component, Input, TemplateRef, ViewChild} from '@angular/core';
import {NgbModal, NgbModalRef} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'loader',
  templateUrl: './loader.component.html',
  styleUrls: ['./loader.component.scss']
})
export class LoaderComponent {

  constructor(private modalService: NgbModal) { }

  modal: NgbModalRef;

  @ViewChild('loaderTemplate')
  private loaderTemplate: TemplateRef<any>;

  @Input()
  set loading(loading) {
    if (loading) {
      setTimeout(() => this.open());
    } else {
      setTimeout(() => this.close());
    }
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
