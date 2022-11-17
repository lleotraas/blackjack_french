package fr.lleotraas.blackjack_french.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_bank")
class Bank(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "player_id")
    var id: Long,
    var amount: Double,
    var pseudo: String
)