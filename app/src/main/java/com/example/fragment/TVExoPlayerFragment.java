package com.example.fragment;

import static android.content.Context.UI_MODE_SERVICE;

import android.app.UiModeManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.item.ItemTV;
import com.example.util.Events;
import com.example.util.GlobalBus;
import com.example.videostreamingapp.R;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.SubtitleView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;

import org.greenrobot.eventbus.Subscribe;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

public class TVExoPlayerFragment extends Fragment {
    private static final String TAG = "StreamPlayerActivity";
    private SimpleExoPlayer player;


    private DefaultBandwidthMeter BANDWIDTH_METER;
    private DataSource.Factory mediaDataSourceFactory;
    private ProgressBar progressBar;
    ImageView imgFull, imgSetting;
    public boolean isFullScr = false;
    Button btnTryAgain;
    SubtitleView subtitleView;
    String channelUrl;

    PlayerView playerView;

    int selectedIndex;

    ArrayList<ItemTV> videoList;
    private static final String streamUrl = "streamUrl";

    public static TVExoPlayerFragment newInstance(String SId) {
        TVExoPlayerFragment f = new TVExoPlayerFragment();
        Bundle args = new Bundle();
        args.putString(streamUrl, SId);
        f.setArguments(args);
        return f;
    }
    public static TVExoPlayerFragment newInstance(int selectedIndex, ArrayList<ItemTV> videoList, String SId) {
        TVExoPlayerFragment fragment = new TVExoPlayerFragment();
        Bundle args = new Bundle();
        args.putInt("SelectedIndex", selectedIndex);
        args.putParcelableArrayList("VideoList", videoList);
        args.putString(streamUrl, SId);

        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_exo_player, container, false);
        GlobalBus.getBus().register(this);
        if(getArguments() != null) {
             selectedIndex = getArguments().getInt("SelectedIndex", 0);
            videoList = getArguments().getParcelableArrayList("VideoList");
        }
        if (getArguments() != null) {
            channelUrl = getArguments().getString(streamUrl);
        }
        progressBar = rootView.findViewById(R.id.progressBar);
        if(!isAndroidTV()) {
            imgFull = rootView.findViewById(R.id.img_full_scr);
        }
        imgSetting = rootView.findViewById(R.id.img_setting);
        imgSetting.setVisibility(View.GONE);
        btnTryAgain = rootView.findViewById(R.id.btn_try_again);
        BANDWIDTH_METER = new DefaultBandwidthMeter.Builder(requireActivity()).build();

        mediaDataSourceFactory = buildDataSourceFactory(true);
        player = new SimpleExoPlayer.Builder(requireActivity()).build();
        if(isAndroidTV()){
            playerView = rootView.findViewById(R.id.playerView);
        }else {
             playerView = rootView.findViewById(R.id.exoPlayerView);
        }
        subtitleView = rootView.findViewById(R.id.exo_subtitles);
        playerView.setPlayer(player);
        playerView.setUseController(true);
        playerView.requestFocus();
        player.setRepeatMode(Player.REPEAT_MODE_ALL);

