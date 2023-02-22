package fr.lleotraas.blackjack_french.features_online_main_screen.data.repository

import android.content.ContentValues
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import fr.lleotraas.blackjack_french.features_online_main_screen.domain.repository.FirebaseHelper
import fr.lleotraas.blackjack_french.features_online_main_screen.domain.repository.UserRepository
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.makeStringFromUri
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.userStatsComparator
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2
import fr.lleotraas.blackjack_french.features_offline_game.domain.model.Card
import fr.lleotraas.blackjack_french.features_offline_game.domain.model.PlayerNumberType
import fr.lleotraas.blackjack_french.features_online_main_screen.domain.model.OnlineStatusType
import fr.lleotraas.blackjack_french.features_online_main_screen.domain.model.User
import fr.lleotraas.blackjack_french.features_online_main_screen.domain.model.UserStats
import fr.lleotraas.blackjack_french.features_online_main_screen.presentation.utils.HandType
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.BET
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.FIRST_SPLIT_HAND
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.HAND
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.IS_DEFAULT_IMAGE_PROFILE
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.IS_GAME_HOST
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.IS_SPLITTING
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.IS_USER_READY
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.NUMBER_OF_GAME_PLAYED
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.NUMBER_OF_LOAN
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.ONLINE_STATUS
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.OPPONENT
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.PICTURE_ROTATION
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.PLAYER_TURN
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.PSEUDO
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.SECOND_SPLIT_HAND
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.SPLIT_TYPE
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.USER_ID
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.USER_PICTURE
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.WALLET
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.WALLET_STATE
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils.Companion.convertDocumentToUser

