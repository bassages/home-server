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

  @ViewChild('errorDialogTemplate', { static: true })
  private errorDialogTemplate: TemplateRef<any>;

  public message: String;
  private modal: NgbModalRef;

  constructor(private modalService: NgbModal,
              private errorHandlingService: ErrorHandingService) {
  }

  public ngOnInit(): void {
    this.errorHandlingService.onError()
                             .subscribe((error: Error) => this.handleError(error));
  }

  public handleError(error: Error): void {
    this.message = error.message;
    this.openDialog();
  }

  public openDialog(): void {
    this.modal = this.modalService.open(this.errorDialogTemplate);
  }

  public closeDialog(): void {
    if (this.modal) {
      this.modal.close();
    }
  }
}
