import {Component, EventEmitter, Output} from '@angular/core';
import { UserService, User } from '../../services/user/user.service';
import {FormsModule} from '@angular/forms';
import {Router} from '@angular/router';
import {NgIf} from '@angular/common';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  standalone: true,
  imports: [
    FormsModule,
    NgIf
  ],
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  user: User = {username: '', password: '', email: '' };
  message: string = '';

  constructor(private userService: UserService, private router: Router) {}

  login(): void {
    const user: User = {
      username: this.user.username,
      password: this.user.password,
      email: this.user.email,
    };

    this.userService.login(user).subscribe(
      (response) => {
        if (response.status === 'ok') {
          this.router.navigate(['/home']).then( () => {
            window.location.reload();
          });
        } else {
          this.message = 'Invalid username or password';
        }

      }
    );
  }

  private resetForm(): void {
    this.user = {username: '', email: '', password: ''};
  }

}
