import {VerbruikKostenOverzicht} from "./verbruikKostenOverzicht";

export class VerbruikInUur extends VerbruikKostenOverzicht {
  uur: number;

  constructor(json: string) {
    super();

    let jsonObject: any = JSON.parse(json);
    for (let property in jsonObject) {
      this[property] = jsonObject[property];
    }
  }
}
