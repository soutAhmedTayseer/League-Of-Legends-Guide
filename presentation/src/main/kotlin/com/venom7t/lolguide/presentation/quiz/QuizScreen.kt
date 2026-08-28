package com.venom7t.lolguide.presentation.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.venom7t.lolguide.domain.champion.model.Champion
import com.venom7t.lolguide.domain.quiz.model.QuizQuestion
import com.venom7t.lolguide.presentation.R
import com.venom7t.lolguide.presentation.common.DataDragonUrls
import com.venom7t.lolguide.presentation.common.components.EmptyContent
import com.venom7t.lolguide.presentation.common.components.LoadingContent
import com.venom7t.lolguide.presentation.common.uiText
import com.venom7t.lolguide.presentation.theme.AppTheme

@Composable
fun QuizScreenRoot(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: QuizViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.onEvent(QuizEvent.ScreenOpened) }

    QuizScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    state: QuizState,
    onEvent: (QuizEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = AppTheme.colors.background,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.champion_detail_back),
                            tint = AppTheme.colors.textPrimary,
                        )
                    }
                },
                title = {
                    Text(
                        text = stringResource(R.string.quiz_title),
                        style = AppTheme.typography.titleLarge,
                        color = AppTheme.colors.textPrimary,
                    )
                },
                actions = {
                    Text(
                        text = stringResource(
                            R.string.quiz_score,
                            state.session.correctAnswers,
                            state.session.questionsAnswered,
                        ),
                        style = AppTheme.typography.label,
                        color = AppTheme.colors.primary,
                        modifier = Modifier.padding(end = AppTheme.dimens.spaceMd),
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppTheme.colors.background),
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> LoadingContent()
                state.insufficientData -> EmptyContent(message = uiText(R.string.quiz_insufficient_data))
                state.question != null -> QuizContent(state = state, question = state.question, onEvent = onEvent)
            }
        }
    }
}

@Composable
private fun QuizContent(
    state: QuizState,
    question: QuizQuestion,
    onEvent: (QuizEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(AppTheme.dimens.spaceMd),
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceMd),
    ) {
        Text(
            text = stringResource(question.promptRes()),
            style = AppTheme.typography.titleMedium,
            color = AppTheme.colors.primary,
        )

        QuestionArt(question = question)

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
            contentPadding = PaddingValues(vertical = AppTheme.dimens.spaceSm),
        ) {
            items(items = question.options, key = { it.id }) { champion ->
                AnswerButton(
                    champion = champion,
                    isCorrectAnswer = champion.id == question.correctChampion.id,
                    isPicked = champion.id == state.pickedChampionId,
                    hasAnswered = state.pickedChampionId != null,
                    onClick = { onEvent(QuizEvent.AnswerPicked(champion.id)) },
                )
            }
        }

        if (state.pickedChampionId != null) {
            Button(
                onClick = { onEvent(QuizEvent.NextQuestionRequested) },
                shape = AppTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.colors.primary,
                    contentColor = AppTheme.colors.onPrimary,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(R.string.quiz_next), style = AppTheme.typography.label)
            }
        }
    }
}

/**
 * The quiz art -- an ability icon or a splash crop -- is deliberately left
 * without a contentDescription. Unlike a decorative icon, this image is the
 * question itself: any real description would name the champion and give the
 * answer away to a screen reader user before they could guess. There is no
 * accessible equivalent of this specific game mode without changing what it
 * tests, so this is a documented, deliberate exception rather than an
 * oversight -- AbilityIconGuess and SplashCropGuess are the two quiz modes
 * this affects.
 */
@Composable
private fun QuestionArt(question: QuizQuestion, modifier: Modifier = Modifier) {
    when (question) {
        is QuizQuestion.AbilityIconGuess -> AsyncImage(
            model = DataDragonUrls.spellIcon(
                version = question.abilityPatchVersion,
                imageFileName = question.abilityImageFileName,
            ),
            contentDescription = null,
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(AppTheme.colors.surfaceElevated, AppTheme.shapes.large),
        )

        is QuizQuestion.SplashCropGuess -> AsyncImage(
            model = DataDragonUrls.championSplash(
                championId = question.correctChampion.id,
                skinNum = question.skinNum,
            ),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            // A cropped, zoomed region: alignment approximates the stored
            // offset so the same question always shows the same crop.
            alignment = androidx.compose.ui.BiasAlignment(
                horizontalBias = question.cropOffsetX * 2f - 1f,
                verticalBias = question.cropOffsetY * 2f - 1f,
            ),
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(1.5f)
                .background(AppTheme.colors.surfaceElevated, AppTheme.shapes.large),
        )
    }
}

@Composable
private fun AnswerButton(
    champion: Champion,
    isCorrectAnswer: Boolean,
    isPicked: Boolean,
    hasAnswered: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = when {
        !hasAnswered -> AppTheme.colors.border
        isCorrectAnswer -> AppTheme.colors.success
        isPicked -> AppTheme.colors.error
        else -> AppTheme.colors.border
    }
    val backgroundColor: Color = when {
        hasAnswered && isCorrectAnswer -> AppTheme.colors.success.copy(alpha = 0.15f)
        hasAnswered && isPicked -> AppTheme.colors.error.copy(alpha = 0.15f)
        else -> AppTheme.colors.surface
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor, AppTheme.shapes.medium)
            .border(AppTheme.dimens.borderWidth, borderColor, AppTheme.shapes.medium)
            .clickable(enabled = !hasAnswered, onClick = onClick)
            .padding(AppTheme.dimens.spaceMd),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = champion.name,
            style = AppTheme.typography.bodyLarge,
            color = AppTheme.colors.textPrimary,
        )
    }
}

private fun QuizQuestion.promptRes(): Int = when (this) {
    is QuizQuestion.AbilityIconGuess -> R.string.quiz_prompt_ability
    is QuizQuestion.SplashCropGuess -> R.string.quiz_prompt_splash
}
