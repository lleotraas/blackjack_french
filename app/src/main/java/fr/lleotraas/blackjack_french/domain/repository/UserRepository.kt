package fr.lleotraas.blackjack_french.domain.repository

import android.net.Uri
import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseUser
import fr.lleotraas.blackjack_french.model.*

interface UserRepository {

    fun createOnlineUser()

    fun createUser(connectedUser: FirebaseUser): User

    fun getCurrentOnlineUser(userId: String): LiveData<User?>

    fun getSearchedUser(userId: String): LiveData<User?>

    fun getAllOnlineUsers(): LiveData<ArrayList<User>>

    fun allUserUpdated():LiveData<ArrayList<User>>

    fun updateOnlineStatusAskForPlay(
        currentUserId: String,
        userOnlineStatus: OnlineStatusType
    )

    fun updateUserPicture(
        user: User?
    )

    fun updateOnlineStatusAskForPlay(
        currentUserId: String,
        searchedUserId: String,
        userOnlineStatus: OnlineStatusType
    )

    fun updateOnlineStatusPlaying(currentUserId: String, searchedUserId: String)

    fun updateIsGameHost(currentUserId: String, userIsGameHost: Boolean)

    fun updateUserIsReady(currentUserId: String, userIsReady: Boolean)

    fun updateOnlineUserWalletAndIsSplitting(user: User?, isSplitting: Boolean)

    fun updateOnlineUserBetAndWallet(user: User)

    fun updateSplitType(user: User)

    fun updateIsSplitting(userId: String, isSplitting: Boolean)

    fun updateOnlineUserWalletAndLoan(user: User)

    fun updateOnlineUserHand(userId: String, userHand: ArrayList<Card>, userFirstSplitHand: ArrayList<Card>, userSecondSplitHand: ArrayList<Card>)

    fun updateUserForNewGame(user: User?, userReady: Boolean?)

    fun updateNumberOfGamePlayed(user: User)

    fun isNumberOfGamePlayedUpdated()

    fun resetCurrentUserAndHandType(user: User?, splitType: HandType, isSplitting: Boolean)

    fun updateUser(currentUser: User?)

    fun signOut(currentUserId: String): LiveData<Boolean>

    fun uploadImage(userId: String, file: Uri, imageResized: ByteArray): Double

    fun deleteImage(userId: String, file: Uri)

    fun updateUserImageList(userId: String?, uri: Uri, image: ByteArray)

    fun updateImageList()

    fun compareImageList()

    fun getAllImage(): LiveData<HashMap<String, ByteArray>?>

    fun createUserStatsToCurrentDate(user: User)

    fun updateUserStats(user: User)

    fun getUserStats(userId: String): LiveData<ArrayList<UserStats>>

}