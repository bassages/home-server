import {Component, OnInit} from '@angular/core';
import {KlimaatService} from '../klimaat.service';
import {ErrorHandingService} from '../../error-handling/error-handing.service';
import {LoadingIndicatorService} from '../../loading-indicator/loading-indicator.service';
import {KlimaatSensor} from '../klimaatSensor';
import sortBy from 'lodash/sortBy';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {KlimaatSensorService} from '../klimaatsensor.service';

@Component({
  selector: 'home-klimaat-sensors',
  templateUrl: './klimaat-sensors.component.html',
  styleUrls: ['./klimaat-sensors.component.scss']
})
export class KlimaatSensorsComponent implements OnInit {

  public sensors: KlimaatSensor[];

  public form: FormGroup;

  public editMode = false;
  public selectedSensor: KlimaatSensor;

  constructor(private klimaatService: KlimaatService,
              private klimaatSensorService: KlimaatSensorService,
              private loadingIndicatorService: LoadingIndicatorService,
              private errorHandlingService: ErrorHandingService,
              private modalService: NgbModal) {
  }

  public ngOnInit(): void {
    this.createForm();
    setTimeout(() => this.getKlimaatSensors());
  }

  private createForm(): void {
    this.form = new FormGroup({
      code: new FormControl(''),
      omschrijving: new FormControl('', [Validators.maxLength(255)]),
    });
  }

  private getKlimaatSensors(): void {
    this.loadingIndicatorService.open();

    this.klimaatSensorService.list().subscribe(
      response => {
        this.sensors = sortBy<KlimaatSensor>(response, ['code']);
      },
      error => this.errorHandlingService.handleError('De klimaat sensors konden nu niet worden opgehaald', error),
      () => this.loadingIndicatorService.close()
    );
  }

  get code(): FormControl {
    return this.form.get('code') as FormControl;
  }

  get omschrijving(): FormControl {
    return this.form.get('omschrijving') as FormControl;
  }

  public startEdit(klimaatSensor: KlimaatSensor) {
    this.editMode = true;
    this.selectedSensor = klimaatSensor;

    this.code.setValue(klimaatSensor.code);
    this.omschrijving.setValue(klimaatSensor.omschrijving);
  }

  public save(): void {
    this.loadingIndicatorService.open();

    const sensorToSave: KlimaatSensor = new KlimaatSensor();
    sensorToSave.code = this.code.value;
    sensorToSave.omschrijving = this.omschrijving.value;

    this.klimaatSensorService.update(sensorToSave).subscribe(
      savedKlimaatSensor => {
        this.selectedSensor.omschrijving = savedKlimaatSensor.omschrijving;
        this.editMode = false;
        this.selectedSensor = null;
      },
      error => {
        this.errorHandlingService.handleError('De wijzingen konden nu niet worden opgeslagen', error);
      },
      () => this.loadingIndicatorService.close()
    );
  }

  public delete() {
    this.loadingIndicatorService.open();

    this.klimaatSensorService.delete(this.selectedSensor).subscribe(
      () => {
        const index = this.sensors.indexOf(this.selectedSensor);
        this.sensors.splice(index, 1);
        this.editMode = false;
      },
      error => this.errorHandlingService.handleError('De klimaatsensor kon niet worden verwijderd', error),
      () => this.loadingIndicatorService.close()
    );
  }

  public cancelEdit() {
    this.editMode = null;
    this.selectedSensor = null;
  }

  public openDeletionConformationDialog(deletionConformationDialogTemplate) {
    this.modalService.open(deletionConformationDialogTemplate).result.then(
    (result) => this.delete(),
    (reason) => console.info('Cancel deletion'));
  }
}
