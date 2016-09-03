/*
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <http://unlicense.org/>
 */
package com.github.nullnoname.pc3dssdemo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

/**
 * Custom crash handler
 * @author NullNoname
 */
public class CrashHandler implements UncaughtExceptionHandler {
	static public final String EXTRA_REPORT = CrashHandler.class.getName() + ".report";
	static public final String EXTRA_LOGPATH = CrashHandler.class.getName() + ".logpath";

	/**
	 * Tag for error log
	 */
	protected String tag;

	/**
	 * Context
	 */
	protected Context context;

	/**
	 * Activity to start after the crash
	 */
	protected Class<?> crashInfoActivityClass;

	/**
	 * Install the custom crash handler to the current thread
	 * @param tag Tag shown in error log
	 * @param context Context
	 */
	public static void install(String tag, Context context) {
		Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(tag, context));
	}

	/**
	 * Install the custom crash handler to the current thread
	 * @param tag Tag shown in error log
	 * @param context Context
	 * @param crashInfoActivityClass Activity to launch when crashed (only usable when the context is an Activity)
	 */
	public static void install(String tag, Context context, Class<?> crashInfoActivityClass) {
		Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(tag, context, crashInfoActivityClass));
	}

	/**
	 * Constructor
	 * @param tag Tag shown in error log
	 * @param context Context
	 */
	public CrashHandler(String tag, Context context) {
		this.tag = tag;
		this.context = context;
	}

	/**
	 * Constructor
	 * @param tag Tag shown in error log
	 * @param context Context
	 * @param crashInfoActivityClass Activity to launch when crashed (only usable when the context is an Activity)
	 */
	public CrashHandler(String tag, Context context, Class<?> crashInfoActivityClass) {
		this.tag = tag;
		this.context = context;
		this.crashInfoActivityClass = crashInfoActivityClass;
	}

	/**
	 * Get File object for crash log
	 * @return File object for crash log (Internal storage will be used if sdcard is not available)
	 */
	public File getCrashLogFile() {
		File externalDir = getExternalFilesDir();
		if(externalDir == null) externalDir = context.getFilesDir();
		return new File(externalDir, "crash_" + tag + "_" + System.currentTimeMillis() + ".log");
	}

	/**
	 * Get the external files directory
	 * @return External files directory
	 */
	public File getExternalFilesDir() {
		if(Build.VERSION.SDK_INT >= 8) {
			return getExternalFilesDir8();
		} else {
			String packageName = context.getPackageName();
			File externalStorageDirectory = Environment.getExternalStorageDirectory();
			File appDir = new File(externalStorageDirectory.getAbsolutePath() + "/Android/data/" + packageName + "/files");
			appDir.mkdirs();
			return appDir;
		}
	}

	/**
	 * Get the external files directory (for Android 2.2+)
	 * @return External files directory
	 */
	@TargetApi(8)
	public File getExternalFilesDir8() {
		return context.getExternalFilesDir(null);
	}

	// Custom crash handler
	@SuppressWarnings("deprecation")
	public void uncaughtException(Thread thread, Throwable ex) {
		Log.e(tag, "Uncaught Exception", ex);

		// Build the crash log
		final File file = getCrashLogFile();
		final StringBuilder report = new StringBuilder();

		report.append("===== BEGIN CRASH LOG =====\n");
		report.append("***** Crash Log Path *****\n");
		report.append(file.getPath() + "\n\n");

		report.append("***** Stack Trace *****\n");
		report.append(getStackTrace(ex) + "\n\n");

		report.append("***** Device *****\n");
		report.append("Brand:" + Build.BRAND + "\n");
		report.append("Device:" + Build.DEVICE + "\n");
		report.append("Model:" + Build.MODEL + "\n");
		report.append("ID:" + Build.ID + "\n");
		report.append("Product:" + Build.PRODUCT + "\n");
		report.append("\n");

		report.append("***** Android Version *****\n");
		report.append("SDK:" + Build.VERSION.SDK_INT + "\n");
		report.append("Release:" + Build.VERSION.RELEASE + "\n");
		report.append("Incremental:" + Build.VERSION.INCREMENTAL + "\n");

		report.append("===== END CRASH LOG =====\n");

		// Now write the crash log to the sdcard
		final String crashReportString = report.toString();

		if(file != null) {
			OutputStream out = null;

			try {
				out = new FileOutputStream(file);
				out.write(crashReportString.getBytes());
				out.flush();
				Log.d(tag, "Successfully written crash log to " + file.getPath());
			} catch (Throwable e) {
				Log.e(tag, "Failed to write crash log", e);
			} finally {
				if(out != null) {
					try {
						out.close();
					} catch (Throwable e2) {}
				}
			}
		} else {
			Log.e(tag, "SD card isn't writable. Crash log cannot be written.");
		}

		// Start the crash info activity and leave
		if(crashInfoActivityClass != null && context instanceof Activity) {
			try {
				final Activity activity = (Activity)context;
				final Intent intent = new Intent(context, crashInfoActivityClass);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtra(EXTRA_REPORT, crashReportString);
				intent.putExtra(EXTRA_LOGPATH, file.getPath());
				activity.startActivity(intent);
			} catch (Throwable e) {
				Log.e(tag, "Failed to start crash info activity", e);
			}
		}

		System.exit(0);	// Kill the VM
	}

	/**
	 * Write Stack Trace to a String
	 * @param t Exception
	 * @return Stack Trace as a String
	 */
	public static String getStackTrace(Throwable t) {
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw, true);
		t.printStackTrace(pw);
		return sw.getBuffer().toString();
    }
}
