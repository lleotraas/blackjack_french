package fr.lleotraas.blackjack_french.utils

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.google.firebase.firestore.DocumentSnapshot
import fr.lleotraas.blackjack_french.R
import fr.lleotraas.blackjack_french.model.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.abs

class Utils {

    companion object {

        private fun getColorType(value: Int) = ColorType.values().first { it.value == value }
        private fun getNumberType(value: Int) = NumberType.values().first { it.index == value }
        const val CURRENT_USER_ID = "user_id"
        const val SEARCHED_USER_ID = "searched_user_id"
        const val MAIN_HAND_BET = "player_bet"
        const val FIRST_SPLIT_BET = "first_split_bet"
        const val SECOND_SPLIT_BET = "second_split_bet"
        const val INSURANCE_BET = "insurance_bet"
        const val TOTAL_BET = "total_bet"
        const val MAIN_HAND = 0
        const val FIRST_SPLIT = 1
        const val SECOND_SPLIT = 2
        const val REQUEST_CODE_SIGN_IN = 2
        const val USER_ID = "id"
        const val NUMBER_OF_LOAN = "numberOfLoan"
        const val WALLET = "wallet"
        const val BET = "bet"
        const val PSEUDO = "pseudo"
        const val USER_PICTURE = "profilePicture"
        const val PICTURE_ROTATION = "pictureRotation"
        const val ONLINE_STATUS = "onlineStatus"
        const val OPPONENT = "opponent"
        const val PLAYER_TURN = "playerTurn"
        const val SPLIT_TYPE = "splitType"
        const val NUMBER_OF_GAME_PLAYED = "numberOfGamePlayed"
        const val IS_DEFAULT_IMAGE_PROFILE = "isDefaultImageProfile"
        const val IS_GAME_HOST = "isGameHost"
        const val IS_USER_READY = "isUserReady"
        const val IS_SPLITTING = "isSplitting"
        const val DECK_LIST = "deckList"
        const val INDEX = "index"

        fun createDeck(): Deck {
            var color: ColorType
            var number: NumberType
            val deck = Deck()

//            deck.deckList.add(Card(NumberType.FOUR,ColorType.HEART,4))
//            deck.deckList.add(Card(NumberType.FOUR,ColorType.HEART,4))
//            deck.deckList.add(Card(NumberType.FOUR,ColorType.HEART,4))
//            deck.deckList.add(Card(NumberType.FOUR,ColorType.HEART,4))
//            deck.deckList.add(Card(NumberType.FOUR,ColorType.HEART,4))
//            deck.deckList.add(Card(NumberType.TEN,ColorType.HEART,10))
//            deck.deckList.add(Card(NumberType.QUEEN,ColorType.HEART,10))
//            deck.deckList.add(Card(NumberType.FOUR,ColorType.HEART,4))
//            deck.deckList.add(Card(NumberType.ACE,ColorType.SPADE,1))
//            deck.deckList.add(Card(NumberType.ACE,ColorType.SPADE,1))
//            deck.deckList.add(Card(NumberType.JACK,ColorType.DIAMOND,10))
//            deck.deckList.add(Card(NumberType.QUEEN,ColorType.CLUB,10))
//            deck.deckList.add(Card(NumberType.KING,ColorType.HEART,10))
//            deck.deckList.add(Card(NumberType.TEN,ColorType.HEART,10))
//            deck.deckList.add(Card(NumberType.JACK,ColorType.HEART,10))
//            deck.deckList.add(Card(NumberType.QUEEN,ColorType.SPADE,10))
//            deck.deckList.add(Card(NumberType.KING,ColorType.HEART,10))
//            deck.deckList.add(Card(NumberType.TEN,ColorType.HEART,10))
//            deck.deckList.add(Card(NumberType.JACK,ColorType.HEART,10))
//            deck.deckList.add(Card(NumberType.QUEEN,ColorType.SPADE,10))
//            deck.deckList.add(Card(NumberType.KING,ColorType.HEART,10))

            for (k in 1..6) {
                for (i in 1..4) {
                    color = getColorType(i)
                    for (j in 1..13) {
                        number = getNumberType(j)
                        val card = Card(number, color, number.value)
                        deck.deckList.add(card)
                    }
                }
            }
            return deck
        }

        fun shuffleDeck(deck: ArrayList<Card>): ArrayList<Card> {
            deck.shuffle()
            return deck
        }

        fun createBet(): HashMap<String, Double> {
            val bet = HashMap<String, Double>()
            bet[MAIN_HAND_BET] = 0.0
            bet[FIRST_SPLIT_BET] = 0.0
            bet[SECOND_SPLIT_BET] = 0.0
            bet[INSURANCE_BET] = 0.0
            bet[TOTAL_BET] = 0.0
            return bet
        }

        fun getCardText(card: Card): String {
            val cardText = when (card.number) {
                NumberType.ACE -> "A"
                NumberType.JACK -> "J"
                NumberType.QUEEN -> "Q"
                NumberType.KING -> "K"
                else -> card.value.toString()
            }
            return cardText
        }

        fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
            observe(lifecycleOwner, object : Observer<T> {
                override fun onChanged(t: T) {
                    observer.onChanged(t)
                    removeObserver(this)
                }
            })
        }

