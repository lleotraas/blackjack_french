package fr.lleotraas.blackjack_french.features_online_main_screen.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import fr.lleotraas.blackjack_french.features_online_main_screen.domain.repository.FirebaseHelper
import javax.inject.Inject

class FirebaseHelperImpl @Inject constructor() : FirebaseHelper {

    override fun getUserCollectionReference() = FirebaseFirestore.getInstance()
        .collection(USER_COLLECTION_NAME)

    override fun getUserStatsCollectionReference(userId: String): CollectionReference = FirebaseFirestore.getInstance()
        .collection(USER_STATS_COLLECTION_NAME)
        .document(userId)
        .collection(DATE_AND_WALLET)

    override fun getUserStatsDocumentReference(userId: String, numberOfGamePlayed: String) = FirebaseFirestore.getInstance()
        .collection(USER_STATS_COLLECTION_NAME)
        .document(userId)
        .collection(DATE_AND_WALLET)
        .document(numberOfGamePlayed)

    override fun getDeckCollectionReference() = FirebaseFirestore.getInstance()
        .collection(DECK_COLLECTION_NAME)

    override fun getOnlineGameChatDocumentReference(userId: String, date: String ) = FirebaseFirestore.getInstance()
        .collection(ONLINE_GAME_CHAT_COLLECTION_NAME)
        .document(userId)
        .collection(ONLINE_GAME_CHAT_DATE_COLLECTION_NAME)
        .document(date)

    override fun getOnlineGameChatCollectionReference(userId: String) = FirebaseFirestore.getInstance()
        .collection(ONLINE_GAME_CHAT_COLLECTION_NAME)
        .document(userId)
        .collection(ONLINE_GAME_CHAT_DATE_COLLECTION_NAME)

    override fun getOnlineGameChatCollectionReferenceToDelete() = FirebaseFirestore.getInstance()
        .collection(ONLINE_GAME_CHAT_COLLECTION_NAME)

    override fun getChatCollectionReference() = FirebaseFirestore.getInstance()
        .collection(CHAT_COLLECTION_NAME)

    override fun getImageStorageCollectionReference() = Firebase
        .storage(STORAGE_ADDRESS)
        .reference
        .child(IMAGE_COLLECTION_NAME)

    override fun getImageStorageCollectionByGsReference() = Firebase.storage

    override fun getCurrentUser(): FirebaseUser? = FirebaseAuth.getInstance().currentUser

    override fun signOut() = Firebase.auth.signOut()

    override fun isCurrentUserLogged(): Boolean = getCurrentUser() != null

    companion object {
        private const val USER_COLLECTION_NAME = "user"
        private const val USER_STATS_COLLECTION_NAME = "userStats"
        private const val DATE_AND_WALLET = "dateAndWallet"
        private const val DECK_COLLECTION_NAME = "deck"
        private const val CHAT_COLLECTION_NAME = "chat"
        private const val ONLINE_GAME_CHAT_COLLECTION_NAME = "onlineGameChat"
        private const val ONLINE_GAME_CHAT_DATE_COLLECTION_NAME = "onlineGameChatDate"
        private const val IMAGE_COLLECTION_NAME = "image"
        const val STORAGE_ADDRESS = "gs://black-jack-f60fb.appspot.com"
    }
}