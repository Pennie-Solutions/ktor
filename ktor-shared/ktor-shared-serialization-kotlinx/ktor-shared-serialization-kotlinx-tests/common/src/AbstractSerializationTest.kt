/*
 * Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package io.ktor.shared.serialization.kotlinx.test

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.shared.serialization.*
import io.ktor.shared.serialization.kotlinx.*
import io.ktor.test.dispatcher.*
import io.ktor.util.reflect.*
import io.ktor.utils.io.charsets.*
import kotlinx.serialization.*
import kotlin.test.*

@Serializable
internal data class User(val id: Long, val login: String)

@Serializable
internal data class Photo(val id: Long, val path: String)

@Serializable
public data class GithubProfile(
    val login: String,
    val id: Int,
    val name: String
)

@OptIn(ExperimentalSerializationApi::class)
public abstract class AbstractSerializationTest<T : SerialFormat> {
    protected abstract val defaultContentType: ContentType
    protected abstract val defaultSerializationFormat: T
    protected abstract fun assertEquals(
        expectedAsJson: String,
        actual: ByteArray,
        format: T,
    ): Boolean

    @Test
    public fun testMapsElements(): Unit = testSuspend {
        val testSerializer = KotlinxSerializationConverter(defaultSerializationFormat)
        testSerializer.testSerialize(
            mapOf(
                "a" to "1",
                "b" to "2"
            )
        ).let { result ->
            assertEquals("""{"a":"1","b":"2"}""", result, defaultSerializationFormat)
        }

        testSerializer.testSerialize(
            mapOf(
                "a" to "1",
                "b" to null
            )
        ).let { result ->
            assertEquals("""{"a":"1","b":null}""", result, defaultSerializationFormat)
        }

        testSerializer.testSerialize(
            mapOf(
                "a" to "1",
                null to "2"
            )
        ).let { result ->
            assertEquals("""{"a":"1",null:"2"}""", result, defaultSerializationFormat)
        }

        // this is not yet supported
        assertFails {
            testSerializer.testSerialize<Map<String, Any>>(
                mapOf(
                    "a" to "1",
                    "b" to 2
                )
            )
        }
    }

    @Test
    public fun testRegisterCustom(): Unit = testSuspend {
        val serializer = KotlinxSerializationConverter(defaultSerializationFormat)

        val user = User(1, "vasya")
        val actual = serializer.testSerialize(user)
        assertEquals("""{"id":1,"login":"vasya"}""", actual, defaultSerializationFormat)
    }

    @Test
    public fun testRegisterCustomList(): Unit = testSuspend {
        val serializer = KotlinxSerializationConverter(defaultSerializationFormat)

        val user = User(2, "petya")
        val photo = Photo(3, "petya.jpg")

        assertEquals(
            """[{"id":2,"login":"petya"}]""",
            serializer.testSerialize(listOf(user)),
            defaultSerializationFormat
        )
        assertEquals(
            """[{"id":3,"path":"petya.jpg"}]""",
            serializer.testSerialize(listOf(photo)),
            defaultSerializationFormat
        )
    }

    protected suspend inline fun <reified T : Any> ContentConverter.testSerialize(data: T): ByteArray {
        val content = serialize(defaultContentType, Charsets.UTF_8, typeInfo<T>(), data)
        return (content as? OutgoingContent.ByteArrayContent)?.bytes() ?: error("Failed to get serialized $data")
    }
}
