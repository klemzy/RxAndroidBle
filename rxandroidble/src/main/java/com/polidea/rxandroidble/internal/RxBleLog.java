package com.polidea.rxandroidble.internal;

import android.support.annotation.IntDef;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This file is a modification of Timber logging library -> https://github.com/JakeWharton/timber
 */
public class RxBleLog {

    @IntDef({VERBOSE, DEBUG, INFO, WARN, ERROR, NONE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LogLevel {

    }

    public static final int VERBOSE = Log.VERBOSE;
    public static final int DEBUG = Log.DEBUG;
    public static final int INFO = Log.INFO;
    public static final int WARN = Log.WARN;
    public static final int ERROR = Log.ERROR;
    public static final int NONE = Integer.MAX_VALUE;
    private static final Pattern ANONYMOUS_CLASS = Pattern.compile("\\$\\d+$");
    private static final ThreadLocal<String> NEXT_TAG = new ThreadLocal<>();

    private static int logLevel = Integer.MAX_VALUE;
    private static File logDestination = null;

    private RxBleLog() {

    }

    public static void setLogLevel(@LogLevel int logLevel) {
        RxBleLog.logLevel = logLevel;
    }

    public static void setLogLevelWithFile(@LogLevel int logLevel, File logDestination) {
        RxBleLog.logLevel = logLevel;
        RxBleLog.logDestination = logDestination;
    }

    private static String createTag() {
        String tag = NEXT_TAG.get();
        if (tag != null) {
            NEXT_TAG.remove();
            return tag;
        }

        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        if (stackTrace.length < 5) {
            throw new IllegalStateException(
                    "Synthetic stacktrace didn't have enough elements: are you using proguard?");
        }
        tag = stackTrace[4].getClassName();
        Matcher m = ANONYMOUS_CLASS.matcher(tag);
        if (m.find()) {
            tag = m.replaceAll("");
        }
        tag = tag.replace("Impl", "");
        tag = tag.replace("RxBle", "");
        return "RxBle#" + tag.substring(tag.lastIndexOf('.') + 1);
    }

    private static String formatString(String message, Object... args) {
        // If no varargs are supplied, treat it as a request to log the string without formatting.
        return args.length == 0 ? message : String.format(message, args);
    }

    public static void v(String message, Object... args) {
        throwShade(Log.VERBOSE, null, message, args);
    }

    public static void v(Throwable t, String message, Object... args) {
        throwShade(Log.VERBOSE, t, message, args);
    }

    public static void d(String message, Object... args) {
        throwShade(Log.DEBUG, null, message, args);
    }

    public static void d(Throwable t, String message, Object... args) {
        throwShade(Log.DEBUG, t, message, args);
    }

    public static void i(String message, Object... args) {
        throwShade(Log.INFO, null, message, args);
    }

    public static void i(Throwable t, String message, Object... args) {
        throwShade(Log.INFO, t, message, args);
    }

    public static void w(String message, Object... args) {
        throwShade(Log.WARN, null, message, args);
    }

    public static void w(Throwable t, String message, Object... args) {
        throwShade(Log.WARN, t, message, args);
    }

    public static void e(String message, Object... args) {
        throwShade(Log.ERROR, null, message, args);
    }

    public static void e(Throwable t, String message, Object... args) {
        throwShade(Log.ERROR, t, message, args);
    }

    private static void throwShade(int priority, Throwable t, String message, Object... args) {
        final String formattedMessage = formatString(message, args);
        final String finalMessage;

        if (formattedMessage == null || formattedMessage.length() == 0) {
            if (t != null) {
                finalMessage = Log.getStackTraceString(t);
            } else {
                // Swallow message if it's null and there's no throwable.
                return;
            }
        } else if (t != null) {
            finalMessage = formattedMessage + "\n" + Log.getStackTraceString(t);
        } else {
            finalMessage = formattedMessage;
        }

        String tag = createTag();
        println(priority, tag, finalMessage);
    }

    private static void println(int priority, String tag, String message) {
        if (message.length() < 4000) {
            if (priority < logLevel) {
                Log.println(priority, tag, message);
            }

            if (logDestination != null) {
                writeToFile(logDestination, String.format(Locale.getDefault(), "%s %s", tag, message));
            }
        } else {
            // It's rare that the message will be this large, so we're ok with the perf hit of splitting
            // and calling Log.println N times.  It's possible but unlikely that a single line will be
            // longer than 4000 characters: we're explicitly ignoring this case here.
            String[] lines = message.split("\n");
            for (String line : lines) {
                if (priority < logLevel) {
                    Log.println(priority, tag, line);
                }

                if (logDestination != null) {
                    writeToFile(logDestination, String.format(Locale.getDefault(), "%s %s", tag, line));
                }
            }
        }
    }

    private static void writeToFile(File file, String msg) {
        OutputStreamWriter outputStreamWriter = null;
        try {
            outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file, true));
            long currentTime = System.currentTimeMillis();
            SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault());
            String message = String.format(Locale.getDefault(), "%s %s.", format.format(new Date(currentTime)), msg);
            outputStreamWriter.write(message);
            outputStreamWriter.write("\n");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputStreamWriter != null) {
                try {
                    outputStreamWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean isAtLeast(int expectedLogLevel) {
        return logLevel <= expectedLogLevel;
    }
}
