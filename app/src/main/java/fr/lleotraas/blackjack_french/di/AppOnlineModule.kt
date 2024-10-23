package fr.lleotraas.blackjack_french.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.lleotraas.blackjack_french.di.Utils.BASE_URL
import fr.lleotraas.blackjack_french.features_online_game.domain.retrofit.CardApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppOnlineModule {

    @Provides
    @Singleton
    fun provideCardApi(): CardApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CardApi::class.java)
    }

}


object Utils {
    const val BASE_URL = "http://192.168.1.18:8080/"
}