import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import * as moment from 'moment';
import {Moment} from 'moment';
import {IDatePickerConfig} from 'ng2-date-picker';
import {isUndefined} from 'util';

const selectedDayFormat = 'dd. DD-MM-YYYY';
const selectedMonthFormat = 'MMMM YYYY';

@Component({
  selector: 'home-date-navigator',
  templateUrl: './date-navigator.component.html',
  styleUrls: ['./date-navigator.component.scss']
})
export class DateNavigatorComponent implements OnInit {

  @Input()
  public mode: string;

  @Input()
  set selectedDate(selectedDate: Moment) {
    if (!isUndefined(selectedDate)) {
      this._selectedDate = selectedDate;
      this.dayPickerModel = this._selectedDate.format(selectedDayFormat);
      this.monthPickerModel = this._selectedDate.format(selectedMonthFormat);
      this.yearPickerModel = this._selectedDate.year();
    }
  }

  @Output()
  public navigation = new EventEmitter<Moment>();

  private _selectedDate: Moment;

  public previouslySelectedDate: Moment;

  public monthPickerConfiguration: IDatePickerConfig;
  public monthPickerModel: String;

  public dayPickerConfiguration: IDatePickerConfig;
  public dayPickerModel: String;

  public yearPickerModel: number;

  constructor() { }

  public ngOnInit(): void {
    this.initDatePickers();
  }

  private initDatePickers() {
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
    if (!isUndefined(selectedDate) && !isUndefined(this.previouslySelectedDate)
          && !selectedDate.isSame(this.previouslySelectedDate)) {
      this.navigation.emit(selectedDate);
    }
    this.previouslySelectedDate = selectedDate;
  }

  public isUpNavigationDisabled(): boolean {
    if (isUndefined(this._selectedDate)) {
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
      this.selectedDate = moment(`${this.yearPickerModel + amount}-${this._selectedDate.format('MM')}-${this._selectedDate.format('DD')}`);

      // Since year mode is not backed by a datepicker, we'll have to trigger the navigation event
      this.navigation.emit(this._selectedDate.clone());
    }
  }
}
