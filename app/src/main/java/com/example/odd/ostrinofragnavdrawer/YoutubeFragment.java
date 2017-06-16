package com.example.odd.ostrinofragnavdrawer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.odd.ostrinofragnavdrawer.Listeners.PlayerListener;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.OnInitializedListener;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import static android.R.drawable.ic_media_pause;
import static android.R.drawable.ic_media_play;


public class YoutubeFragment extends Fragment implements View.OnClickListener,
        OnInitializedListener,
        YouTubePlayer.PlayerStateChangeListener,
        YouTubePlayer.PlaybackEventListener{

    // API キー
    public static final String API_KEY = "AIzaSyDSMKvbGUJxKhPz5t4PMFEByD5qFy1sjEA";

    // YouTubeのビデオID
    private String currentlyPlaying;
    private boolean minimized = false, playing, playerStopped, shuffle = false;
    public YouTubePlayer mPlayer = null;
    private Stack<String> preQueue, played, queue;
    private List<Ost> ostQueue;
    private RelativeLayout layoutView;
    private List<String> videoIds;
    private FrameLayout playerLayout;
    public Button btnPrevious, btnPause, btnNext, btnMinimize;
    private int playbackPosMilliSec, currPlayingIndex;
    private PlayerListener[] playerListeners;

    public YoutubeFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.youtube_api, container, false);
        layoutView = (RelativeLayout) rootView;
        playerLayout = (FrameLayout) rootView.findViewById(R.id.youtube_layout);
        // YouTubeフラグメントインスタンスを取得

        btnPrevious = (Button) rootView.findViewById(R.id.btnPrevious);
        btnPause = (Button) rootView.findViewById(R.id.btnPause);
        btnNext = (Button) rootView.findViewById(R.id.btnNext);
        btnMinimize = (Button) rootView.findViewById(R.id.btnMinimize);

        btnPrevious.setOnClickListener(this);
        btnPause.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnMinimize.setOnClickListener(this);

        YouTubePlayerSupportFragment youTubePlayerFragment = YouTubePlayerSupportFragment.newInstance();

        // レイアウトにYouTubeフラグメントを追加
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.youtube_layout, youTubePlayerFragment).commit();

        // YouTubeフラグメントのプレーヤーを初期化する
        youTubePlayerFragment.initialize(API_KEY, this);

        return rootView;
    }

    // YouTubeプレーヤーの初期化成功
    @Override
    public void onInitializationSuccess(Provider provider, YouTubePlayer player, boolean wasRestored) {
        this.mPlayer = player;
        initPlayer();
    }

    // YouTubeプレーヤーの初期化失敗
    @Override
    public void onInitializationFailure(Provider provider, YouTubeInitializationResult error) {
        // YouTube error
        String errorMessage = error.toString();
        Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
        Log.d("errorMessage:", errorMessage);
    }

    public void initPlayer() {

        mPlayer.loadVideo(currentlyPlaying);
        mPlayer.play();
        playing = true;
        playerStopped = false;
        queue = new Stack<>();

        mPlayer.setShowFullscreenButton(false);
        mPlayer.setPlayerStateChangeListener(this);
        mPlayer.setPlaybackEventListener(this);
    }

    public void initiateQueue(List<Ost> ostList, int startIndex) {
        //btnNext.setVisibility(View.VISIBLE);
        this.ostQueue = ostList;
        videoIds = new ArrayList<>();
        for (Ost ost : ostList) {
            videoIds.add(Util.urlToId(ost.getUrl()));
        }
        played = new Stack<>();
        preQueue = new Stack<>();
        currentlyPlaying = videoIds.get(startIndex);
        for (int i = 0; i < ostList.size(); i++) {
            String videoId = videoIds.get(i);
            if (i < startIndex) {
                played.add(videoId);
            } else if (i > startIndex) {
                preQueue.add(0, videoId);
            }
        }

        if (shuffle) {
            shuffleOn();
        }
    }

    public void addToQueue(String url) {
        btnNext.setVisibility(View.VISIBLE);
        queue.add(0, Util.urlToId(url));
    }

    public void removeFromQueue(String url){
        System.out.println(queue.toString());
        if(queue.contains(url)){
            queue.remove(url);
        } else{
            preQueue.remove(url);
        }
    }

    public void previous() {
        if (!played.isEmpty()) {
            preQueue.push(currentlyPlaying);
            currentlyPlaying = played.pop();
            mPlayer.loadVideo(currentlyPlaying);
        }
        if (played.isEmpty()) {
            btnPrevious.setVisibility(View.INVISIBLE);
        }
        System.out.println("preQueue: " + preQueue.toString());
        System.out.println("played: " + played.toString());
        notifyPlayerListeners(true);
    }

    public void pausePlayer() {
        mPlayer.pause();
    }

    public void unPausePlayer() {
        mPlayer.play();
    }


    public void next() {
        if (!queue.isEmpty()) {
            played.push(currentlyPlaying);
            currentlyPlaying = queue.pop();
            mPlayer.loadVideo(currentlyPlaying);
        } else if (!preQueue.isEmpty()) {
            played.push(currentlyPlaying);
            currentlyPlaying = preQueue.pop();
            mPlayer.loadVideo(currentlyPlaying);
        }
        if (preQueue.isEmpty()) {
            btnNext.setVisibility(View.INVISIBLE);
        }
        btnPrevious.setVisibility(View.VISIBLE);
        notifyPlayerListeners(false);
    }

    public void setVideoId(String url) {
        currentlyPlaying = Util.urlToId(url);
    }

    @Override
    public void onLoading() {

    }

    @Override
    public void onLoaded(String s) {

    }

    @Override
    public void onAdStarted() {

    }

    @Override
    public void onVideoStarted() {
    }

    @Override
    public void onVideoEnded() {
        //Toast.makeText(getActivity(), "Video ended", Toast.LENGTH_SHORT).show();
        next();
    }

    @Override
    public void onError(YouTubePlayer.ErrorReason errorReason) {

    }

    @Override
    public void onPlaying() {
        playing = true;
        btnPause.setBackgroundResource(ic_media_pause);
        playerStopped = false;
    }

    @Override
    public void onPaused() {
        playing = false;
        btnPause.setBackgroundResource(ic_media_play);

    }

    @Override
    public void onStopped() {
        playing = false;
        playerStopped = true;
        playbackPosMilliSec = mPlayer.getCurrentTimeMillis();
        btnPause.setBackgroundResource(ic_media_play);
    }

    @Override
    public void onBuffering(boolean b) {

    }

    @Override
    public void onSeekTo(int i) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnPrevious: {
                previous();
                btnNext.setVisibility(View.VISIBLE);
                break;
            }

            case R.id.btnPause: {
                if (playerStopped) {
                    mPlayer.loadVideo(currentlyPlaying, playbackPosMilliSec);
                } else if (playing) {
                    mPlayer.pause();
                    btnPause.setBackgroundResource(ic_media_play);
                    playing = false;
                } else {
                    btnPause.setBackgroundResource(ic_media_pause);
                    mPlayer.play();
                    playing = true;
                }
                break;
            }

            case R.id.btnNext: {
                next();
                break;
            }

           case R.id.btnMinimize:{
               //mPlayer.setFullscreen(true);
               break;
            }
        }
    }

    public void shuffleOn() {
        long seed = System.nanoTime();
        List<Ost> ostQ = ostQueue.subList(currPlayingIndex, ostQueue.size());
        Collections.shuffle(preQueue, new Random(seed));
        Collections.shuffle(ostQ, new Random(seed));
        notifyShuffle(ostQ);
        shuffle = true;
    }

    public void shuffleOff() {
        currPlayingIndex = videoIds.indexOf(currentlyPlaying);
        played = new Stack<>();
        preQueue = new Stack<>();
        for (int i = 0; i < videoIds.size(); i++) {
            String videoId = videoIds.get(i);
            if (i < currPlayingIndex) {
                played.add(videoId);
            } else if (i > currPlayingIndex) {
                preQueue.add(0, videoId);
            }
        }
        shuffle = false;

    }

    public void setPlayerListeners(PlayerListener[] playerListeners) {
        this.playerListeners = playerListeners;
    }

    public void notifyPlayerListeners(boolean previous) {
        for (int i = 0; i < playerListeners.length; i++) {
            playerListeners[i].updateCurrentlyPlaying(videoIds.indexOf(currentlyPlaying));
            if(previous){
                playerListeners[i].previous();
            }
            else {
                playerListeners[i].next();
            }

        }
    }

    public void notifyShuffle(List<Ost> ostList){
        for (int i = 0; i <playerListeners.length ; i++) {
            playerListeners[i].shuffle(ostList);
        }
    }
}