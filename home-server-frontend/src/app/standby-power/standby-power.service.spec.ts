import {inject, TestBed} from '@angular/core/testing';
import {StandbyPowerService} from './standby-power.service';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {StandbyPowerInPeriod} from './standby-power-in-period';

describe('StandbyPowerService', () => {

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [StandbyPowerService],
      imports: [HttpClientTestingModule],
    });
  });

  afterEach(inject([HttpTestingController], (httpMock: HttpTestingController) => {
    httpMock.verify();
  }));

  it('should get standbypower for a given year by getting it from the backend api',
    inject([HttpTestingController, StandbyPowerService],
      (httpMock: HttpTestingController, service: StandbyPowerService) => {

        const year = 2019;
        // Call the service
        service.get(year).subscribe(data => {
          expect(data.length).toBe(2);
        });

        // Set the expectations for the HttpClient mock
        const req = httpMock.expectOne( `/api/standby-power/${year}`);
        expect(req.request.method).toEqual('GET');

        // Set the fake data to be returned by the mock
        const standbyPowerInPeriod1: StandbyPowerInPeriod = new StandbyPowerInPeriod();
        const standbyPowerInPeriod2: StandbyPowerInPeriod = new StandbyPowerInPeriod();
        const result = [standbyPowerInPeriod1, standbyPowerInPeriod2];
        req.flush(result);
      })
  );
});
