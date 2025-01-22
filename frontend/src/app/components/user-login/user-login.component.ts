import {Component, EventEmitter, Output} from '@angular/core';
import { UserService, User } from '../../services/user.service';
import {FormsModule} from '@angular/forms';

@Component({
  selector: 'app-user-login',
  templateUrl: './user-login.component.html',
  standalone: true,
  imports: [
    FormsModule
  ],
  styleUrls: ['./user-login.component.css']
})
export class UserLoginComponent {
  user: User = { username: '', password: '' };
  message: string = '';

  constructor(private userService: UserService) {}

  @Output() loginSuccess = new EventEmitter<string>();

  login(): void {
    this.userService.login(this.user).subscribe({
      next: (response) => {
        if (response.status === 'ok') {
          this.loginSuccess.emit(this.user.username);
        } else {
          console.error('Login failed');
        }
      },
    });
  }
}
