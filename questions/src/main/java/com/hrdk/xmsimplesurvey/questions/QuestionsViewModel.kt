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

    init {
        handleIntent()
    }

    private fun handleIntent() {
        viewModelScope.launch {
            userIntent.consumeAsFlow().collect { intent ->
                when (intent) {
                    QuestionsIntent.LoadQuestions -> {
                        loadQuestions()
                    }
                    is SubmitQuestion -> {
                        _state.update { _state.value.copy(currentQuestion = intent.question) }
                        updateQuestionList(intent.question)
                        submitQuestion(intent.question)
                    }
                }
            }
        }
    }

    private fun updateQuestionList(question: Question) {
        val questionsToUpdate = _state.value.questions.toMutableList()
        val index = questionsToUpdate.indexOfFirst { it.id == question.id }
        if (index != -1) {
            questionsToUpdate[index] = question
        }
        _state.update { _state.value.copy(questions = questionsToUpdate.toList()) }
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            _state.update { _state.value.copy(questionsLoadingState = QuestionsLoadingState.Loading) }
            val response = questionsApi.getQuestions()
            if (response.isSuccessful) {
                val questions = response.body()?.toVmQuestions() ?: listOf(Question(-1, "server choose ignorance today"))
                _state.update {
                    _state.value.copy(
                        questions = questions,
                        questionsLoadingState = QuestionsLoadingState.Success
                    )
                }
            } else {
                _state.update { _state.value.copy(questionsLoadingState = QuestionsLoadingState.Error) }
            }
        }
    }

    private fun submitQuestion(question: Question) {
        viewModelScope.launch {
            _state.update { _state.value.copy(questionSubmissionState = QuestionSubmissionState.Loading) }
            val response = questionsApi.submitQuestion(QuestionSubmissionDto(question.id, question.answerText))
            if (response.isSuccessful) {
                val submittedQuestion = question.copy(isQuestionSubmitted = true)
                updateQuestionList(submittedQuestion)
                _state.update { _state.value.copy(questionSubmissionState = QuestionSubmissionState.Success) }
            } else {
                _state.update { _state.value.copy(questionSubmissionState = QuestionSubmissionState.Error) }
            }
        }
    }
}

data class QuestionsScreenState(
    val questionsLoadingState: QuestionsLoadingState = QuestionsLoadingState.Idle,
    val questions: List<Question> = listOf(),
    val questionSubmissionState: QuestionSubmissionState = QuestionSubmissionState.Idle,
    val currentQuestion: Question? = null
)

sealed class QuestionsIntent {
    data object LoadQuestions : QuestionsIntent()
    data class SubmitQuestion(val question: Question) : QuestionsIntent()
}

sealed class QuestionsLoadingState {
    data object Idle : QuestionsLoadingState()
    data object Loading : QuestionsLoadingState()
    data object Success : QuestionsLoadingState()
    data object Error : QuestionsLoadingState()
}

sealed class QuestionSubmissionState {
    data object Idle : QuestionSubmissionState()
    data object Loading : QuestionSubmissionState()
    data object Success : QuestionSubmissionState()
    data object Error : QuestionSubmissionState()
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