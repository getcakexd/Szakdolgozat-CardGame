import { TestBed } from '@angular/core/testing';

import { ClubChatService } from './club-chat.service';

describe('ClubChatService', () => {
  let service: ClubChatService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ClubChatService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
