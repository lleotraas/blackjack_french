package fr.lleotraas.blackjack_french.data.repository

import android.content.ContentValues
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.toObject
import fr.lleotraas.blackjack_french.model.*
import fr.lleotraas.blackjack_french.domain.repository.FirebaseHelper
import fr.lleotraas.blackjack_french.domain.repository.UserRepository
import fr.lleotraas.blackjack_french.utils.Utils
import fr.lleotraas.blackjack_french.utils.Utils.Companion.makeStringFromUri
import fr.lleotraas.blackjack_french.utils.Utils.Companion.userStatsComparator
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2
import fr.lleotraas.blackjack_french.utils.Utils.Companion.convertDocumentToUser

class UserRepositoryImpl @Inject constructor (
    private val mFirebaseHelper: FirebaseHelper
): UserRepository {

    private var currentUser = MutableLiveData<OnlineUser?>()
    private var searchedUser = MutableLiveData<OnlineUser?>()
    private var listOfOnlineUser = MutableLiveData<ArrayList<OnlineUser>>()
    private var listOfImageByteArray = MutableLiveData<HashMap<String ,ByteArray>>()
    private var listOfGamePlayed = MutableLiveData<ArrayList<UserStats>>()
    private var isUserUpdateNumberOfPlayedGames = false
    private var isTaskExecuted = MutableLiveData(false)
    private var fakeUser = MutableLiveData<User?>()

    override fun getFakeUser(): LiveData<User?> {
        return fakeUser
    }

    override fun createOnlineUser() {
        val connectedUser = mFirebaseHelper.getCurrentUser()
        val onlineUser =
            hashMapOf(
            "id" to connectedUser?.uid,
            "numberOfLoan" to 0,
            "wallet" to 1500.0,
            "bet" to Utils.createBet(),
            "pseudo" to connectedUser?.displayName,
            "profilePicture" to connectedUser?.photoUrl,
            "pictureRotation" to 0f,
            "onlineStatus" to OnlineStatusType.OFFLINE,
            "opponentId" to "",
            "playerTurn" to null,
            "splitType" to null,
            "numberOfGamePlayed" to 0,
            "isDefaultImageProfile" to true,
            "isGameHost" to false,
            "isUserReady" to false,
            "isSplitting" to false
        )

//            createUser(connectedUser!!)

        if (connectedUser != null) {
            mFirebaseHelper.getUserCollectionReference().document(connectedUser.uid).set(onlineUser)
            currentUser.postValue(OnlineUser(onlineUser))
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

    override fun getCurrentOnlineUser(userId: String): LiveData<OnlineUser?> {
        mFirebaseHelper.getUserCollectionReference().document(userId).get().addOnSuccessListener { documentSnapshot ->
            val userOnline = convertDocumentToUser(documentSnapshot)
            if (userOnline?.onlineUser?.size != 0) {
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

    override fun getSearchedUser(userId: String): LiveData<OnlineUser?> {
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

    override fun getAllOnlineUsers(): LiveData<ArrayList<OnlineUser>> {
        mFirebaseHelper.getUserCollectionReference().whereNotEqualTo("onlineStatus", OnlineStatusType.OFFLINE).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val onlineUserList = ArrayList<OnlineUser>()
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

    override fun allUserUpdated(): LiveData<ArrayList<OnlineUser>> {
        val allOnline = mFirebaseHelper.getUserCollectionReference().whereNotEqualTo("onlineStatus", OnlineStatusType.OFFLINE)
        allOnline.addSnapshotListener { value, exception ->
            if (exception != null) {
                Log.w(ContentValues.TAG, "Listen failed.",exception)
                return@addSnapshotListener
            }

            val onlineUserList = ArrayList<OnlineUser>()
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
            "onlineStatus", userOnlineStatus,
            "opponent", ""
        )
    }

    override fun updateUserPicture(
        user: User?
    ) {
        mFirebaseHelper.getUserCollectionReference().document(user?.id.toString()).update(
            "userPicture", user?.isDefaultProfileImage
        )
    }

    override fun updateOnlineStatusAskForPlay(
        currentUserId: String,
        searchedUserId: String,
        userOnlineStatus: OnlineStatusType
    ) {
        mFirebaseHelper.getUserCollectionReference().document(searchedUserId).update(
            "onlineStatus", OnlineStatusType.ASK_FOR_PLAY,
            "opponent", currentUserId,
            "playerTurn", PlayerNumberType.PLAYER_TWO
        )
        mFirebaseHelper.getUserCollectionReference().document(currentUserId).update(
            "onlineStatus", userOnlineStatus,
            "opponent", searchedUserId,
            "playerTurn", PlayerNumberType.PLAYER_ONE
        )
    }

    override fun updateOnlineStatusPlaying(currentUserId: String, searchedUserId: String) {
        mFirebaseHelper.getUserCollectionReference().document(searchedUserId).update(
            "onlineStatus", OnlineStatusType.PLAYING,
            "opponent", currentUserId
        )
        mFirebaseHelper.getUserCollectionReference().document(currentUserId).update(
            "onlineStatus", OnlineStatusType.PLAYING,
            "opponent", searchedUserId,
            "isUserReady", false,
            "bet", Utils.createBet(),
            "isSplitting", false
        )
    }

    override fun updateIsGameHost(currentUserId: String, userIsGameHost: Boolean) {
        mFirebaseHelper.getUserCollectionReference().document(currentUserId).update(
            "isGameHost", userIsGameHost,
            "isUserReady", false,
            "bet", Utils.createBet(),
            "isSplitting", false
        )
    }

    override fun updateUserIsReady(currentUserId: String, userIsReady: Boolean) {
        mFirebaseHelper.getUserCollectionReference().document(currentUserId).update("isUserReady", userIsReady)
    }

    override fun updateOnlineUserWalletAndIsSplitting(user: User?, isSplitting: Boolean) {
        mFirebaseHelper.getUserCollectionReference().document(user?.id!!).update(
            "bet", user.bet,
            "wallet", user.wallet,
            "isSplitting", isSplitting
        )
    }

    override fun updateOnlineUserBetAndWallet(user: User) {
        mFirebaseHelper.getUserCollectionReference().document(user.id!!).update(
            "bet", user.bet,
            "wallet", user.wallet
        )
    }

    override fun updateSplitType(user: User) {
        mFirebaseHelper.getUserCollectionReference().document(user.id!!).update(
            "splitType", user.splitType
        )
    }

    override fun updateIsSplitting(userId: String, isSplitting: Boolean) {
        mFirebaseHelper.getUserCollectionReference().document(userId).update(
            "isSplitting", isSplitting
        )
    }

    override fun updateOnlineUserWalletAndLoan(user: User) {
        mFirebaseHelper.getUserCollectionReference().document(user.id!!).update(
            "wallet", user.wallet,
            "numberOfLoan", user.numberOfLoan
        )
    }

    override fun updateOnlineUserHand(userId: String, userHand: ArrayList<Card>, userFirstSplitHand: ArrayList<Card>, userSecondSplitHand: ArrayList<Card>) {
        mFirebaseHelper.getUserCollectionReference().document(userId).update(
            "hand", userHand,
            "firstSplitHand", userFirstSplitHand,
            "secondSplitHand", userSecondSplitHand
        )
    }

    override fun updateUserForNewGame(
        user: User?,
        userReady: Boolean?
    ) {
        mFirebaseHelper.getUserCollectionReference().document(user?.id!!).update(
            "bet", user.bet,
            "wallet", user.wallet,
            "isUserReady", userReady,
        )
    }

    override fun updateNumberOfGamePlayed(user: User) {
        if (!isUserUpdateNumberOfPlayedGames) {
            isUserUpdateNumberOfPlayedGames  = true
            user.numberOfGamePlayed = user.numberOfGamePlayed?.plus(1)
            mFirebaseHelper.getUserCollectionReference().document(user.id.toString()).update(
                "numberOfGamePlayed", user.numberOfGamePlayed
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
            "bet", user.bet,
            "wallet", user.wallet,
            "splitType", splitType,
            "isSplitting", isSplitting
        )
    }

    override fun updateUser(currentUser: User?) {
        mFirebaseHelper.getUserCollectionReference().document(currentUser?.id!!).update(
            "pseudo", currentUser.pseudo,
            "isDefaultProfileImage", currentUser.isDefaultProfileImage,
            "pictureRotation", currentUser.pictureRotation
        )
    }

    override fun signOut(currentUserId: String): LiveData<Boolean> {
        mFirebaseHelper.getUserCollectionReference().document(currentUserId).update(
            "onlineStatus", OnlineStatusType.OFFLINE
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
            "userPicture", makeStringFromUri(uri)
        )
        val imageList = listOfImageByteArray.value!!
        imageList[userId.toString()] = image
        listOfImageByteArray.postValue(imageList)
    }

    override fun updateImageList() {
        val oneMegabyte: Long = 1024 * 1024
        val array: HashMap<String, ByteArray> = if (listOfImageByteArray.value?.isNotEmpty() == true) {
            listOfImageByteArray.value!!
        } else {
            HashMap()
        }
        mFirebaseHelper.getUserCollectionReference().whereEqualTo("isDefaultProfileImage", false).get().addOnSuccessListener { documents ->
            for (document in documents) {
                val user = document.toObject<User>()
                val url = mFirebaseHelper.getImageStorageCollectionReference().child(user.id.toString()).child(user.userPicture.toString())
                Log.e(javaClass.simpleName, "updateImageList: PATH: $url")
                url.getBytes(oneMegabyte).addOnSuccessListener { byteArray ->
                    array[user.id.toString()] = byteArray
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
        mFirebaseHelper.getUserCollectionReference().whereEqualTo("isDefaultProfileImage", false).get().addOnSuccessListener { documents ->
            documents.forEach { document ->

                val user = document.toObject<User>()

                mFirebaseHelper.getImageStorageCollectionReference().listAll().addOnSuccessListener { (_, prefixes) ->

                    prefixes.forEach { prefix ->

                        Log.e(javaClass.simpleName, "compareImageList: prefix: ${prefix.name}")

                        prefix.list(1).addOnSuccessListener { (items2,_) ->

                            items2.forEach { item2 ->
                                if (user.isDefaultProfileImage == false) {

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

    override fun getAllImage(): LiveData<HashMap<String, ByteArray>> = listOfImageByteArray

    override fun createUserStatsToCurrentDate(user: User) {
        val userStats = UserStats(Date(), user.wallet)
        mFirebaseHelper.getUserStatsDocumentReference(user.id.toString(), user.numberOfGamePlayed.toString()).set(
            userStats
        )
        Log.e(javaClass.simpleName, "createUserStatsToCurrentDate: ${user.pseudo} stats created wallet: ${userStats.walletStateWhenGameEnding}, date: ${userStats.date} numberOfGamePlayed:${user.numberOfGamePlayed}")
    }

    override fun updateUserStats(user: User) {
        mFirebaseHelper.getUserStatsDocumentReference(user.id.toString(), user.numberOfGamePlayed.toString()).update(
            "walletStateWhenGameEnding", user.wallet ?: 0.0
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