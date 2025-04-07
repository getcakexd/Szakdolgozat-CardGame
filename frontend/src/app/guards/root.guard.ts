import { Injectable } from "@angular/core"
import { CanActivate, Router } from "@angular/router"
import {AuthService} from '../services/auth/auth.service';
@Injectable({
  providedIn: "root",
})
export class RootGuard implements CanActivate {
  constructor(
    private authService: AuthService,
    private router: Router,
  ) {}

  canActivate(): boolean {
    if (this.authService.isRoot()) {
      return true
    }

    this.router.navigate(["/"])
    return false
  }
}

