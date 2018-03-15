import {TariefIndicator} from "../opgenomen-vermogen/tariefIndicator";

export class Meterstand {
  dateTime: Date;
  stroomTariefIndicator: TariefIndicator;
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