        fun addCardToPlayerHandList(playerHand: Player, deck: OnlineDeck, handType: HandType): Player {
            when (handType) {
                // Card draw on the first split
                HandType.FIRST_SPLIT -> {
                    playerHand.score[FIRST_SPLIT] += deck.deckList!![deck.index!!].value!!
                    playerHand.hand[FIRST_SPLIT].add(deck.deckList!![deck.index!!])
                }
                //Card draw on the second split
                HandType.SECOND_SPLIT -> {
                    playerHand.score[SECOND_SPLIT] += deck.deckList!![deck.index!!].value!!
                    playerHand.hand[SECOND_SPLIT].add(deck.deckList!![deck.index!!])
                }
                // Card draw on the main hand
                else -> {
                    playerHand.score[MAIN_HAND] += deck.deckList!![deck.index!!].value!!
                    playerHand.hand[MAIN_HAND].add(deck.deckList!![deck.index!!])
                }
            }
            return playerHand
        }

        fun addCardToDealerList(dealer: Dealer, deck: OnlineDeck): Dealer {
            dealer.score += deck.deckList!![deck.index!!].value!!
            dealer.hand.add(deck.deckList!![deck.index!!])
            return dealer
        }

        fun addScoreToHand(playerHand: Player, handType: HandType): Int {
            return when (handType) {
                HandType.FIRST_SPLIT -> {
                    playerHand.score[FIRST_SPLIT]
                }
                HandType.SECOND_SPLIT -> {
                    playerHand.score[SECOND_SPLIT]
                }
                else -> {
                    playerHand.score[MAIN_HAND]
                }
            }
        }

        fun playerDrawAnAceOrNot(deck: OnlineDeck, playerHand: Player, splitType: HandType): Boolean {
            var isAceDraw = playerHand.isPlayerDrawAce[splitType.ordinal]
            if (
                deck.deckList!![deck.index!!].number == NumberType.ACE &&
                !isAceDraw
            ) {
               isAceDraw = true
            }

            if (
                deck.deckList!![deck.index!!].number == NumberType.ACE &&
                playerHand.score[splitType.ordinal] > 11 &&
                isAceDraw &&
                !playerHand.isPlayerScoreSoft[splitType.ordinal]
            ) {
                isAceDraw = false
            }
            return isAceDraw
        }

        fun dealerDrawAnAceOrNot(deck: OnlineDeck, dealer: Dealer): Boolean {
            var isAceDraw = dealer.isDealerDrawAce
            if (deck.deckList!![deck.index!!].number == NumberType.ACE && !dealer.isDealerDrawAce) {
                isAceDraw = true
            }

            if (deck.deckList!![deck.index!!].number == NumberType.ACE && dealer.score > 11 && dealer.isDealerDrawAce && !dealer.isDealerScoreSoft) {
                isAceDraw = false
            }
            return isAceDraw
        }

        fun modifyPlayerScoreWithAce(playerHand: Player, deck: OnlineDeck, splitType: HandType): Boolean {
            var isScoreSoft = playerHand.isPlayerScoreSoft[splitType.ordinal]
            if (
                deck.deckList!![deck.index!!].number == NumberType.ACE &&
                playerHand.score[splitType.ordinal] < 12 &&
                playerHand.isPlayerDrawAce[splitType.ordinal]
            ) {
                playerHand.score[splitType.ordinal] += 10
                isScoreSoft = true
            }
            if (
                playerHand.score[splitType.ordinal] > 21 &&
                playerHand.isPlayerDrawAce[splitType.ordinal]
            ) {
                playerHand.score[splitType.ordinal] -= 10
                isScoreSoft = false
            }

            return isScoreSoft
        }

