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

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private MediaPlayer mp;
    private boolean recording = false;
    private TextView recordList;
    private Queue soundQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeMediaPlayer();

        ListView listViewLeft = (ListView) findViewById(R.id.buttonList1);
        ArrayAdapter<CharSequence> portraitStartAdapter =
                new ArrayAdapter<>(getApplicationContext(), R.layout.list_entry_left,
                        getResources().getTextArray(R.array.portrait_start));
        listViewLeft.setAdapter(portraitStartAdapter);
        listViewLeft.setOnItemClickListener(this);

        ListView listViewRight = (ListView) findViewById(R.id.buttonList2);
        ArrayAdapter<CharSequence> portraitEndAdapter = new ArrayAdapter<>(getApplicationContext(),
                R.layout.list_entry_right, getResources().getTextArray(R.array.portrait_end));
        listViewRight.setAdapter(portraitEndAdapter);
        listViewRight.setOnItemClickListener(this);

        recordList = (TextView) findViewById(R.id.record_list);

        soundQueue = new LinkedList();
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
        if (mp != null)
            cleanMediaPlayer();
        super.onDestroy();
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
        if (mp.isPlaying())
            mp.stop();
        mp.reset();
        mp.release();
        mp = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mp.isPlaying())
            mp.stop();

        int soundID = 0;
        switch (parent.getId()) {
            case R.id.buttonList1:
                soundID = ListConstants.soundIDs[position];
                break;
            case R.id.buttonList2:
                soundID = ListConstants.soundIDs[position + ListConstants.LEFT_LIST_SIZE];
                break;
            default:
                break;
        }

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
            StringBuilder recordListText = new StringBuilder(recordList.getText());
            recordListText.append("    ");
            recordListText.append(parent.getItemAtPosition(position).toString());
            recordList.setText(recordListText);
            soundQueue.add(soundID);
        }
    }

    public void beginRecord(View view) {
        recording = true;
        findViewById(R.id.maga_banner).setVisibility(View.INVISIBLE);
        recordList.setVisibility(View.VISIBLE);
    }

    public void stopRecord(View view) {
        recording = false;
        findViewById(R.id.maga_banner).setVisibility(View.VISIBLE);
        recordList.setVisibility(View.INVISIBLE);
        recordList.setText(R.string.record_list_text);
        soundQueue.clear();
    }

    public void playRecording(View view) {
        Uri uri;
        try {
            mp.reset();
            uri = Uri.parse("android.resource://com.trump.m.trumpsoundboard/" + soundQueue.poll());
            mp.setDataSource(getApplicationContext(), uri);
            mp.prepareAsync();
        } catch (IOException e) {
            Log.e(getPackageName(), e.toString());
        }
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (!soundQueue.isEmpty()) {
                    Log.e(getPackageName(), "Playing " + soundQueue.peek());
                    Uri uri;
                    try {
                        mp.reset();
                        uri = Uri.parse("android.resource://com.trump.m.trumpsoundboard/" + soundQueue.poll());
                        mp.setDataSource(getApplicationContext(), uri);
                        mp.prepareAsync();
                    } catch (IOException e) {
                        Log.e(getPackageName(), e.toString());
                    }
                }
            }
        });
        recordList.setText(R.string.record_list_text);
    }
}
