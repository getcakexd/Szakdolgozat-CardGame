import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EditGameDialogComponent } from './edit-game-dialog.component';

describe('EditGameDialogComponent', () => {
  let component: EditGameDialogComponent;
  let fixture: ComponentFixture<EditGameDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EditGameDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EditGameDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
