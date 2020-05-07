import {Component, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {NgbModal, NgbModalRef} from '@ng-bootstrap/ng-bootstrap';
import {LoadingIndicatorService} from './loading-indicator.service';
import isUndefined from 'lodash/isUndefined';

@Component({
  selector: 'home-loading-indicator',
  templateUrl: './loading-indicator.component.html',
  styleUrls: ['./loading-indicator.component.scss']
})
export class LoadingIndicatorComponent implements OnInit {

  @ViewChild('loadingIndicatorTemplate', { static: true })
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
    if (isUndefined(this.modal)) {
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
