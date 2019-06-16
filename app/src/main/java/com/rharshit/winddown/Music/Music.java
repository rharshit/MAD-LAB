package com.rharshit.winddown.Music;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.rharshit.winddown.R;
import com.rharshit.winddown.Util.Blur;
import com.rharshit.winddown.Util.Scale;
import com.rharshit.winddown.Util.Theme;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


public class Music extends AppCompatActivity {
    private static final String TAG = "Music";
    private static final float albumArtMarginFactor = 0.1f;
    public static int oneTimeOnly = 0;
    private static MediaPlayer mediaPlayer;
    private static boolean isPlaying;
    private static ImageButton bPlayPause;
    private static String album;
    private static String name;
    private static String uri;
    private static String id;
    int vWidth;
    private ImageButton b1, b4;
    private ImageView iv;
    private ImageView ivB;
    private RelativeLayout rl;
    private ImageView arrow;
    private double startTime = 0;
    private double finalTime = 0;
    private Handler myHandler = new Handler();
    private int forwardTime = 5000;
    private int backwardTime = 5000;
    private SeekBar seekbar;
    private TextView tx1, tx2, tx3, tx4, tx5;
    private Context mContext;
    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            startTime = mediaPlayer.getCurrentPosition();
            tx1.setText(String.format("%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                    toMinutes((long) startTime)))
            );
            seekbar.setProgress((int) startTime);
            myHandler.postDelayed(this, 100);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Theme.getTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        mContext = this;

        b1 = findViewById(R.id.button);
//        b2 = (ImageButton) findViewById(R.id.button2);
//        b3 = (ImageButton)findViewById(R.id.button3);
        b4 = findViewById(R.id.button4);
        bPlayPause = findViewById(R.id.play_pause);

        iv = findViewById(R.id.imageView);
        ivB = findViewById(R.id.imageViewBlur);
        rl = findViewById(R.id.rl_music_album_art);

        tx1 = findViewById(R.id.textView2);
        tx2 = findViewById(R.id.textView3);
        tx4 = findViewById(R.id.textView4);
        tx5 = findViewById(R.id.textView5);

        arrow = findViewById(R.id.arrow_open_music_list);
        arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mContext, MusicList.class);
                startActivityForResult(i, 0);
            }
        });

        vWidth = getWindowManager().getDefaultDisplay().getWidth();

        ViewGroup.LayoutParams param = ivB.getLayoutParams();
        param.width = vWidth;
        param.height = vWidth;

        ViewGroup.LayoutParams param2 = iv.getLayoutParams();
        param2.width = (int) (vWidth * (1 - 2 * albumArtMarginFactor));
        param2.height = (int) (vWidth * (1 - 2 * albumArtMarginFactor));

        ivB.setLayoutParams(param);
        iv.setLayoutParams(param2);

        ViewGroup.LayoutParams rlParam = rl.getLayoutParams();
        rlParam.width = vWidth;
        rlParam.height = (int) (vWidth * (1 + albumArtMarginFactor));
        rl.setPadding(0, (int) (vWidth * albumArtMarginFactor), 0, 0);
        rl.setLayoutParams(rlParam);

        Drawable art = ivB.getDrawable();

        float s = (float) art.getIntrinsicHeight() / vWidth;
        if (s > 1) {
            s = 1 / s;
        }
        Log.d(TAG, "onCreate: " + s);

        ivB.setImageBitmap(Blur.transform(mContext, art, 25,
                art.getIntrinsicHeight(), vWidth,
                s));

        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.song);
        } else {
            tx4.setText(name);
            tx5.setText(album);

            Bitmap albumArt = getAlbumArt(id);
            if (albumArt != null) {
                iv.setImageBitmap(albumArt.copy(albumArt.getConfig(), false));
                s = (float) albumArt.getHeight() / vWidth;
                if (s > 1) {
                    s = 1 / s;
                }
                Log.d(TAG, "onActivityResult: " + s);

                Bitmap blur = Blur.transform(mContext, albumArt, 25,
                        albumArt.getHeight(), vWidth,
                        s);
                ivB.setImageBitmap(blur.copy(blur.getConfig(), false));
            } else {
                iv.setImageDrawable(getDrawable(R.drawable.ic_music));
                ivB.setImageDrawable(getDrawable(android.R.color.transparent));
            }
        }
        isPlaying = mediaPlayer.isPlaying();
        seekbar = findViewById(R.id.seekBar);
        seekbar.setClickable(false);

        bPlayPause.setImageDrawable(getDrawable(isPlaying
                ? R.drawable.ic_pause_black
                : R.drawable.ic_play_arrow_black));

        bPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                    pauseSong();
                } else {
                    playSong();
                }
                updateIsPlaying();
            }
        });

