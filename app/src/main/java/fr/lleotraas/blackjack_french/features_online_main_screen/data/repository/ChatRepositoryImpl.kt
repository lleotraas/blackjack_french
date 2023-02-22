package fr.lleotraas.blackjack_french.features_online_main_screen.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import fr.lleotraas.blackjack_french.features_online_main_screen.domain.repository.ChatRepository
import fr.lleotraas.blackjack_french.features_online_main_screen.domain.repository.FirebaseHelper
import fr.lleotraas.blackjack_french.features_online_main_screen.domain.model.Message
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.CUSTOM_USER_PICTURE
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.DATE
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.MESSAGE
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.PICTURE_ROTATION
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.PSEUDO
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.USER_ID
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.USER_PICTURE
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.convertDocumentToMessage
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor (
    private val firebaseHelper: FirebaseHelper
): ChatRepository {

    private var listOfChat = MutableLiveData<List<Message>>()

    override fun sendMessage(message: Message) {
        val messageToSet = hashMapOf(
            USER_ID to message.id,
            PSEUDO to message.userName,
            DATE to message.date,
            MESSAGE to message.message,
            USER_PICTURE to message.userPicture,
            CUSTOM_USER_PICTURE to message.customUserPicture,
            PICTURE_ROTATION to if (message.pictureRotation != null) message.pictureRotation else 0f
        )
        firebaseHelper.getChatCollectionReference().document(message.date!!).set(messageToSet)
    }

    override fun getAllMessage(): LiveData<List<Message>> {
        firebaseHelper.getChatCollectionReference().get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val onlineChat = ArrayList<Message>()
                for (document in task.result) {
                    onlineChat.add(convertDocumentToMessage(document))
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
                onlineChat.add(convertDocumentToMessage(document))
            }
            listOfChat.postValue(onlineChat)
        }
        return listOfChat
    }
}