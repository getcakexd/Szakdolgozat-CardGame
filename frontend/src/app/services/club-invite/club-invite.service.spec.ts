import { TestBed } from '@angular/core/testing';

import { ClubInviteService } from './club-invite.service';

describe('ClubInviteService', () => {
  let service: ClubInviteService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ClubInviteService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
