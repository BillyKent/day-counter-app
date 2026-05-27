package com.daycounter.presentation.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.daycounter.domain.model.WidgetBinding
import com.daycounter.domain.repository.WidgetBindingRepository
import com.daycounter.domain.usecase.CalculateStreakUseCase
import com.daycounter.domain.usecase.GetAllCountersUseCase
import com.daycounter.presentation.R
import com.daycounter.presentation.theme.DayCounterTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PickerCounterRow(val id: Long, val goalName: String, val streakDays: Int)

@HiltViewModel
class CounterPickerViewModel @Inject constructor(
    getAllCounters: GetAllCountersUseCase,
    calculateStreak: CalculateStreakUseCase,
    private val widgetBindings: WidgetBindingRepository,
) : ViewModel() {

    val counters: StateFlow<List<PickerCounterRow>> = getAllCounters()
        .map { list ->
            list.map { PickerCounterRow(it.id, it.goalName, calculateStreak(it.startDate)) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyList())

    suspend fun bind(widgetId: Int, counterId: Long) {
        val existing = widgetBindings.get(widgetId)
        if (existing == null) {
            widgetBindings.insert(WidgetBinding(widgetId, counterId))
        } else {
            widgetBindings.update(existing.copy(counterId = counterId))
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

@AndroidEntryPoint
class CounterPickerActivity : ComponentActivity() {

    @Inject
    lateinit var widgetStateUpdater: WidgetStateUpdater

    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            DayCounterTheme {
                PickerScreen(onCounterChosen = { counterId -> commit(counterId) })
            }
        }
    }

    private fun commit(counterId: Long) {
        lifecycleScope.launch {
            val vm: CounterPickerViewModel = androidx.lifecycle.ViewModelProvider(
                this@CounterPickerActivity,
            )[CounterPickerViewModel::class.java]
            vm.bind(appWidgetId, counterId)
            widgetStateUpdater.refresh(applicationContext, appWidgetId, counterId)
            setResult(
                Activity.RESULT_OK,
                Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId),
            )
            finish()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PickerScreen(
    onCounterChosen: (Long) -> Unit,
    viewModel: CounterPickerViewModel = hiltViewModel(),
) {
    val counters by viewModel.counters.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.widget_select_counter)) }) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(counters, key = { it.id }) { row ->
                Card(
                    onClick = { onCounterChosen(row.id) },
                    modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 72.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = row.goalName, style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = stringResource(R.string.home_streak_days, row.streakDays),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        }
    }
}
