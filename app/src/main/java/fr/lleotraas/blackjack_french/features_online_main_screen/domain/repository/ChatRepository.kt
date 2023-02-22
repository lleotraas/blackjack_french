package fr.lleotraas.blackjack_french.features_online_main_screen.domain.repository

import androidx.lifecycle.LiveData
import fr.lleotraas.blackjack_french.features_online_main_screen.domain.model.Message

interface ChatRepository {

    fun sendMessage(message: Message)

    fun getAllMessage(): LiveData<List<Message>>

    fun listenToMessage(): LiveData<List<Message>>
}