import {VerbruikKostenOverzicht} from "./verbruikKostenOverzicht";

export class VerbruikOpDag extends VerbruikKostenOverzicht {
  dag: Date;

  constructor(json: string) {
    super();

    let jsonObject: any = JSON.parse(json);
    for (let property in jsonObject) {
      this[property] = jsonObject[property];
    }
  }
}
