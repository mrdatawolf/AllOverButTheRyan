package com.kuhrusty.ryan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

/**
 * This displays the remaining time; when tapped, it starts the countdown, and
 * when tapped again, it resets.
 */
public class TimerActivity extends ActionBarActivity {//implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String LOG_TAG = "AllOverButTheRyan";

//String KEY_PREF_TIMER_SECONDS = "timer_seconds";

    //  this value is duplicated in pref_general.xml.
    private final int defaultSecondsRemaining = 30;
    private int secondsRemaining = defaultSecondsRemaining;
    private long timerIntervalMS = 1000L;
//            getDefaultSharedPreferences(this).getInt(KEY_PREF_TIMER_SECONDS, 30);
    private boolean timerRunning = false;
    private int runs = 0;
    private int timeouts = 0;

    private CountDownTimer timer;
    private MediaPlayer mediaPlayer;
    private TextView timerDisplay;
    private static Random random = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_timer);

        Resources res = getResources();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        secondsRemaining = Integer.parseInt(sharedPref.getString(SettingsActivity.KEY_PREF_TIMER_DURATION, "" + defaultSecondsRemaining));

        // Create the text view
        TextView tv = new TextView(this);
        tv.setTextSize(120);
        tv.setText(Integer.toString(secondsRemaining));
        tv.setTextColor(res.getColor(R.color.waitingFG));
        tv.setBackgroundColor(res.getColor(R.color.waitingBG));
        tv.setGravity(Gravity.CENTER);
//        tv.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (timerRunning) {
//                    resetTimer();
//                } else {
//                    startTimer();
//                }
////                finish();
//                return true;
//            }
//        });
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timerRunning) {
                    resetTimer();
                } else {
                    startTimer();
                }
            }
        });
        setContentView(tv);
        timerDisplay = tv;
    }

    void resetTimer() {
        timerRunning = false;
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        secondsRemaining = Integer.parseInt(sharedPref.getString(SettingsActivity.KEY_PREF_TIMER_DURATION, "" + defaultSecondsRemaining));

        Resources res = getResources();
        timerDisplay.setText(Integer.toString(secondsRemaining));
        timerDisplay.setTextColor(res.getColor(R.color.waitingFG));
        timerDisplay.setBackgroundColor(res.getColor(R.color.waitingBG));
    }

    void startTimer() {
        if (timer != null) {
            timer.cancel();
        }
        ++runs;
        //  OK... there is a, uhh... well, let's say "bug," where CountDownTimer
        //  won't deliver the last tick because a few ms fewer than a full
        //  interval remains, so it goes 5, 4, 3, 2... ... onFinish().  So one
        //  workaround is to crank down the timer interval; the fact that we're
        //  firing twice per second won't be visible, because the second fire
        //  just sets the text to what it already is.  Ugh.
        timer = new CountDownTimer(secondsRemaining * 1000L, timerIntervalMS / 2) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsRemaining = (int)((millisUntilFinished - 1L) / 1000L) + 1;
                //Log.d(LOG_TAG, "timer onTick(" + millisUntilFinished +
                //        "), calling it " + secondsRemaining + "s remaining");
                handleTick(secondsRemaining);
            }
            @Override
            public void onFinish() {
                //Log.d(LOG_TAG, "timer onFinish()");
                handleTimeUp();
            }
        };
        timer.start();
        timerRunning = true;
        Resources res = getResources();
        timerDisplay.setTextColor(res.getColor(R.color.runningFG));
        timerDisplay.setBackgroundColor(res.getColor(R.color.runningBG));
    }

    void handleTick(int secondsRemaining) {
        timerDisplay.setText(Integer.toString(secondsRemaining));
    }

    void handleTimeUp() {
        //  set timer = null?
        ++timeouts;
        Resources res = getResources();
        CharSequence text = res.getText(R.string.arghh1);
        //  for debugging, let's run through the list of strings sequentially.
        //  There's got to be a better way than hard-coding the number of
        //  strings here...
        switch ((timeouts - 1) % 3) {  //  #6 doesn't fit
            //handled above case 0: text = res.getText(R.string.arghh1); break;
            case 1: text = res.getText(R.string.arghh2); break;
            case 2: text = res.getText(R.string.arghh3); break;
            //case 3: text = res.getText(R.string.arghh4); break;
            //case 4: text = res.getText(R.string.arghh5); break;
            //case 5: text = res.getText(R.string.arghh6); break;
        }
        timerDisplay.setText(text);
        timerDisplay.setTextColor(res.getColor(R.color.endedFG));
        timerDisplay.setBackgroundColor(res.getColor(R.color.endedBG));

        //  figure out which audio file to play
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String soundName = sharedPref.getString(SettingsActivity.KEY_PREF_SOUND, "ryan");
        int sound = R.raw.ryan;
        //Log.d(LOG_TAG, "choosing sound: " + soundName);
        if (soundName.equals("random")) sound = nextRand();
        else if (soundName.equals("sequence")) sound = nextSequence(timeouts - 1);
        else if (soundName.equals("ryan")) sound = R.raw.ryan;
        else if (soundName.equals("takeyourdamnturn")) sound = R.raw.takeyourdamnturn;
        else if (soundName.equals("cletus")) sound = R.raw.cletus;
        else if (soundName.equals("go1")) sound = R.raw.go1;
        else if (soundName.equals("stab")) sound = R.raw.stab;
        else if (soundName.equals("youpass")) sound = R.raw.youpass;
        mediaPlayer = MediaPlayer.create(getApplicationContext(), sound);
        mediaPlayer.start();
    }

    private int nextRand() {
        if (random == null) {
            random = new Random();
        }
        int rv = random.nextInt(6);
        //Log.d(LOG_TAG, "got random " + rv);
        return nextSequence(rv);
    }

    private int nextSequence(int count) {
        switch (count % 6) {
            case 0: return R.raw.ryan;
            case 1: return R.raw.takeyourdamnturn;
            case 2: return R.raw.youpass;
            case 3: return R.raw.go1;
            case 4: return R.raw.cletus;
            case 5: return R.raw.stab;
        }
        return R.raw.ryan;
    }

    @Override
    public void onStop() {
        if (timer != null) {
            timer.cancel();

        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_timer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //  did our timer duration change?
        if (!timerRunning) {
            resetTimer();
        }
//Log.d(LOG_TAG, "got onResume(), registering pref change listener");
//        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
////.getPreference(SettingsActivity.KEY_PREF_TIMER_DURATION)
//                .registerOnSharedPreferenceChangeListener(this);
    }
//    @Override
//    protected void onPause() {
//        super.onPause();
//Log.d(LOG_TAG, "got onPause(), unregistering pref change listener");
//        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
//                .unregisterOnSharedPreferenceChangeListener(this);
//    }
//    @Override
//    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//        if (key.equals(SettingsActivity.KEY_PREF_TIMER_DURATION)) {
//Log.d(LOG_TAG, "got onSharedPreferenceChanged(sp, " + key + "), timerRunning == " + timerRunning);
//            if (!timerRunning) {
//                resetTimer();
//            }
//        }
//    }
}
