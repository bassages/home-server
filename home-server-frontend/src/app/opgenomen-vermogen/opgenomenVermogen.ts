export class OpgenomenVermogen {
  datumtijd: Date;
  watt: number;
  tariefIndicator: string;

  constructor(json: string) {
    const jsonObject: any = JSON.parse(json);
    for (const property in jsonObject) {
      this[property] = jsonObject[property];
    }
  }
}
