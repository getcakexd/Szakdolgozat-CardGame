import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LobbyDetailComponent } from './lobby-detail.component';

describe('LobbyDetailComponent', () => {
  let component: LobbyDetailComponent;
  let fixture: ComponentFixture<LobbyDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LobbyDetailComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LobbyDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
