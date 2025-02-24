/*
 * Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package io.ktor.shared.serialization.kotlinx.test.cbor

import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.tests.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.shared.serialization.kotlinx.cbor.*
import kotlinx.serialization.*
import kotlinx.serialization.cbor.*
import kotlinx.serialization.json.*

@OptIn(ExperimentalSerializationApi::class)
class CborClientKotlinxSerializationTest : AbstractClientContentNegotiationTest() {
    override val defaultContentType: ContentType = ContentType.Application.Cbor
    override val customContentType: ContentType = ContentType.parse("application/x-cbor")

    override fun ContentNegotiation.Config.configureContentNegotiation(contentType: ContentType) {
        cbor(contentType = contentType)
    }

    override suspend fun <T : Any> ApplicationCall.respond(
        responseJson: String,
        contentType: ContentType,
        serializer: KSerializer<T>
    ) {
        val actual = Json.decodeFromString(serializer, responseJson)
        val bytes = Cbor.encodeToByteArray(serializer, actual)
        respondBytes(bytes, contentType)
    }

    override suspend fun ApplicationCall.respondWithRequestBody(contentType: ContentType) {
        respondBytes(receive(), contentType)
    }
}
