package org.d3ifcool.wayantiara.automaticfan.history

import com.squareup.moshi.Moshi
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import xin.sparkle.moshi.NullSafeKotlinJsonAdapterFactory
import xin.sparkle.moshi.NullSafeStandardJsonAdapters

interface HistoryApi {

    @FormUrlEncoded
    @POST("insertKipasAngin.php")
    fun createHistory(
        @Field("status") status: String
    ): Call<ResponseHistori>


    @GET("getKipasAngin.php")
    fun getHistory(): Call<ResponseHistori>

    companion object {
        private const val BASE_URL = "https://api.luckytruedev.com/kipasangin/"
    }

    object ApiClient {
        fun create(): HistoryApi {
            val moshi = Moshi.Builder()
                .add(NullSafeStandardJsonAdapters.FACTORY)
                .add(NullSafeKotlinJsonAdapterFactory())
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
            return retrofit.create(HistoryApi::class.java)
        }
    }
}