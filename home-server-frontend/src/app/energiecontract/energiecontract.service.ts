import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs/Observable";
import {EnergieContract} from "./energiecontract";
import * as moment from "moment";

@Injectable()
export class EnergiecontractService {

  constructor(private http: HttpClient) { }

  public getAll(): Observable<EnergieContract[]> {
    return this.http.get<BackendEnergiecontract[]>('/api/energiecontract').map(EnergiecontractService.allToEnergieContract);
  }

  static toEnergieContract(backendEnergieContract: BackendEnergiecontract): EnergieContract {
    const energiecontract: EnergieContract = new EnergieContract();
    energiecontract.id = backendEnergieContract.id;
    energiecontract.leverancier = backendEnergieContract.leverancier;
    energiecontract.stroomPerKwhDalTarief = backendEnergieContract.stroomPerKwhDalTarief;
    energiecontract.stroomPerKwhNormaalTarief = backendEnergieContract.stroomPerKwhNormaalTarief;
    energiecontract.gasPerKuub = backendEnergieContract.gasPerKuub;
    energiecontract.validFrom = moment(backendEnergieContract.validFrom, 'YYYY-MM-DD');
    energiecontract.validTo = moment(backendEnergieContract.validTo, 'YYYY-MM-DD');
    return energiecontract;
  }

  private static allToEnergieContract(backendEnegiecontracten: BackendEnergiecontract[]): EnergieContract[] {
    return backendEnegiecontracten.map(EnergiecontractService.toEnergieContract);
  }
}

class BackendEnergiecontract {
  id: number;
  validFrom: string;
  validTo: string;
  stroomPerKwhNormaalTarief: number;
  stroomPerKwhDalTarief: number;
  gasPerKuub: number;
  leverancier: string;
}
