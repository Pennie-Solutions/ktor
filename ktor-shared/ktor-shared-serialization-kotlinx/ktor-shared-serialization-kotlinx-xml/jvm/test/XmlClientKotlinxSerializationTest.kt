/*
 * Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.tests.*
import io.ktor.http.*
import io.ktor.shared.serialization.kotlinx.*
import io.ktor.shared.serialization.kotlinx.xml.*
import org.junit.*

class XmlClientKotlinxSerializationTest : AbstractClientContentNegotiationTest() {
    private val converter = KotlinxSerializationConverter(DefaultXml)
    override val defaultContentType: ContentType = ContentType.Application.Json
    override val customContentType: ContentType = ContentType.parse("text/xml")

    override fun ContentNegotiation.Config.configureContentNegotiation(contentType: ContentType) {
        register(contentType, converter)
    }

    @Test
    @Ignore
    override fun testGeneric() {
    }

    @Test
    @Ignore
    override fun testSealed() {
    }

    @Test
    @Ignore
    override fun testSerializeNested() {
    }
}
