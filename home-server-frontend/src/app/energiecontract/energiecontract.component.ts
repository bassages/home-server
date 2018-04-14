import {Component, OnInit} from '@angular/core';
import {EnergieContract} from "./energiecontract";
import {LoadingIndicatorService} from "../loading-indicator/loading-indicator.service";
import {ErrorHandingService} from "../error-handling/error-handing.service";
import {EnergiecontractService} from "./energiecontract.service";
import * as _ from "lodash";

@Component({
  selector: 'energiecontract',
  templateUrl: './energiecontract.component.html',
  styleUrls: ['./energiecontract.component.scss']
})
export class EnergiecontractComponent implements OnInit {

  public energiecontracten: EnergieContract[];

  constructor(private energiecontractService: EnergiecontractService,
              private loadingIndicatorService: LoadingIndicatorService,
              private errorHandlingService: ErrorHandingService) { }

  public ngOnInit(): void {
    setTimeout(() => { this.getEnergieContracten(); },0);
  }

  private getEnergieContracten() {
    this.loadingIndicatorService.open();

    this.energiecontractService.getAll().subscribe(
      response => this.energiecontracten = _.sortBy<EnergieContract>(response, ['validFrom']),
      error => this.errorHandlingService.handleError("De energiecontracten konden nu niet worden opgehaald", error),
      () => this.loadingIndicatorService.close()
    );
  }

  public startAdd(): void {

  }

  public startEdit(energiecontract: EnergieContract): void {

  }

  public cancelEdit(): void {

  }

}
