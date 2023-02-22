package fr.lleotraas.blackjack_french.features_online_game.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import fr.lleotraas.blackjack_french.features_online_main_screen.domain.repository.FirebaseHelper
import fr.lleotraas.blackjack_french.features_online_game.domain.repository.OnlineGameChatRepository
import fr.lleotraas.blackjack_french.features_online_main_screen.domain.model.Message
import javax.inject.Inject

class OnlineGameChatRepositoryImpl @Inject constructor(
    private val firebaseHelper: FirebaseHelper
) : OnlineGameChatRepository {

    private var listOfChat = MutableLiveData<List<Message>>()

    override fun sendMessage(userId: String, message: Message) {
        firebaseHelper.getOnlineGameChatDocumentReference(userId, message.date.toString()).set(message)
    }

    override fun getAllMessage(userId: String): LiveData<List<Message>> {
        firebaseHelper.getOnlineGameChatCollectionReference(userId).get().addOnCompleteListener { task ->
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

    override fun listenToMessage(userId: String): LiveData<List<Message>> {
        Log.e(javaClass.simpleName, "listenToMessage: userId:$userId")
        firebaseHelper.getOnlineGameChatCollectionReference(userId).addSnapshotListener { value, exception ->
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

    override fun deleteAllMessage(userId: String) {
        if (listOfChat.value?.isNotEmpty() == true) {
            listOfChat.value!!.forEach {
                firebaseHelper.getOnlineGameChatCollectionReference(userId)
                    .document(it.date.toString()).delete()
                Log.e(javaClass.simpleName, "deleteAllMessage: Successfully deleted chat for userId:$userId")
            }
        }
    }
}