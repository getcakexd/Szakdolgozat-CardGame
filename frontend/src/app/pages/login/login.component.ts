import {Component} from '@angular/core';
import { UserService } from '../../services/user/user.service';
import {FormsModule} from '@angular/forms';
import {Router} from '@angular/router';
import {NgIf} from '@angular/common';
import {User} from '../../models/user.model';

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
  user: User = { id: 0, username: '', password: '', email: '', role: '' };
  message: string = '';

  constructor(private userService: UserService, private router: Router) {}

  login(): void {
    const user: User = {
      id: 0,
      username: this.user.username,
      password: this.user.password,
      email: this.user.email,
      role: this.user.role
    };

    this.userService.login(user).subscribe({
      next: () => {
        this.router.navigate(['/home']).then( () => {
          window.location.reload();
        });
      },
      error: () => {
        this.message = "Invalid username or password";
      }
    });
  }

  private resetForm(): void {
    this.user = {id:0, username: '', email: '', password: '', role: ''};
  }

}
