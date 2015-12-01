package ca.yorku.cse.mack.demotiltballcse03076;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.io.IOException;

/**
 * Original code structure by:
* DemoAndroid - with modifications by...
*
* Login ID - CSE03076
* Student ID - 210635597
* Last name - Likhite
* First name(s) - Rohan
 *
 * Reutilizado e modificado por:
 * Filipe Peracchi Pisoni
 * Alex Moraes
*/

public class DemoTiltBallSetup extends Activity 
{
	private MediaPlayer mediaPlayer;
	private int mediaPointer;

	private Spinner spinOrderOfControl, spinGain, spinPathMode, spinPathWidth, spinlapNum;

	final String[] ORDER_OF_CONTROL = { "Padrão", "Inverter X", "Inverter Y", "Inverter Todos" }; // NOTE: do not change strings
	final String[] GAIN = { "Muito fácil", "Fácil", "Médio", "Difícil", "Muito difícil" };
	final String[] PATH_TYPE = { "Ativar", "Desativar"};
	final String[] PATH_WIDTH = { "Narrow", "Medium", "Wide" };
	final String[] NUM_LAPS = {"1", "2", "3", "4", "5"};

	// somewhat arbitrary mappings for gain by order of control
	final static int[] GAIN_ARG_POSITION_CONTROL = { 5, 10, 20, 40, 80 };
	final static int[] GAIN_ARG_VELOCITY_CONTROL = { 25, 50, 100, 200, 400 };

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setup);

		mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.duck_after_the_bread);
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mediaPlayer.setLooping(true);
		mediaPointer = 0;

		spinOrderOfControl = (Spinner) findViewById(R.id.paramOrderOfControl);
		ArrayAdapter<CharSequence> adapter2 = new ArrayAdapter<CharSequence>(this, R.layout.spinnerstyle, ORDER_OF_CONTROL);
		spinOrderOfControl.setAdapter(adapter2);

		spinGain = (Spinner) findViewById(R.id.paramGain);
		ArrayAdapter<CharSequence> adapter3 = new ArrayAdapter<CharSequence>(this, R.layout.spinnerstyle, GAIN);
		spinGain.setAdapter(adapter3);
		spinGain.setSelection(0); // "very low" default

		spinPathMode = (Spinner) findViewById(R.id.paramPathType);
		ArrayAdapter<CharSequence> adapter1 = new ArrayAdapter<CharSequence>(this, R.layout.spinnerstyle, PATH_TYPE);
		spinPathMode.setAdapter(adapter1);
		spinPathMode.setSelection(0); // free

		spinPathWidth = (Spinner) findViewById(R.id.paramPathWidth);
		ArrayAdapter<CharSequence> adapter4 = new ArrayAdapter<CharSequence>(this, R.layout.spinnerstyle, PATH_WIDTH);
		spinPathWidth.setAdapter(adapter4);
		spinPathWidth.setSelection(1); // medium

		spinlapNum = (Spinner) findViewById(R.id.paramlapNum);
		ArrayAdapter<CharSequence> adapter5 = new ArrayAdapter<CharSequence>(this, R.layout.spinnerstyle, NUM_LAPS);
		spinlapNum.setAdapter(adapter5);
		spinlapNum.setSelection(1); // 1 Lap
	}

	@Override
	public void onPause() {
		super.onPause();
		mediaPointer = mediaPlayer.getCurrentPosition();
		mediaPlayer.pause();
	}

	@Override
	public void onResume() {
		super.onResume();
		try {
			Thread.sleep(100);
		}catch (InterruptedException e){
			e.printStackTrace();
		}
		mediaPlayer.seekTo(mediaPointer);
		mediaPlayer.start();
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		mediaPlayer.stop();
		mediaPlayer.release();
	}

	@Override
	public void onNewIntent(Intent intent) {
		if (intent != null)
			setIntent(intent);
	}

	/** Called when the "OK" button is pressed. */
	public void clickOK(View view) 
	{
		// get user's choices... 
		String invertOption = (String) spinOrderOfControl.getSelectedItem();
        int difficultyOption = spinGain.getSelectedItemPosition();
        int tiltOption = spinPathMode.getSelectedItemPosition();

		// actual gain value depends on order of control
		int invert;
		if (invertOption.equals("Padrão"))
            invert = 0;
		else if (invertOption.equals("Inverter X"))
            invert = 1;
        else if (invertOption.equals("Inverter Y"))
            invert = 2;
        else
            invert = 3;

        int difficulty = difficultyOption;
        boolean tiltLimiter;

        if (tiltOption == 0)
            tiltLimiter = true;
        else
            tiltLimiter = false;

		String pathType = PATH_TYPE[spinPathMode.getSelectedItemPosition()];
		String pathWidth = PATH_WIDTH[spinPathWidth.getSelectedItemPosition()];
		String lapNums = NUM_LAPS[spinlapNum.getSelectedItemPosition()];
		// bundle up parameters to pass on to activity
		Bundle b = new Bundle();
		b.putString("orderOfControl", "Velocity");
		b.putInt("gain", GAIN_ARG_VELOCITY_CONTROL[0]);
        b.putInt("invert", invert);
        b.putInt("difficulty", difficulty);
        b.putBoolean("tiltLimiter", tiltLimiter);
		b.putString("pathType", PATH_TYPE[0]);
		b.putString("pathWidth", PATH_WIDTH[0]);
		b.putString("totalLaps", NUM_LAPS[0]);

		// start experiment activity
		Intent i = new Intent(getApplicationContext(), DemoTiltBallActivity.class);
		i.putExtras(b);
		startActivity(i);
		finish();
	}

	/** Called when the "Exit" button is pressed. */
	public void clickExit(View view)
	{
		super.onDestroy(); // cleanup
		this.finish(); // terminate
	}

	/** Called when the "Exit" button is pressed. */
	public void clickCredits(View view)
	{
		Intent myIntent = new Intent(DemoTiltBallSetup.this, CreditsActivity.class);
		myIntent.putExtra("media", mediaPlayer.getCurrentPosition());
		DemoTiltBallSetup.this.startActivity(myIntent);
	}
}
