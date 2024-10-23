package fr.lleotraas.blackjack_french.features_online_game.domain.retrofit

import retrofit2.http.GET
import retrofit2.http.Path

interface CardApi {

    @GET("/add/card/table_id={table_id}/number={number}/color={color}")
    suspend fun addCard(
        @Path("table_id") tableId: Int,
        @Path("number") number: String,
        @Path("color") color: String
    )



}