export class Klimaat {
  dateTime: Date;
  temperatuur: number;
  luchtvochtigheid: number;
  temperatuurTrend: Trend;

  constructor(json: string) {
    let jsonObject: any = JSON.parse(json);
    for (let property in jsonObject) {
      this[property] = jsonObject[property];
    }
  }

}
