package com.example.mp3playerapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    TextView tName,tAuthor,tStart,tEnd;
    Button bPlay;
    ImageButton iPlay, iSkipFor,iSkipBack;
    MediaPlayer mediaPlayer;
    ListView listView;
    SeekBar seekBar;
    private List<Uri> songList = new ArrayList<>();
    private List<String> songName=new ArrayList<>();
    private ArrayAdapter arrayAdapter;
    private int currentSongIndex = 0;
    private Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        tName=findViewById(R.id.txtName);
        tAuthor=findViewById(R.id.txtAuthor);
        tStart=findViewById(R.id.txtStart);
        tEnd=findViewById(R.id.txtEnd);
        bPlay=findViewById(R.id.button);
        iPlay=findViewById(R.id.imgPlay);
        iSkipFor=findViewById(R.id.imgFord);
        iSkipBack=findViewById(R.id.imgBack);
        seekBar=findViewById(R.id.seekBar);
        listView=findViewById(R.id.lv);

        arrayAdapter=new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1,songName);
        listView.setAdapter(arrayAdapter);

        bPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("audio/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(Intent.createChooser(intent, "Select Music"), 1);
            }
        });
        iPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer!=null)
                {
                    if (mediaPlayer.isPlaying())
                    {
                        mediaPlayer.pause();
                        iPlay.setImageResource(R.drawable.baseline_play_circle_24);
                        return;
                    }
                    if (!mediaPlayer.isPlaying())
                    {
                        mediaPlayer.start();
                        iPlay.setImageResource(R.drawable.baseline_pause_circle_24);
                    }
                }
            }
        });
        iSkipFor.setOnClickListener(v->{
            if (mediaPlayer.isPlaying()) playMusic(currentSongIndex+1);
        });
        iSkipBack.setOnClickListener(v->{if (mediaPlayer.isPlaying()) playMusic(currentSongIndex+1);});
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null)
                {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            iPlay.setImageResource(R.drawable.baseline_pause_circle_24);
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri songUri = data.getClipData().getItemAt(i).getUri();
                    songList.add(songUri);
                }
                for (Uri uri: songList)
                {
                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    retriever.setDataSource(this, uri);
                    String name = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                    if (!songName.contains(name))
                    {
                        songName.add(name);
                        arrayAdapter.notifyDataSetChanged();
                    }
                }
            }
            else if (data.getData() != null) {
                Uri songUri = data.getData();
                if (songList.contains(songUri)) return;
                songList.add(songUri);
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(this, songUri);
                String name=retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                songName.add(name);
                arrayAdapter.notifyDataSetChanged();
                currentSongIndex=songList.size()-1;
            }
            if (!songList.isEmpty()) {
                playMusic(currentSongIndex);
            }
        }
    }
    private void playMusic(int index) {
        if (index < 0 || index >= songList.size()) {
            if (index < 0) index = songList.size()-1;
            else {
                index = 0;
            }
        }
        currentSongIndex = index;
        Uri audioUri = songList.get(index);

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(this, audioUri);

        String title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        String artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        try {
            retriever.release();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (title == null) title = "Unknown Title";
        if (artist == null) artist = "Unknown Artist";

        tName.setText(title);
        tAuthor.setText(artist);
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        mediaPlayer = MediaPlayer.create(this, audioUri);
        if (mediaPlayer != null) {
            String duration = millisecondToString(mediaPlayer.getDuration());
            tEnd.setText(duration);
            mediaPlayer.setOnPreparedListener(mp->{
                seekBar.setMax(mp.getDuration());
                mp.start();
                updateSeekBar();
            });
            mediaPlayer.setOnCompletionListener(mp -> playMusic(currentSongIndex + 1)); // Tự chuyển bài
        } else {
            Toast.makeText(this, "Failed to play music", Toast.LENGTH_SHORT).show();
        }
    }
    public String millisecondToString(int time)
    {
        String elapsedTime= "";
        int minutes = time / 1000/60;
        int seconds = time/1000 % 60;
        elapsedTime = minutes +":";
        if (seconds<10){
            elapsedTime+="0";
        }
        elapsedTime+=seconds;
        return elapsedTime;
    }
    private void updateSeekBar() {
        if (mediaPlayer != null) {
            int currentPosition = mediaPlayer.getCurrentPosition();
            seekBar.setProgress(currentPosition);
            String currentTime = millisecondToString(currentPosition);
            tStart.setText(currentTime+"");
            handler.postDelayed(this::updateSeekBar, 1000);
        }
    }
}