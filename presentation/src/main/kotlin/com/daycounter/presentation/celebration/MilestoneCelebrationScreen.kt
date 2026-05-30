package com.daycounter.presentation.celebration

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.daycounter.presentation.R
import com.daycounter.presentation.components.ProgressRing

@Composable
fun MilestoneCelebrationScreen(
    counterId: Long,
    milestone: Int,
    onClose: () -> Unit,
    viewModel: MilestoneCelebrationViewModel = hiltViewModel<MilestoneCelebrationViewModel, MilestoneCelebrationViewModel.Factory>(
        creationCallback = { factory -> factory.create(counterId, milestone) },
    ),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val shareText = stringResource(R.string.celebration_share_text, state.counterName, state.milestone)
    CelebrationContent(
        state = state,
        onClose = onClose,
        onShare = {
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            ContextCompat.startActivity(
                context,
                Intent.createChooser(send, null),
                null,
            )
        },
    )
}

@Composable
internal fun CelebrationContent(
    state: CelebrationUiState,
    onClose: () -> Unit,
    onShare: () -> Unit,
) {
    var animateTarget by remember { mutableStateOf(0) }
    LaunchedEffect(state.milestone) { animateTarget = state.milestone }
    val animatedDays by animateIntAsState(
        targetValue = animateTarget,
        animationSpec = tween(durationMillis = 900),
        label = "celebrationRing",
    )

    Surface(
        modifier = Modifier.fillMaxSize().testTag("celebration_screen"),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Box(Modifier.fillMaxSize()) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                    .testTag("celebration_close"),
            ) {
                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.celebration_close))
            }

            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                ProgressRing(
                    days = animatedDays,
                    target = state.milestone,
                    contentDescription = stringResource(
                        R.string.home_ring_content_description,
                        state.milestone,
                        state.milestone,
                    ),
                    diameter = 200.dp,
                    strokeWidth = 16.dp,
                ) {
                    Text(
                        text = state.milestone.toString(),
                        style = MaterialTheme.typography.displayLarge,
                        modifier = Modifier.testTag("celebration_milestone"),
                    )
                }
                Text(
                    text = state.counterName,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 24.dp),
                )
                Text(
                    text = stringResource(state.messageRes),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 12.dp).testTag("celebration_message"),
                )
                Button(
                    onClick = onClose,
                    modifier = Modifier
                        .fillMaxWidth()
                        .sizeIn(minHeight = 48.dp)
                        .padding(top = 32.dp)
                        .testTag("celebration_keep_going"),
                ) {
                    Text(stringResource(R.string.celebration_keep_going))
                }
                TextButton(
                    onClick = onShare,
                    modifier = Modifier
                        .fillMaxWidth()
                        .sizeIn(minHeight = 48.dp)
                        .padding(top = 4.dp)
                        .testTag("celebration_share"),
                ) {
                    Text(stringResource(R.string.celebration_share))
                }
            }
        }
    }
}
