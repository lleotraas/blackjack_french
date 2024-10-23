package fr.lleotraas.blackjack_french.features_offline_game.domain.utils

import fr.lleotraas.blackjack_french.features_offline_game.domain.model.Dealer
import fr.lleotraas.blackjack_french.features_offline_game.domain.model.NumberType
import fr.lleotraas.blackjack_french.features_online_game.domain.model.OnlineDeck

class DealerUtils {
    companion object {

        fun modifyDealerScoreWithAce(dealer: Dealer, deck: OnlineDeck): Boolean {
            var isScoreSoft = dealer.isDealerScoreSoft
            if (
                deck.deckList[deck.index].number == NumberType.ACE &&
                dealer.score < 12 && dealer.isDealerDrawAce
            ) {
                dealer.score += 10
                isScoreSoft = true
            }

            if (
                dealer.score > 21 &&
                dealer.isDealerDrawAce
            ) {
                dealer.score -= 10
                isScoreSoft = false
            }

            return isScoreSoft
        }

        fun addCardToDealerList(dealer: Dealer, deck: OnlineDeck): Dealer {
            dealer.score += deck.deckList[deck.index].value!!
            dealer.hand.add(deck.deckList[deck.index])
            return dealer
        }

        fun dealerDrawAnAceOrNot(deck: OnlineDeck, dealer: Dealer): Boolean {
            var isAceDraw = dealer.isDealerDrawAce
            if (deck.deckList[deck.index].number == NumberType.ACE && !dealer.isDealerDrawAce) {
                isAceDraw = true
            }

            if (deck.deckList[deck.index].number == NumberType.ACE && dealer.score > 11 && dealer.isDealerDrawAce && !dealer.isDealerScoreSoft) {
                isAceDraw = false
            }
            return isAceDraw
        }

        fun isDealerHaveBlackJack(dealer: Dealer) = dealer.score == 21 && dealer.hand.size == 2

    }
}