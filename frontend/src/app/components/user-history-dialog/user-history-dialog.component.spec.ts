import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UserHistoryDialogComponent } from './user-history-dialog.component';

describe('UserHistoryDialogComponent', () => {
  let component: UserHistoryDialogComponent;
  let fixture: ComponentFixture<UserHistoryDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserHistoryDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UserHistoryDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
