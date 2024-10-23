package fr.lleotraas.blackjack_french.features_offline_game.domain.utils

import fr.lleotraas.blackjack_french.R
import fr.lleotraas.blackjack_french.features_offline_game.domain.model.*
import fr.lleotraas.blackjack_french.features_online_game.domain.model.OnlineDeck
import fr.lleotraas.blackjack_french.features_online_main_screen.presentation.utils.HandType

class ScoreUtils {
    companion object {

        fun compareScore(
            playerScore: Int,
            playerHandSize: Int,
            handType: String,
            isPlayerFirstSplit: Boolean,
            dealer: Dealer
        ): Int {
            return when  {
                dealer.score == 21 && dealer.hand.size == 2 &&
                        playerScore == 21 && playerHandSize == 2 &&
                        handType == HandType.MainHand.valueOf &&
                        !isPlayerFirstSplit-> {
                    R.string.fragment_main_game_draw
                }
                dealer.score == 21 &&
                        dealer.hand.size == 2 -> {
                    R.string.fragment_main_game_you_lose
                }
                playerScore == 21 &&
                        playerHandSize == 2 &&
                        handType == Utils.MAIN_HAND &&
                        !isPlayerFirstSplit -> {
                    R.string.online_game_fragment_blackjack
                }
                dealer.score > playerScore -> {
                    R.string.fragment_main_game_you_lose
                }
                dealer.score < playerScore -> {
                    R.string.fragment_main_game_you_win
                }
                else -> {
                    R.string.fragment_main_game_draw
                }
            }
        }

        fun compareScore(playerScore: Int, playerHandSize: Int): Int {
            return when  {
                playerScore == 21 && playerHandSize == 2 -> {
                    R.string.online_game_fragment_blackjack
                }
                else -> {
                    R.string.fragment_main_game_you_win
                }
            }
        }

        fun playerHaveASoftScoreOrNot(playerHand: CustomPlayer, deck: OnlineDeck): Boolean {
            var isScoreSoft = playerHand.isPlayerScoreSoft
            if (
                deck.deckList[deck.index].number == NumberType.ACE &&
                playerHand.score < 12 &&
                playerHand.isPlayerDrawAce
            ) {
                playerHand.score = playerHand.score.plus(10)
                isScoreSoft = true
            }
            if (
                playerHand.score > 21 &&
                playerHand.isPlayerDrawAce
            ) {
                playerHand.score = playerHand.score.minus(10)
                isScoreSoft = false
            }
            return isScoreSoft
        }

        fun playerDrawAnAceOrNot(deck: OnlineDeck, playerHand: CustomPlayer): Boolean {
            var isAceDraw = playerHand.isPlayerDrawAce
            if (
                deck.deckList[deck.index].number == NumberType.ACE &&
                !isAceDraw
            ) {
                isAceDraw = true
            }

            if (
                deck.deckList[deck.index].number == NumberType.ACE &&
                playerHand.score > 11 &&
                isAceDraw &&
                !playerHand.isPlayerScoreSoft
            ) {
                isAceDraw = false
            }
            return isAceDraw
        }

        fun addCardToPlayerHandList(playerHand: CustomPlayer, deck: OnlineDeck, handType: HandType): CustomPlayer {
            playerHand.score =
                playerHand.score.plus(deck.deckList[deck.index].value!!)
            playerHand.hand.add(deck.deckList[deck.index])
            return playerHand
        }

        fun getHandScore(hand: ArrayList<Card>): Int {
            var score = 0
            for (card in hand) {
                score += card.value!!
            }
            return score
        }

        fun allPlayerBust(playerList: ArrayList<CustomPlayer>): Boolean {
            for (player in playerList) {
                if (player.score < 22) {
                    return false
                }
            }
            return true
        }

        fun allPlayerHaveBlackjack(playerList: ArrayList<CustomPlayer>): Boolean {
            val playerNotBustList = ArrayList<CustomPlayer>()
            for (player in playerList) {
                if (player.score < 22) {
                    playerNotBustList.add(player)
                }
            }
            for (player in playerNotBustList) {
                if (
                    !playerHaveBlackjack(player)
                ) {
                    return false
                }
            }
            return true
        }

        private fun playerHaveBlackjack(player: CustomPlayer): Boolean {
            return player.score == 21 &&
                    player.hand.size == 2 &&
                    player.playerHandType == HandType.MainHand &&
                    !player.isPlayerFirstSplit
        }

        fun comparePlayerScoreWithDealer(
            offlineUser: OfflineUser,
            dealer: Dealer,
            indexOfPlayer: Int
        ): Double {
            val playerToCompare = offlineUser.player[indexOfPlayer]
            val resultScore: Int
            var winBet = 0.0
            val score = playerToCompare.score
            val handSize = playerToCompare.hand.size
            val handType = offlineUser.player[indexOfPlayer].playerHandType.valueOf
            val isFirstSplit = offlineUser.player[indexOfPlayer].isPlayerFirstSplit
            if (score in 1..21) {

                resultScore =
                    if (dealer.score < 22) {
                        compareScore(score, handSize, handType, isFirstSplit, dealer)
                    } else {
                        compareScore(score, handSize)
                    }
                offlineUser.player[indexOfPlayer].resultScore = resultScore
                winBet += Utils.paymentForPlayer(resultScore, playerToCompare.bet)
            }
            return winBet
        }

    }
}