package fr.lleotraas.blackjack_french.features_offline_game.domain.utils

import fr.lleotraas.blackjack_french.features_offline_game.domain.model.Card
import fr.lleotraas.blackjack_french.features_offline_game.domain.model.CustomPlayer
import fr.lleotraas.blackjack_french.features_offline_game.domain.model.OfflineUser
import fr.lleotraas.blackjack_french.features_offline_game.domain.model.Player
import fr.lleotraas.blackjack_french.features_online_main_screen.presentation.utils.HandType

class SplitUtils {
    companion object {

        fun modifyFirstSplitScore(playerHand: Player, splitType: String) {
            playerHand.score[splitType]?.plus(10)
        }

        private fun getCurrentPlayerSplit(offlineUser: OfflineUser, currentSplitHand: HandType): CustomPlayer {
            var player = Utils.createCustomPlayer()
            for (index in 0 until offlineUser.player.size) {
                if (
                    offlineUser.currentPlayerNumber == offlineUser.player[index].playerNumber &&
                    HandType.FirstSplit == currentSplitHand
                ) {
                    player = offlineUser.player[index]
                }
            }
            return player
        }

        fun splitPlayerGame(arrayOfPlayer: ArrayList<CustomPlayer>, handType: HandType, currentPlayer: CustomPlayer) {
            // Add card in second split from main hand or from first split.
            var playerToAdd: CustomPlayer
            val handOfNewSplit = ArrayList<Card>()
            val currentPlayerIndex = Utils.getPlayerIndex(arrayOfPlayer, currentPlayer)
            if (
                !currentPlayer.isPlayerSecondSplit && currentPlayer.isPlayerFirstSplit && handType == HandType.MainHand ||
                handType == HandType.FirstSplit
            ) {
                currentPlayer.isPlayerSecondSplit = true
                if(handType == HandType.MainHand) {
                    arrayOfPlayer[currentPlayerIndex + 1].isPlayerSecondSplit = true
                } else {
                    currentPlayer.isPlayerFirstSplit = true
                }
                val splitCard = currentPlayer.hand[currentPlayer.hand.size - 1]
                splitCard.isAnimate = true
                handOfNewSplit.add(splitCard)
                playerToAdd = Utils.createCustomPlayer(
                    arrayOfPlayer,
                    Utils.getPlayerIndex(arrayOfPlayer, currentPlayer),
                    handOfNewSplit
                )
                arrayOfPlayer.add(
                    Utils.getPlayerIndex(
                        arrayOfPlayer,
                        currentPlayer
                    ) + if(handType == HandType.MainHand) 2 else 1, playerToAdd)
            }
            // Add card in first split from main hand.
            if (!currentPlayer.isPlayerFirstSplit) {
                currentPlayer.isPlayerFirstSplit = true
                val mainHandSplitCard = currentPlayer.hand[currentPlayer.hand.size - 1]
                mainHandSplitCard.isAnimate = true
                handOfNewSplit.add(mainHandSplitCard)
                playerToAdd = Utils.createCustomPlayer(
                    arrayOfPlayer,
                    Utils.getPlayerIndex(arrayOfPlayer, currentPlayer),
                    handOfNewSplit
                )
                arrayOfPlayer.add(Utils.getPlayerIndex(arrayOfPlayer, currentPlayer) + 1, playerToAdd)
            }
            // Refresh main hand score or first split score.
            if (handType == HandType.MainHand) {
                val mainHandCardRemoved = currentPlayer.hand[currentPlayer.hand.size - 1]
                currentPlayer.score = currentPlayer.score.minus(mainHandCardRemoved.value!!)
                currentPlayer.hand.remove(mainHandCardRemoved)
            } else {
                val firstSplitCardRemoved = currentPlayer.hand[currentPlayer.hand.size - 1]
                currentPlayer.score = currentPlayer.score.minus(firstSplitCardRemoved.value!!)
                currentPlayer.hand.remove(firstSplitCardRemoved)
            }
        }

        fun playerHave2Aces(offlineUser: OfflineUser): Boolean {
            val firstCard =
                Utils.getCurrentPlayer(offlineUser).hand[0]
            val secondCard =
                getCurrentPlayerSplit(offlineUser, HandType.FirstSplit).hand[0]
            return firstCard.value == 1 && secondCard.value == 1
        }

    }
}