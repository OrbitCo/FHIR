import { TestBed } from '@angular/core/testing';

import { DashboardCommonService } from './dashboard-common.service';

describe('DashboardServicesService', () => {
  let service: DashboardCommonService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(DashboardCommonService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
