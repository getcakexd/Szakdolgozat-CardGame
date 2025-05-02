import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LobbyChatComponent } from './lobby-chat.component';

describe('LobbyChatComponent', () => {
  let component: LobbyChatComponent;
  let fixture: ComponentFixture<LobbyChatComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LobbyChatComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LobbyChatComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
