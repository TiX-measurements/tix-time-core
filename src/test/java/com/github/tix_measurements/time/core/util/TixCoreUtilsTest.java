package com.github.tix_measurements.time.core.util;

import org.junit.Test;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;

import static org.assertj.core.api.Assertions.assertThat;

public class TixCoreUtilsTest {

	@Test
	public void testNanosOfDay() {
		ZonedDateTime zNow = ZonedDateTime.now(ZoneId.of(ZoneOffset.UTC.getId()));
		long nanosOfDay = TixCoreUtils.NANOS_OF_DAY.get();
		assertThat(nanosOfDay % 100000).isNotZero();
		assertThat(nanosOfDay).isPositive();

		long millisOfSecond = (nanosOfDay / 1000000L) % 1000;
		long expectedMillisOfSecond = zNow.getLong(ChronoField.MILLI_OF_SECOND);
		assertThat(Math.abs(millisOfSecond - expectedMillisOfSecond)).isLessThan(100L);

		long secondsOfMinute = (nanosOfDay / 1000000000L) % 60;
		long expectedSecondsOfMinute = zNow.getLong(ChronoField.SECOND_OF_MINUTE);
		boolean leapMinute = false;
		if (millisOfSecond < expectedMillisOfSecond) {
			expectedSecondsOfMinute += 1;
			expectedSecondsOfMinute %= 60;
			if (expectedSecondsOfMinute == 0) {
				leapMinute = true;
			}
		}
		assertThat(secondsOfMinute).isEqualTo(expectedSecondsOfMinute);

		long minutesOfHour = ((nanosOfDay / 1000000000L) / 60) % 60;
		long expectedMinutesOfHour = zNow.getLong(ChronoField.MINUTE_OF_HOUR);
		boolean leapHour = false;
		if (leapMinute) {
			expectedMinutesOfHour += 1;
			expectedMinutesOfHour %= 60;
			if (expectedMinutesOfHour == 0) {
				leapHour = true;
			}
		}
		assertThat(minutesOfHour).isEqualTo(expectedMinutesOfHour);

		long hoursOfDay = (((nanosOfDay / 1000000000L) / 60) / 60);
		long expectedHoursOfDay = zNow.getLong(ChronoField.HOUR_OF_DAY);
		boolean leapDay = false;
		if (leapHour) {
			expectedHoursOfDay += 1;
			expectedHoursOfDay %= 24;
		}
		assertThat(hoursOfDay).isEqualTo(expectedHoursOfDay);
	}
}
