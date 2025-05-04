import { Component, Input, Output, EventEmitter } from "@angular/core"
import { CommonModule } from "@angular/common"
import { MatButtonModule } from "@angular/material/button"
import { MatIconModule } from "@angular/material/icon"
import { MatTooltipModule } from "@angular/material/tooltip"
import { TranslateModule } from "@ngx-translate/core"
import { GameStatus } from "../../models/card-game.model"

@Component({
  selector: "app-game-controls",
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule, MatTooltipModule, TranslateModule],
  templateUrl: "./game-controls.component.html",
  styleUrls: ["./game-controls.component.scss"],

})
export class GameControlsComponent {
  @Input() gameStatus: GameStatus = GameStatus.WAITING
  @Input() isCurrentPlayerTurn = false
  @Output() partnerMessage = new EventEmitter<string>()

  sendPartnerMessage(type: string): void {
    this.partnerMessage.emit(type)
  }
}
