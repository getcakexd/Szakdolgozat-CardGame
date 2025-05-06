import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ClubService } from '../../services/club/club.service';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar } from '@angular/material/snack-bar';
import { NgIf } from '@angular/common';
import { AuthService } from '../../services/auth/auth.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import {MatIcon} from '@angular/material/icon';

@Component({
  selector: 'app-club-create',
  templateUrl: './club-create.component.html',
  standalone: true,
  imports: [
    FormsModule,
    MatInputModule,
    MatFormFieldModule,
    MatCheckboxModule,
    MatButtonModule,
    MatCardModule,
    MatIcon,
    NgIf,
    TranslateModule
  ],
  styleUrls: ['./club-create.component.scss']
})
export class ClubCreateComponent {
  name: string = '';
  description: string = '';
  isPublic: boolean = true;
  userId: number = 0;
  message: string = '';
  isSubmitting: boolean = false;

  constructor(
    private authService: AuthService,
    private clubService: ClubService,
    private snackBar: MatSnackBar,
    private translate: TranslateService
  ) {
    this.userId = this.authService.getCurrentUserId() || 0;
  }

  createClub() {
    if (!this.name.trim()) {
      this.snackBar.open(
        this.translate.instant('CLUB_CREATE.NAME_REQUIRED'),
        this.translate.instant('COMMON.CLOSE'),
        { duration: 3000 }
      );
      return;
    }

    this.isSubmitting = true;
    this.clubService.createClub(this.name, this.description, this.isPublic, this.userId)
      .subscribe({
        next: (response: any) => {
          this.snackBar.open(
            this.translate.instant('CLUB_CREATE.SUCCESS'),
            this.translate.instant('COMMON.CLOSE'),
            { duration: 3000 }
          );
          this.resetForm();
          window.location.reload();
        },
        error: (err: any) => {
          this.message = this.translate.instant('CLUB_CREATE.ERROR');
          this.snackBar.open(
            this.message,
            this.translate.instant('COMMON.CLOSE'),
            { duration: 3000 }
          );
          this.isSubmitting = false;
        }
      });
  }

  resetForm() {
    this.name = '';
    this.description = '';
    this.isPublic = true;
    this.isSubmitting = false;
  }
}
