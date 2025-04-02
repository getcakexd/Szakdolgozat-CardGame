import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LobbyCreateDialogComponent } from './lobby-create-dialog.component';

describe('LobbyCreateDialogComponent', () => {
  let component: LobbyCreateDialogComponent;
  let fixture: ComponentFixture<LobbyCreateDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LobbyCreateDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LobbyCreateDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
