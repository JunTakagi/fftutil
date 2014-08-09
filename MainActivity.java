package com.example.audiotest1;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import org.jtransforms.fft.FloatFFT_1D;

public class MainActivity extends Activity implements View.OnClickListener {
	final static int SAMPLING_RATE = 44100;
	final static int MAX_I = 100;
	int FFT_SIZE;
	AudioRecord audioRec = null;
	Button btn = null;
	TextView lbl = null;
	boolean bIsRecording = false;
	int bufSize;
	Handler handler;
	String labelText;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		handler = new Handler();
		lbl = (TextView)findViewById(R.id.label1);
		btn = (Button)findViewById(R.id.button1);
		btn.setOnClickListener(this);
		bufSize = AudioRecord.getMinBufferSize(
						  	SAMPLING_RATE,
						  	AudioFormat.CHANNEL_CONFIGURATION_MONO,
						  	AudioFormat.ENCODING_PCM_16BIT);
		FFT_SIZE = getMin2Power(bufSize);
		if (FFT_SIZE > bufSize) bufSize = FFT_SIZE;
		audioRec = new AudioRecord(
							MediaRecorder.AudioSource.MIC, 
							SAMPLING_RATE,
							AudioFormat.CHANNEL_CONFIGURATION_MONO,
							AudioFormat.ENCODING_PCM_16BIT,
							bufSize);
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
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == btn) {
			if (bIsRecording) {
				btn.setText(R.string.start_label);
				bIsRecording = false;
			} else {
				// 録音開始
				Log.v("AudioRecord", "startRecording");
				audioRec.startRecording();
				bIsRecording = true;
				// 録音スレッド
				new Thread(new Runnable() {
					@Override

					public void run() {
						FloatFFT_1D fft = new FloatFFT_1D(FFT_SIZE);
						short buf[] = new short[bufSize];
						float[] FFTdata = new float[FFT_SIZE];
						// TODO Auto-generated method stub
						while (bIsRecording) {
							// 録音データ読み込み
							int readSize = audioRec.read(buf, 0, buf.length);
							Log.v("AudioRecord", "read " + buf.length + " bytes");
							// FFT用のデータに移し替え
							for(int i=0; i<readSize; i++){
								FFTdata[i] = (float) buf[i];
							}
							for(int i=readSize; i<FFT_SIZE; i++) {
								FFTdata[i] = 0.0f;
							}
							//fft.realForward(FFTdata);
							
							//データの中身出力
							StringBuilder sb = new StringBuilder();
							for (double d : FFTdata) {
								sb.append(d);
								sb.append(" ");
							}
							labelText = sb.toString();
							handler.post(new Runnable(){
								@Override
								public void run() {
									lbl.setText(labelText);
								}
							});
						}
						// 録音停止
						Log.v("AudioRecord", "stop");
						audioRec.stop();
					}
				}).start();
				btn.setText(R.string.stop_label);
			}
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		super.onDestroy();
		audioRec.release();
	}
	
	public int getMin2Power(int length) {
		int i = 1;
		while(i < MAX_I) {
			int power = (int)Math.pow(2.0, (double)i);
			if (length > power) {
				return length;
			}
			++i;
		}
		return -1;
	}	
}
