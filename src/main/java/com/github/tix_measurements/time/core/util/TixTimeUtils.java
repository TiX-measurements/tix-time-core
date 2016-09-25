package com.github.tix_measurements.time.core.util;

import java.time.LocalTime;
import java.util.function.Supplier;

/**
 * Class that contains all the utilities functions and methods that are used across all the <code>tix-time-core</code> lib project.
 */
public class TixTimeUtils {

	/**
	 * Returns the number of nanoseconds since the start of the day at local time.
	 */
	public static final Supplier<Long> NANOS_OF_DAY = () -> LocalTime.now().toNanoOfDay();
}
