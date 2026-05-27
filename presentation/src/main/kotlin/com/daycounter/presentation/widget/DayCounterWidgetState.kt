package com.daycounter.presentation.widget

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStoreFile
import androidx.glance.state.GlanceStateDefinition
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** State pushed to each widget instance via the Glance state DataStore. */
@Serializable
data class DayCounterWidgetState(
    val counterId: Long? = null,
    val goalName: String = "",
    val streakDays: Int = 0,
    val isCounterDeleted: Boolean = false,
) {
    companion object {
        val DEFAULT = DayCounterWidgetState()
    }
}

internal object DayCounterWidgetStateSerializer : Serializer<DayCounterWidgetState> {
    override val defaultValue: DayCounterWidgetState = DayCounterWidgetState.DEFAULT

    override suspend fun readFrom(input: InputStream): DayCounterWidgetState =
        Json.decodeFromString(
            DayCounterWidgetState.serializer(),
            input.readBytes().decodeToString(),
        )

    override suspend fun writeTo(t: DayCounterWidgetState, output: OutputStream) {
        output.write(
            Json.encodeToString(DayCounterWidgetState.serializer(), t).encodeToByteArray(),
        )
    }
}

object DayCounterWidgetStateDefinition : GlanceStateDefinition<DayCounterWidgetState> {

    private const val DATA_STORE_FILENAME_PREFIX = "day_counter_widget_state_"

    override suspend fun getDataStore(
        context: Context,
        fileKey: String,
    ): DataStore<DayCounterWidgetState> = androidx.datastore.core.DataStoreFactory.create(
        serializer = DayCounterWidgetStateSerializer,
        produceFile = { getLocation(context, fileKey) },
    )

    override fun getLocation(context: Context, fileKey: String): File =
        context.dataStoreFile("$DATA_STORE_FILENAME_PREFIX$fileKey")
}
