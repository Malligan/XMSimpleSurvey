package com.hrdk.xmsimplesurvey.questions

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface XMQuestionsApi {
    @GET("/questions")
    suspend fun getQuestions(): Response<List<SingleQuestionDto>>

    @POST("/question/submit")
    suspend fun submitQuestion(@Body questionSubmissionDto: QuestionSubmissionDto): Response<Unit>
}