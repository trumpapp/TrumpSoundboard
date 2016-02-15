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

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private MediaPlayer mp;

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

        try {
            mp.reset();
            Uri uri = Uri.parse("android.resource://com.trump.m.trumpsoundboard/" + soundID);
            mp.setDataSource(getApplicationContext(), uri);
            mp.prepareAsync();
        } catch (IOException e) {
            Log.e(getPackageName(), e.toString());
        }
    }
}
