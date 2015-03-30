package com.bagri.common.util;

public class DateUtils {
	
	private static final long MILLIS_PER_SECOND = 1000;
	private static final long MILLIS_PER_MINUTE = 60*MILLIS_PER_SECOND;
	private static final long MILLIS_PER_HOUR = 60*MILLIS_PER_MINUTE;
	private static final long MILLIS_PER_DAY = 24*MILLIS_PER_HOUR;

    /**
     * Get readable time duration from long
     *
     * @param duration duration
     * @return formatted duration: days/hours/mins/sec/ms
     */
    public static String getDuration(long duration) {
        StringBuilder result = new StringBuilder();
        if (duration == 0) {
            result.append("0 ms");
        } else {
            long days = duration / MILLIS_PER_DAY;
            if (days > 0) {
                result.append(days).append(" days ");
                duration = duration - days * MILLIS_PER_DAY;
            }
            long hours = duration / MILLIS_PER_HOUR;
            if (hours > 0) {
                result.append(hours).append(" hours ");
                duration = duration - hours * MILLIS_PER_HOUR;
            }
            long mins = duration / MILLIS_PER_MINUTE;
            if (mins > 0) {
                result.append(mins).append(" min ");
                duration = duration - mins * MILLIS_PER_MINUTE;
            }
            long secs = duration / MILLIS_PER_SECOND;
            if (secs > 0) {
                result.append(secs).append(" sec ");
                duration = duration - secs * MILLIS_PER_SECOND;
            }
            if (duration > 0) {
                result.append(duration).append(" ms ");
            }
            result.deleteCharAt(result.length() - 1);
        }
        return result.toString();
    }
	
}
