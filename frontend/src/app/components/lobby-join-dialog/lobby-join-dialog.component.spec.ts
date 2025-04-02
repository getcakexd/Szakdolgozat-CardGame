import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LobbyJoinDialogComponent } from './lobby-join-dialog.component';

describe('LobbyJoinDialogComponent', () => {
  let component: LobbyJoinDialogComponent;
  let fixture: ComponentFixture<LobbyJoinDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LobbyJoinDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LobbyJoinDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
