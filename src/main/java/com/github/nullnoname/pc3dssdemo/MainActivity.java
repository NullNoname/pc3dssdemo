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
import java.io.InputStream;
import java.io.InputStreamReader;

import com.github.nullnoname.paudiotrack.AssetFileInputProvider;
import com.github.nullnoname.paudiotrack.ChannelAudioTrack;
import com.github.nullnoname.paudiotrack.LibraryAudioTrack;

import paulscode.sound.IStreamListener;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemException;
import paulscode.sound.SoundSystemLogger;
import paulscode.sound.codecs.CodecIBXM;
import paulscode.sound.codecs.CodecJOrbis;
import paulscode.sound.codecs.CodecJSpeex;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * MainActivity
 * @author NullNoname
 */
public class MainActivity extends Activity implements IStreamListener {
	/** Log Tag */
	private static final String TAG = "MainActivity";
	/** Asset filenames */
	private static final String[] FILENAMES = {"swansong.ogg", "bm.xm", "fables.spx", "gamestart.ogg"};

	/** Log buffer */
	private StringBuffer logBuffer;

	/** ScrollView of log display */
	private ScrollView scrollViewLog;
	/** TextView of log display */
	private TextView textViewLog;

	/** EditText for Audio Buffer Size */
	private EditText editTextAudioBufferSize;
	/** EditText for Audio Buffer Size Multiplier*/
	private EditText editTextAudioBufferSizeMultiplier;

	/** Footsteps play button */
	private Button buttonPlaySE;

	/* These are buttons for songs */
	private Button buttonPlay0, buttonPlay1, buttonPlay2;
	private Button buttonStop0, buttonStop1, buttonStop2;

	/** Our logger */
	private CustomSoundSystemLogger logger;
	/** PaulsCode 3D Sound System */
	private SoundSystem soundSystem;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		CrashHandler.install(TAG, this, CrashActivity.class);
		super.onCreate(savedInstanceState);

