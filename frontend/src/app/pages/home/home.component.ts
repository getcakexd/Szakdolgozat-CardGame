import { Component,  OnInit } from "@angular/core"
import {NgIf, NgFor, NgClass} from "@angular/common"
import { RouterLink } from "@angular/router"
import { AuthService } from "../../services/auth/auth.service"
import { TranslateModule } from "@ngx-translate/core"
import { MatCardModule } from "@angular/material/card"
import { MatButtonModule } from "@angular/material/button"
import { MatIconModule } from "@angular/material/icon"
import { MatRippleModule } from "@angular/material/core"

interface FeatureCard {
  title: string
  description: string
  icon: string
  route: string
  color: string
}

@Component({
  selector: "app-home",
  imports: [NgIf, NgFor, RouterLink, TranslateModule, MatCardModule, MatButtonModule, MatIconModule, MatRippleModule, NgClass],
  templateUrl: "./home.component.html",
  standalone: true,
  styleUrl: "./home.component.scss",
})
export class HomeComponent implements OnInit {
  username: string | null = null
  featureCards: FeatureCard[] = [
    {
      title: "HOME.FEATURE_FRIENDS_TITLE",
      description: "HOME.FEATURE_FRIENDS_DESC",
      icon: "people",
      route: "/friends",
      color: "friends-card",
    },
    {
      title: "HOME.FEATURE_CLUBS_TITLE",
      description: "HOME.FEATURE_CLUBS_DESC",
      icon: "groups",
      route: "/clubs",
      color: "clubs-card",
    },
    {
      title: "HOME.FEATURE_RULES_TITLE",
      description: "HOME.FEATURE_RULES_DESC",
      icon: "menu_book",
      route: "/games",
      color: "rules-card",
    },
    {
      title: "HOME.FEATURE_LOBBY_TITLE",
      description: "HOME.FEATURE_LOBBY_DESC",
      icon: "casino",
      route: "/lobby",
      color: "lobby-card",
    },
    {
      title: "HOME.FEATURE_STATS_TITLE",
      description: "HOME.FEATURE_STATS_DESC",
      icon: "insights",
      route: "/stats",
      color: "stats-card",
    },
    {
      title: "HOME.FEATURE_LEADERBOARD_TITLE",
      description: "HOME.FEATURE_LEADERBOARD_DESC",
      icon: "leaderboard",
      route: "/leaderboard",
      color: "leaderboard-card",
    },
  ]

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.username = this.authService.currentUser?.username || null
  }
}
