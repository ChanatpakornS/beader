package com.beader.core.common

import com.beader.core.common.result.DataResult
import com.beader.core.common.result.map
import com.beader.core.common.result.onError
import com.beader.core.common.result.onSuccess
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DataResultTest {
    @Test
    fun `map transforms success payload only`() {
        val success: DataResult<Int> = DataResult.Success(2)
        val mapped = success.map { it * 21 }

        assertEquals(DataResult.Success(42), mapped)
    }

    @Test
    fun `map is a no-op on error and loading`() {
        val error: DataResult<Int> = DataResult.Error(IllegalStateException("boom"))

        assertEquals(error, error.map { it * 2 })
        assertEquals(DataResult.Loading, DataResult.Loading.map { it })
    }

    @Test
    fun `onSuccess and onError invoke the matching branch only`() {
        var successCalled = false
        var errorCalled = false

        (DataResult.Success(1) as DataResult<Int>)
            .onSuccess { successCalled = true }
            .onError { errorCalled = true }

        assertTrue(successCalled)
        assertFalse(errorCalled)
    }
}
