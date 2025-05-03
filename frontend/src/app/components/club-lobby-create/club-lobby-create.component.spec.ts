import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ClubLobbyCreateComponent } from './club-lobby-create.component';

describe('ClubLobbyCreateComponent', () => {
  let component: ClubLobbyCreateComponent;
  let fixture: ComponentFixture<ClubLobbyCreateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ClubLobbyCreateComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ClubLobbyCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
