import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ClubCreateComponent } from './club-create.component';

describe('ClubCreateComponent', () => {
  let component: ClubCreateComponent;
  let fixture: ComponentFixture<ClubCreateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ClubCreateComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ClubCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
