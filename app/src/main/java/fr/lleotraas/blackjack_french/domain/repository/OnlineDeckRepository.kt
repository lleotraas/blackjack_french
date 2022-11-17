package fr.lleotraas.blackjack_french.domain.repository

import androidx.lifecycle.LiveData
import fr.lleotraas.blackjack_french.model.OnlineDeck
import fr.lleotraas.blackjack_french.model.PlayerNumberType

interface OnlineDeckRepository {
    fun isDeckExist(currentUserId: String): Boolean

    fun createOnlineDeck(currentUserId: String, deckLocal: OnlineDeck)

    fun getOnlineDeck(currentUserId: String): LiveData<OnlineDeck?>

    fun updateOnlineDeckIndex(currentUserId: String, index: Int)

    fun updateOnlineDeckPlayerTurn(currentUserId: String, playerNumberType: PlayerNumberType)

    fun deleteOnlineDeck(currentUserId: String)
}