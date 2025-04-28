import { Component, Input } from "@angular/core"
import { CommonModule } from "@angular/common"
import { type Card, CardSuit, CardRank } from "../../models/card-game.model"
import { MatCardModule } from "@angular/material/card"
import { MatIconModule } from "@angular/material/icon"

@Component({
  selector: "app-card",
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule],
  templateUrl: "./card.component.html",
  styleUrls: ["./card.component.css"],
})
export class CardComponent {
  @Input() card!: Card
  @Input() selectable = false

  getSuitIcon(): string {
    switch (this.card.suit) {
      case CardSuit.HEARTS:
        return "favorite"
      case CardSuit.DIAMONDS:
        return "diamond"
      case CardSuit.CLUBS:
        return "spa"
      case CardSuit.SPADES:
        return "filter_vintage"
      default:
        return "help"
    }
  }

  getSuitColor(): string {
    switch (this.card.suit) {
      case CardSuit.HEARTS:
      case CardSuit.DIAMONDS:
        return "red"
      case CardSuit.CLUBS:
      case CardSuit.SPADES:
        return "black"
      default:
        return "black"
    }
  }

  getCardRankDisplay(): string {
    switch (this.card.rank) {
      case CardRank.SEVEN:
        return "7"
      case CardRank.EIGHT:
        return "8"
      case CardRank.NINE:
        return "9"
      case CardRank.TEN:
        return "10"
      case CardRank.UNDER:
        return "J"
      case CardRank.OVER:
        return "Q"
      case CardRank.KING:
        return "K"
      case CardRank.ACE:
        return "A"
      default:
        return "?"
    }
  }
}