		logBuffer = new StringBuffer();
		initGUI();
		initSoundSystem();
	}

	/**
	 * GUI Init
	 */
	private void initGUI() {
		setContentView(R.layout.activity_main);

		// Find the controls
		scrollViewLog = (ScrollView)findViewById(R.id.scrollViewLog);
		textViewLog = (TextView)findViewById(R.id.textViewLog);

		editTextAudioBufferSize = (EditText)findViewById(R.id.editTextAudioBufferSize);
		editTextAudioBufferSizeMultiplier = (EditText)findViewById(R.id.editTextAudioBufferSizeMultiplier);

		buttonPlaySE = (Button)findViewById(R.id.buttonPlaySE);
		buttonPlay0 = (Button)findViewById(R.id.buttonPlay0);
		buttonPlay1 = (Button)findViewById(R.id.buttonPlay1);
		buttonPlay2 = (Button)findViewById(R.id.buttonPlay2);
		buttonStop0 = (Button)findViewById(R.id.buttonStop0);
		buttonStop1 = (Button)findViewById(R.id.buttonStop1);
		buttonStop2 = (Button)findViewById(R.id.buttonStop2);

		// Erase log text
		textViewLog.setText("");

		// Add the button click behaviors
		buttonPlaySE.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				playSong(3);
			}
		});
		buttonPlay0.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				playSong(0);
			}
		});
		buttonPlay1.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				playSong(1);
			}
		});
		buttonPlay2.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				playSong(2);
			}
		});
		buttonStop0.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				stopSong(0);
			}
		});
		buttonStop1.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				stopSong(1);
			}
		});
		buttonStop2.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				stopSong(2);
			}
		});
	}

	/**
	 * 3D Sound System Init
	 */
	private void initSoundSystem() {
		// Set our custom logger before doing anything else
		logger = new CustomSoundSystemLogger();
		SoundSystemConfig.setLogger(logger);

		// Add our stream listener. At the end of a song, endOfStream will be called.
		SoundSystemConfig.addStreamListener(this);

		// Set our AssetFileInputProvider
		SoundSystemConfig.setFileInputProvider(new AssetFileInputProvider(this));

		try {
			// Set codecs
			SoundSystemConfig.setCodec("ogg", CodecJOrbis.class);
			SoundSystemConfig.setCodec("spx", CodecJSpeex.class);
			SoundSystemConfig.setCodec("mod", CodecIBXM.class);
			SoundSystemConfig.setCodec("xm", CodecIBXM.class);
			SoundSystemConfig.setCodec("s3m", CodecIBXM.class);

			// Set library
			SoundSystemConfig.addLibrary(LibraryAudioTrack.class);

			logger.message("Welcome!", 0);
		} catch (SoundSystemException e) {
			logger.errorMessage(TAG, "Sound System init failed", 0);
			logger.printStackTrace(e, 0);
		}
	}

	/**
	 * Get the audio buffer size setting
	 * @return Audio buffer size setting
	 */
	private int getAudioBufferSize() {
		try {
			String s = editTextAudioBufferSize.getText().toString();
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	/**
	 * Get the audio buffer size multiplier setting
	 * @return Audio buffer size multiplier setting
	 */
	private int getAudioBufferSizeMultiplier() {
		try {
			String s = editTextAudioBufferSizeMultiplier.getText().toString();
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return 1;
		}
	}

	/**
	 * Play a song
	 * @param n Song number (3:Sound Effect)
	 */
	private void playSong(final int n) {
		final Thread t = new Thread(new Runnable() {
			public void run() {
				playSongSub(n);
			}
		});
		t.start();
	}

	/**
	 * Play a song (actual code in thread)
	 * @param n Song number (3:Sound Effect)
	 */
	private void playSongSub(final int n) {
		final String filename = FILENAMES[n];
		logger.message("Start playing '" + filename + "'", 0);

		ChannelAudioTrack.setDefaultStreamBufferSize(getAudioBufferSize());
		ChannelAudioTrack.setDefaultStreamBufferSizeMultiplier(getAudioBufferSizeMultiplier());

		if(soundSystem == null) soundSystem = new SoundSystem();

		if(n == 3) { // Sound Effect
			String tempSourceName = soundSystem.quickPlay(false, AssetFileInputProvider.createAssetURL(filename), filename, false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0);
			logger.message("Temporary source name:" + tempSourceName, 0);
		} else { // Streaming songs
			soundSystem.backgroundMusic(filename, AssetFileInputProvider.createAssetURL(filename), filename, false);
		}
	}

	/**
	 * Stop a song
	 * @param n Song number (3:Sound Effect)
	 */
	private void stopSong(final int n) {
		final String filename = FILENAMES[n];
		logger.message("Stopping '" + filename + "'", 0);

		if(soundSystem != null) {
			soundSystem.stop(filename);
		}
	}

	/**
	 * Shutdown the 3D Sound System
	 */
	private void shutdownSoundSystem() {
		try {
			final Thread t = new Thread(new Runnable() {
				public void run() {
					shutdownSoundSystemSub();
				}
			});
			t.start();
		} catch (Exception e) {
			Log.w(TAG, "Problem during cleanup", e);
		}
	}

	/**
	 * Shutdown the 3D Sound System (actual code in thread)
	 */
	private void shutdownSoundSystemSub() {
		try {
			if(soundSystem != null) {
				soundSystem.cleanup();
				soundSystem = null;
			}
		} catch (Exception e) {
			Log.w(TAG, "Problem during cleanup", e);
		}
	}

	/**
	 * Update the log display, scroll to the bottom
	 */
	private void updateLogDisplay() {
		updateLogDisplay(2);
	}

	/**
	 * Update the log display
	 * @param scrollAction 0:No Scroll, 1:Scroll up, 2:Scroll down
	 */
	private void updateLogDisplay(final int scrollAction) {
		runOnUiThread(new Runnable() {
			public void run() {
				// Update the log text
				if(textViewLog != null) textViewLog.setText(logBuffer);

				// Scroll to bottom
				if(scrollViewLog != null && scrollAction != 0) {
					scrollViewLog.postDelayed(new Runnable() {
						public void run() {
							if(scrollViewLog != null) {
								if(scrollAction == 1)
									scrollViewLog.fullScroll(View.FOCUS_UP);
								else if(scrollAction == 2)
									scrollViewLog.fullScroll(View.FOCUS_DOWN);
							}
						}
					}, 200);
				}
			}
		});
	}

	/**
	 * Display Credits
	 */
	private void credits() {
		InputStream in = null;

		try {
			in = getAssets().open("legal.txt");
			InputStreamReader isr = new InputStreamReader(in, "UTF-8");
			BufferedReader br = new BufferedReader(isr);

			StringBuilder sb = new StringBuilder();
			String s;
			while((s = br.readLine()) != null) {
				sb.append(s);
				sb.append('\n');
			}
			logBuffer.setLength(0);
			logBuffer.append(sb);
			updateLogDisplay(1);
		} catch (Exception e) {
			logger.errorMessage(TAG, "Failed to load legal.txt", 0);
			logger.printStackTrace(e, 0);
		} finally {
			try {in.close();} catch (Exception e2) {}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		// Shutdown
		if(id == R.id.action_shutdown) {
			shutdownSoundSystem();
			return true;
		}
		// Credits and License Info
		else if(id == R.id.action_credits) {
			Thread t = new Thread(new Runnable() {
				public void run() {
					credits();
				}
			});
			t.start();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		shutdownSoundSystem();
	}

	/**
	 * Notification of end-of-stream
	 * @param sourcename Source name
	 * @param queueSize Remaining source queue
	 */
	public void endOfStream(String sourcename, int queueSize) {
		logger.message("'" + sourcename + "' has reached the end of the stream", 0);
	}

	/**
	 * Our custom sound system logger
	 */
	private class CustomSoundSystemLogger extends SoundSystemLogger {
		@Override
		public void message(String message, int indent) {
			Log.d("3D Sound System", message);
			logBuffer.append(message);
			logBuffer.append('\n');
			updateLogDisplay();
		}

		@Override
		public void importantMessage(String message, int indent) {
			Log.w("3D Sound System", message);
			logBuffer.append(message);
			logBuffer.append('\n');
			updateLogDisplay();
		}

		@Override
		public void errorMessage(String classname, String message, int indent) {
			Log.e("3D Sound System", classname + ":" + message);
			logBuffer.append(classname);
			logBuffer.append(':');
			logBuffer.append(message);
			logBuffer.append('\n');
			updateLogDisplay();
		}

		@Override
		public void printStackTrace(Exception e, int indent) {
			Log.e("3D Sound System", "Stack Trace", e);
			logBuffer.append(CrashHandler.getStackTrace(e));
			logBuffer.append('\n');
			updateLogDisplay();
		}
	}
}
