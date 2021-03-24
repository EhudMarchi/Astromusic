package ehud.marchi.astromusic;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RemoteViews;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;

import org.jetbrains.annotations.Nullable;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


public class MusicPlayerService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, SongAdapter.onSongSelectedListener{

    private MediaPlayer mediaPlayer = new MediaPlayer();
    public static ArrayList<Song> songs = new ArrayList<>();
    int currentPlaying = 0;
    NotificationCompat.Builder builder;
    NotificationManager manager;
    final int NOTIF_ID = 1;
    RemoteViews remoteViews;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.reset();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String command=intent.getStringExtra("command");
        currentPlaying = intent.getIntExtra("song",0);
        loadData();
        if(command!=null) {
            switch (command) {
                case "new_song":
                    newSong();
                    break;

                case "play":
                    play();
                    break;

                case "next":
                    next();
                    break;

                case "prev":
                    previous();
                    break;

                case "pause":
                    pause();
                    break;

                case "stop":
                    stop();
                    break;
                case "close":
                    Log.d("command","close");
                    stopSelf();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void newSong() {
        Log.d("command","new_song");
        if (!songs.isEmpty() && !mediaPlayer.isPlaying()) {
            try {
                mediaPlayer.setDataSource(this.songs.get(currentPlaying).getSongLink());
                mediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void play() {
        Log.d("command","play");
        if (!songs.isEmpty() && !mediaPlayer.isPlaying()) {
                mediaPlayer.start();
        }
    }

    private void next() {
        Log.d("command","next");
        mediaPlayer.reset();
        if(!songs.isEmpty()) {
            try {
                mediaPlayer.setDataSource(this.songs.get(currentPlaying).getSongLink());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mediaPlayer.prepareAsync();
    }

    private void previous() {
        Log.d("command","prev");
        mediaPlayer.reset();
        if(!songs.isEmpty()) {
            try {
                mediaPlayer.setDataSource(this.songs.get(currentPlaying).getSongLink());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mediaPlayer.prepareAsync();
    }

    private void pause() {
        Log.d("command","pause");
        if (mediaPlayer.isPlaying())
            mediaPlayer.pause();
    }

    private void stop() {
        Log.d("command","stop");
        if (mediaPlayer.isPlaying())
        {mediaPlayer.stop();
            mediaPlayer.reset();
            if(!songs.isEmpty()) {
                try {
                    mediaPlayer.setDataSource(this.songs.get(currentPlaying).getSongLink());
                    //mediaPlayer.prepareAsync();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mediaPlayer !=null) {
            if(mediaPlayer.isPlaying())
                mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if(currentPlaying<songs.size())
        {
            next();
        }
        //stopSelf();
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        this.mediaPlayer.start();
        ShowNotification();
        remoteViews.setTextViewText(R.id.song_name, songs.get(currentPlaying).getSongName());
        remoteViews.setTextViewText(R.id.song_artist, songs.get(currentPlaying).getSongArtist());
        Glide.with(this).asBitmap().load(songs.get(currentPlaying).getImageURL()).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @androidx.annotation.Nullable com.bumptech.glide.request.transition.Transition<? super Bitmap> transition) {
                remoteViews.setImageViewBitmap(R.id.song_image, resource);
                builder.setCustomBigContentView(remoteViews);
                Notification notification = builder.build();
                manager.notify(NOTIF_ID, notification);
            }

            @Override
            public void onLoadCleared(@androidx.annotation.Nullable Drawable placeholder) {
            }
        });
        Notification notification = builder.build();
        manager.notify(NOTIF_ID, notification);
    }
    private void loadData() {
        try {
            FileInputStream fis = openFileInput("songList.dat");
            ObjectInputStream ois = new ObjectInputStream(fis);
            this.songs = (ArrayList<Song>) ois.readObject();
            ois.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    public void ShowNotification() {

        manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        String channelId = null;
        if(Build.VERSION.SDK_INT >= 26) {

            channelId =  "astromusoc_channel_id" ;
            CharSequence channelName =  "Astromusic Channel" ;
            NotificationChannel notificationChannel =  new  NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW);
            manager.createNotificationChannel(notificationChannel);
        }

        builder = new NotificationCompat.Builder(getApplicationContext(),channelId);
        builder.setSmallIcon(R.drawable.planet);

        remoteViews = new RemoteViews(getPackageName(),R.layout.notification_layout);

        Intent playIntent = new Intent(this, MusicPlayerService.class);
        playIntent.putExtra("command", "play");
        PendingIntent playPendingIntent = PendingIntent.getService(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.play, playPendingIntent);

        Intent stopIntent = new Intent(this,MusicPlayerService.class);
        stopIntent.putExtra("command","stop");
        PendingIntent stopPendingIntent = PendingIntent.getService(this,1,stopIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.stop,stopPendingIntent);

        Intent nextIntent=new Intent(this, MusicPlayerService.class);
        nextIntent.putExtra("command", "next");
        onSongSelected(currentPlaying++);
        nextIntent.putExtra("song", currentPlaying);
        PendingIntent nextPendingIntent=PendingIntent.getService(this, 2, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.next, nextPendingIntent);

        Intent prevIntent=new Intent(this, MusicPlayerService.class);
        prevIntent.putExtra("command", "prev");
        onSongSelected(currentPlaying--);
        prevIntent.putExtra("song", currentPlaying);
        PendingIntent prevPendingIntent=PendingIntent.getService(this, 3, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.previous, prevPendingIntent);

        Intent closeIntent=new Intent(this, MusicPlayerService.class);
        closeIntent.putExtra("command", "close");
        PendingIntent closePendingIntent=PendingIntent.getService(this, 4, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.close, closePendingIntent);

        builder.setCustomBigContentView(remoteViews);
        startForeground(NOTIF_ID,builder.build());
    }

    @Override
    public void onSongSelected(int songIndex) {
        currentPlaying = songIndex;
    }
}
