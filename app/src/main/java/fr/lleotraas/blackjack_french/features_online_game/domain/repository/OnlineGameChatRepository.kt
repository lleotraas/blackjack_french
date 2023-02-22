package fr.lleotraas.blackjack_french.features_online_game.domain.repository

import androidx.lifecycle.LiveData
import fr.lleotraas.blackjack_french.features_online_main_screen.domain.model.Message

interface OnlineGameChatRepository {

    fun sendMessage(userId: String, message: Message)

    fun getAllMessage(userId: String): LiveData<List<Message>>

    fun listenToMessage(userId: String): LiveData<List<Message>>

    fun deleteAllMessage(userId: String)

}