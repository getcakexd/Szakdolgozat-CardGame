import { Component } from '@angular/core';
import { UserService, User } from '../../services/user.service';
import {FormsModule} from '@angular/forms';

@Component({
  selector: 'app-user-create',
  templateUrl: './user-create.component.html',
  standalone: true,
  imports: [
    FormsModule
  ],
  styleUrls: ['./user-create.component.css']
})
export class UserCreateComponent {
  newUser: User = { username: '', password: '' };
  message: string = '';

  constructor(private userService: UserService) {}

  createUser(): void {
    this.userService.createUser(this.newUser).subscribe((response) => {
      this.message = response.message;
    });
  }
}
