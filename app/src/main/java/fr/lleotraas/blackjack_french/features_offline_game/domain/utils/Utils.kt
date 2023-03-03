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
        const val DEFAULT_BET = "default_bet"
        const val HAND = "hand"
        const val FIRST_SPLIT_HAND = "first_split_hand"
        const val SECOND_SPLIT_HAND = "second_split_hand"
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
//            deck.deckList.add(Card(NumberType.ACE,ColorType.CLUB,1))
//            deck.deckList.add(Card(NumberType.FOUR,ColorType.HEART,4))
//            deck.deckList.add(Card(NumberType.ACE,ColorType.CLUB,1))
//            deck.deckList.add(Card(NumberType.ACE,ColorType.CLUB,1))
//            deck.deckList.add(Card(NumberType.ACE,ColorType.CLUB,1))
//            deck.deckList.add(Card(NumberType.SIX,ColorType.HEART,6))
//            deck.deckList.add(Card(NumberType.FIVE,ColorType.HEART,5))
//            deck.deckList.add(Card(NumberType.ACE,ColorType.CLUB,1))
//            deck.deckList.add(Card(NumberType.ACE,ColorType.SPADE,1))
//            deck.deckList.add(Card(NumberType.FIVE,ColorType.HEART,5))
//            deck.deckList.add(Card(NumberType.THREE,ColorType.DIAMOND,3))
//            deck.deckList.add(Card(NumberType.FOUR,ColorType.DIAMOND,4))
//            deck.deckList.add(Card(NumberType.HEIGHT,ColorType.DIAMOND,8))
//            deck.deckList.add(Card(NumberType.THREE,ColorType.SPADE,3))
//            deck.deckList.add(Card(NumberType.THREE,ColorType.SPADE,3))
//            deck.deckList.add(Card(NumberType.FOUR,ColorType.DIAMOND,4))
//            deck.deckList.add(Card(NumberType.FOUR,ColorType.HEART,4))
//            deck.deckList.add(Card(NumberType.KING,ColorType.SPADE,10))
//            deck.deckList.add(Card(NumberType.KING,ColorType.SPADE,10))
//            deck.deckList.add(Card(NumberType.TEN,ColorType.HEART,10))
//            deck.deckList.add(Card(NumberType.TEN,ColorType.HEART,10))
//            deck.deckList.add(Card(NumberType.ACE,ColorType.CLUB,1))
//            deck.deckList.add(Card(NumberType.ACE,ColorType.CLUB,1))
//            deck.deckList.add(Card(NumberType.TEN,ColorType.HEART,10))
//            deck.deckList.add(Card(NumberType.TEN,ColorType.HEART,10))
//            deck.deckList.add(Card(NumberType.KING,ColorType.SPADE,10))
//            deck.deckList.add(Card(NumberType.KING,ColorType.SPADE,10))
//            deck.deckList.add(Card(NumberType.KING,ColorType.SPADE,10))
//            deck.deckList.add(Card(NumberType.KING,ColorType.SPADE,10))
//            deck.deckList.add(Card(NumberType.KING,ColorType.SPADE,10))
//            deck.deckList.add(Card(NumberType.KING,ColorType.SPADE,10))
//            deck.deckList.add(Card(NumberType.TEN,ColorType.HEART,10))
//            deck.deckList.add(Card(NumberType.KING,ColorType.SPADE,10))
//            deck.deckList.add(Card(NumberType.KING,ColorType.SPADE,10))
//            deck.deckList.add(Card(NumberType.KING,ColorType.SPADE,10))
//            deck.deckList.add(Card(NumberType.KING,ColorType.SPADE,10))
//            deck.deckList.add(Card(NumberType.KING,ColorType.SPADE,10))
//            deck.deckList.add(Card(NumberType.KING,ColorType.SPADE,10))



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

        fun createBet(defaultBetValue: Double): HashMap<String, Double> {
            val bet = HashMap<String, Double>()
            bet[MAIN_HAND] = 0.0
            bet[FIRST_SPLIT_HAND] = 0.0
            bet[SECOND_SPLIT_HAND] = 0.0
            bet[INSURANCE] = 0.0
            bet[DEFAULT_BET] = defaultBetValue
            bet[TOTAL] = 0.0
            return bet
        }

        fun createArrayListOfBet(size: Int): ArrayList<HashMap<String, Double>> {
            val arrayOfBet = ArrayList<HashMap<String, Double>>()
            for (i in 0 until size) {
                arrayOfBet.add(createBet(0.0))
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

        fun addCardToPlayerHandList(playerHand: CustomPlayer, deck: OnlineDeck, handType: HandType): CustomPlayer {
            playerHand.score =
                playerHand.score.plus(deck.deckList?.get(deck.index!!)?.value!!)
            playerHand.hand.add(deck.deckList!![deck.index!!])
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

        fun playerDrawAnAceOrNot(deck: OnlineDeck, playerHand: CustomPlayer): Boolean {
            var isAceDraw = playerHand.isPlayerDrawAce
            if (
                deck.deckList!![deck.index!!].number == NumberType.ACE &&
                !isAceDraw
            ) {
               isAceDraw = true
            }

            if (
                deck.deckList!![deck.index!!].number == NumberType.ACE &&
                playerHand.score > 11 &&
                isAceDraw &&
                !playerHand.isPlayerScoreSoft
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

        fun playerHaveASoftScoreOrNot(playerHand: CustomPlayer, deck: OnlineDeck): Boolean {
            var isScoreSoft = playerHand.isPlayerScoreSoft
            if (
                deck.deckList!![deck.index!!].number == NumberType.ACE &&
                playerHand.score < 12 &&
                playerHand.isPlayerDrawAce
            ) {
                playerHand.score = playerHand.score.plus(10)
                isScoreSoft = true
            }
            if (
                playerHand.score > 21 &&
                playerHand.isPlayerDrawAce
            ) {
                playerHand.score = playerHand.score.minus(10)
                isScoreSoft = false
            }
            return isScoreSoft
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
                playerScore == 21 && playerHandSize == 2 &&
                !isPlayerFirstSplit-> {
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

        fun splitPlayerGame(arrayOfPlayer: ArrayList<CustomPlayer>, handType: HandType, currentPlayer: CustomPlayer) {
            // Add card in second split from main hand or from first split.
            var playerToAdd: CustomPlayer
            val handOfNewSplit = ArrayList<Card>()
            val currentPlayerIndex = getPlayerIndex(arrayOfPlayer, currentPlayer)
            if (
                !currentPlayer.isPlayerSecondSplit && currentPlayer.isPlayerFirstSplit && handType == HandType.MainHand ||
                handType == HandType.FirstSplit
            ) {
                currentPlayer.isPlayerSecondSplit = true
                if(handType == HandType.MainHand) {
                    arrayOfPlayer[currentPlayerIndex + 1].isPlayerSecondSplit = true
                } else {
                    currentPlayer.isPlayerFirstSplit = true
                }
                val splitCard = currentPlayer.hand[currentPlayer.hand.size - 1]
                splitCard.isAnimate = true
//                val cardSplitValue = playerHand.hand[playerHand.hand.size - 1].value!!
                handOfNewSplit.add(splitCard)
                playerToAdd = createCustomPlayer(arrayOfPlayer, getPlayerIndex(arrayOfPlayer, currentPlayer), handOfNewSplit)
                arrayOfPlayer.add(getPlayerIndex(arrayOfPlayer, currentPlayer) + if(handType == HandType.MainHand) 2 else 1, playerToAdd)
//                playerHand.score = playerHand.score.plus(cardSplitValue)
            }
            // Add card in first split from main hand.
            if (!currentPlayer.isPlayerFirstSplit) {
                currentPlayer.isPlayerFirstSplit = true
                val mainHandSplitCard = currentPlayer.hand[currentPlayer.hand.size - 1]
                mainHandSplitCard.isAnimate = true
                val mainHandCardSplitValue = currentPlayer.hand[currentPlayer.hand.size - 1].value!!
                handOfNewSplit.add(mainHandSplitCard)
                playerToAdd = createCustomPlayer(arrayOfPlayer, getPlayerIndex(arrayOfPlayer, currentPlayer), handOfNewSplit)
                arrayOfPlayer.add(getPlayerIndex(arrayOfPlayer, currentPlayer) + 1, playerToAdd)
//                playerHand.score = playerHand.score.plus(mainHandCardSplitValue)
            }
            // Refresh main hand score or first split score.
            if (handType == HandType.MainHand) {
                val mainHandCardRemoved = currentPlayer.hand[currentPlayer.hand.size - 1]
                currentPlayer.score = currentPlayer.score.minus(mainHandCardRemoved.value!!)
                currentPlayer.hand.remove(mainHandCardRemoved)
            } else {
                val firstSplitCardRemoved = currentPlayer.hand[currentPlayer.hand.size - 1]
                currentPlayer.score = currentPlayer.score.minus(firstSplitCardRemoved.value!!)
                currentPlayer.hand.remove(firstSplitCardRemoved)
            }
        }

        fun playerHave2Aces(offlineUser: OfflineUser): Boolean {
            val firstCard =
                getCurrentPlayer(offlineUser, offlineUser.currentHandType).hand[0]
            val secondCard =
                getCurrentPlayerSplit(offlineUser, HandType.FirstSplit).hand[0]
            return firstCard.value == 1 && secondCard.value == 1
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
        fun walletWithdrawal(bet: Double, numberOfPlayer: Int) = bet * numberOfPlayer
        fun retrieveBetInWallet(wallet: Double, bet: Double): Double =
            wallet - bet

        fun retrieveInsuranceBetInWallet(wallet: Double, bet: Double) =
            wallet - (bet / 2.0)

        fun addBetInWallet(wallet: Double, bet: Double): Double =
            wallet + bet

        fun changeTotalBet(totalBet: Double, bet: Double) =
            totalBet + bet

        fun paymentForPlayer(resultScore: Int, bet: Double): Double {
            return when(resultScore) {
                R.string.fragment_main_game_you_win -> bet
                R.string.fragment_main_game_you_lose -> -bet
                R.string.online_game_fragment_blackjack -> bet + bet * 0.5
                else -> 0.0
            }
        }

        fun updateOnlineWallet(user: User): User {
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
        ) = tenOfThousand * 10000.0 + thousand * 1000.0 + hundred * 100.0 + dozens * 10.0 + unity

        fun makeTotalBet(
            unity: Int,
            dozens: Int,
            hundred: Int,
            thousand: Int,
            tenOfThousand: Int,
            numberOfBox: Int
        ) = (tenOfThousand * 10000.0 + thousand * 1000.0 + hundred * 100.0 + dozens * 10.0 + unity) * numberOfBox

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

        fun createPlayerHand(playerNumber: Int, defaultBetValue: Double): Player = Player(
            createScoreMap(),
            createHandMap(),
            createBet(defaultBetValue),
            PlayerNumberType.values()[playerNumber],
            createResultScoreMap(),
            isPlayerDrawAce = createBooleanMap(),
            isPlayerScoreSoft = createBooleanMap(),
            isPlayerFirstSplit = false,
            isPlayerSecondSplit = false
        )

        private fun createResultScoreMap(): java.util.HashMap<String, Int> {
            val map = HashMap<String, Int>()
            map[MAIN_HAND] = R.string.fragment_main_game_none
            map[FIRST_SPLIT_HAND] = R.string.fragment_main_game_none
            map[SECOND_SPLIT_HAND] = R.string.fragment_main_game_none
            return map
        }

        fun createArrayListOfPlayerHand(size: Int, defaultBetValue: Double): ArrayList<Player> {
            val arrayOfPlayer = kotlin.collections.ArrayList<Player>()
            for (i in 0 until size) {
                arrayOfPlayer.add(createPlayerHand(i, defaultBetValue))
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

        fun createCustomPlayerList(listSize: Int, defaultBet: Double): ArrayList<CustomPlayer> {
            val arrayOfCustomPlayer = ArrayList<CustomPlayer>()
            for (playerIndex in 0 until listSize) {
                arrayOfCustomPlayer.add(
                    CustomPlayer(
                        bet = defaultBet,
                        score = 0,
                        insuranceBet = 0.0,
                        isDouble = false,
                        hand = ArrayList(),
                        playerNumber = getPlayerNumberType(playerIndex),
                        playerHandType = HandType.MainHand,
                        isCurrentPlayer = getPlayerNumberType(playerIndex) == PlayerNumberType.PLAYER_ONE,
                        resultScore = R.string.fragment_main_game_none,
                        isPlayerDrawAce = false,
                        isPlayerScoreSoft = false,
                        isPlayerFirstSplit = false,
                        isPlayerSecondSplit = false,
                        isInsuranceOpen = false
                    )
                )
            }
            return arrayOfCustomPlayer
        }

        fun updateCurrentPlayerInArrayListOfCustomPlayer(arrayOfCustomPlayer: ArrayList<CustomPlayer>, currentPlayerNumber: PlayerNumberType, currentHandType: HandType) {
            for (customPlayerIndex in 0 until arrayOfCustomPlayer.size) {
                arrayOfCustomPlayer[customPlayerIndex].isCurrentPlayer =
                    arrayOfCustomPlayer[customPlayerIndex].playerNumber == currentPlayerNumber &&
                            arrayOfCustomPlayer[customPlayerIndex].playerHandType == currentHandType
            }
        }

        fun updateCurrentPlayerScoreInArrayListOfCustomPlayer(arrayOfCustomPlayer: ArrayList<CustomPlayer>, currentPlayerNumber: PlayerNumberType, currentHandType: HandType, newScore: Int) {
            for (customPlayerIndex in 0 until arrayOfCustomPlayer.size) {
                if (
                    arrayOfCustomPlayer[customPlayerIndex].playerNumber == currentPlayerNumber &&
                    arrayOfCustomPlayer[customPlayerIndex].playerHandType == currentHandType
                ) {
                    arrayOfCustomPlayer[customPlayerIndex].score = newScore
                }
            }
        }

        fun addCustomPlayerInArrayListOfCustomPlayer(
            arrayOfCustomPlayer: ArrayList<CustomPlayer>,
            currentPlayerNumber: PlayerNumberType,
            playerIndex: Int,
            nextHandIndex: Int,
            hand: ArrayList<Card>
        ) {
            if (currentPlayerNumber == PlayerNumberType.PLAYER_SEVEN) {
                arrayOfCustomPlayer.add(createCustomPlayer(arrayOfCustomPlayer, playerIndex, hand))
            } else {
                arrayOfCustomPlayer.add(
                    playerIndex + nextHandIndex,
                    createCustomPlayer(arrayOfCustomPlayer, playerIndex, hand)
                )
            }
        }

        private fun createCustomPlayer(
            arrayOfCustomPlayer: ArrayList<CustomPlayer>,
            playerIndex: Int,
            hand: ArrayList<Card>
            ): CustomPlayer {
            return CustomPlayer(
                bet = arrayOfCustomPlayer[playerIndex].bet,
                score = getHandScore(hand),
                insuranceBet = 0.0,
                isDouble = false,
                hand = hand,
                playerNumber = arrayOfCustomPlayer[playerIndex].playerNumber,
                playerHandType = getHandTypeFromInt(if (arrayOfCustomPlayer[playerIndex].isPlayerSecondSplit) 2 else 1),
                isCurrentPlayer = false,
                resultScore = arrayOfCustomPlayer[playerIndex].resultScore,
                isPlayerDrawAce = false,
                isPlayerScoreSoft = false,
                isPlayerFirstSplit = false,
                isPlayerSecondSplit = false,
                isInsuranceOpen = false
            )
        }

        private fun getHandScore(hand: ArrayList<Card>): Int {
            var score = 0
            for (card in hand) {
                score += card.value!!
            }
            return score
        }

        private fun createCustomPlayer(): CustomPlayer {
            return CustomPlayer(
                bet = 0.0,
                score = 0,
                insuranceBet = 0.0,
                isDouble = false,
                hand = ArrayList(),
                playerNumber = PlayerNumberType.PLAYER_ONE,
                playerHandType = HandType.MainHand,
                isCurrentPlayer = false,
                resultScore = R.string.fragment_main_game_none,
                isPlayerDrawAce = false,
                isPlayerScoreSoft = false,
                isPlayerFirstSplit = false,
                isPlayerSecondSplit = false,
                isInsuranceOpen = false
            )
        }

        fun nextPlayerTurn(offlineUser: OfflineUser): PlayerNumberType {
           return when (offlineUser.currentPlayerNumber) {
                PlayerNumberType.PLAYER_ONE -> PlayerNumberType.PLAYER_TWO
                PlayerNumberType.PLAYER_TWO -> PlayerNumberType.PLAYER_THREE
                PlayerNumberType.PLAYER_THREE -> PlayerNumberType.PLAYER_FOUR
                PlayerNumberType.PLAYER_FOUR -> PlayerNumberType.PLAYER_FIVE
                PlayerNumberType.PLAYER_FIVE -> PlayerNumberType.PLAYER_SIX
                else -> PlayerNumberType.PLAYER_SEVEN
            }
        }

        fun getPlayerIndex(arrayOfCustomPlayer: ArrayList<CustomPlayer>, currentPlayer: CustomPlayer): Int {
            var indexToReturn = 0
            for (index in 0 until arrayOfCustomPlayer.size) {
                if (currentPlayer == arrayOfCustomPlayer[index]) {
                    indexToReturn = index
                }
            }
            return indexToReturn
        }

        fun getCurrentPlayer(offlineUser: OfflineUser, currentHandType: HandType): CustomPlayer {
            var player = createCustomPlayer()
            for (index in 0 until offlineUser.player.size) {
                if (
                    offlineUser.currentPlayerNumber == offlineUser.player[index].playerNumber &&
                    offlineUser.currentHandType == offlineUser.player[index].playerHandType
                ) {
                    player = offlineUser.player[index]
                }
            }
            return player
        }

        fun getCurrentPlayerSplit(offlineUser: OfflineUser, currentSplitHand: HandType): CustomPlayer {
            var player = createCustomPlayer()
            for (index in 0 until offlineUser.player.size) {
                if (
                    offlineUser.currentPlayerNumber == offlineUser.player[index].playerNumber &&
                    HandType.FirstSplit == currentSplitHand
                ) {
                    player = offlineUser.player[index]
                }
            }
            return player
        }

        fun getColorByPlayerNumber(playerNumberType: PlayerNumberType): Int {
            return when(playerNumberType) {
                PlayerNumberType.PLAYER_ONE -> R.color.facebook
                PlayerNumberType.PLAYER_TWO -> R.color.google
                PlayerNumberType.PLAYER_THREE -> R.color.purple_200
                PlayerNumberType.PLAYER_FOUR -> R.color.teal_200
                PlayerNumberType.PLAYER_FIVE -> R.color.purple_700
                PlayerNumberType.PLAYER_SIX -> R.color.yellow
                else -> R.color.green
            }
        }

        fun openInsurance(arrayOfCustomPlayer: ArrayList<CustomPlayer>) {
            for (player in arrayOfCustomPlayer) {
                player.isInsuranceOpen = true
            }
        }

        fun closeInsurance(arrayOfCustomPlayer: ArrayList<CustomPlayer>) {
            for (player in arrayOfCustomPlayer) {
                player.isInsuranceOpen = false
            }
        }

        private fun getHandTypeFromInt(index: Int): HandType {
            return when(index) {
                0 -> HandType.MainHand
                1 -> HandType.FirstSplit
                else -> HandType.SecondSplit
            }
        }

        private fun getPlayerNumberType(playerIndex: Int): PlayerNumberType {
            return when (playerIndex) {
                0 -> PlayerNumberType.PLAYER_ONE
                1 -> PlayerNumberType.PLAYER_TWO
                2 -> PlayerNumberType.PLAYER_THREE
                3 -> PlayerNumberType.PLAYER_FOUR
                4 -> PlayerNumberType.PLAYER_FIVE
                5 -> PlayerNumberType.PLAYER_SIX
                else -> PlayerNumberType.PLAYER_SEVEN
            }
        }

        fun getIndexOfPlayerNumberType(playerNumberType: PlayerNumberType): Int {
            return when(playerNumberType) {
                PlayerNumberType.PLAYER_ONE -> 1
                PlayerNumberType.PLAYER_TWO -> 2
                PlayerNumberType.PLAYER_THREE -> 3
                PlayerNumberType.PLAYER_FOUR -> 4
                PlayerNumberType.PLAYER_FIVE -> 5
                PlayerNumberType.PLAYER_SIX -> 6
                else -> 7
            }
        }



        fun initializeMainBet(player: ArrayList<CustomPlayer>, defaultBet: Double) {
            for (playerIndex in 0 until player.size) {
                player[playerIndex].bet = defaultBet
            }
        }

        fun calculateRvPosition(offlineUser: OfflineUser): Int {
            return when (offlineUser.currentHandType) {
                HandType.FirstSplit -> { 1 }
                HandType.SecondSplit -> { 1 }
                else -> { 0 }
            }
        }

        fun Double.formatStringBet() = this.toString().replaceAfter(".","").replace(".","") + "€"

        private fun String.toOnlineStatusType() = OnlineStatusType.valueOf(this)

        private fun String.toPlayerNumberType() = PlayerNumberType.valueOf(this)

        private fun String.toSplitType(): HandType = HandType.MainHand.toHandType(this)


    }
}