package fr.lleotraas.blackjack_french.domain.repository

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

interface FirebaseHelper  {

    fun getUserCollectionReference(): CollectionReference
    fun getUserStatsCollectionReference(userId: String): CollectionReference
    fun getUserStatsDocumentReference(userId: String, numberOfGamePlayed: String): DocumentReference
    fun getDeckCollectionReference(): CollectionReference
    fun getChatCollectionReference(): CollectionReference
    fun getOnlineGameChatDocumentReference(userId: String, date: String): DocumentReference
    fun getOnlineGameChatCollectionReference(userId: String): CollectionReference
    fun getOnlineGameChatCollectionReferenceToDelete(): CollectionReference
    fun getImageStorageCollectionReference(): StorageReference
    fun getImageStorageCollectionByGsReference(): FirebaseStorage
    fun getCurrentUser(): FirebaseUser?
    fun signOut()
    fun isCurrentUserLogged(): Boolean
}