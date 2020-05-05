import {Component, EventEmitter, Input, OnInit, Output, QueryList, ViewChild, ViewChildren} from '@angular/core';
import * as moment from 'moment';
import {Moment} from 'moment';
import {DatePickerDirective, IDatePickerDirectiveConfig} from 'ng2-date-picker';
import {FormControl, FormGroup, Validators} from '@angular/forms';

const selectedDayFormat = 'dd. DD-MM-YYYY';
const selectedMonthFormat = 'MMMM YYYY';

@Component({
  selector: 'home-date-navigator',
  templateUrl: './date-navigator.component.html',
  styleUrls: ['./date-navigator.component.scss']
})
export class DateNavigatorComponent {

  @Input()
  public mode: string;

  @Input()
  public responsiveSize = false;

  @Input()
  set selectedDate(selectedDate: Moment) {
    if (selectedDate !== undefined) {
      this._selectedDate = selectedDate;
      this.selectedDay.setValue(selectedDate);
      this.selectedMonth.setValue(selectedDate);
      this.selectedYear.setValue(selectedDate.year());
    }
  }

  @Output()
  public navigation = new EventEmitter<Moment>();

  @ViewChildren('picker')
  public pickers: QueryList<DatePickerDirective>;

  public form: FormGroup;

  private _selectedDate: Moment;

  public previouslySelectedDate: Moment;

  public monthPickerConfiguration: IDatePickerDirectiveConfig;
  public dayPickerConfiguration: IDatePickerDirectiveConfig;

  public yearPickerFormattedValue: number;

  constructor() {
    this.initDatePickerConfigurations();
    this.createForm();
  }

  private createForm(): void {
    this.form = new FormGroup({
      selectedDay: new FormControl({value: this._selectedDate}, [Validators.required]),
      selectedMonth: new FormControl({value: this._selectedDate}, [Validators.required]),
      selectedYear: new FormControl('', [Validators.required])
    });
  }

  private initDatePickerConfigurations() {
    this.dayPickerConfiguration = {
      format: selectedDayFormat,
      max: moment()
    };
    this.monthPickerConfiguration = {
      format: selectedMonthFormat,
      max: moment()
    };
  }

  public datePickerChanged(selectedDate: Moment): void {
    if (selectedDate !== undefined && this.previouslySelectedDate !== undefined
      && !selectedDate.isSame(this.previouslySelectedDate)) {
      this.pickers.forEach((item, index, array) => {
        item.elemRef.nativeElement.blur();
        item.api.close();
      });
      this.navigation.emit(selectedDate);
    }
    this.previouslySelectedDate = selectedDate;
  }

  get selectedDay(): FormControl {
    return this.form.get('selectedDay') as FormControl;
  }

  get selectedMonth(): FormControl {
    return this.form.get('selectedMonth') as FormControl;
  }

  get selectedYear(): FormControl {
    return this.form.get('selectedYear') as FormControl;
  }

  public isUpNavigationDisabled(): boolean {
    if (this._selectedDate === undefined) {
      return true;
    }

    const now: Moment = moment();
    if (this.mode === 'day') {
      return now.date() === this._selectedDate.date()
        && now.month() === this._selectedDate.month()
        && now.year() === this._selectedDate.year();
    } else if (this.mode === 'month') {
      return now.month() === this._selectedDate.month() && now.year() === this._selectedDate.year();
    } else if (this.mode === 'year') {
      return now.year() === this._selectedDate.year();
    }
  }

  public navigate(amount: number): void {
    if (this.mode === 'day') {
      this.selectedDate = this._selectedDate.clone().add(amount, 'days');

    } else if (this.mode === 'month') {
      this.selectedDate = this._selectedDate.clone().add(amount, 'months');

    } else if (this.mode === 'year') {
      this.selectedDate = moment(
        `${this.selectedYear.value + amount}-${this._selectedDate.format('MM')}-${this._selectedDate.format('DD')}`);

      // Since year mode is not backed by a datepicker, we'll have to trigger the navigation event
      this.navigation.emit(this._selectedDate.clone());
    }
  }
}
