package com.daycounter.presentation.counter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.daycounter.domain.model.Counter
import com.daycounter.domain.usecase.CalculateStreakUseCase
import com.daycounter.domain.usecase.GetCounterByIdUseCase
import com.daycounter.presentation.R
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CounterDetailViewModel @Inject constructor(
    private val getCounter: GetCounterByIdUseCase,
    private val calculateStreak: CalculateStreakUseCase,
) : ViewModel() {

    suspend fun load(counterId: Long): Pair<Counter, Int>? {
        val counter = getCounter(counterId) ?: return null
        return counter to calculateStreak(counter.startDate)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CounterDetailScreen(
    counterId: Long,
    onEdit: (Long) -> Unit,
    onCounterMissing: () -> Unit,
    onBack: () -> Unit,
    viewModel: CounterDetailViewModel = hiltViewModel(),
) {
    var loaded by remember { mutableStateOf<Pair<Counter, Int>?>(null) }
    var missing by remember { mutableStateOf(false) }

    LaunchedEffect(counterId) {
        val result = viewModel.load(counterId)
        if (result == null) {
            missing = true
        } else {
            loaded = result
        }
    }
    LaunchedEffect(missing) {
        if (missing) onCounterMissing()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.counter_detail_title)) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                actions = {
                    val current = loaded
                    if (current != null) {
                        IconButton(
                            onClick = { onEdit(current.first.id) },
                            modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = stringResource(R.string.counter_edit_title),
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        val current = loaded ?: return@Scaffold
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(current.first.goalName, style = MaterialTheme.typography.headlineMedium)
            Text(
                stringResource(R.string.home_streak_days, current.second),
                style = MaterialTheme.typography.displayLarge,
            )
        }
    }
}
