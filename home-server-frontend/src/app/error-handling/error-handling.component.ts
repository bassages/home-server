import {Component, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {NgbModal, NgbModalRef} from '@ng-bootstrap/ng-bootstrap';
import {ErrorHandingService} from './error-handing.service';
import {Error} from './error';

@Component({
  selector: 'home-error-handler',
  templateUrl: './error-handling.component.html',
  styleUrls: ['./error-handling.component.scss']
})
export class ErrorHandlingComponent implements OnInit {

  @ViewChild('errorDialogTemplate')
  private errorDialogTemplate: TemplateRef<any>;

  modal: NgbModalRef;
  message: String;

  constructor(private modalService: NgbModal,
              private errorHandlingService: ErrorHandingService) {
  }

  ngOnInit() {
    this.errorHandlingService.onError()
                             .subscribe((error: Error) => this.handleError(error));
  }

  handleError(error: Error) {
    this.message = error.message;
    this.openErrorDialog();
  }

  openErrorDialog() {
    this.modal = this.modalService.open(this.errorDialogTemplate);
  }

  close() {
    if (this.modal) {
      this.modal.close();
    }
  }
}
