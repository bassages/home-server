import {Klimaat} from './klimaat';
import {Trend} from './trend';

export class RealtimeKlimaat extends Klimaat {
  temperatuurTrend: Trend;
  luchtvochtigheidTrend: Trend;
  sensorCode: string;
}
