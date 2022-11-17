package fr.lleotraas.blackjack_french.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import fr.lleotraas.blackjack_french.domain.repository.ChatRepository
import fr.lleotraas.blackjack_french.domain.repository.FirebaseHelper
import fr.lleotraas.blackjack_french.model.Message
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor (
    private val firebaseHelper: FirebaseHelper
): ChatRepository {

    private var listOfChat = MutableLiveData<List<Message>>()

    override fun sendMessage(message: Message) {
        firebaseHelper.getChatCollectionReference().document(message.date!!).set(message)
    }

    override fun getAllMessage(): LiveData<List<Message>> {
        firebaseHelper.getChatCollectionReference().get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val onlineChat = ArrayList<Message>()
                for (document in task.result) {
                    onlineChat.add(document.toObject(Message::class.java))
                }
                listOfChat.postValue(onlineChat)
            }
        }
        return listOfChat
    }

    override fun listenToMessage(): LiveData<List<Message>> {
        firebaseHelper.getChatCollectionReference().addSnapshotListener { value, exception ->
            if (exception != null) {
                Log.w(javaClass.simpleName, "Listen failed.",exception)
                return@addSnapshotListener
            }
            val onlineChat = ArrayList<Message>()
            for (document in value!!) {
                onlineChat.add(document.toObject(Message::class.java))
            }
            listOfChat.postValue(onlineChat)
        }
        return listOfChat
    }
}