import { Component, Input } from "@angular/core"
import { CommonModule } from "@angular/common"
import { Card, CardSuit, CardRank } from "../../models/card-game.model"
import { MatCardModule } from "@angular/material/card"
import { MatIconModule } from "@angular/material/icon"

@Component({
  selector: "app-card",
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule],
  templateUrl: "./card.component.html",
  styleUrls: ["./card.component.scss"],
})
export class CardComponent {
  @Input() card!: Card
  @Input() selectable = false

  getSuitIcon(): string {
    switch (this.card.suit) {
      case CardSuit.HEARTS:
        return "favorite"
      case CardSuit.BELLS:
        return "notifications"
      case CardSuit.LEAVES:
        return "eco"
      case CardSuit.ACORNS:
        return "park"
      default:
        return "help"
    }
  }

  getSuitImagePath() {
    switch (this.card.suit) {
      case CardSuit.HEARTS:
        return "hearts.png"
      case CardSuit.BELLS:
        return "bells.png"
      case CardSuit.LEAVES:
        return "leaves.png"
      case CardSuit.ACORNS:
        return "acorns.png"
      default:
        return "help"
    }
  }

  getSuitColor(): string {
    switch (this.card.suit) {
      case CardSuit.HEARTS:
        return "red"
      case CardSuit.BELLS:
        return "yellow"
      case CardSuit.LEAVES:
        return "green"
      case CardSuit.ACORNS:
        return "brown"
      default:
        return "black"
    }
  }

  getCardRankDisplay(): string {
    switch (this.card.rank) {
      case CardRank.SEVEN:
        return "VII"
      case CardRank.EIGHT:
        return "VIII"
      case CardRank.NINE:
        return "IX"
      case CardRank.TEN:
        return "X"
      case CardRank.UNDER:
        return "U"
      case CardRank.OVER:
        return "O"
      case CardRank.KING:
        return "K"
      case CardRank.ACE:
        return "A"
      default:
        return "?"
    }
  }
}
