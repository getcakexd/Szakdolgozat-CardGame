import {Component, EventEmitter, Output} from '@angular/core';
import { UserService, User } from '../../services/user.service';
import {FormsModule} from '@angular/forms';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  standalone: true,
  imports: [
    FormsModule
  ],
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  user: User = { username: '', password: '', email: '' };
  message: string = '';

  constructor(private userService: UserService) {}

  @Output() loginEvent = new EventEmitter<User>();

  login(): void {
    const user: User = {
      username: this.user.username,
      password: this.user.password,
      email: this.user.email
    };

    this.userService.login(user).subscribe(() => {
      this.loginEvent.emit(this.user);
    });
  }
}
