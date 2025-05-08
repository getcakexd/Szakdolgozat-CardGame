import { Component, OnInit, HostListener } from "@angular/core"
import { NgIf } from "@angular/common"
import { RouterLink } from "@angular/router"
import { MatToolbarModule } from "@angular/material/toolbar"
import { MatButtonModule } from "@angular/material/button"
import { MatIconModule } from "@angular/material/icon"
import { MatMenuModule } from "@angular/material/menu"
import { MatListModule } from "@angular/material/list"
import { MatExpansionModule } from "@angular/material/expansion"
import { AuthService } from "../../services/auth/auth.service"
import { ThemeToggleComponent } from "../theme-toggle/theme-toggle.component"
import { TranslationService } from "../../services/translation/translation.service"
import { LanguageSelectorComponent } from "../language-selector/language-selector.component"
import { TranslatePipe } from "@ngx-translate/core"

@Component({
  selector: "app-navbar",
  standalone: true,
  imports: [
    NgIf,
    RouterLink,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatListModule,
    MatExpansionModule,
    ThemeToggleComponent,
    LanguageSelectorComponent,
    TranslatePipe,
  ],
  templateUrl: "./navbar.component.html",
  styleUrls: ["./navbar.component.scss"],
})
export class NavbarComponent implements OnInit {
  isLoggedIn = false
  username = ""
  isAdmin = false
  isAgent = false
  isRoot = false
  isMobile = false
  sidenavOpen = false

  constructor(
    protected authService: AuthService,
    public translationService: TranslationService,
  ) {
    this.checkScreenSize()
  }

  @HostListener("window:resize", ["$event"])
  onResize() {
    this.checkScreenSize()
  }

  checkScreenSize() {
    this.isMobile = window.innerWidth < 768
  }

  toggleSidenav() {
    this.sidenavOpen = !this.sidenavOpen

    if (this.sidenavOpen) {
      document.body.style.overflow = "hidden"
    } else {
      document.body.style.overflow = ""
    }
  }

  closeSidenav(event: MouseEvent) {
    if ((event.target as HTMLElement).classList.contains("sidenav-overlay")) {
      this.sidenavOpen = false
      document.body.style.overflow = ""
    }
  }

  ngOnInit(): void {
    this.isLoggedIn = this.authService.isLoggedIn()
    this.username = this.authService.currentUser?.username || ""
    this.isAdmin = this.authService.isAdmin()
    this.isAgent = this.authService.isAgent()
    this.isRoot = this.authService.isRoot()
  }

  logout(): void {
    this.authService.logout()
    this.sidenavOpen = false
    document.body.style.overflow = ""
  }
}
