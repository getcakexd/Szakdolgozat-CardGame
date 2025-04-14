import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LobbyHomeComponent } from './lobby-home.component';

describe('LobbyHomeComponent', () => {
  let component: LobbyHomeComponent;
  let fixture: ComponentFixture<LobbyHomeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LobbyHomeComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LobbyHomeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
