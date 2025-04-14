import { Component, OnInit } from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { User } from '../../models/user.model';
import {LobbyService} from '../../services/lobby/lobby.service';
import {AuthService} from '../../services/auth/auth.service';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';
import {MatIcon} from '@angular/material/icon';
import {MatButton} from '@angular/material/button';
import {MatError, MatFormField, MatLabel} from '@angular/material/form-field';
import {NgIf} from '@angular/common';
import {MatInput} from '@angular/material/input';

@Component({
  selector: 'app-lobby-join',
  templateUrl: './lobby-join.component.html',
  standalone: true,
  imports: [
    MatIcon,
    TranslatePipe,
    MatButton,
    MatError,
    NgIf,
    ReactiveFormsModule,
    MatLabel,
    MatFormField,
    MatInput
  ],
  styleUrls: ['./lobby-join.component.css']
})
export class LobbyJoinComponent implements OnInit {
  joinForm: FormGroup;
  currentUser: User | null = null;
  isSubmitting = false;

  constructor(
    private fb: FormBuilder,
    private lobbyService: LobbyService,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar,
    private translate: TranslateService
  ) {
    this.joinForm = this.fb.group({
      lobbyCode: ['', [
        Validators.required,
        Validators.minLength(6),
        Validators.maxLength(6),
        Validators.pattern('^[A-Za-z0-9]+$')
      ]]
    });
  }

  ngOnInit(): void {
    this.currentUser = this.authService.currentUser;
    if (!this.currentUser) {
      this.snackBar.open(
        this.translate.instant('ERRORS.LOGIN_REQUIRED'),
        this.translate.instant('COMMON.CLOSE'),
        { duration: 3000 }
      );
      this.router.navigate(['/login']);
      return;
    }
  }

  onSubmit(): void {
    if (this.joinForm.invalid || !this.currentUser) {
      return;
    }

    const lobbyCode = this.joinForm.get('lobbyCode')?.value.toUpperCase();

    this.isSubmitting = true;
    this.lobbyService.joinLobby(lobbyCode, this.currentUser.id).subscribe({
      next: (lobby) => {
        this.isSubmitting = false;
        this.snackBar.open(
          this.translate.instant('LOBBY.JOIN.SUCCESS'),
          this.translate.instant('COMMON.CLOSE'),
          { duration: 3000 }
        );
        this.router.navigate(['/lobby', lobby.id]);
      },
      error: (error) => {
        this.isSubmitting = false;
        this.snackBar.open(
          error.error.message || this.translate.instant('LOBBY.JOIN.FAILED'),
          this.translate.instant('COMMON.CLOSE'),
          { duration: 3000 }
        );
      }
    });
  }
}
