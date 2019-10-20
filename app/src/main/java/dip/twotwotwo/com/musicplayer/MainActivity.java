package dip.twotwotwo.com.musicplayer;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private SeekBar seekBar;
    private SongAdapter adapter;
    private ArrayList<Song> songs;
    private ListView listView;
    MediaPlayer mediaPlayer;
    SongThread thread;
    boolean threadRunning = false;
    int songProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.list);
        seekBar = findViewById(R.id.seekBar);
        songs = new ArrayList<>();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                songProgress = progress;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(songProgress);
            }
        });

        getAllSongs();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Song song = songs.get(position);
                String path = song.path;

                if(mediaPlayer != null){
                    mediaPlayer.stop();
                }
                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(path);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    seekBar.setMax(mediaPlayer.getDuration());

                    threadRunning = true;
                    thread = new SongThread();
                    thread.start();

                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, "Song not found", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void getAllSongs(){
        if(ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 12);
        }
        ContentResolver resolver = getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        Cursor cursor = resolver.query(songUri, null,
                selection, null, MediaStore.Audio.Media.TITLE + " ASC");

        if (cursor != null) {
            if(cursor.moveToFirst()){
                int songTitleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int songArtistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                int songAlbumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
                int songLocationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);

                do{
                    String title = cursor.getString(songTitleColumn);
                    String artist = cursor.getString(songArtistColumn);
                    String album = cursor.getString(songAlbumColumn);
                    String path = cursor.getString(songLocationColumn);

                    songs.add(new Song(title, artist, album, path));
                }while (cursor.moveToNext());
            }
            cursor.close();
        }
        adapter = new SongAdapter(songs, MainActivity.this);
        listView.setAdapter(adapter);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode){
            case 12:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    getAllSongs();
                }
                break;
                default:
                    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void pauseSong(View view) {
        if(mediaPlayer != null){
            mediaPlayer.pause();
        }
    }
    public void stopSong(View view) {
        if(mediaPlayer != null){
            mediaPlayer.stop();
            threadRunning = false;
            seekBar.setProgress(0);
        }
    }
    public void startSong(View view) {
        if(mediaPlayer != null){
            mediaPlayer.start();
        }
    }
    class SongThread extends Thread{
        @Override
        public void run() {
            while (threadRunning){
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(mediaPlayer != null) {
                                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                            }
                        }
                    });
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
    }
}
