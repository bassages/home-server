export class OpgenomenVermogen {
  datumtijd: Date;
  watt: number;
  tariefIndicator: TariefIndicator;

  constructor(json: string) {
    let jsonObject: any = JSON.parse(json);
    for (let property in jsonObject) {
      this[property] = jsonObject[property];
    }
  }

}
