import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Energiecontract} from './energiecontract';
import * as moment from 'moment';
import {map} from 'rxjs/operators';

class BackendEnergiecontract {
  id: number;
  validFrom: string;
  validTo: string;
  stroomPerKwhNormaalTarief: number;
  stroomPerKwhDalTarief: number;
  gasPerKuub: number;
  leverancier: string;
  remark: string;
}

@Injectable()
export class EnergiecontractService {

static toEnergieContract(backendEnergieContract: BackendEnergiecontract): Energiecontract {
    const energiecontract: Energiecontract = new Energiecontract();
    energiecontract.id = backendEnergieContract.id;
    energiecontract.leverancier = backendEnergieContract.leverancier;
    energiecontract.remark = backendEnergieContract.remark;
    energiecontract.stroomPerKwhDalTarief = backendEnergieContract.stroomPerKwhDalTarief;
    energiecontract.stroomPerKwhNormaalTarief = backendEnergieContract.stroomPerKwhNormaalTarief;
    energiecontract.gasPerKuub = backendEnergieContract.gasPerKuub;
    energiecontract.validFrom = moment(backendEnergieContract.validFrom, 'YYYY-MM-DD');
    energiecontract.validTo = moment(backendEnergieContract.validTo, 'YYYY-MM-DD');
    return energiecontract;
  }

  private static allToEnergieContract(backendEnegiecontracten: BackendEnergiecontract[]): Energiecontract[] {
    return backendEnegiecontracten.map(EnergiecontractService.toEnergieContract);
  }

  private static toBackendEnergieContract(energieContract: Energiecontract) {
    const backendEnergiecontract: BackendEnergiecontract = new BackendEnergiecontract();
    backendEnergiecontract.id = energieContract.id;
    backendEnergiecontract.validFrom = energieContract.validFrom.format('YYYY-MM-DD');
    backendEnergiecontract.leverancier = energieContract.leverancier;
    backendEnergiecontract.remark = energieContract.remark;
    backendEnergiecontract.gasPerKuub = energieContract.gasPerKuub;
    backendEnergiecontract.stroomPerKwhNormaalTarief = energieContract.stroomPerKwhNormaalTarief;
    backendEnergiecontract.stroomPerKwhDalTarief = energieContract.stroomPerKwhDalTarief;
    return backendEnergiecontract;
  }

  constructor(private http: HttpClient) { }

  public getAll(): Observable<Energiecontract[]> {
    return this.http.get<BackendEnergiecontract[]>('/api/energiecontract')
                    .pipe(map(EnergiecontractService.allToEnergieContract));
  }

  public delete(id: number): Observable<Object> {
    return this.http.delete(`/api/energiecontract/${id}`);
  }

  public save(energieContract: Energiecontract): Observable<Energiecontract> {
    return this.http.post<BackendEnergiecontract>('/api/energiecontract', EnergiecontractService.toBackendEnergieContract(energieContract))
      .pipe(map(EnergiecontractService.toEnergieContract));
  }
}
