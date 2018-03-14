export class Meterstand {
  dateTime: Date;
  stroomTariefIndicator: string;
  stroomTarief1: number;
  stroomTarief2: number;
  gas: number;

  constructor(json: string) {
    let jsonObject: any = JSON.parse(json);
    for (let property in jsonObject) {
      this[property] = jsonObject[property];
    }
  }
}
