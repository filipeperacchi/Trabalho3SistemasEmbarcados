package ca.yorku.cse.mack.demotiltballcse03076;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;

/**
 * Created by Alex on 30/11/2015.
 */
public class CreditsActivity extends Activity {
    private MediaPlayer mediaPlayer;
    private int mediaPointer;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.credits);

        Intent intent = getIntent();
        int value = intent.getIntExtra("media", 0);

        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.duck_after_the_bread);
        mediaPlayer.setLooping(true);
        mediaPointer = value;
    }

    @Override
    public void onPause() {
        super.onPause();
        mediaPlayer.pause();
        Intent intent = getIntent();
        intent.putExtra("media", mediaPlayer.getCurrentPosition());
        setIntent(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        mediaPlayer.seekTo(mediaPointer);
        mediaPlayer.start();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mediaPlayer.stop();
        mediaPlayer.release();
    }
}
