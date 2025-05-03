import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ClubLeaderboardComponent } from './club-leaderboard.component';

describe('ClubLeaderboardComponent', () => {
  let component: ClubLeaderboardComponent;
  let fixture: ComponentFixture<ClubLeaderboardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ClubLeaderboardComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ClubLeaderboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
