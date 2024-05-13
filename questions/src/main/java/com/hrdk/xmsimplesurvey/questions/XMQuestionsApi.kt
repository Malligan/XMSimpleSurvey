package com.hrdk.xmsimplesurvey.questions

import retrofit2.Response
import retrofit2.http.GET

interface XMQuestionsApi {
    @GET("/questions") // this is the end point that we need for this project to get the api response.
    suspend fun getCountries(): Response<List<SingleQuestionDto>>
}