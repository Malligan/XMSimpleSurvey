package com.hrdk.xmsimplesurvey.questions

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "https://xm-assignment.web.app"
    val api: XMQuestionsApi by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(XMQuestionsApi::class.java)
    }
}