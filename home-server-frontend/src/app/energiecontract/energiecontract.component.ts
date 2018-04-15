import {Component, OnInit, ViewChild} from '@angular/core';
import {Energiecontract} from "./energiecontract";
import {LoadingIndicatorService} from "../loading-indicator/loading-indicator.service";
import {ErrorHandingService} from "../error-handling/error-handing.service";
import {EnergiecontractService} from "./energiecontract.service";
import * as _ from "lodash";
import {FormControl, FormGroup, Validators} from "@angular/forms";
import * as moment from "moment";
import {Moment} from "moment";
import {DatePickerDirective, IDatePickerConfig} from "ng2-date-picker";
import {DecimalPipe} from "@angular/common";
import {isNullOrUndefined} from "util";

const datePickerFormat: string = 'dddd DD-MM-YYYY';
const pricePattern = /^\d(,\d{1,6})*$/;

@Component({
  selector: 'energiecontract',
  templateUrl: './energiecontract.component.html',
  styleUrls: ['./energiecontract.component.scss']
})
export class EnergiecontractComponent implements OnInit {
  @ViewChild('datePicker') datePicker: DatePickerDirective;

  public energiecontracten: Energiecontract[];

  public form: FormGroup;

  public selectedDate: Moment;
  public datePickerConfiguration: IDatePickerConfig;
  public datePickerModel: String;

  public editMode: boolean = false;

  public selectedEnergiecontract: Energiecontract;

  constructor(private energiecontractService: EnergiecontractService,
              private loadingIndicatorService: LoadingIndicatorService,
              private errorHandlingService: ErrorHandingService,
              private decimalPipe: DecimalPipe) {
  }

  public ngOnInit(): void {
    this.datePickerConfiguration = {
      format: datePickerFormat,
      max: moment()
    };
    this.createForm();
    setTimeout(() => { this.getEnergieContracten(); },0);
  }

  private createForm(): void {
    this.form = new FormGroup({
      leverancier: new FormControl('',[Validators.required, Validators.maxLength(255)]),
      gas: new FormControl('', [Validators.required, Validators.pattern(pricePattern)]),
      stroomNormaalTarief: new FormControl('',[Validators.required, Validators.pattern(pricePattern)]),
      stroomDalTarief: new FormControl('', Validators.pattern(pricePattern)),
    });
  }

  private getEnergieContracten(): void {
    this.loadingIndicatorService.open();

    this.energiecontractService.getAll().subscribe(
      response => this.energiecontracten = this.sort(response),
      error => this.errorHandlingService.handleError("De energiecontracten konden nu niet worden opgehaald", error),
      () => this.loadingIndicatorService.close()
    );
  }

  private sort(energiecontracten: Energiecontract[]): Energiecontract[] {
    return _.sortBy<Energiecontract>(energiecontracten, ['validFrom']);
  }

  get leverancier(): FormControl {
    return this.form.get('leverancier') as FormControl;
  }

  get gas(): FormControl {
    return this.form.get('gas') as FormControl;
  }

  get stroomNormaalTarief(): FormControl {
    return this.form.get('stroomNormaalTarief') as FormControl;
  }

  get stroomDalTarief(): FormControl {
    return this.form.get('stroomDalTarief') as FormControl;
  }

  public startAdd(): void {
    this.editMode = true;
    this.selectedEnergiecontract = null;

    this.leverancier.setValue('');
    this.gas.setValue('');
    this.stroomNormaalTarief.setValue('');
    this.stroomDalTarief.setValue('');

    this.selectedDate = moment();
    this.datePickerModel = this.selectedDate.format(datePickerFormat);
  }

  public startEdit(energiecontract: Energiecontract): void {
    this.editMode = true;
    this.selectedEnergiecontract = energiecontract;

    this.leverancier.setValue(energiecontract.leverancier);
    this.gas.setValue(this.formatPrice(energiecontract.gasPerKuub));
    this.stroomNormaalTarief.setValue(this.formatPrice(energiecontract.stroomPerKwhNormaalTarief));
    this.stroomDalTarief.setValue(this.formatPrice(energiecontract.stroomPerKwhDalTarief));

    this.selectedDate = energiecontract.validFrom;
    this.datePickerModel = energiecontract.validFrom.format(datePickerFormat);
  }

  private formatPrice(price: number): string {
    return this.decimalPipe.transform(price, '1.6-6')
  }

  public cancelEdit(): void {
    this.editMode = null;
    this.selectedEnergiecontract = null;
  }

  public datePickerChanged(selectedDate: Moment): void {
    this.selectedDate = selectedDate;
  }

  public save(): void {
    this.loadingIndicatorService.open();

    const energiecontract: Energiecontract = this.selectedEnergiecontract ? this.selectedEnergiecontract : new Energiecontract();
    energiecontract.validFrom = this.selectedDate;
    energiecontract.leverancier = this.leverancier.value;

    energiecontract.gasPerKuub = this.toFloat(this.gas.value);
    energiecontract.stroomPerKwhNormaalTarief = this.toFloat(this.stroomNormaalTarief.value);
    energiecontract.stroomPerKwhDalTarief = this.toFloat(this.stroomDalTarief.value);

    this.energiecontractService.save(energiecontract).subscribe(
      energiecontract => {
        if (this.selectedEnergiecontract) {
          this.selectedEnergiecontract.id = energiecontract.id
        } else {
         this.energiecontracten.push(energiecontract);
         this.sort(this.energiecontracten);
        }
        this.editMode = false;
        this.selectedEnergiecontract = null;
      },
      error => {
        this.errorHandlingService.handleError("Het energiecontract kon nu niet worden opgeslagen", error);
      },
      () => this.loadingIndicatorService.close()
    );
  }

  public delete(): void {
    this.loadingIndicatorService.open();
    this.energiecontractService.delete(this.selectedEnergiecontract.id).subscribe(
      () => {
        const index = this.energiecontracten.indexOf(this.selectedEnergiecontract);
        this.energiecontracten.splice(index, 1);
        this.editMode = false;
      },
      error => this.errorHandlingService.handleError("Het energiecontract kon niet worden verwijderd", error),
      () => this.loadingIndicatorService.close()
    );
  }

  public openDatePicker(): void {
    this.datePicker.api.open();
  }

  private toFloat(value: string): number {
    if (isNullOrUndefined(value)) {
      return null;
    }
    let parsed: number = parseFloat(value.replace(',', '.'));
    if (isNaN(parsed)) {
      return null;
    }
    return parsed;
  }
}