        fun modifyFirstSplitScore(playerHand: Player, splitType: Int) {
            playerHand.score[splitType] += 10
        }

        fun modifyDealerScoreWithAce(dealer: Dealer, deck: OnlineDeck): Boolean {
            var isScoreSoft = dealer.isDealerScoreSoft
            if (
                deck.deckList!![deck.index!!].number == NumberType.ACE &&
                dealer.score < 12 && dealer.isDealerDrawAce
            ) {
                dealer.score += 10
                isScoreSoft = true
            }

            if (
                dealer.score > 21 &&
                dealer.isDealerDrawAce
            ) {
                dealer.score -= 10
                isScoreSoft = false
            }

            return isScoreSoft
        }

        fun disableDoubleBtn(playerHand: Player, user: User, dealer: Dealer, time: Double): Boolean {
            if (playerHand.hand[MAIN_HAND].size > 2 && user.splitType == HandType.MAIN_HAND) {
                return false
            }
            if (playerHand.hand[FIRST_SPLIT].size > 2 && user.splitType == HandType.FIRST_SPLIT) {
                return false
            }
            if (playerHand.hand[SECOND_SPLIT].size > 2 && user.splitType == HandType.SECOND_SPLIT) {
                return false
            }
            if (playerHand.hand[FIRST_SPLIT].isNotEmpty()) {
                if (playerHand.hand[FIRST_SPLIT][0].value == 1 && playerHand.hand[MAIN_HAND][0].value == 1) {
                    return false
                }
            }
            if (
                time >= 1.0 &&
                playerHand.hand[getHandType(user.splitType)].size == 2 &&
                playerHand.playerNumberType == user.playerTurn &&
                dealer.score < 12
            ) {
                return true
            }
            return false
        }

        fun compareScore(playerScore: Int, playerHandSize: Int, dealer: Dealer): Int {
            return when  {
                dealer.score == 21 && dealer.hand.size == 2 && playerScore == 21 && playerHandSize == 2 -> {
                    R.string.fragment_main_game_draw
                }
                dealer.score == 21 && dealer.hand.size == 2 -> {
                    R.string.fragment_main_game_you_lose
                }
                playerScore == 21 && playerHandSize == 2 -> {
                    R.string.online_game_fragment_blackjack
                }
                dealer.score > playerScore -> {
                    R.string.fragment_main_game_you_lose
                }
                dealer.score < playerScore -> {
                    R.string.fragment_main_game_you_win
                }
                else -> {
                    R.string.fragment_main_game_draw
                }
            }
        }

        fun getListOfPlayerScore(player: Player): ArrayList<Int> {
            val listOfPlayerScore = ArrayList<Int>()
            when{
                player.score[MAIN_HAND] in 1..21 -> listOfPlayerScore.add(player.score[MAIN_HAND])
                player.score[FIRST_SPLIT] in 1..21 -> listOfPlayerScore.add(player.score[FIRST_SPLIT])
                player.score[SECOND_SPLIT] in 1..21 -> listOfPlayerScore.add(player.score[SECOND_SPLIT])
            }

            return listOfPlayerScore
        }

        fun splitPlayerGame(handType: HandType, playerHand: Player) {
            // Add card in second split from main hand or from first split.
            if (
                !playerHand.isPlayerSecondSplit && playerHand.isPlayerFirstSplit && handType == HandType.MAIN_HAND ||
                handType == HandType.FIRST_SPLIT
            ) {
                playerHand.hand[SECOND_SPLIT].add(playerHand.hand[handType.ordinal][playerHand.hand[handType.ordinal].size - 1])
                playerHand.score[SECOND_SPLIT] += playerHand.hand[handType.ordinal][playerHand.hand[handType.ordinal].size - 1].value!!
                playerHand.isPlayerSecondSplit = true
            }
            // Add card in first split from main hand.
            if (!playerHand.isPlayerFirstSplit) {
                playerHand.hand[FIRST_SPLIT].add(playerHand.hand[MAIN_HAND][playerHand.hand[MAIN_HAND].size - 1])
                playerHand.score[FIRST_SPLIT] += playerHand.hand[MAIN_HAND][playerHand.hand[MAIN_HAND].size - 1].value!!
                playerHand.isPlayerFirstSplit = true
            }
            // Refresh main hand score or first split score.
            if (handType == HandType.MAIN_HAND) {
                playerHand.score[MAIN_HAND] -= playerHand.hand[MAIN_HAND][playerHand.hand[MAIN_HAND].size - 1].value!!
                playerHand.hand[MAIN_HAND].remove(playerHand.hand[MAIN_HAND][playerHand.hand[MAIN_HAND].size - 1])
            } else {
                playerHand.score[FIRST_SPLIT] -= playerHand.hand[FIRST_SPLIT][playerHand.hand[FIRST_SPLIT].size - 1].value!!
                playerHand.hand[FIRST_SPLIT].remove(playerHand.hand[FIRST_SPLIT][playerHand.hand[FIRST_SPLIT].size - 1])
            }
        }

