import {Moment} from 'moment';

export class Energiecontract {
  id: number;
  validFrom: Moment;
  validTo: Moment;
  stroomPerKwhNormaalTarief: number;
  stroomPerKwhDalTarief: number;
  gasPerKuub: number;
  leverancier: string;
  remark: string;
}
