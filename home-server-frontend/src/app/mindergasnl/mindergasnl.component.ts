import {Component, OnInit} from '@angular/core';
import {MindergasnlService} from "./mindergasnl.service";
import {LoadingIndicatorService} from "../loading-indicator/loading-indicator.service";
import {ErrorHandingService} from "../error-handling/error-handing.service";
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {AbstractControl} from "@angular/forms/src/model";

const authenticatieTokenMaxLengthValidator = Validators.maxLength(255);

@Component({
  selector: 'mindergasnl',
  templateUrl: './mindergasnl.component.html',
  styleUrls: ['./mindergasnl.component.scss']
})
export class MindergasnlComponent implements OnInit {

  public form: FormGroup;
  public showSavedMessage: boolean = false;

  constructor(private mindergasnlService: MindergasnlService,
              private loadingIndicatorService: LoadingIndicatorService,
              private errorHandlingService: ErrorHandingService) { }

  ngOnInit() {
    this.form  = new FormGroup({
      automatischUploaden: new FormControl(),
      authenticatietoken: new FormControl('', authenticatieTokenMaxLengthValidator)
    });

    this.automatischUploaden.valueChanges.subscribe(value => this.setAuthenticatieTokenValidators(value));

    setTimeout(() => { this.getMinderGasNlSettings(); },0);
  }

  get automatischUploaden(): AbstractControl {
    return this.form.get('automatischUploaden');
  }

  get authenticatietoken(): AbstractControl {
    return this.form.get('authenticatietoken');
  }

  private getMinderGasNlSettings(): void {
    this.loadingIndicatorService.open();
    this.mindergasnlService.get().subscribe(
      minderGasNlSettings => this.form.setValue(minderGasNlSettings),
      error => this.errorHandlingService.handleError("De instellingen voor MinderGas.nl konden nu niet opgehaald worden", error),
      () => this.loadingIndicatorService.close()
    );
  }

  public save(): void {
    if (this.form.valid) {
      this.loadingIndicatorService.open();
      this.mindergasnlService.update(this.form.getRawValue()).subscribe(
        () => this.flashSavedMessage(),
        error => this.errorHandlingService.handleError("De instellingen konden nu niet opgeslagen", error),
        () => this.loadingIndicatorService.close()
      );
    }
  }

  private flashSavedMessage(): void {
    this.showSavedMessage = true;
    setTimeout(() => { this.showSavedMessage = false; },2500);
  }

  private setAuthenticatieTokenValidators(automatischUploaden: boolean): void {
    if (automatischUploaden) {
      this.authenticatietoken.setValidators([authenticatieTokenMaxLengthValidator, Validators.required]);
    } else {
      this.authenticatietoken.setValidators(authenticatieTokenMaxLengthValidator);
    }
    this.authenticatietoken.updateValueAndValidity();
  }
}
