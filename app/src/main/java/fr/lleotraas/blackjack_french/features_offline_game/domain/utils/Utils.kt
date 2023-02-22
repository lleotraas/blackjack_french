package fr.lleotraas.blackjack_french.features_offline_game.domain.utils

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.google.firebase.firestore.DocumentSnapshot
import fr.lleotraas.blackjack_french.R
import fr.lleotraas.blackjack_french.features_offline_game.domain.model.*
import fr.lleotraas.blackjack_french.features_online_game.domain.model.OnlineDeck
import fr.lleotraas.blackjack_french.features_online_main_screen.domain.model.*
import fr.lleotraas.blackjack_french.features_online_main_screen.presentation.utils.HandType

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
        const val MAIN_HAND = "main_hand"
        const val FIRST_SPLIT = "first_split"
        const val SECOND_SPLIT = "second_split"
        const val INSURANCE = "insurance_bet"
        const val TOTAL = "total_bet"
        const val HAND = "hand"
        const val FIRST_SPLIT_HAND = "firstSplitHand"
        const val SECOND_SPLIT_HAND = "secondSplitHand"
//        const val MAIN_HAND = 0
//        const val FIRST_SPLIT = 1
//        const val SECOND_SPLIT = 2
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
        const val DATE = "date"
        const val MESSAGE = "message"
        const val CUSTOM_USER_PICTURE = "custom_user_picture"
        const val WALLET_STATE = "walletStateWhenGameEnding"

        fun createDeck(): Deck {
            var color: ColorType
            var number: NumberType
            val deck = Deck()

//            deck.deckList.add(Card(NumberType.FOUR,ColorType.HEART,4))
            deck.deckList.add(Card(NumberType.TEN,ColorType.HEART,10))
            deck.deckList.add(Card(NumberType.FOUR,ColorType.HEART,4))
            deck.deckList.add(Card(NumberType.ACE,ColorType.SPADE,1))
            deck.deckList.add(Card(NumberType.ACE,ColorType.CLUB,1))
            deck.deckList.add(Card(NumberType.FOUR,ColorType.DIAMOND,4))
            deck.deckList.add(Card(NumberType.FIVE,ColorType.HEART,5))
//            deck.deckList.add(Card(NumberType.QUEEN,ColorType.HEART,10))
//            deck.deckList.add(Card(NumberType.HEIGHT,ColorType.HEART,8))
//            deck.deckList.add(Card(NumberType.JACK,ColorType.DIAMOND,10))
//            deck.deckList.add(Card(NumberType.THREE,ColorType.SPADE,3))
//            deck.deckList.add(Card(NumberType.KING,ColorType.SPADE,10))
//            deck.deckList.add(Card(NumberType.FOUR,ColorType.DIAMOND,4))
//            deck.deckList.add(Card(NumberType.THREE,ColorType.DIAMOND,3))
//            deck.deckList.add(Card(NumberType.TEN,ColorType.SPADE,10))


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
            bet[MAIN_HAND] = 0.0
            bet[FIRST_SPLIT_HAND] = 0.0
            bet[SECOND_SPLIT_HAND] = 0.0
            bet[INSURANCE] = 0.0
            bet[TOTAL] = 0.0
            return bet
        }

        fun createArrayListOfBet(size: Int): ArrayList<HashMap<String, Double>> {
            val arrayOfBet = ArrayList<HashMap<String, Double>>()
            for (i in 0 until size) {
                arrayOfBet.add(createBet())
            }
            return arrayOfBet
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

        fun incrementDeckIndex(deck: OnlineDeck) = deck.index?.plus(1)

        fun addCardToPlayerHandList(playerHand: Player, deck: OnlineDeck, handType: HandType): Player {
            playerHand.score[handType.valueOf] =
                playerHand.score[handType.valueOf]!!.plus(deck.deckList?.get(deck.index!!)?.value!!)
            playerHand.hand[handType.valueOf]?.add(deck.deckList!![deck.index!!])
            return playerHand
        }

        fun addCardToDealerList(dealer: Dealer, deck: OnlineDeck): Dealer {
            dealer.score += deck.deckList!![deck.index!!].value!!
            dealer.hand.add(deck.deckList!![deck.index!!])
            return dealer
        }

        fun addScoreToHand(playerHand: Player, handType: HandType): Int {
            return when (handType) {
                HandType.FirstSplit -> {
                    playerHand.score[FIRST_SPLIT] ?: 0
                }
                HandType.SecondSplit -> {
                    playerHand.score[SECOND_SPLIT] ?: 0
                }
                else -> {
                    playerHand.score[MAIN_HAND] ?: 0
                }
            }
        }

        fun playerDrawAnAceOrNot(deck: OnlineDeck, playerHand: Player, splitType: HandType): Boolean {
            var isAceDraw = playerHand.isPlayerDrawAce[splitType.valueOf]
            if (
                deck.deckList!![deck.index!!].number == NumberType.ACE &&
                !isAceDraw!!
            ) {
               isAceDraw = true
            }

            if (
                deck.deckList!![deck.index!!].number == NumberType.ACE &&
                playerHand.score[splitType.valueOf]!! > 11 &&
                isAceDraw!! &&
                !playerHand.isPlayerScoreSoft[splitType.valueOf]!!
            ) {
                isAceDraw = false
            }
            return isAceDraw!!
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

        fun playerHaveASoftScoreOrNot(playerHand: Player, deck: OnlineDeck, splitType: HandType): Boolean {
            var isScoreSoft = playerHand.isPlayerScoreSoft[splitType.valueOf]
            if (
                deck.deckList!![deck.index!!].number == NumberType.ACE &&
                playerHand.score[splitType.valueOf]!! < 12 &&
                playerHand.isPlayerDrawAce[splitType.valueOf]!!
            ) {
                playerHand.score[splitType.valueOf] = playerHand.score[splitType.valueOf]?.plus(10)!!
                isScoreSoft = true
            }
            if (
                playerHand.score[splitType.valueOf]!! > 21 &&
                playerHand.isPlayerDrawAce[splitType.valueOf]!!
            ) {
                playerHand.score[splitType.valueOf] = playerHand.score[splitType.valueOf]?.minus(10)!!
                isScoreSoft = false
            }
            return isScoreSoft!!
        }

        fun modifyFirstSplitScore(playerHand: Player, splitType: String) {
            playerHand.score[splitType]?.plus(10)
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
            if (playerHand.hand[MAIN_HAND]?.size!! > 2 && user.splitType == HandType.MainHand) {
                return false
            }
            if (playerHand.hand[FIRST_SPLIT]?.size!! > 2 && user.splitType == HandType.FirstSplit) {
                return false
            }
            if (playerHand.hand[SECOND_SPLIT]?.size!! > 2 && user.splitType == HandType.SecondSplit) {
                return false
            }
            if (playerHand.hand[FIRST_SPLIT]!!.isNotEmpty()) {
                if (playerHand.hand[FIRST_SPLIT]?.get(0)?.value == 1 && playerHand.hand[MAIN_HAND]?.get(0)?.value == 1) {
                    return false
                }
            }
            if (
                time >= 1.0 &&
                playerHand.hand[user.splitType?.valueOf]?.size == 2 &&
                playerHand.playerNumberType == user.playerTurn &&
                dealer.score < 12
            ) {
                return true
            }
            return false
        }

        fun compareScore(
            playerScore: Int,
            playerHandSize: Int,
            handType: String,
            isPlayerFirstSplit: Boolean,
            dealer: Dealer
        ): Int {
            return when  {
                dealer.score == 21 && dealer.hand.size == 2 &&
                playerScore == 21 && playerHandSize == 2 -> {
                    R.string.fragment_main_game_draw
                }
                dealer.score == 21 &&
                        dealer.hand.size == 2 -> {
                    R.string.fragment_main_game_you_lose
                }
                playerScore == 21 &&
                playerHandSize == 2 &&
                handType == MAIN_HAND &&
                !isPlayerFirstSplit -> {
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

        fun compareScore(playerScore: Int, playerHandSize: Int): Int {
            return when  {
                playerScore == 21 && playerHandSize == 2 -> {
                    R.string.online_game_fragment_blackjack
                }
                else -> {
                    R.string.fragment_main_game_you_win
                }
            }
        }

        fun getListOfPlayerScore(player: Player): ArrayList<Int> {
            val listOfPlayerScore = ArrayList<Int>()
            when{
                player.score[MAIN_HAND] in 1..21 -> listOfPlayerScore.add(player.score[MAIN_HAND]!!)
                player.score[FIRST_SPLIT] in 1..21 -> listOfPlayerScore.add(player.score[FIRST_SPLIT]!!)
                player.score[SECOND_SPLIT] in 1..21 -> listOfPlayerScore.add(player.score[SECOND_SPLIT]!!)
            }

            return listOfPlayerScore
        }

        fun splitPlayerGame(handType: HandType, playerHand: Player) {
            // Add card in second split from main hand or from first split.
            if (
                !playerHand.isPlayerSecondSplit && playerHand.isPlayerFirstSplit && handType == HandType.MainHand ||
                handType == HandType.FirstSplit
            ) {
                val splitCard = playerHand.hand[handType.valueOf]!![playerHand.hand[handType.valueOf]!!.size - 1]
                val cardSplitValue = playerHand.hand[handType.valueOf]!![playerHand.hand[handType.valueOf]!!.size - 1].value!!
                playerHand.hand[SECOND_SPLIT_HAND]?.add(splitCard)
                playerHand.score[SECOND_SPLIT_HAND] = playerHand.score[SECOND_SPLIT_HAND]!!.plus(cardSplitValue)
                playerHand.isPlayerSecondSplit = true
            }
            // Add card in first split from main hand.
            if (!playerHand.isPlayerFirstSplit) {
                val mainHandSplitCard = playerHand.hand[MAIN_HAND]!![playerHand.hand[MAIN_HAND]!!.size - 1]
                val mainHandCardSplitValue = playerHand.hand[MAIN_HAND]!![playerHand.hand[MAIN_HAND]!!.size - 1].value!!
                playerHand.hand[FIRST_SPLIT_HAND]?.add(mainHandSplitCard)
                playerHand.score[FIRST_SPLIT_HAND] = playerHand.score[FIRST_SPLIT_HAND]!!.plus(mainHandCardSplitValue)
                playerHand.isPlayerFirstSplit = true
            }
            // Refresh main hand score or first split score.
            if (handType == HandType.MainHand) {
                val mainHandCardRemoved = playerHand.hand[MAIN_HAND]!![playerHand.hand[MAIN_HAND]!!.size - 1]
                playerHand.score[MAIN_HAND] = playerHand.score[MAIN_HAND]!!.minus(mainHandCardRemoved.value!!)
                playerHand.hand[MAIN_HAND]?.remove(mainHandCardRemoved)
            } else {
                val firstSplitCardRemoved = playerHand.hand[FIRST_SPLIT_HAND]!![playerHand.hand[FIRST_SPLIT_HAND]!!.size - 1]
                playerHand.score[FIRST_SPLIT_HAND] = playerHand.score[FIRST_SPLIT_HAND]!!.minus(firstSplitCardRemoved.value!!)
                playerHand.hand[FIRST_SPLIT_HAND]?.remove(firstSplitCardRemoved)
            }
        }

        fun getPlayerHand(handType: HandType): String = when(handType) {
                HandType.MainHand -> MAIN_HAND
                HandType.FirstSplit -> FIRST_SPLIT
                else -> SECOND_SPLIT
            }

        fun getHandType(handType: HandType?) = when(handType) {
            HandType.MainHand -> 0
            HandType.FirstSplit -> 1
            else -> 2
        }

        fun getHandTypeForDoubledBox(handType: String) = when(handType) {
            SECOND_SPLIT -> FIRST_SPLIT
            else -> MAIN_HAND
        }

        fun getBetTypeByInt(index: Int) = when(index) {
            0 -> MAIN_HAND
            1 -> FIRST_SPLIT
            else -> SECOND_SPLIT
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
            user.wallet = user.wallet?.minus(user.bet!![TOTAL]!!)
            return user
        }

        fun updateInsurance(user: User, dealerHaveBlackjack: Boolean) =
            if (user.bet!![INSURANCE]!! > 0 && dealerHaveBlackjack)
                user.bet!![INSURANCE]!! * 3
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

        fun isPlayerHaveBlackjack(player: Player) =
            player.score[MAIN_HAND] == 21 &&
            player.hand[MAIN_HAND]?.size == 2 &&
            !player.isPlayerFirstSplit

        fun isDealerHaveBlackJack(dealer: Dealer) = dealer.score == 21 && dealer.hand.size == 2

        fun convertTimeInPercent(seconds: Double): Int {
            return (seconds / 20.0 * 100.0).toInt()
        }

        fun getTimeCount(seconds: Double): Int = abs(20 - seconds.toInt())

        fun getInvertedTabIndex(tabSize: Int, index: Int) = abs((tabSize - 1) - index)

        private fun createCustomUser(user: User, listOfImage: HashMap<String, ByteArray>?) = CustomUser(
            user.id.toString(),
            user.wallet.toString().toDouble(),
            user.pseudo.toString(),
            if (user.isDefaultProfileImage == true) user.userPicture else listOfImage?.get(user.id.toString()),
            user.onlineStatus!!,
            user.pictureRotation!!
        )

        fun createListOfCustomUser(listOfOnlineUser: ArrayList<User>?, allImage: HashMap<String, ByteArray>?, userId: String): ArrayList<CustomUser> {
            val listOFCustomUser = ArrayList<CustomUser>()
            if (listOfOnlineUser != null) {
                for (user in listOfOnlineUser) {
                    if (user.id.toString() != userId) {
                        Log.e("OnlineMainScreen", "updateUserList: username: ${user.pseudo}")
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
            listOfChat.forEach { chat ->
                listOfImage.forEach { image ->
                    if (chat.userPicture?.contains("http") == false &&
                                chat.id == image.key
                            ) {
                        chat.customUserPicture = image.value
                    }
                }
            }
            return listOfChat
        }

        fun convertDocumentToUser(documentSnapshot: DocumentSnapshot?): User? {
            return if(documentSnapshot?.data?.isNotEmpty() == true) User(
                documentSnapshot.data?.get(USER_ID).toString(),
                documentSnapshot.data?.get(NUMBER_OF_LOAN).toString().toInt(),
                documentSnapshot.data?.get(WALLET).toString().toDouble(),
                documentSnapshot.data?.get(BET) as HashMap<String, Double>,
                documentSnapshot.data?.get(PSEUDO).toString(),
                documentSnapshot.data?.get(USER_PICTURE).toString(),
                documentSnapshot.data?.get(PICTURE_ROTATION).toString().toFloat(),
                documentSnapshot.data?.get(ONLINE_STATUS).toString().toOnlineStatusType(),
                documentSnapshot.data?.get(OPPONENT).toString(),
                if (documentSnapshot.data?.get(PLAYER_TURN) != null) documentSnapshot.data?.get(
                    PLAYER_TURN
                ).toString().toPlayerNumberType() else null,
                if (documentSnapshot.data?.get(SPLIT_TYPE) != null) documentSnapshot.data?.get(
                    SPLIT_TYPE
                ).toString().toSplitType() else null,
                documentSnapshot.data?.get(NUMBER_OF_GAME_PLAYED).toString().toInt(),
                documentSnapshot.data?.get(IS_DEFAULT_IMAGE_PROFILE).toString().toBoolean(),
                documentSnapshot.data?.get(IS_GAME_HOST).toString().toBoolean(),
                documentSnapshot.data?.get(IS_USER_READY).toString().toBoolean(),
                documentSnapshot.data?.get(IS_SPLITTING).toString().toBoolean(),
            ) else null
        }

        fun convertDocumentToMessage(documentSnapshot: DocumentSnapshot) =
                Message(
                    documentSnapshot.data?.get(USER_ID).toString(),
                    documentSnapshot.data?.get(PSEUDO).toString(),
                    documentSnapshot.data?.get(DATE).toString(),
                    documentSnapshot.data?.get(MESSAGE).toString(),
                    documentSnapshot.data?.get(USER_PICTURE).toString(),
                    if (documentSnapshot.data?.get(CUSTOM_USER_PICTURE) != null) documentSnapshot.data?.get(
                        CUSTOM_USER_PICTURE
                    ).toString().toByteArray() else null,
                    if (documentSnapshot.data?.get(PICTURE_ROTATION) != null) documentSnapshot.data?.get(
                        PICTURE_ROTATION
                    ).toString().toFloat() else 0f
                )

        fun createPlayerHand(playerNumber: Int): Player = Player(
            createScoreMap(),
            createHandMap(),
            HandType.MainHand,
            PlayerNumberType.values()[playerNumber],
            isPlayerDrawAce = createBooleanMap(),
            isPlayerScoreSoft = createBooleanMap(),
            isPlayerFirstSplit = false,
            isPlayerSecondSplit = false
        )

        fun createArrayListOfPlayerHand(size: Int): ArrayList<Player> {
            val arrayOfPlayer = kotlin.collections.ArrayList<Player>()
            for (i in 0 until size) {
                arrayOfPlayer.add(createPlayerHand(i))
            }
            return arrayOfPlayer
        }

        private fun createBooleanMap(): HashMap<String, Boolean> {
            val map = HashMap<String, Boolean>()
            map[MAIN_HAND] = false
            map[FIRST_SPLIT_HAND] = false
            map[SECOND_SPLIT_HAND] = false
            return map
        }

        private fun createHandMap(): HashMap<String, ArrayList<Card>> {
            val map = HashMap<String, ArrayList<Card>>()
            map[MAIN_HAND] = ArrayList()
            map[FIRST_SPLIT_HAND] = ArrayList()
            map[SECOND_SPLIT_HAND] = ArrayList()
            return map
        }

        private fun createScoreMap(): HashMap<String, Int> {
            val map = HashMap<String, Int>()
            map[MAIN_HAND] = 0
            map[FIRST_SPLIT_HAND] = 0
            map[SECOND_SPLIT_HAND] = 0
            return map
        }

        fun createDealer() = Dealer(
            0,
            ArrayList(),
            isDealerDrawAce = false,
            isDealerScoreSoft = false
        )

        fun getHandTab(index: Int) =
            when(index) {
                0 -> MAIN_HAND
                1 -> FIRST_SPLIT_HAND
                else -> SECOND_SPLIT_HAND
            }

        private fun String.toOnlineStatusType() = OnlineStatusType.valueOf(this)

        private fun String.toPlayerNumberType() = PlayerNumberType.valueOf(this)

        private fun String.toSplitType(): HandType = HandType.MainHand.toHandType(this)


    }
}