        fun getPlayerHand(handType: HandType): String = when(handType) {
                HandType.MAIN_HAND -> MAIN_HAND_BET
                HandType.FIRST_SPLIT -> FIRST_SPLIT_BET
                else -> SECOND_SPLIT_BET
            }

        fun getHandType(handType: HandType?) = when(handType) {
            HandType.MAIN_HAND -> 0
            HandType.FIRST_SPLIT -> 1
            else -> 2
        }

        fun getHandTypeForDoubledBox(handType: HandType) = when(handType) {
            HandType.SECOND_SPLIT -> 1
            else -> 0
        }

        fun getBetTypeByInt(index: Int) = when(index) {
            0 -> MAIN_HAND_BET
            1 -> FIRST_SPLIT_BET
            else -> SECOND_SPLIT_BET
        }

        fun getNextPlayer(player: PlayerNumberType): PlayerNumberType = when(player) {
            PlayerNumberType.PLAYER_ONE -> PlayerNumberType.PLAYER_TWO
            PlayerNumberType.PLAYER_TWO -> PlayerNumberType.DEALER
            else -> PlayerNumberType.PLAYER_ONE
        }

        fun retrieveBetInWallet(wallet: Double, bet: Double): Double =
            wallet - bet

        fun retrieveInsuranceBetInWallet(wallet: Double, bet: Double) =
            wallet - (bet / 2.0)

        fun addBetInWallet(wallet: Double, bet: Double): Double =
            wallet + bet

        fun changeTotalBet(totalBet: Double, bet: Double) =
            totalBet + bet

        fun paymentForPlayer(messageResult: String, bet: Double): Double {
            return when(messageResult) {
                "WIN" -> bet * 2.0
                "LOSE" -> 0.0
                "BJ" -> (bet * 2.0) + (bet * 0.5)
                else -> bet
            }
        }

        fun updateWallet(user: User): User {
            for (userBet in user.bet!!.entries) {
                user.wallet = user.wallet?.plus(userBet.value)
            }
            user.wallet = user.wallet?.minus(user.bet!![TOTAL_BET]!!)
            return user
        }

        fun updateInsurance(user: User, dealerHaveBlackjack: Boolean) =
            if (user.bet!![INSURANCE_BET]!! > 0 && dealerHaveBlackjack)
                user.bet!![INSURANCE_BET]!! * 3
            else
                0.0


        fun makeBet(
            unity: Int,
            dozens: Int,
            hundred: Int,
            thousand: Int,
            tenOfThousand: Int
        ) = (tenOfThousand * 10000.0 + thousand * 1000.0 + hundred * 100.0 + dozens * 10.0 + unity)

        fun getArrayOfBetString(bet: String): CharArray {
            val string = bet.replaceAfter(".", "").replace(".","")
            return string.toCharArray()
        }

        fun getIndex(betArraySize: Int, betTabTvSize: Int, index: Int) = (betTabTvSize - betArraySize) + index

        fun isDealerHaveBlackJack(dealer: Dealer) = dealer.score == 21 && dealer.hand.size == 2

        fun convertTimeInPercent(seconds: Double): Int {
            return (seconds / 20.0 * 100.0).toInt()
        }

        fun getTimeCount(seconds: Double): Int = abs(20 - seconds.toInt())

        fun getInvertedTabIndex(tabSize: Int, index: Int) = abs((tabSize - 1) - index)

