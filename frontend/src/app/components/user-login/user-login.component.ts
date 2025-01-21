import { Component } from '@angular/core';
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

  login(): void {
    this.userService.login(this.user).subscribe((response) => {
      this.message = response.status === 'ok' ? 'Login successful' : 'Login failed';
    });
  }
}
