package io.github.drumber.kitsune.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.ZoneOffset
import java.util.Calendar
import java.util.TimeZone

class DateUtilTest {

    @Test
    fun shouldFormatISODate() {
        // given
        val expectedDateString = "2023-11-12T12:42:12.123Z"
        val time = Calendar.getInstance().apply {
            set(2023, Calendar.NOVEMBER, 12, 13, 42, 12)
            set(Calendar.MILLISECOND, 123)
            timeZone = TimeZone.getTimeZone(ZoneOffset.of("+1"))
        }

        // when
        val dateString = time.formatUtcDate()

        // then
        assertThat(dateString).isEqualTo(expectedDateString)
    }

    @Test
    fun shouldParseISODate() {
        // given
        val time = Calendar.getInstance().apply {
            set(2023, Calendar.NOVEMBER, 12, 13, 42, 12)
            set(Calendar.MILLISECOND, 123)
            timeZone = TimeZone.getTimeZone(ZoneOffset.of("+1"))
        }.time
        val isoDateString = time.formatUtcDate()

        // when
        val parsedDate = isoDateString.parseUtcDate()

        // then
        assertThat(parsedDate).isEqualTo(time)
    }

}