        View decorView = Objects.requireNonNull(getActivity()).getWindow().getDecorView();
        decorView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_UP:
                            // Do something when the up button is pressed
                            break;
                        case KeyEvent.KEYCODE_DPAD_DOWN:
                            // Do something when the down button is pressed
                            break;
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            // Do something when the left button is pressed
                            break;
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                            // Do something when the right button is pressed
                            int nextIndex = (selectedIndex + 1) % videoList.size();
                            // Update fragment with the next video
                            ItemTV nextVideo = videoList.get(nextIndex);
                            channelUrl = nextVideo.getTvURL(); // Assuming tvURL is the URL of the video
                            TVExoPlayerFragment nextFragment = TVExoPlayerFragment.newInstance(nextIndex, videoList, channelUrl);
                            getParentFragmentManager().beginTransaction()
                                    .replace(R.id.playerSection, nextFragment)
                                    .commit();
                            break;
                        default:
                            return false;
                    }
                }
                return true;
            }
        });


        Uri uri = Uri.parse(channelUrl);

        MediaSource mediaSource = buildMediaSource(uri);
        player.prepare(mediaSource);
        player.setPlayWhenReady(true);

        if(isAndroidTV()){
            Events.FullScreen fullScreen = new Events.FullScreen();
            fullScreen.setFullScreen(true);
            GlobalBus.getBus().post(fullScreen);
        }
        player.addListener(new Player.EventListener() {
            @Override
            public void onTimelineChanged(@NotNull Timeline timeline, int reason) {
                Log.d(TAG, "onTimelineChanged: ");
            }

            @Override
            public void onTracksChanged(@NotNull TrackGroupArray trackGroups, @NotNull TrackSelectionArray trackSelections) {
                Log.d(TAG, "onTracksChanged: " + trackGroups.length);
            }

            @Override
            public void onLoadingChanged(boolean isLoading) {
                Log.d(TAG, "onLoadingChanged: " + isLoading);
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                Log.d(TAG, "onPlayerStateChanged: " + playWhenReady);
                if (playbackState == PlaybackStateCompat.STATE_PLAYING) {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {

            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

            }

            @Override
            public void onPlayerError(@NotNull ExoPlaybackException error) {
                Log.e(TAG, "onPlayerError: ", error);
                player.stop();
                btnTryAgain.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                //    errorDialog();
            }

            @Override
            public void onPositionDiscontinuity(int reason) {
                Log.d(TAG, "onPositionDiscontinuity: true");
            }

            @Override
            public void onPlaybackParametersChanged(@NotNull PlaybackParameters playbackParameters) {

            }

            @Override
            public void onSeekProcessed() {

            }
        });

        if(!isAndroidTV()) {
            imgFull.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isFullScr) {
                        isFullScr = false;
                        Events.FullScreen fullScreen = new Events.FullScreen();
                        fullScreen.setFullScreen(false);
                        GlobalBus.getBus().post(fullScreen);
                    } else {
                        isFullScr = true;
                        Events.FullScreen fullScreen = new Events.FullScreen();
                        fullScreen.setFullScreen(true);
                        GlobalBus.getBus().post(fullScreen);
                    }
                }
            });
        }
        btnTryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnTryAgain.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                retryLoad();
            }
        });

        return rootView;
    }

    public void retryLoad() {
        Uri uri = Uri.parse(channelUrl);
        MediaSource mediaSource = buildMediaSource(uri);
        player.prepare(mediaSource);
        player.setPlayWhenReady(true);
    }

    private MediaSource buildMediaSource(Uri uri) {
        int type = Util.inferContentType(uri);
        switch (type) {
            case C.TYPE_SS:
                return new SsMediaSource.Factory(new DefaultSsChunkSource.Factory(mediaDataSourceFactory), buildDataSourceFactory(false)).createMediaSource(uri);
            case C.TYPE_DASH:
                return new DashMediaSource.Factory(new DefaultDashChunkSource.Factory(mediaDataSourceFactory), buildDataSourceFactory(false)).createMediaSource(uri);
            case C.TYPE_HLS:
                return new HlsMediaSource.Factory(mediaDataSourceFactory).createMediaSource(uri);
            case C.TYPE_OTHER:
                return new ProgressiveMediaSource.Factory(mediaDataSourceFactory).createMediaSource(uri);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    public DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultDataSourceFactory(requireActivity(), bandwidthMeter,
                buildHttpDataSourceFactory(bandwidthMeter));
    }

    public HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultHttpDataSourceFactory(Util.getUserAgent(requireActivity(), "ExoPlayerDemo"), bandwidthMeter);
    }



    private void loadNextVideo() {
        // Implement logic to load the next video
    }

    private void loadPreviousVideo() {
        // Implement logic to load the previous video
    }

    @Subscribe
    public void getFullScreen(Events.FullScreen fullScreen) {
        isFullScr = fullScreen.isFullScreen();
        if (fullScreen.isFullScreen()) {
            imgFull.setImageResource(R.drawable.ic_fullscreen_exit);
        } else {
            imgFull.setImageResource(R.drawable.ic_fullscreen);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (player != null && player.getPlayWhenReady()) {
            player.setPlayWhenReady(false);
            player.getPlaybackState();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (player != null && player.getPlayWhenReady()) {
            player.setPlayWhenReady(false);
            player.getPlaybackState();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (player != null) {
            player.setPlayWhenReady(true);
            player.getPlaybackState();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        GlobalBus.getBus().unregister(this);
        if (player != null) {
            player.setPlayWhenReady(false);
            player.stop();
            player.release();
        }
    }

    private boolean isAndroidTV() {
        UiModeManager uiModeManager = (UiModeManager) Objects.requireNonNull(getContext()).getSystemService(UI_MODE_SERVICE);
        return uiModeManager != null && uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION;
    }
}
