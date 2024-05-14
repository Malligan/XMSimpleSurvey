package com.hrdk.xmsimplesurvey.questions

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuestionsScreen(
    navController: NavController
) {
    val coroutineScope = rememberCoroutineScope()
    val viewModel by remember { mutableStateOf(QuestionsViewModel(RetrofitInstance.api)) }

    LaunchedEffect(key1 = "singleEffect") {
        viewModel.userIntent.send(QuestionsIntent.LoadQuestions)
    }

    val screenState = viewModel.state.collectAsState()
    val questionsState by remember { derivedStateOf { screenState.value.questions } }
    val questionsSubmitted by remember {
        derivedStateOf {
            questionsState.filter { it.isQuestionSubmitted }
        }
    }
    val pageCount by remember { derivedStateOf { questionsState.size } }
    val pagerState = rememberPagerState(pageCount = { pageCount })

    val focusManager = LocalFocusManager.current // for clearing focus on tap out of input
    Column(modifier = Modifier.pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }) {
        when (screenState.value.questionsLoadingState) {
            is QuestionsLoadingState.Error -> {
            }
            QuestionsLoadingState.Idle -> {
            }
            QuestionsLoadingState.Loading -> {
            }
            is QuestionsLoadingState.Success -> {}
        }

        if (questionsState.isNotEmpty()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                ViewPagerItem(screenState, questionsState[page]) {
                    val question = questionsState[pagerState.currentPage].copy(answerText = it)
                    coroutineScope.launch {
                        viewModel.userIntent.send(QuestionsIntent.SubmitQuestion(question))
                    }
                }
            }
        } else {
            LoadingWidget()
        }

        when (screenState.value.questionSubmissionState) {
            is QuestionSubmissionState.Error -> {
                var showComposable by remember { mutableStateOf(true) }

                LaunchedEffect(key1 = showComposable) {
                    delay(5000)
                    showComposable = false
                }

                if (showComposable) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Failure!")
                        Button(
                            onClick = {
                                val question = questionsState[pagerState.currentPage].copy(answerText = screenState.value.currentQuestion?.answerText ?: "")
                                coroutineScope.launch {
                                    viewModel.userIntent.send(QuestionsIntent.SubmitQuestion(question))
                                }
                            }
                        ) {
                            Text(text = "Retry")
                        }
                    }
                }
            }
            QuestionSubmissionState.Idle -> {}
            QuestionSubmissionState.Loading -> {}
            QuestionSubmissionState.Success -> {
                var showComposable by remember { mutableStateOf(true) }

                LaunchedEffect(key1 = showComposable) {
                    delay(5000)
                    showComposable = false
                }

                if (showComposable) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Success")
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Question: ${pagerState.currentPage+1}/${pagerState.pageCount}")
            Text(text = "Questions submitted: ${questionsSubmitted.size}/${pagerState.pageCount}")
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Favorite")
            }

            val isPervEnabled by remember {
                derivedStateOf { pagerState.currentPage > 0 }
            }

            Button(
                enabled = isPervEnabled,
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }
                }
            ) {
                Text(text = stringResource(id = R.string.prev_button))
            }

            val isNextEnabled by remember {
                derivedStateOf { pagerState.currentPage < pageCount - 1 }
            }

            Button(
                enabled = isNextEnabled,
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
            ) {
                Text(text = stringResource(id = R.string.next_button))
            }
        }
    }
}

@Composable
private fun ColumnScope.LoadingWidget() {
    Box(
        modifier = Modifier.Companion
            .weight(1f)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.width(64.dp),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

@Composable
fun ViewPagerItem(screenState: State<QuestionsScreenState>, question: Question, onClick: ((String) -> Unit)) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
    ) {
        val questionText = remember { derivedStateOf { screenState.value.questions.first { it.id == question.id }.questionText } }
        Text(modifier = Modifier.padding(16.dp), text = questionText.value)

        val answerText = remember { derivedStateOf { screenState.value.questions.first { it.id == question.id }.answerText } }
        val answerTextFieldState = remember { mutableStateOf(answerText.value) }

        TextField(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            value = answerTextFieldState.value,
            onValueChange = { answerTextFieldState.value = it },
            label = { Text("Type here for an answer...") }
        )

        val isQuestionSubmitted = remember { derivedStateOf { screenState.value.questions.first { it.id == question.id }.isQuestionSubmitted } }

        val buttonAvailability = remember {
            derivedStateOf {
                !isQuestionSubmitted.value &&
                        answerTextFieldState.value.isNotEmpty() &&
                        screenState.value.questionSubmissionState != QuestionSubmissionState.Loading
            }
        }

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            enabled = buttonAvailability.value,
            onClick = {
                onClick.invoke(answerTextFieldState.value)
            }
        ) {
            Text(text = "Submit")
        }
    }
}
