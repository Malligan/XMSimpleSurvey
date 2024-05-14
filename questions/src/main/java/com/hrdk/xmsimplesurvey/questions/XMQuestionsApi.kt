package com.hrdk.xmsimplesurvey.questions

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST

interface XMQuestionsApi {
    @GET("/questions")
    suspend fun getQuestions(): Response<List<SingleQuestionDto>>

    @POST("/questions")
    suspend fun submitQuestion(): Response<Unit>
}