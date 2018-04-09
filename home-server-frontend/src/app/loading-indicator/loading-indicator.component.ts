import {Component, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {NgbModal, NgbModalRef} from "@ng-bootstrap/ng-bootstrap";
import {LoadingIndicatorService} from "./loading-indicator.service";
import * as _ from "lodash";

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
              private loadingIndicatorService: LoadingIndicatorService) {
  }

  public ngOnInit(): void {
    this.loadingIndicatorService.onOpen().subscribe(() => this.open());
    this.loadingIndicatorService.onClose().subscribe(() => this.close());
  }

  public open(): void {
    if (_.isUndefined(this.modal)) {
      this.modal = this.modalService.open(this.loaderTemplate, { backdrop: 'static' });
    }
  }

  public close(): void {
    if (this.modal) {
      this.modal.close();
      this.modal = undefined;
    }
  }
}
