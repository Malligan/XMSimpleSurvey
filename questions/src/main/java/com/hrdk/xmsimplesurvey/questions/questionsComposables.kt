package com.hrdk.xmsimplesurvey.questions

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    var pageCount by remember { mutableIntStateOf(0) }
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val questionsState by remember { derivedStateOf { mutableListOf<Question>() } }
    val questionsSubmitted by remember { mutableIntStateOf(0) }

    val viewModel by remember { mutableStateOf(QuestionsViewModel(RetrofitInstance.api)) }

    LaunchedEffect(key1 = "singleEffect") {
        viewModel.userIntent.send(QuestionsIntent.LoadQuestions)
    }

    val screenState = viewModel.state.collectAsState()

    var showQuestionsLoading by remember { mutableStateOf(true) }
    val loadingInProgress by remember {
        derivedStateOf { screenState.value.questionsLoadingState == QuestionsLoadingState.Loading }
    }

    val isPervEnabled by remember {
        derivedStateOf { pagerState.currentPage > 0 }
    }

    val isNextEnabled by remember {
        derivedStateOf { pagerState.currentPage < pageCount - 1 }
    }

    Column(modifier = Modifier) {

        when (screenState.value.questionsLoadingState) {
            is QuestionsLoadingState.Error -> {
            }
            QuestionsLoadingState.Idle -> {
            }
            QuestionsLoadingState.Loading -> {
                LaunchedEffect(key1 = showQuestionsLoading) {
                    delay(300)  // prevent blinking of loading
                    showQuestionsLoading = false
                }


            }
            is QuestionsLoadingState.Success -> {
                val loadedQuestions = screenState.value.questionsLoadingState as QuestionsLoadingState.Success
                pageCount = loadedQuestions.questions.count()
                questionsState.addAll(loadedQuestions.questions)
            }
        }

        if (showQuestionsLoading && loadingInProgress) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.width(64.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        }

        if (questionsState.isNotEmpty()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                ViewPagerItem(questionsState[page]) { }
            }
        }

        when (screenState.value.questionSubmissionScreenState?.questionSubmissionState) {
            is QuestionSubmissionState.Error -> {
                var showComposable by remember { mutableStateOf(false) }

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
                            onClick = { // TODO question submit
                                coroutineScope.launch {
                                    viewModel.userIntent.send(QuestionsIntent.SubmitQuestion(Question(-1, "", "")))
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
                var showComposable by remember { mutableStateOf(false) }

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
            null -> {}
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Question: ${pagerState.currentPage+1}/${pagerState.pageCount}")
            Text(text = "Questions submitted: ${questionsSubmitted}/${pagerState.pageCount}")
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Favorite")
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
fun ViewPagerItem(question: Question, onClick: ((String) -> Unit)) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
    ) {
        Text(modifier = Modifier.padding(16.dp), text = question.questionText)
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            text = "input here"
        )
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            enabled = !question.isQuestionSubmitted,
            onClick = {
                onClick.invoke("answer")
            }
        ) {
            Text(text = "Submit")
        }
    }
}
