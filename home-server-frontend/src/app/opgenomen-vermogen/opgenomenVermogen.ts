export class OpgenomenVermogen {
  datumtijd: Date;
  watt: number;
  tariefIndicator: string;

  constructor(json: string) {
    let jsonObject: any = JSON.parse(json);
    for (let property in jsonObject) {
      this[property] = jsonObject[property];
    }
  }
}
