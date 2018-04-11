import {Moment} from "moment";

export class Klimaat {
  dateTime: Moment;

  temperatuur: number;
  luchtvochtigheid: number;

  temperatuurTrend: string;
  luchtvochtigheidTrend: string;
}
