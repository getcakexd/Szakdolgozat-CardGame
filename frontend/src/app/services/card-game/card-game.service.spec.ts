import { TestBed } from '@angular/core/testing';

import { CardGameService } from './card-game.service';

describe('CardGameService', () => {
  let service: CardGameService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CardGameService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
