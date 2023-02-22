package fr.lleotraas.blackjack_french.features_offline_game.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_bank")
data class Wallet(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "player_id")
    var id: Long,
    var amount: Double,
    var pseudo: String
)