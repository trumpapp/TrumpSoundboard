/*
    This file is part of TrumpSoundboard.

    TrumpSoundboard is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Foobar is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with TrumpSoundboard.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.trump.m.trumpsoundboard;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private ViewGroup layout;

    private MediaPlayer mp;
    private boolean recording = false;
    private TextView recordList;
    private List<Integer> soundQueue;

    private GestureDetectorCompat gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeMediaPlayer();
        setUpListViews();

        recordList = (TextView) findViewById(R.id.record_list);
        soundQueue = new LinkedList<>();

        layout = (ViewGroup) findViewById(R.id.relative_layout);
        this.gestureDetector = new GestureDetectorCompat(this, new CustomGestureListener());

        Toast.makeText(getApplicationContext(), "Swipe down to play a sequence",
                Toast.LENGTH_SHORT).show();
    }


    private void initializeMediaPlayer() {
        mp = new MediaPlayer();
        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
    }

    private void cleanMediaPlayer() {
        if (mp.isPlaying()) {
            mp.stop();
        }
        mp.reset();
        mp.release();
        mp = null;
    }

    private void setUpListViews() {
        ListView listViewLeft = (ListView) findViewById(R.id.buttonList1);
        ArrayAdapter<CharSequence> portraitStartAdapter =
                new ArrayAdapter<>(getApplicationContext(), R.layout.list_entry_left,
                        getResources().getTextArray(R.array.portrait_start));
        listViewLeft.setAdapter(portraitStartAdapter);
        listViewLeft.setOnItemClickListener(this);

        ListView listViewRight = (ListView) findViewById(R.id.buttonList2);
        ArrayAdapter<CharSequence> portraitEndAdapter;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            portraitEndAdapter = new ArrayAdapter<>(getApplicationContext(),
                    R.layout.list_entry_left, getResources().getTextArray(R.array.portrait_end));
        } else {
            portraitEndAdapter = new ArrayAdapter<>(getApplicationContext(),
                    R.layout.list_entry_right, getResources().getTextArray(R.array.portrait_end));
        }
        listViewRight.setAdapter(portraitEndAdapter);
        listViewRight.setOnItemClickListener(this);
    }

    private int getSoundIdFromString(String soundName) {
        soundName = soundName.toLowerCase().replace(" ", "_");
        return getResources().getIdentifier(soundName, "raw", getPackageName());
    }

    private void playRecording() {
        Uri uri;
        int nextSoundIndex = 0;
        try {
            mp.reset();
            uri = Uri.parse("android.resource://com.trump.m.trumpsoundboard/"
                    + soundQueue.get(nextSoundIndex));
            mp.setDataSource(getApplicationContext(), uri);
            mp.prepareAsync();
        } catch (IOException e) {
            Log.e(getPackageName(), e.toString());
        } catch (IndexOutOfBoundsException ignored) {}

        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            int nextSoundIndex = 1;

            @Override
            public void onCompletion(MediaPlayer mp) {
                if (nextSoundIndex < soundQueue.size()) {
                    Uri uri;
                    try {
                        mp.reset();
                        uri = Uri.parse("android.resource://com.trump.m.trumpsoundboard/"
                                + soundQueue.get(nextSoundIndex++));
                        mp.setDataSource(getApplicationContext(), uri);
                        mp.prepareAsync();
                    } catch (IOException e) {
                        Log.e(getPackageName(), e.toString());
                    }
                }
            }
        });
    }

    private void recordListDown() {
        Transition bounds = new ChangeBounds();
        bounds.setDuration(200);
        bounds.setInterpolator(new LinearInterpolator());
        TransitionManager.beginDelayedTransition(layout, bounds);
        TransitionManager.beginDelayedTransition(layout);

        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 190, r.getDisplayMetrics());

        ViewGroup.LayoutParams lparams = recordList.getLayoutParams();
        lparams.height = (int) px;
        recordList.setLayoutParams(lparams);
    }

    private void recordListUp() {
        Transition bounds = new ChangeBounds();
        bounds.setDuration(200);
        bounds.setInterpolator(new LinearInterpolator());
        TransitionManager.beginDelayedTransition(layout, bounds);
        TransitionManager.beginDelayedTransition(layout);

        ViewGroup.LayoutParams lparams = recordList.getLayoutParams();
        lparams.height = 0;
        recordList.setLayoutParams(lparams);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        initializeMediaPlayer();
    }

    @Override
    protected void onPause() {
        if (mp != null)
            cleanMediaPlayer();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mp != null) {
            cleanMediaPlayer();
        }
        super.onDestroy();
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mp.isPlaying()) {
            mp.stop();
        }
        String soundName = parent.getItemAtPosition(position).toString();
        int soundID = getSoundIdFromString(soundName);

        if (!recording) {
            try {
                mp.reset();
                Uri uri = Uri.parse("android.resource://com.trump.m.trumpsoundboard/" + soundID);
                mp.setDataSource(getApplicationContext(), uri);
                mp.prepareAsync();
            } catch (IOException e) {
                Log.e(getPackageName(), e.toString());
            }
        } else {
            String recordListText = recordList.getText() + soundName + "   ";
            recordList.setText(recordListText);
            soundQueue.add(soundID);
        }
    }

    class CustomGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (distanceY < 0) {
                if (!recording) {
                    recording = true;
                    recordListDown();
                }
            } else {
                recording = false;
                recordListUp();
            }
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return onScroll(e1, e2, 0, -velocityY);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            playRecording();
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            // Clear the list
            soundQueue.clear();
            recordList.setText(R.string.record_list_text);
        }
    }
}
