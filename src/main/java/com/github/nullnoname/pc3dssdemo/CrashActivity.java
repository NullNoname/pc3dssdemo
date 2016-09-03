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

import java.io.BufferedReader;
import java.io.InputStreamReader;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Crash log activity
 * @author NullNoname
 */
public class CrashActivity extends Activity {
	private static final String TAG = "CrashActivity";

	/** Close button */
	private Button buttonCrashClose;
	/** Crash Log TextView */
	private TextView textViewCrashLog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		CrashHandler.install(TAG, this);
		super.onCreate(savedInstanceState);
		initGUI();
		displayLog();
	}

	private void initGUI() {
		setContentView(R.layout.activity_crash);

		buttonCrashClose = (Button)findViewById(R.id.buttonCrashClose);
		textViewCrashLog = (TextView)findViewById(R.id.textViewCrashLog);

		buttonCrashClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				quit();
			}
		});
	}

	private void displayLog() {
		StringBuffer sbuf = new StringBuffer();

		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(openFileInput("crash.log")));
			String s;
			while((s = r.readLine()) != null) {
				sbuf.append(s);
				sbuf.append('\n');
			}
			r.close();
			textViewCrashLog.setText(sbuf);
		} catch (Exception e) {
			if(sbuf.length() == 0)
				textViewCrashLog.setText("Failed to load crash log");
			else
				textViewCrashLog.setText(sbuf);
		}
	}

	@Override
	public void onBackPressed() {
		quit();
	}

	/**
	 * Quit the Application
	 */
	private void quit() {
		android.os.Process.killProcess(android.os.Process.myPid());
	}
}
