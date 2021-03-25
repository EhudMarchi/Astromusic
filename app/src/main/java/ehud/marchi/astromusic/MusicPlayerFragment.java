package ehud.marchi.astromusic;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;


public class MusicPlayerFragment extends Fragment implements SongAdapter.onSongSelectedListener {

    boolean firstClick = true;
    boolean isPlaying = false;
    ImageButton playBtn, stopBtn, prevBtn, nextBtn;
    private RecyclerView songsRecyclerView;
    private SongAdapter recyclerViewAdapter;
    private Song selectedSong = null;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;
    ImageView selectedSongImage ,galaxyImageview;
    TextView selectedSongName, selectedSongArtist, nowPlaying;
    RelativeLayout selectedSongLayout;
    Animation animRotate;
    public MusicPlayerFragment() {
        // Required empty public constructor
    }

    public static MusicPlayerFragment newInstance(String param1, String param2) {
        MusicPlayerFragment fragment = new MusicPlayerFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }

    }
    private void setUpSongsRecyclerView() {
        songsRecyclerView = getView().findViewById(R.id.songs_recyclerview);
        songsRecyclerView.setHasFixedSize(true);
        recyclerViewLayoutManager = new LinearLayoutManager(getContext());
        recyclerViewAdapter = new SongAdapter(getContext(),MusicPlayerService.songs, selectedSong, this);
        songsRecyclerView.setLayoutManager(recyclerViewLayoutManager);
        songsRecyclerView.setAdapter(recyclerViewAdapter);
        ItemTouchHelper.Callback callback = new SongMoveCallback(recyclerViewAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(songsRecyclerView);
        songsRecyclerView.setAdapter(recyclerViewAdapter);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_music_player, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpSongsRecyclerView();
        selectedSongLayout = getView().findViewById(R.id.selected_song);
        selectedSongImage = getView().findViewById(R.id.selected_song_image);
        selectedSongName = getView().findViewById(R.id.selected_song_name);
        selectedSongArtist = getView().findViewById(R.id.selected_song_artist);
        galaxyImageview = getView().findViewById(R.id.galaxy);
        nowPlaying = getView().findViewById(R.id.now_playing);
        animRotate = AnimationUtils.loadAnimation(getContext(),R.anim.rotate);
        getView().findViewById(R.id.planet_logo).startAnimation(animRotate);
        playBtn = getView().findViewById(R.id.play);
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedSongLayout.getVisibility() == View.GONE)
                {
                    onSongSelected(recyclerViewAdapter.m_SelectedItemIndex);
                }

                if(!isPlaying) {
                    playBtn.setBackgroundResource(R.drawable.pause);
                    Intent intent = new Intent(getContext(), MusicPlayerService.class);
                    intent.putExtra("song", recyclerViewAdapter.m_SelectedItemIndex);
                    if(firstClick) {
                        intent.putExtra("command", "new_song");
                    }
                    else {
                        intent.putExtra("command", "play");
                    }
                    getContext().startService(intent);
                    galaxyImageview.setVisibility(View.VISIBLE);
                    nowPlaying.setVisibility(View.VISIBLE);
                    Animation spinAnim = AnimationUtils.loadAnimation(getContext(),R.anim.infinite_rotate);
                    galaxyImageview.startAnimation(spinAnim);
                    nowPlaying.startAnimation(AnimationUtils.loadAnimation(getContext(),R.anim.fade_in));
                    isPlaying = true;
                    firstClick= false;
                    stopBtn.setEnabled(true);
                    stopBtn.setAlpha(1f);
                }
                else {
                    playBtn.setBackgroundResource(R.drawable.play);
                    Intent intent = new Intent(getContext(), MusicPlayerService.class);
                    intent.putExtra("command", "pause");
                    galaxyImageview.clearAnimation();
                    Animation fadeOutAnim = AnimationUtils.loadAnimation(getContext(),R.anim.fade_out);
                    galaxyImageview.startAnimation(fadeOutAnim);
                    nowPlaying.startAnimation(fadeOutAnim);
                    galaxyImageview.setVisibility(View.GONE);
                    nowPlaying.setVisibility(View.GONE);
                    getContext().startService(intent);
                    isPlaying = false;
                }
                playBtn.startAnimation(AnimationUtils.loadAnimation(getContext(),R.anim.click));
            }
        });
        stopBtn = getView().findViewById(R.id.stop);
        stopBtn.setEnabled(false);
        stopBtn.setAlpha(0.4f);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPlaying) {
                    playBtn.setBackgroundResource(R.drawable.play);
                    Intent intent = new Intent(getContext(), MusicPlayerService.class);
                    intent.putExtra("command", "stop");
                    intent.putExtra("song", recyclerViewAdapter.m_SelectedItemIndex);
                    getContext().startService(intent);
                    isPlaying = false;
                    stopBtn.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.click));
                    galaxyImageview.clearAnimation();
                    Animation fadeOutAnim = AnimationUtils.loadAnimation(getContext(),R.anim.fade_out);
                    galaxyImageview.startAnimation(fadeOutAnim);
                    nowPlaying.startAnimation(fadeOutAnim);
                    galaxyImageview.setVisibility(View.GONE);
                    nowPlaying.setVisibility(View.GONE);
                }
            }
        });
        prevBtn = getView().findViewById(R.id.previous);
        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(recyclerViewAdapter.m_SelectedItemIndex>0) {
                    recyclerViewAdapter.m_SelectedItemIndex--;
                    selectedSong = MusicPlayerService.songs.get(recyclerViewAdapter.m_SelectedItemIndex);
                    refreshSelectedSong(MusicPlayerService.songs.get(recyclerViewAdapter.m_SelectedItemIndex));
                    if(isPlaying) {
                        Intent intent = new Intent(getContext(), MusicPlayerService.class);
                        intent.putExtra("command", "prev");
                        intent.putExtra("song", recyclerViewAdapter.m_SelectedItemIndex);
                        getContext().startService(intent);
                    }
                    prevBtn.startAnimation(AnimationUtils.loadAnimation(getContext(),R.anim.click));
                }
            }
        });
        nextBtn = getView().findViewById(R.id.next);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(recyclerViewAdapter.m_SelectedItemIndex<MusicPlayerService.songs.size()) {
                    recyclerViewAdapter.m_SelectedItemIndex++;
                    selectedSong = MusicPlayerService.songs.get(recyclerViewAdapter.m_SelectedItemIndex);
                    refreshSelectedSong(MusicPlayerService.songs.get(recyclerViewAdapter.m_SelectedItemIndex));
                    if(isPlaying) {
                        Intent intent = new Intent(getContext(), MusicPlayerService.class);
                        intent.putExtra("command", "next");
                        intent.putExtra("song", recyclerViewAdapter.m_SelectedItemIndex);
                        getContext().startService(intent);
                    }
                    nextBtn.startAnimation(AnimationUtils.loadAnimation(getContext(),R.anim.click));
                }
            }
        });
        selectedSongLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog viewSongDialog = new Dialog(getContext());
                viewSongDialog.setContentView(R.layout.dialog_view_song);
                viewSongDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                Glide.with(viewSongDialog.getContext()).load(selectedSong.getImageURL()).into((ImageView) viewSongDialog.findViewById(R.id.song_image));
                TextView songName = (TextView) viewSongDialog.findViewById(R.id.song_name);
                songName.setText(selectedSong.getSongName());
                TextView songArtist = (TextView) viewSongDialog.findViewById(R.id.song_artist);
                songArtist.setText(selectedSong.getSongArtist());
                viewSongDialog.show();
            }
        });
    }

    @Override
    public void onSongSelected(int songIndex) {
        selectedSong = MusicPlayerService.songs.get(songIndex);
        refreshSelectedSong(selectedSong);
        Log.d("song",selectedSong.getSongName());
        Intent intent = new Intent(getContext(), MusicPlayerService.class);
        if(isPlaying)
        {
            intent.putExtra("command", "next");
            intent.putExtra("song", recyclerViewAdapter.m_SelectedItemIndex);
        }
        else
        {
            intent.putExtra("command", "stop");
        }
        getContext().startService(intent);
    }
    private void refreshSelectedSong(Song selectedSong)
    {
        if(selectedSongLayout.getVisibility() == View.GONE) {
            selectedSongLayout.setVisibility(View.VISIBLE);
            Animation animSlideIn = AnimationUtils.loadAnimation(getContext(), R.anim.slide_left_to_right);
            selectedSongLayout.startAnimation(animSlideIn);
        }
        else
        {
            Animation animFadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
            selectedSongLayout.startAnimation(animFadeIn);
        }
        selectedSongName.setText(selectedSong.getSongName());
        selectedSongArtist.setText(selectedSong.getSongArtist());
        Glide.with(getContext()).load(selectedSong.getImageURL()).placeholder(R.drawable.galaxy).into(selectedSongImage);
        if(isPlaying)
        {
            Intent intent = new Intent(getContext(), MusicPlayerService.class);
            intent.putExtra("song", recyclerViewAdapter.m_SelectedItemIndex);
            //intent.putExtra("command", "new_song");
            getContext().startService(intent);
        }
        if(recyclerViewAdapter.m_SelectedItemIndex == MusicPlayerService.songs.size()-1)
        {
            nextBtn.setEnabled(false);
            nextBtn.setAlpha(0.4f);
        }
        else
        {
            nextBtn.setEnabled(true);
            nextBtn.setAlpha(1f);
        }
        if(recyclerViewAdapter.m_SelectedItemIndex == 0)
        {
            prevBtn.setEnabled(false);
            prevBtn.setAlpha(0.4f);
        }
        else
        {
            prevBtn.setEnabled(true);
            prevBtn.setAlpha(1f);
        }
    }
}