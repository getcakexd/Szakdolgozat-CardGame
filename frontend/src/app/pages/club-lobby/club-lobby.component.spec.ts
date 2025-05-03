import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ClubLobbyComponent } from './club-lobby.component';

describe('ClubLobbyComponent', () => {
  let component: ClubLobbyComponent;
  let fixture: ComponentFixture<ClubLobbyComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ClubLobbyComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ClubLobbyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
