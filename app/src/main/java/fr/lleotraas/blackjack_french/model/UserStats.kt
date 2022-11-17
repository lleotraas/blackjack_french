package fr.lleotraas.blackjack_french.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.*
import kotlin.Comparator

class UserStats(
    @ServerTimestamp
    val date: Date? = null,
    var walletStateWhenGameEnding: Double? = null
)

class UserStatsComparator : Comparator<UserStats> {
     override fun compare(userStatsLeft: UserStats?, userStatsRight: UserStats?): Int {
         return userStatsLeft?.date?.compareTo(userStatsRight?.date) ?: 0
     }

 }