//        b3.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(getApplicationContext(), "Playing sound",Toast.LENGTH_SHORT).show();
//                mediaPlayer.start();
//
//                finalTime = mediaPlayer.getDuration();
//                startTime = mediaPlayer.getCurrentPosition();
//
//                if (oneTimeOnly == 0) {
//                    seekbar.setMax((int) finalTime);
//                    oneTimeOnly = 1;
//                }
//
//                tx2.setText(String.format("%d:%d",
//                        TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
//                        TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
//                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
//                                        finalTime)))
//                );
//
//                tx1.setText(String.format("%d:%d",
//                        TimeUnit.MILLISECONDS.toMinutes((long) startTime),
//                        TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
//                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
//                                        startTime)))
//                );
//
//                seekbar.setProgress((int)startTime);
//                myHandler.postDelayed(UpdateSongTime,100);
//                b2.setEnabled(true);
//                b3.setEnabled(false);
//            }
//        });
//
//        b2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(getApplicationContext(), "Pausing sound",Toast.LENGTH_SHORT).show();
//                mediaPlayer.pause();
//                b2.setEnabled(false);
//                b3.setEnabled(true);
//            }
//        });

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int temp = (int) startTime;

                if ((temp + forwardTime) <= finalTime) {
                    startTime = startTime + forwardTime;
                    mediaPlayer.seekTo((int) startTime);
                    Toast.makeText(getApplicationContext(), "You have Jumped forward 5 seconds", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Cannot jump forward 5 seconds", Toast.LENGTH_SHORT).show();
                }
            }
        });

        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int temp = (int) startTime;

                if ((temp - backwardTime) > 0) {
                    startTime = startTime - backwardTime;
                    mediaPlayer.seekTo((int) startTime);
                    Toast.makeText(getApplicationContext(), "You have Jumped backward 5  seconds", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Cannot jump backward 5 seconds", Toast.LENGTH_SHORT).show();
                }
            }
        });

        final TextView seekBarHint = findViewById(R.id.seekbarhint);

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int y = 0;

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

                seekBarHint.setVisibility(View.VISIBLE);
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                if (!mediaPlayer.isPlaying()) {
                    return;
                }
                seekBarHint.setVisibility(View.VISIBLE);
                startTime = mediaPlayer.getCurrentPosition();
                seekBarHint.setText(String.format("%d:%d",
                        TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                        TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                        toMinutes((long) startTime))));


                double percent = progress / (double) seekBar.getMax();
                int offset = seekBar.getThumbOffset();
                int seekWidth = seekBar.getWidth();
                int val = (int) Math.round(percent * (seekWidth - 2 * offset));
                int labelWidth = seekBarHint.getWidth();
                seekBarHint.setX(offset + seekBar.getX() + val
                        - Math.round(percent * offset)
                        - Math.round(percent * labelWidth / 2));

                if (progress > 0 && mediaPlayer != null && !mediaPlayer.isPlaying()) {


                    Music.this.seekbar.setProgress(0);
                }

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {


                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(seekBar.getProgress());
                }
            }
        });


    }

    private void updateIsPlaying() {
        isPlaying = mediaPlayer.isPlaying();
        bPlayPause.setImageDrawable(getDrawable(isPlaying
                ? R.drawable.ic_pause_black
                : R.drawable.ic_play_arrow_black));
    }

    private void playSong() {
        Toast.makeText(getApplicationContext(), "Playing sound", Toast.LENGTH_SHORT).show();
        mediaPlayer.start();

        finalTime = mediaPlayer.getDuration();
        startTime = mediaPlayer.getCurrentPosition();

        if (oneTimeOnly == 0) {
            seekbar.setMax((int) finalTime);
            oneTimeOnly = 1;
        }

        tx2.setText(String.format("%d:%d",
                TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                finalTime)))
        );

        tx1.setText(String.format("%d:%d",
                TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                startTime)))
        );

        seekbar.setProgress((int) startTime);
        myHandler.postDelayed(UpdateSongTime, 100);
    }

    private void pauseSong() {
        Toast.makeText(getApplicationContext(), "Pausing sound", Toast.LENGTH_SHORT).show();
        mediaPlayer.pause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 1) {
            pauseSong();
            updateIsPlaying();

            uri = data.getStringExtra("URI");
            name = data.getStringExtra("NAME");
            album = data.getStringExtra("ALBUM");
            id = data.getStringExtra("ID");
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(uri));
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Error occured", Toast.LENGTH_SHORT).show();
            }
            tx4.setText(name);
            tx5.setText(album);

            Bitmap albumArt = getAlbumArt(id);
            if (albumArt != null) {
                iv.setImageBitmap(albumArt.copy(albumArt.getConfig(), false));
                float s = (float) albumArt.getHeight() / vWidth;
                if (s > 1) {
                    s = 1 / s;
                }
                Log.d(TAG, "onActivityResult: " + s);

                Bitmap blur = Blur.transform(mContext, albumArt, 25,
                        albumArt.getHeight(), vWidth,
                        s);
                ivB.setImageBitmap(blur.copy(blur.getConfig(), false));
            } else {
                iv.setImageDrawable(getDrawable(R.drawable.ic_music));
                ivB.setImageDrawable(getDrawable(android.R.color.transparent));
            }
            playSong();
            updateIsPlaying();
        }
    }

    private Bitmap getAlbumArt(String id) {
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
                MediaStore.Audio.Albums._ID + "=?",
                new String[]{String.valueOf(id)},
                null);

        if (cursor.moveToFirst()) {
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));

            Bitmap bmp = BitmapFactory.decodeFile(path);
            if (bmp == null) {
                return null;
            }
            return Scale.scaleBitmap(bmp, vWidth / 2, vWidth / 2);
        }
        return null;
    }
}
