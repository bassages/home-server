export enum LedState {
  ON,
  OFF
}

export class Led {
  constructor(public state: LedState) { }
}
