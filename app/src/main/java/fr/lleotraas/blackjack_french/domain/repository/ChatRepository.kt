package fr.lleotraas.blackjack_french.domain.repository

import androidx.lifecycle.LiveData
import fr.lleotraas.blackjack_french.model.Message

interface ChatRepository {

    fun sendMessage(message: Message)

    fun getAllMessage(): LiveData<List<Message>>

    fun listenToMessage(): LiveData<List<Message>>
}