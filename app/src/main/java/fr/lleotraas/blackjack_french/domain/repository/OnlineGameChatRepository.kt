package fr.lleotraas.blackjack_french.domain.repository

import androidx.lifecycle.LiveData
import fr.lleotraas.blackjack_french.model.Message

interface OnlineGameChatRepository {

    fun sendMessage(userId: String, message: Message)

    fun getAllMessage(userId: String): LiveData<List<Message>>

    fun listenToMessage(userId: String): LiveData<List<Message>>

    fun deleteAllMessage(userId: String)

}