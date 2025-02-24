/*
 * Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.server.http.content

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.util.date.*
import java.time.*

/**
 * Creates [CachingOptions] instance with [ZonedDateTime] expiration time
 */
public fun CachingOptions(cacheControl: CacheControl? = null, expires: ZonedDateTime): CachingOptions =
    CachingOptions(cacheControl, expires.toGMTDate())