class UserRepositoryImpl @Inject constructor (
    private val mFirebaseHelper: FirebaseHelper
): UserRepository {

    private var currentUser = MutableLiveData<User?>()
    private var searchedUser = MutableLiveData<User?>()
    private var listOfOnlineUser = MutableLiveData<ArrayList<User>>()
    private var listOfImageByteArray = MutableLiveData<HashMap<String ,ByteArray>?>()
    private var listOfGamePlayed = MutableLiveData<ArrayList<UserStats>>()
    private var isUserUpdateNumberOfPlayedGames = false
    private var isTaskExecuted = MutableLiveData(false)

    override fun createOnlineUser() {
        val connectedUser = mFirebaseHelper.getCurrentUser()
        val onlineUser =
            hashMapOf(
            USER_ID to connectedUser?.uid,
            NUMBER_OF_LOAN to 0,
            WALLET to 1500.0,
            BET to Utils.createBet(),
            PSEUDO to connectedUser?.displayName,
            USER_PICTURE to connectedUser?.photoUrl,
            PICTURE_ROTATION to 0f,
            ONLINE_STATUS to OnlineStatusType.OFFLINE,
            OPPONENT to "",
            PLAYER_TURN to null,
            SPLIT_TYPE to null,
            NUMBER_OF_GAME_PLAYED to 0,
            IS_DEFAULT_IMAGE_PROFILE to true,
            IS_GAME_HOST to false,
            IS_USER_READY to false,
            IS_SPLITTING to false
        )

        if (connectedUser != null) {
            mFirebaseHelper.getUserCollectionReference().document(connectedUser.uid).set(onlineUser)
            currentUser.postValue(createUser(connectedUser))
        }
    }

    override fun createUser(connectedUser: FirebaseUser): User {
        return User(
            connectedUser.uid,
            0,
            1500.0,
            Utils.createBet(),
            connectedUser.displayName!!,
            connectedUser.photoUrl.toString(),
            0f,
            OnlineStatusType.OFFLINE,
            "",
            null,
            null,
            0,
            isDefaultProfileImage = true,
            isGameHost = false,
            isUserReady = false,
            isSplitting = false
        )
    }

    override fun getCurrentOnlineUser(userId: String): LiveData<User?> {
        mFirebaseHelper.getUserCollectionReference().document(userId).get().addOnSuccessListener { documentSnapshot ->
            val userOnline = convertDocumentToUser(documentSnapshot)
            if (userOnline != null) {
                currentUser.postValue(userOnline)
                isTaskExecuted.postValue(false)
            } else {
                createOnlineUser()
            }
        }
        mFirebaseHelper.getUserCollectionReference().document(userId).addSnapshotListener {value, exception ->
            if (exception != null) {
                Log.w(ContentValues.TAG, "Listen failed.",exception)
                return@addSnapshotListener
            }
            if (value != null) {
                currentUser.postValue(convertDocumentToUser(value))
            }
        }
        return currentUser
    }

    override fun getSearchedUser(userId: String): LiveData<User?> {
//        val onlineUser = ArrayList<User?>()
        mFirebaseHelper.getUserCollectionReference().document(userId).get().addOnSuccessListener { documentSnapshot ->
//            onlineUser.add(documentSnapshot.toObject(User::class.java))
            searchedUser.postValue(convertDocumentToUser(documentSnapshot))
        }

        mFirebaseHelper.getUserCollectionReference().document(userId).addSnapshotListener {value, exception ->
            if (exception != null) {
                Log.w(ContentValues.TAG, "Listen failed.",exception)
                return@addSnapshotListener
            }
            if (value != null) {
                searchedUser.postValue(convertDocumentToUser(value))
            }
        }
        return searchedUser
    }

    override fun getAllOnlineUsers(): LiveData<ArrayList<User>> {
        mFirebaseHelper.getUserCollectionReference().whereNotEqualTo(ONLINE_STATUS, OnlineStatusType.OFFLINE).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val onlineUserList = ArrayList<User>()
                for (document in task.result) {
                    if (mFirebaseHelper.getCurrentUser()!!.uid != document.id) {
                        onlineUserList.add(convertDocumentToUser(document)!!)
                    }
                }
                listOfOnlineUser.postValue(onlineUserList)
            } else {
                Log.e(ContentValues.TAG, "Error getting documents: ${task.exception}")
            }
        }
        return listOfOnlineUser
    }

    override fun allUserUpdated(): LiveData<ArrayList<User>> {
        val allOnline = mFirebaseHelper.getUserCollectionReference().whereNotEqualTo(ONLINE_STATUS, OnlineStatusType.OFFLINE)
        allOnline.addSnapshotListener { value, exception ->
            if (exception != null) {
                Log.w(ContentValues.TAG, "Listen failed.",exception)
                return@addSnapshotListener
            }

            val onlineUserList = ArrayList<User>()
            for (document in value!!) {
                if (mFirebaseHelper.getCurrentUser()!!.uid != document.id) {
                    onlineUserList.add(convertDocumentToUser(document)!!)
                }
            }
            Log.e("UserRepository", "getAllOnlineUsers: onlineUserList size: ${onlineUserList.size}")
            listOfOnlineUser.postValue(onlineUserList)
        }
        return listOfOnlineUser
    }

    override fun updateOnlineStatusAskForPlay(
        currentUserId: String,
        userOnlineStatus: OnlineStatusType
    ) {
        mFirebaseHelper.getUserCollectionReference().document(currentUserId).update(
            ONLINE_STATUS, userOnlineStatus,
            OPPONENT, ""
        )
    }

    override fun updateUserPicture(
        user: User?
    ) {
        mFirebaseHelper.getUserCollectionReference().document(user?.id.toString()).update(
            USER_PICTURE, user?.isDefaultProfileImage
        )
    }

    override fun updateOnlineStatusAskForPlay(
        currentUserId: String,
        searchedUserId: String,
        userOnlineStatus: OnlineStatusType
    ) {
        mFirebaseHelper.getUserCollectionReference().document(searchedUserId).update(
            ONLINE_STATUS, OnlineStatusType.ASK_FOR_PLAY,
            OPPONENT, currentUserId,
            PLAYER_TURN, PlayerNumberType.PLAYER_TWO
        )
        mFirebaseHelper.getUserCollectionReference().document(currentUserId).update(
            ONLINE_STATUS, userOnlineStatus,
            OPPONENT, searchedUserId,
            PLAYER_TURN, PlayerNumberType.PLAYER_ONE
        )
    }

    override fun updateOnlineStatusPlaying(currentUserId: String, searchedUserId: String) {
        mFirebaseHelper.getUserCollectionReference().document(searchedUserId).update(
            ONLINE_STATUS, OnlineStatusType.PLAYING,
            OPPONENT, currentUserId
        )
        mFirebaseHelper.getUserCollectionReference().document(currentUserId).update(
            ONLINE_STATUS, OnlineStatusType.PLAYING,
            OPPONENT, searchedUserId,
            IS_USER_READY, false,
            BET, Utils.createBet(),
            IS_SPLITTING, false
        )
    }

    override fun updateIsGameHost(currentUserId: String, userIsGameHost: Boolean) {
        mFirebaseHelper.getUserCollectionReference().document(currentUserId).update(
            IS_GAME_HOST, userIsGameHost,
            IS_USER_READY, false,
            BET, Utils.createBet(),
            IS_SPLITTING, false
        )
    }

    override fun updateUserIsReady(currentUserId: String, userIsReady: Boolean) {
        mFirebaseHelper.getUserCollectionReference().document(currentUserId).update(IS_USER_READY, userIsReady)
    }

    override fun updateOnlineUserWalletAndIsSplitting(user: User?, isSplitting: Boolean) {
        mFirebaseHelper.getUserCollectionReference().document(user?.id!!).update(
            BET, user.bet,
            WALLET, user.wallet,
            IS_SPLITTING, isSplitting
        )
    }

    override fun updateOnlineUserBetAndWallet(user: User) {
        mFirebaseHelper.getUserCollectionReference().document(user.id!!).update(
            BET, user.bet,
            WALLET, user.wallet
        )
    }

    override fun updateSplitType(user: User) {
        mFirebaseHelper.getUserCollectionReference().document(user.id!!).update(
            SPLIT_TYPE, user.splitType
        )
    }

    override fun updateIsSplitting(userId: String, isSplitting: Boolean) {
        mFirebaseHelper.getUserCollectionReference().document(userId).update(
            IS_SPLITTING, isSplitting
        )
    }

    override fun updateOnlineUserWalletAndLoan(user: User) {
        mFirebaseHelper.getUserCollectionReference().document(user.id!!).update(
            WALLET, user.wallet,
            NUMBER_OF_LOAN, user.numberOfLoan
        )
    }

    override fun updateOnlineUserHand(userId: String, userHand: ArrayList<Card>, userFirstSplitHand: ArrayList<Card>, userSecondSplitHand: ArrayList<Card>) {
        mFirebaseHelper.getUserCollectionReference().document(userId).update(
            HAND, userHand,
            FIRST_SPLIT_HAND, userFirstSplitHand,
            SECOND_SPLIT_HAND, userSecondSplitHand
        )
    }

    override fun updateUserForNewGame(
        user: User?,
        userReady: Boolean?
    ) {
        mFirebaseHelper.getUserCollectionReference().document(user?.id!!).update(
            BET, user.bet,
            WALLET, user.wallet,
            IS_USER_READY, userReady,
        )
    }

    override fun updateNumberOfGamePlayed(user: User) {
        if (!isUserUpdateNumberOfPlayedGames) {
            isUserUpdateNumberOfPlayedGames  = true
            user.numberOfGamePlayed = user.numberOfGamePlayed?.plus(1)
            mFirebaseHelper.getUserCollectionReference().document(user.id.toString()).update(
                NUMBER_OF_GAME_PLAYED, user.numberOfGamePlayed
            ).addOnCompleteListener {
                createUserStatsToCurrentDate(user)
            }
        }
    }

    override fun isNumberOfGamePlayedUpdated() {
        isUserUpdateNumberOfPlayedGames = false
    }

    override fun resetCurrentUserAndHandType(user: User?, splitType: HandType, isSplitting: Boolean) {
        mFirebaseHelper.getUserCollectionReference().document(user?.id!!).update(
            BET, user.bet,
            WALLET, user.wallet,
            SPLIT_TYPE, splitType,
            IS_SPLITTING, isSplitting
        )
    }

    override fun updateUser(currentUser: User?) {
        mFirebaseHelper.getUserCollectionReference().document(currentUser?.id!!).update(
            PSEUDO, currentUser.pseudo,
            IS_DEFAULT_IMAGE_PROFILE, currentUser.isDefaultProfileImage,
            PICTURE_ROTATION, currentUser.pictureRotation
        )
    }

    override fun signOut(currentUserId: String): LiveData<Boolean> {
        mFirebaseHelper.getUserCollectionReference().document(currentUserId).update(
            ONLINE_STATUS, OnlineStatusType.OFFLINE
        ).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                mFirebaseHelper.signOut()
                isTaskExecuted.postValue(true)
            }
        }
        return isTaskExecuted
    }

    override fun uploadImage(userId: String, file: Uri, imageResized: ByteArray): Double {
        val progress = 0.0
        val uploadTask = mFirebaseHelper.getImageStorageCollectionReference()
            .child(userId)
            .child(makeStringFromUri(file))
            .putBytes(imageResized)
        uploadTask.addOnFailureListener { exception ->
            Log.e(javaClass.simpleName, "uploadImage: ${exception.message}")
        }.addOnSuccessListener { taskSnapshot ->
            Log.e(javaClass.simpleName, "uploadImage: Upload Image Successfully, image size: ${taskSnapshot.metadata?.sizeBytes}")
            updateUserImageList(userId, file, imageResized)
//        }.addOnProgressListener { (bytesTransferred, totalByteCount) ->
//            progress = (100.0 * bytesTransferred) / totalByteCount
//            Log.e(javaClass.simpleName, "uploadImage: Upload progression: $progress")
        }.addOnPausedListener {
            Log.e(javaClass.simpleName, "uploadImage: Upload is paused")
        }
        return progress
    }

    override fun deleteImage(userId: String, file: Uri) {
        val deleteTask = mFirebaseHelper.getImageStorageCollectionReference()
            .child(userId)
            .child(makeStringFromUri(file))
            .delete()
        deleteTask.addOnFailureListener { exception ->
            Log.e(javaClass.simpleName, "deleteImage: Image not deleted because ${exception.message}")
        }.addOnSuccessListener { }
    }

    override fun updateUserImageList(userId: String?, uri: Uri, image: ByteArray) {
        mFirebaseHelper.getUserCollectionReference().document(userId.toString()).update(
            USER_PICTURE, makeStringFromUri(uri)
        )
        val imageList = listOfImageByteArray.value
        imageList?.set(userId.toString(), image)
        listOfImageByteArray.postValue(imageList)
    }

    override fun updateImageList() {
        val oneMegabyte: Long = 1024 * 1024
        val array: HashMap<String, ByteArray> = if (listOfImageByteArray.value?.isNotEmpty() == true) {
            listOfImageByteArray.value!!
        } else {
            HashMap()
        }
        mFirebaseHelper.getUserCollectionReference().whereEqualTo(IS_DEFAULT_IMAGE_PROFILE, false).get().addOnSuccessListener { documents ->
            for (document in documents) {
                val user = convertDocumentToUser(document)
                val url = mFirebaseHelper.getImageStorageCollectionReference().child(user?.id.toString()).child(user?.userPicture.toString())
                Log.e(javaClass.simpleName, "updateImageList: PATH: $url")
                url.getBytes(oneMegabyte).addOnSuccessListener { byteArray ->
                    array[user?.id.toString()] = byteArray
                    listOfImageByteArray.postValue(array)
                    Log.e(javaClass.simpleName, "updateImageList: download Success array size: ${array.size}")
                }.addOnFailureListener { exception ->
                    Log.e(javaClass.simpleName, "updateImageList: download Failed ${exception.message}")
                }
            }

        }.addOnFailureListener { exception ->
            Log.e(javaClass.simpleName, "updateImageList: Failure: ${exception.message}")
        }

    }

    override fun compareImageList() {
        val oneMegabyte: Long = 1024 * 1024
        var hashMap = HashMap<String, ByteArray>()
        if (listOfImageByteArray.value?.isNotEmpty() == true) {
            hashMap = listOfImageByteArray.value!!
        }
        mFirebaseHelper.getUserCollectionReference().whereEqualTo(IS_DEFAULT_IMAGE_PROFILE, false).get().addOnSuccessListener { documents ->
            documents.forEach { document ->

                val user = convertDocumentToUser(document)

                mFirebaseHelper.getImageStorageCollectionReference().listAll().addOnSuccessListener { (_, prefixes) ->

                    prefixes.forEach { prefix ->

                        Log.e(javaClass.simpleName, "compareImageList: prefix: ${prefix.name}")

                        prefix.list(1).addOnSuccessListener { (items2,_) ->

                            items2.forEach { item2 ->
                                if (user?.isDefaultProfileImage == false) {

                                    if (user.userPicture != item2.name && user.id.toString() == prefix.name) {
                                        hashMap[item2.name]
                                        item2.getBytes(oneMegabyte).addOnSuccessListener { byteArray ->
                                            Log.e(javaClass.simpleName, "compareImageList: user: ${user.pseudo} have picture:${user.userPicture} but new picture is:${item2.name}")
                                            hashMap[user.id.toString()] = byteArray
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun getAllImage(): MutableLiveData<HashMap<String, ByteArray>?> = listOfImageByteArray

    override fun createUserStatsToCurrentDate(user: User) {
        val userStats = UserStats(Date(), user.wallet)
        mFirebaseHelper.getUserStatsDocumentReference(user.id.toString(), user.numberOfGamePlayed.toString()).set(
            userStats
        )
        Log.e(javaClass.simpleName, "createUserStatsToCurrentDate: ${user.pseudo} stats created wallet: ${userStats.walletStateWhenGameEnding}, date: ${userStats.date} numberOfGamePlayed:${user.numberOfGamePlayed}")
    }

    override fun updateUserStats(user: User) {
        mFirebaseHelper.getUserStatsDocumentReference(user.id.toString(), user.numberOfGamePlayed.toString()).update(
            WALLET_STATE, user.wallet ?: 0.0
        )
    }

    override fun getUserStats(userId: String): LiveData<ArrayList<UserStats>> {
        mFirebaseHelper.getUserStatsCollectionReference(userId).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val listOfUserStats = ArrayList<UserStats>()
                for (userStats in task.result) {
                    listOfUserStats.add(userStats.toObject(UserStats::class.java))
                }
                userStatsComparator(listOfUserStats)
                listOfGamePlayed.postValue(listOfUserStats)
            }
        }
        return listOfGamePlayed
    }
}