package com.hrdk.xmsimplesurvey.questions

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import retrofit2.Response

@ExperimentalCoroutinesApi
class QuestionsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var questionsApi: XMQuestionsApi
    private lateinit var viewModel: QuestionsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        questionsApi = Mockito.mock(XMQuestionsApi::class.java)
        viewModel = QuestionsViewModel(questionsApi)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test loadQuestions`() = runTest {
        val questions = listOf(SingleQuestionDto(1, "question 1"), SingleQuestionDto(2, "question 2"))
        val resultQuestions = listOf(Question(1, "question 1"), Question(2, "question 2"))
        `when`(questionsApi.getQuestions()).thenReturn(Response.success(questions))

        viewModel.userIntent.send(QuestionsIntent.LoadQuestions)

        verify(questionsApi).getQuestions()
        assertEquals(resultQuestions, viewModel.state.value.questions)
        assertEquals(QuestionsLoadingState.Success, viewModel.state.value.questionsLoadingState)
    }

    @Test
    fun `test submitQuestion`() = runTest {
        val question = Question(1, "question 1", "answer 1")
        `when`(questionsApi.submitQuestion(QuestionSubmissionDto(question.id, question.answerText))).thenReturn(Response.success(Unit))

        viewModel.userIntent.send(QuestionsIntent.SubmitQuestion(question))

        verify(questionsApi).submitQuestion(QuestionSubmissionDto(question.id, question.answerText))
        assertEquals(QuestionSubmissionState.Success, viewModel.state.value.questionSubmissionState)
        assertTrue(viewModel.state.value.currentQuestion?.isQuestionSubmitted ?: false)
        println(viewModel.state.value.currentQuestion)
    }
}