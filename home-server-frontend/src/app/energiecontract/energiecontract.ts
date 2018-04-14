import {Moment} from "moment";

export class EnergieContract {
  id: number;
  validFrom: Moment;
  validTo: Moment;
  stroomPerKwhNormaalTarief: number;
  stroomPerKwhDalTarief: number;
  gasPerKuub: number;
  leverancier: string;
}
