package fr.lleotraas.blackjack_french.features_online_game.data.repository

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import fr.lleotraas.blackjack_french.features_offline_game.domain.model.Card
import fr.lleotraas.blackjack_french.features_online_main_screen.domain.repository.FirebaseHelper
import fr.lleotraas.blackjack_french.features_online_game.domain.repository.OnlineDeckRepository
import fr.lleotraas.blackjack_french.features_online_game.domain.model.OnlineDeck
import fr.lleotraas.blackjack_french.features_offline_game.domain.model.PlayerNumberType
import fr.lleotraas.blackjack_french.features_online_game.domain.retrofit.CardApi
import javax.inject.Inject

class OnlineDeckRepositoryImpl @Inject constructor (
    private val mFirebaseHelper: FirebaseHelper,
    private val api: CardApi
): OnlineDeckRepository {

    private var onlineDeck = MutableLiveData<OnlineDeck?>()
    private lateinit var registration: ListenerRegistration

    override fun isDeckExist(currentUserId: String): Boolean {
        if (onlineDeck.value != null) {
            return true
        }
        return false
    }

    override fun createOnlineDeck(currentUserId: String, deckLocal: OnlineDeck) {

        mFirebaseHelper.getDeckCollectionReference().document(currentUserId).set(deckLocal)
        Log.e(ContentValues.TAG, "createDeck: Deck Created", )
        onlineDeck.postValue(deckLocal)
    }

    override fun getOnlineDeck(currentUserId: String): LiveData<OnlineDeck?> {
        mFirebaseHelper.getDeckCollectionReference().document(currentUserId).get().addOnSuccessListener { documentSnapshot ->
            val deckOnline = documentSnapshot.toObject<OnlineDeck>()
            if (deckOnline != null) {
                onlineDeck.postValue(deckOnline)
            }
        }

        registration = mFirebaseHelper.getDeckCollectionReference().document(currentUserId).addSnapshotListener { value, exception ->
            if (exception != null) {
                Log.w(ContentValues.TAG, "Listen failed,", exception)
                return@addSnapshotListener
            }
            if (value != null) {
                val deckOnline = value.toObject<OnlineDeck>()
                if (deckOnline != null) {
                    onlineDeck.postValue(deckOnline)
                }
            }
        }
        return onlineDeck
    }

    override fun updateOnlineDeckIndex(currentUserId: String, index: Int) {
        mFirebaseHelper.getDeckCollectionReference().document(currentUserId).update(
            "index", index
        )
    }

    override fun updateOnlineDeckPlayerTurn(currentUserId: String, playerNumberType: PlayerNumberType) {
        mFirebaseHelper.getDeckCollectionReference().document(currentUserId).update("playerTurn", playerNumberType)
    }

    override fun deleteOnlineDeck(currentUserId: String) {
//        registration.remove()
        mFirebaseHelper.getDeckCollectionReference().document(currentUserId).delete()
        Log.e(ContentValues.TAG, "deleteOnlineDeck: Deck deleted")
    }

    override suspend fun addCard(tableId: Int, card: Card) {
        api.addCard(
            tableId,
            card.number!!.name,
            card.color!!.name
        )
    }
}