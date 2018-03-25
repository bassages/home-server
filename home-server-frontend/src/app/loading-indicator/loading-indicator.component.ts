import {Component, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {NgbModal, NgbModalRef} from "@ng-bootstrap/ng-bootstrap";
import {LoadingIndicatorService} from "./loading-indicator.service";

@Component({
  selector: 'loading-indicator',
  templateUrl: './loading-indicator.component.html',
  styleUrls: ['./loading-indicator.component.scss']
})
export class LoadingIndicatorComponent implements OnInit {

  @ViewChild('loadingIndicatorTemplate')
  private loaderTemplate: TemplateRef<any>;

  modal: NgbModalRef;

  constructor(private modalService: NgbModal,
              private loadingIndicatorService: LoadingIndicatorService) { }

  ngOnInit() {
    this.loadingIndicatorService.onOpen().subscribe(() => this.open());
    this.loadingIndicatorService.onClose().subscribe(() => this.close());
  }

  open() {
    this.modal = this.modalService.open(this.loaderTemplate, { backdrop: 'static' });
  }

  close() {
    if (this.modal) {
      this.modal.close();
    }
  }
}
