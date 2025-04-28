import { Component, Input } from "@angular/core"
import { CommonModule } from "@angular/common"
import type { Player } from "../../models/card-game.model"
import { MatCardModule } from "@angular/material/card"
import { MatIconModule } from "@angular/material/icon"
import { MatBadgeModule } from "@angular/material/badge"
import { TranslateModule } from "@ngx-translate/core"

@Component({
  selector: "app-player-info",
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule, MatBadgeModule, TranslateModule],
  templateUrl: "./player-info.component.html",
  styleUrls: ["./player-info.component.css"],
})
export class PlayerInfoComponent {
  @Input() player!: Player
  @Input() isCurrentPlayer = false
  @Input() isCurrentTurn = false
}
