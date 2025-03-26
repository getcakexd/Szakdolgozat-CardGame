import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ClubChatComponent } from './club-chat.component';

describe('ClubChatComponent', () => {
  let component: ClubChatComponent;
  let fixture: ComponentFixture<ClubChatComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ClubChatComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ClubChatComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
