package com.hrdk.xmsimplesurvey.questions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hrdk.xmsimplesurvey.questions.QuestionsIntent.SubmitQuestion
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class QuestionsViewModel(private val questionsApi: XMQuestionsApi) : ViewModel() {
    val userIntent = Channel<QuestionsIntent>(Channel.UNLIMITED)
    private val _state = MutableStateFlow(QuestionsScreenState())
    val state: StateFlow<QuestionsScreenState>
        get() = _state
    val questionsRepository = ""

    init {
        handleIntent()
    }

    private fun handleIntent() {
        viewModelScope.launch {
            userIntent.consumeAsFlow().collect {
                when (it) {
                    QuestionsIntent.LoadQuestions -> {
                        loadQuestions()
                    }
                    is SubmitQuestion -> {

                    }
                }
            }
        }
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            _state.update { _state.value.copy(questionsLoadingState = QuestionsLoadingState.Loading) }
            val response = questionsApi.getQuestions()
            if (response.isSuccessful) {
                val questions = response.body()?.toVmQuestions() ?: listOf(Question(-1, "server choose ignorance today"))
                _state.update { _state.value.copy(questionsLoadingState = QuestionsLoadingState.Success(questions)) }
            } else {
                _state.update { _state.value.copy(questionsLoadingState = QuestionsLoadingState.Error(response.message())) }
            }
        }
    }
}

data class QuestionsScreenState(
    val questionsLoadingState: QuestionsLoadingState = QuestionsLoadingState.Idle,
    val questions: List<Question> = listOf(),
    val questionSubmissionScreenState: QuestionSubmissionScreenState? = null,
    val currentQuestion: Question? = null
)

data class QuestionSubmissionScreenState(
    val question: Question,
    val questionSubmissionState: QuestionSubmissionState
)

sealed class QuestionsIntent {
    data object LoadQuestions : QuestionsIntent()
    data class SubmitQuestion(val question: Question) : QuestionsIntent()
}

sealed class QuestionsLoadingState {
    data object Idle : QuestionsLoadingState()
    data object Loading : QuestionsLoadingState()
    data class Success(val questions: List<Question>) : QuestionsLoadingState()
    data class Error(val error: String?) : QuestionsLoadingState()
}

sealed class QuestionSubmissionState {
    data object Idle : QuestionSubmissionState()
    data object Loading : QuestionSubmissionState()
    data object Success : QuestionSubmissionState()
    data class Error(val error: String?) : QuestionSubmissionState()
}

data class Question(
    val id: Int,
    val questionText: String = "",
    val answerText: String = "",
    val isQuestionSubmitted: Boolean = false
)

fun List<SingleQuestionDto>.toVmQuestions(): List<Question> {
    return this.map { Question(it.id, it.question) }
}