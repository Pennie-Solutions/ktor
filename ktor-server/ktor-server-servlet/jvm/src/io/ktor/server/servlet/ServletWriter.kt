/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.server.servlet

import io.ktor.util.cio.*
import io.ktor.utils.io.*
import io.ktor.utils.io.pool.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import java.io.*
import java.util.concurrent.TimeoutException
import javax.servlet.*

internal fun CoroutineScope.servletWriter(output: ServletOutputStream): ReaderJob {
    val writer = ServletWriter(output)
    return reader(Dispatchers.Unconfined, writer.channel) {
        writer.run()
    }
}

internal val ArrayPool = object : DefaultPool<ByteArray>(1024) {
    override fun produceInstance() = ByteArray(4096)
    override fun validateInstance(instance: ByteArray) {
        if (instance.size != 4096) {
            throw IllegalArgumentException(
                "Tried to recycle wrong ByteArray instance: most likely it hasn't been borrowed from this pool"
            )
        }
    }
}

private const val MAX_COPY_SIZE = 512 * 1024 // 512K

private class ServletWriter(val output: ServletOutputStream) : WriteListener {
    val channel = ByteChannel()

    private val events = Channel<Unit>(2)

    public suspend fun run() {
        val buffer = ArrayPool.borrow()
        try {
            output.setWriteListener(this)
            events.receive()
            loop(buffer)

            finish()

            // we shouldn't recycle it in finally
            // because in case of error the buffer could be still hold by servlet container
            // so we simply drop it as buffer leak has only limited performance impact
            // (buffer will be collected by GC and pool will produce another one)
            ArrayPool.recycle(buffer)
        } catch (t: Throwable) {
            onError(t)
        } finally {
            events.close()
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun finish() {
        awaitReady()
        output.flush()
        awaitReady()
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun loop(buffer: ByteArray) {
        if (channel.availableForRead == 0) {
            awaitReady()
            output.flush()
        }

        var copied = 0L
        while (true) {
            val rc = channel.readAvailable(buffer)
            if (rc == -1) break

            copied += rc
            if (copied > MAX_COPY_SIZE) {
                copied = 0
                yield()
            }

            awaitReady()
            output.write(buffer, 0, rc)
            awaitReady()

            if (channel.availableForRead == 0) output.flush()
        }
    }

    private suspend fun awaitReady() {
        if (output.isReady) return
        return awaitReadySuspend()
    }

    private suspend fun awaitReadySuspend() {
        do {
            events.receive()
        } while (!output.isReady)
    }

    override fun onWritePossible() {
        try {
            if (!events.trySend(Unit).isSuccess) {
                events.trySendBlocking(Unit)
            }
        } catch (ignore: Throwable) {
        }
    }

    override fun onError(t: Throwable) {
        val wrapped = wrapException(t)
        events.close(wrapped)
        channel.cancel(wrapped)
    }

    private fun wrapException(cause: Throwable): Throwable {
        return if (cause is IOException || cause is TimeoutException) {
            ChannelWriteException("Failed to write to servlet async stream", exception = cause)
        } else cause
    }
}
