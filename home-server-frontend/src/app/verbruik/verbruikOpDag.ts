export class VerbruikOpDag {
  dag: Date;
  stroomVerbruikDal: number;
  stroomKostenDal: number;
  stroomVerbruikNormaal: number;
  stroomKostenNormaal: number;
  gasVerbruik: number;
  gasKosten: number;

  constructor(json: string) {
    let jsonObject: any = JSON.parse(json);
    for (let property in jsonObject) {
      this[property] = jsonObject[property];
    }
  }
}
