package fr.lleotraas.blackjack_french.features_online_game.domain.repository

import androidx.lifecycle.LiveData
import fr.lleotraas.blackjack_french.features_offline_game.domain.model.Card
import fr.lleotraas.blackjack_french.features_online_game.domain.model.OnlineDeck
import fr.lleotraas.blackjack_french.features_offline_game.domain.model.PlayerNumberType

interface OnlineDeckRepository {
    fun isDeckExist(currentUserId: String): Boolean

    fun createOnlineDeck(currentUserId: String, deckLocal: OnlineDeck)

    fun getOnlineDeck(currentUserId: String): LiveData<OnlineDeck?>

    fun updateOnlineDeckIndex(currentUserId: String, index: Int)

    fun updateOnlineDeckPlayerTurn(currentUserId: String, playerNumberType: PlayerNumberType)

    fun deleteOnlineDeck(currentUserId: String)

    suspend fun addCard(tableId: Int, card: Card)
}