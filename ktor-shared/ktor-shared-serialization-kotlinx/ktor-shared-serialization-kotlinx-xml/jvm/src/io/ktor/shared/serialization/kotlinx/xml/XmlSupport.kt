/*
 * Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.shared.serialization.kotlinx.xml

import io.ktor.http.*
import io.ktor.shared.serialization.*
import io.ktor.shared.serialization.kotlinx.*
import kotlinx.serialization.*
import nl.adaptivity.xmlutil.*
import nl.adaptivity.xmlutil.serialization.*

/**
 * The default XML configuration. The settings are:
 * - Every declaration without a namespace is automatically wrapped in the namespace.
 *       See also [XMLOutputFactory.IS_REPAIRING_NAMESPACES].
 * - The XML declaration is not generated.
 * - The indent is empty.
 * - Polymorphic serialization is disabled.
 *
 * See [XML] for more details.
 */
public val DefaultXml: XML = XML {
    repairNamespaces = true
    xmlDeclMode = XmlDeclMode.None
    indentString = ""
    autoPolymorphic = false
    this.policy
}

/**
 * Registers the `application/xml` (or another specified [contentType]) content type
 * to the [ContentNegotiation] plugin using kotlinx.serialization.
 *
 * @param format instance. [DefaultXml] is used by default.
 * @param contentType for which the [format] should be used. `application/xml` is used by default.
 */
public fun Configuration.xml(
    format: XML = DefaultXml,
    contentType: ContentType = ContentType.Application.Xml
) {
    serialization(contentType, format)
}
