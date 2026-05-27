package com.daycounter.domain.di

import java.time.Clock
import java.time.ZoneId

/**
 * Indirection so the data/presentation layers can provide a [Clock] and [ZoneId] via Hilt
 * without `:domain` having any Hilt dependency. Domain depends only on [Clock] and [ZoneId]
 * from `java.time` — both are JVM types, satisfying constitution Principle III.
 */
interface SystemClockProvider {
    val clock: Clock
    val zone: ZoneId
}
