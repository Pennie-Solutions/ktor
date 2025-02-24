/*
 * Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.shared.serialization.*
import io.ktor.shared.serialization.kotlinx.*
import io.ktor.shared.serialization.kotlinx.xml.*
import io.ktor.test.dispatcher.*
import io.ktor.util.reflect.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import nl.adaptivity.xmlutil.serialization.*
import kotlin.test.*

@Serializable
internal data class User(val id: Long, val login: String)

@Serializable
internal data class Photo(val id: Long, val path: String)

class XmlSerializationTest {

    @Test
    fun testRegisterCustom() = testSuspend {
        val serializer = KotlinxSerializationConverter(DefaultXml)

        val user = User(1, "vasya")
        val actual = serializer.testSerialize(user)
        assertEquals("<User id=\"1\" login=\"vasya\"/>", actual)
    }

    @Test
    fun testRegisterCustomList() = testSuspend {
        val serializer = KotlinxSerializationConverter(DefaultXml)

        val user = User(2, "petya")
        val photo = Photo(3, "petya.jpg")

        assertEquals("<ArrayList id=\"2\" login=\"petya\"/>", serializer.testSerialize(listOf(user)))
        assertEquals("<ArrayList id=\"3\" path=\"petya.jpg\"/>", serializer.testSerialize(listOf(photo)))
    }

    private suspend inline fun <reified T : Any> ContentConverter.testSerialize(data: T): String {
        val content = serialize(ContentType.Application.Xml, Charsets.UTF_8, typeInfo<T>(), data)
        return (content as? TextContent)?.text ?: error("Failed to get serialized $data")
    }
}