        private fun createCustomUser(user: OnlineUser, listOfImage: HashMap<String, ByteArray>) = CustomUser(
            user.onlineUser[USER_ID]!!.toString(),
            user.onlineUser[WALLET]!!.toString().toDouble(),
            user.onlineUser[PSEUDO]!!.toString(),
            if (user.onlineUser[IS_DEFAULT_IMAGE_PROFILE].toString().toBoolean()) user.onlineUser[USER_PICTURE] else listOfImage[user.onlineUser[USER_ID].toString()],
            OnlineStatusType.valueOf(user.onlineUser[ONLINE_STATUS]!!.toString()),
            user.onlineUser[PICTURE_ROTATION]!!.toString().toFloat()
        )

        fun createListOfCustomUser(listOfOnlineUser: ArrayList<OnlineUser>?, allImage: HashMap<String, ByteArray>, userId: String): ArrayList<CustomUser> {
            val listOFCustomUser = ArrayList<CustomUser>()
            if (listOfOnlineUser != null) {
                for (user in listOfOnlineUser) {
                    if (user.onlineUser[USER_ID].toString() != userId) {
                        Log.e("OnlineMainScreen", "updateUserList: username: ${user.onlineUser[PSEUDO]}")
                        listOFCustomUser.add(createCustomUser(user, allImage))
                    }
                }
            }
            Log.e("OnlineMainScreen", "updateUserList: listOfCustomUser size: ${listOFCustomUser.size}")
            return listOFCustomUser
        }

        fun makeStringFromUri(uri: Uri): String {
            return uri.lastPathSegment.toString().replaceBefore("/", "").replace("/","")
        }

        fun formatDate(date: String): String {
            return date
                .replaceAfter(":","")
                .replaceAfterLast(" ", "")
                .replaceAfterLast(" ","")
                .replaceBefore(" ","")
        }

        fun userStatsComparator(userStatsList: ArrayList<UserStats>) {
            Collections.sort(userStatsList, UserStatsComparator())
        }

        fun loadCustomPhotoInChat(listOfChat: ArrayList<Message>, listOfImage: HashMap<String, ByteArray>): ArrayList<Message> {
            for (chat in listOfChat) {
                listOfImage.forEach {
                    if (chat.userPicture?.contains("http") == false &&
                                chat.id == it.key
                            ) {
                        chat.customUserPicture = it.value
                    }
                }
            }
            return listOfChat
        }

        fun convertDocumentToUser(documentSnapshot: DocumentSnapshot): OnlineUser? {
            val hashMap = HashMap<String, Any?>()
            documentSnapshot.data?.entries?.forEach { entry ->
                hashMap[entry.key] = entry.value
            }
            return OnlineUser(hashMap)
//            return User(
//                documentSnapshot.data?.get(USER_ID).toString(),
//                documentSnapshot.data?.get(NUMBER_OF_LOAN).toString().toInt(),
//                documentSnapshot.data?.get(WALLET).toString().toDouble(),
//                documentSnapshot.data?.get(BET) as HashMap<String, Double>,
//                documentSnapshot.data?.get(PSEUDO).toString(),
//                documentSnapshot.data?.get(USER_PICTURE).toString(),
//                documentSnapshot.data?.get(PICTURE_ROTATION).toString().toFloat(),
//                OnlineStatusType.valueOf(documentSnapshot.data?.get(ONLINE_STATUS).toString()),
//                documentSnapshot.data?.get(OPPONENT).toString(),
//                if (documentSnapshot.data?.get(PLAYER_TURN) != null) PlayerNumberType.valueOf(documentSnapshot.data?.get(PLAYER_TURN).toString()) else null,
//                if (documentSnapshot.data?.get(SPLIT_TYPE) != null) HandType.valueOf(documentSnapshot.data?.get(SPLIT_TYPE).toString()) else null,
//                documentSnapshot.data?.get(NUMBER_OF_GAME_PLAYED).toString().toInt(),
//                documentSnapshot.data?.get(IS_DEFAULT_IMAGE_PROFILE).toString().toBoolean(),
//                documentSnapshot.data?.get(IS_GAME_HOST).toString().toBoolean(),
//                documentSnapshot.data?.get(IS_USER_READY).toString().toBoolean(),
//                documentSnapshot.data?.get(IS_SPLITTING).toString().toBoolean(),
//            )
        }
    }
}