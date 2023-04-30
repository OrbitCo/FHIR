import { TestBed } from '@angular/core/testing';

import { PartnerProfileService } from './partner-profile.service';

describe('PartnerProfileService', () => {
  let service: PartnerProfileService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PartnerProfileService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
