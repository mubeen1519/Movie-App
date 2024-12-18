package com.example.videostreamingapp;

import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.FragmentManager;
import androidx.mediarouter.app.MediaRouteButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adapter.HomeTVAdapter;
import com.example.adapter.InfoAdapter;
import com.example.cast.Casty;
import com.example.cast.MediaData;
import com.example.fragment.ChromecastScreenFragment;
import com.example.fragment.EmbeddedImageFragment;
import com.example.fragment.PlayRippleFragment;
import com.example.fragment.PremiumContentFragment;
import com.example.fragment.TVExoPlayerFragment;
import com.example.item.ItemTV;
import com.example.util.API;
import com.example.util.BannerAds;
import com.example.util.Constant;
import com.example.util.Events;
import com.example.util.GlobalBus;
import com.example.util.IsRTL;
import com.example.util.LinearLayoutPagerManager;
import com.example.util.NetworkUtils;
import com.example.util.PlayerUtil;
import com.example.util.RvOnClickListener;
import com.example.util.ShareUtils;
import com.example.util.StatusBarUtil;
import com.example.util.WatchListClickListener;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class TVDetailsActivity extends BaseActivity {

    ProgressBar mProgressBar;
    LinearLayout lyt_not_found;
    RelativeLayout lytParent;
    WebView webView;
    TextView textTitle;
    RecyclerView rvRelated, rvInfo;
    ItemTV itemTV;
    ArrayList<ItemTV> mListItemRelated;
    ArrayList<String> mListInfo;
    HomeTVAdapter homeSportAdapter;
    String Id;
    LinearLayout lytRelated, lytView;
    MyApplication myApplication;
    NestedScrollView nestedScrollView;
    Toolbar toolbar;
    private FragmentManager fragmentManager;
    private int playerHeight;
    FrameLayout frameLayout;
    boolean isFullScreen = false;
    boolean isFromNotification = false;
    LinearLayout mAdViewLayout;
    boolean isPurchased = false, isWatchList = false;
    private Casty casty;
    int selectedIndex;
    ImageView imgFacebook, imgTwitter, imgWhatsApp;
    MaterialButton btnWatchList, btnServer1, btnServer2, btnServer3;
    InfoAdapter infoAdapter;
    MediaRouteButton mediaRouteButton;
    ArrayList<ItemTV> videoList;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        StatusBarUtil.setStatusBarBlack(this);
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_tv_details);
        IsRTL.ifSupported(this);
        //    GlobalBus.getBus().register(this);
        mAdViewLayout = findViewById(R.id.adView);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            if (isAndroidTV()) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().setDisplayShowHomeEnabled(false);

            } else {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
        }
        if (!isAndroidTV()) {
            casty = Casty.create(this)
                    .withMiniController();
            mediaRouteButton = findViewById(R.id.media_route_button);
            casty.setUpMediaRouteButton(mediaRouteButton);
        }
        myApplication = MyApplication.getInstance();
        fragmentManager = getSupportFragmentManager();
        Intent intent = getIntent();
        Id = intent.getStringExtra("Id");
        selectedIndex = getIntent().getIntExtra("Index", 0);
        Bundle data = this.getIntent().getBundleExtra("bundle");
        if(data != null) {
            videoList = data.getParcelableArrayList("VideoList");
        }

        if (intent.hasExtra("isNotification")) {
            isFromNotification = true;
        }

        frameLayout = findViewById(R.id.playerSection);
        int columnWidth = NetworkUtils.getScreenWidth(this);
        frameLayout.setLayoutParams(new RelativeLayout.LayoutParams(columnWidth, columnWidth / 2));
        playerHeight = frameLayout.getLayoutParams().height;

        BannerAds.showBannerAds(this, mAdViewLayout);

        mListItemRelated = new ArrayList<>();
        mListInfo = new ArrayList<>();
        itemTV = new ItemTV();
        lytRelated = findViewById(R.id.lytRelated);
        mProgressBar = findViewById(R.id.progressBar1);
        lyt_not_found = findViewById(R.id.lyt_not_found);
        lytParent = findViewById(R.id.lytParent);
        nestedScrollView = findViewById(R.id.nestedScrollView);
        webView = findViewById(R.id.webView);
        textTitle = findViewById(R.id.textTitle);
        rvRelated = findViewById(R.id.rv_related);
        rvInfo = findViewById(R.id.rv_info);
        lytView = findViewById(R.id.lytView);
        imgFacebook = findViewById(R.id.imgFacebook);
        imgTwitter = findViewById(R.id.imgTwitter);
        imgWhatsApp = findViewById(R.id.imgWhatsApp);
        btnWatchList = findViewById(R.id.btnWatchList);
        btnServer1 = findViewById(R.id.btnServer1);
        btnServer2 = findViewById(R.id.btnServer2);
        btnServer3 = findViewById(R.id.btnServer3);

        rvRelated.setHasFixedSize(true);
        rvRelated.setLayoutManager(new LinearLayoutPagerManager(TVDetailsActivity.this, LinearLayoutManager.HORIZONTAL, false, 2.15));
        rvRelated.setFocusable(false);
        rvRelated.setNestedScrollingEnabled(false);

        rvInfo.setHasFixedSize(true);
        rvInfo.setLayoutManager(new LinearLayoutManager(TVDetailsActivity.this, LinearLayoutManager.HORIZONTAL, false));
        rvInfo.setFocusable(false);
        rvInfo.setNestedScrollingEnabled(false);

        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.getSettings().setJavaScriptEnabled(true);
        if (NetworkUtils.isConnected(TVDetailsActivity.this)) {
            getDetails();
        } else {
            showToast(getString(R.string.conne_msg1));
        }

    }

    private void getDetails() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("tv_id", Id);
        jsObj.addProperty("user_id", myApplication.getIsLogin() ? myApplication.getUserId() : "");
        params.put("data", API.toBase64(jsObj.toString()));
        client.post(Constant.TV_DETAILS_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                mProgressBar.setVisibility(View.VISIBLE);
                lytParent.setVisibility(View.GONE);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                mProgressBar.setVisibility(View.GONE);
                lytParent.setVisibility(View.VISIBLE);

                String result = new String(responseBody);
                try {
                    JSONObject mainJson = new JSONObject(result);
                    isPurchased = mainJson.getBoolean(Constant.USER_PLAN_STATUS);
                    JSONObject objJson = mainJson.getJSONObject(Constant.ARRAY_NAME);
                    if (objJson.length() > 0) {
                        if (objJson.has(Constant.STATUS)) {
                            lyt_not_found.setVisibility(View.VISIBLE);
                        } else {
                            isWatchList = objJson.getBoolean(Constant.USER_WATCHLIST_STATUS);
                            itemTV.setTvId(objJson.getString(Constant.TV_ID));
                            itemTV.setTvName(objJson.getString(Constant.TV_TITLE));
                            itemTV.setTvDescription(objJson.getString(Constant.TV_DESC));
                            itemTV.setTvImage(objJson.getString(Constant.TV_IMAGE));
                            itemTV.setTvCategory(objJson.getString(Constant.TV_CATEGORY));
                            itemTV.setTvURL(objJson.getString(Constant.TV_URL));
                            itemTV.setTvURL2(objJson.getString(Constant.TV_URL_2));
                            itemTV.setTvURL3(objJson.getString(Constant.TV_URL_3));
                            itemTV.setTvType(objJson.getString(Constant.TV_TYPE));
                            itemTV.setPremium(objJson.getString(Constant.TV_ACCESS).equals("Paid"));
                            itemTV.setTvShareLink(objJson.getString(Constant.MOVIE_SHARE_LINK));
                            itemTV.setTvView(objJson.getString(Constant.MOVIE_VIEW));

                            JSONArray jsonArrayChild = objJson.getJSONArray(Constant.RELATED_TV_ARRAY_NAME);
                            if (jsonArrayChild.length() != 0) {
                                for (int j = 0; j < jsonArrayChild.length(); j++) {
                                    JSONObject objChild = jsonArrayChild.getJSONObject(j);
                                    ItemTV item = new ItemTV();
                                    item.setTvId(objChild.getString(Constant.TV_ID));
                                    item.setTvName(objChild.getString(Constant.TV_TITLE));
                                    item.setTvImage(objChild.getString(Constant.TV_IMAGE));
                                    item.setPremium(objChild.getString(Constant.TV_ACCESS).equals("Paid"));
                                    mListItemRelated.add(item);
                                }
                            }
                        }
                        displayData();

                    } else {
                        mProgressBar.setVisibility(View.GONE);
                        lytParent.setVisibility(View.GONE);
                        lyt_not_found.setVisibility(View.VISIBLE);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                mProgressBar.setVisibility(View.GONE);
                lytParent.setVisibility(View.GONE);
                lyt_not_found.setVisibility(View.VISIBLE);
            }
        });
    }

    private void displayData() {
        setTitle("");
        textTitle.setSelected(true);
        textTitle.setText(itemTV.getTvName());
        mListInfo.add(itemTV.getTvCategory());
        if (!itemTV.getTvView().isEmpty()) {
            mListInfo.add(getString(R.string.view, itemTV.getTvView()));
        }
        infoAdapter = new InfoAdapter(mListInfo);
        rvInfo.setAdapter(infoAdapter);

        String mimeType = "text/html";
        String encoding = "utf-8";
        String htmlText = itemTV.getTvDescription();

        boolean isRTL = Boolean.parseBoolean(getResources().getString(R.string.isRTL));
        String direction = isRTL ? "rtl" : "ltr";

        String text = "<html dir=" + direction + "><head>"
                + "<style type=\"text/css\">@font-face {font-family: MyFont;src: url(\"file:///android_asset/fonts/custom.otf\")}body{font-family: MyFont;color: #9c9c9c;font-size:14px;margin-left:0px;line-height:1.3}"
                + "</style></head>"
                + "<body>"
                + htmlText
                + "</body></html>";

        webView.loadDataWithBaseURL(null, text, mimeType, encoding, null);

        initRipplePlay();//initPlayer();
        initWatchList();

        if (!mListItemRelated.isEmpty()) {
            homeSportAdapter = new HomeTVAdapter(TVDetailsActivity.this, mListItemRelated);
            rvRelated.setAdapter(homeSportAdapter);

            homeSportAdapter.setOnItemClickListener(new RvOnClickListener() {
                @Override
                public void onItemClick(int position) {
                    String sportId = mListItemRelated.get(position).getTvId();
                    Intent intent = new Intent(TVDetailsActivity.this, TVDetailsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("Id", sportId);
                    startActivity(intent);
                }
            });

        } else {
            lytRelated.setVisibility(View.GONE);
        }

        if (!isAndroidTV()) {
            casty.setOnConnectChangeListener(new Casty.OnConnectChangeListener() {
                @Override
                public void onConnected() {
                    initCastPlayer();
                }

                @Override
                public void onDisconnected() {
                    initPlayer(itemTV.getTvURL());
                }
            });
        }

        imgFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShareUtils.shareFacebook(TVDetailsActivity.this, itemTV.getTvName(), itemTV.getTvShareLink());
            }
        });

        imgTwitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShareUtils.shareTwitter(TVDetailsActivity.this, itemTV.getTvName(), itemTV.getTvShareLink(), "", "");
            }
        });

        imgWhatsApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShareUtils.shareWhatsapp(TVDetailsActivity.this, itemTV.getTvName(), itemTV.getTvShareLink());
            }
        });

        btnServer1.setOnClickListener(view -> initPlayer(itemTV.getTvURL()));
        btnServer2.setOnClickListener(view -> initPlayer(itemTV.getTvURL2()));
        btnServer3.setOnClickListener(view -> initPlayer(itemTV.getTvURL3()));
        otherServerUrls();
    }

    private void initWatchList() {
        btnWatchList.setIconResource(isWatchList ? R.drawable.ic_watch_list_remove : R.drawable.ic_watch_list_add);
        //    btnWatchList.setText(isWatchList ? getString(R.string.remove_from_watch_list) : getString(R.string.add_to_watch_list));
        btnWatchList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (myApplication.getIsLogin()) {
                    if (NetworkUtils.isConnected(TVDetailsActivity.this)) {
                        WatchListClickListener watchListClickListener = new WatchListClickListener() {
                            @Override
                            public void onItemClick(boolean isAddWatchList, String message) {
                                isWatchList = isAddWatchList;
                                btnWatchList.setIconResource(isAddWatchList ? R.drawable.ic_watch_list_remove : R.drawable.ic_watch_list_add);
//                                btnWatchList.setText(isAddWatchList ? getString(R.string.remove_from_watch_list) : getString(R.string.add_to_watch_list));
                            }
                        };
                        new WatchList(TVDetailsActivity.this).applyWatch(isWatchList, Id, "LiveTV", watchListClickListener);
                    } else {
                        showToast(getString(R.string.conne_msg1));
                    }
                } else {
                    showToast(getString(R.string.login_first_watch_list));
                    Intent intentLogin = new Intent(TVDetailsActivity.this, SignInActivity.class);
                    intentLogin.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intentLogin.putExtra("isOtherScreen", true);
                    intentLogin.putExtra("postId", Id);
                    intentLogin.putExtra("postType", "LiveTV");
                    startActivity(intentLogin);
                }
            }
        });
    }

    private void initRipplePlay() {
        if(isAndroidTV()){
            initPlayer(itemTV.getTvURL());
        }else {
            PlayRippleFragment playRippleFragment = PlayRippleFragment.newInstance(itemTV.getTvImage());
            playRippleFragment.setOnSkipClickListener(new RvOnClickListener() {
                @Override
                public void onItemClick(int position) {
                    toolbar.setVisibility(View.GONE);
                    initPlayer(itemTV.getTvURL());
                }
            });
            fragmentManager.beginTransaction().replace(R.id.playerSection, playRippleFragment).commitAllowingStateLoss();
        }
    }

    private void initPlayer(String streamUrl) {
        if (itemTV.isPremium()) {
            if (isPurchased) {
                setPlayer(streamUrl);
            } else {
                PremiumContentFragment premiumContentFragment = PremiumContentFragment.newInstance(Id, "LiveTV");
                fragmentManager.beginTransaction().replace(R.id.playerSection, premiumContentFragment).commitAllowingStateLoss();
            }
        } else {
            setPlayer(streamUrl);
        }
    }

    private void initCastPlayer() {
        if (itemTV.isPremium()) {
            if (isPurchased) {
                castScreen();
            } else {
                PremiumContentFragment premiumContentFragment = PremiumContentFragment.newInstance(Id, "Movies");
                fragmentManager.beginTransaction().replace(R.id.playerSection, premiumContentFragment).commitAllowingStateLoss();
            }
        } else {
            castScreen();
        }
    }

    private void setPlayer(String streamUrl) {
        if (streamUrl.isEmpty()) {
            EmbeddedImageFragment embeddedImageFragment = EmbeddedImageFragment.newInstance(streamUrl, itemTV.getTvImage(), false);
            fragmentManager.beginTransaction().replace(R.id.playerSection, embeddedImageFragment).commitAllowingStateLoss();
        } else {
            if (!itemTV.getTvType().equals(Constant.VIDEO_TYPE_EMBED)) {
                if (PlayerUtil.isYoutubeUrl(streamUrl)) {
                    itemTV.setTvType(Constant.VIDEO_TYPE_YOUTUBE);
                } else if (PlayerUtil.isVimeoUrl(streamUrl)) {
                    itemTV.setTvType(Constant.VIDEO_TYPE_VIMEO);
                } else {
                    itemTV.setTvType(Constant.VIDEO_TYPE_HLS);
                }
            }
            switch (itemTV.getTvType()) { //URL Embed
                case Constant.VIDEO_TYPE_HLS:
                    if (isAndroidTV()) {
                        TVExoPlayerFragment fragment = TVExoPlayerFragment.newInstance(selectedIndex, videoList,streamUrl);
                        fragmentManager.beginTransaction().replace(R.id.playerSection, fragment).commitAllowingStateLoss();
                    } else {
                        if (casty.isConnected()) {
                            castScreen();
                        } else {
                            TVExoPlayerFragment exoPlayerFragment = TVExoPlayerFragment.newInstance(streamUrl);
                            fragmentManager.beginTransaction().replace(R.id.playerSection, exoPlayerFragment).commitAllowingStateLoss();
                        }
                    }
                    break;
                case Constant.VIDEO_TYPE_EMBED:
                    Intent intent = new Intent(TVDetailsActivity.this, EmbeddedPlayerActivity.class);
                    intent.putExtra("streamUrl", streamUrl);
                    startActivity(intent);
                    break;
                case Constant.VIDEO_TYPE_YOUTUBE:
                    Intent intentYt = new Intent(TVDetailsActivity.this, YtPlayerActivity.class);
                    intentYt.putExtra("videoId", PlayerUtil.getVideoIdFromYoutubeUrl(streamUrl));
                    startActivity(intentYt);
                    break;
                case Constant.VIDEO_TYPE_VIMEO:
                    Intent intentVm = new Intent(TVDetailsActivity.this, VimeoPlayerActivity.class);
                    intentVm.putExtra("videoId", PlayerUtil.getVideoIdFromVimeoUrl(streamUrl));
                    startActivity(intentVm);
                    break;
            }
        }
    }

    public void showToast(String msg) {
        Toast.makeText(TVDetailsActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //    GlobalBus.getBus().unregister(this);
    }

    @Subscribe
    public void getFullScreen(Events.FullScreen fullScreen) {
        isFullScreen = fullScreen.isFullScreen();
        if (fullScreen.isFullScreen()) {
            gotoFullScreen();
        } else {
            gotoPortraitScreen();
        }
    }

    private boolean isAndroidTV() {
        UiModeManager uiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);
        return uiModeManager != null && uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION;
    }

    private void gotoPortraitScreen() {
        nestedScrollView.setVisibility(View.VISIBLE);
//        toolbar.setVisibility(View.VISIBLE);
        mAdViewLayout.setVisibility(View.VISIBLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        frameLayout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, playerHeight));
    }

    private void gotoFullScreen() {
        nestedScrollView.setVisibility(View.GONE);
//        toolbar.setVisibility(View.GONE);
        mAdViewLayout.setVisibility(View.GONE);
        frameLayout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @Override
    public void onBackPressed() {
        if(isAndroidTV()){
            super.onBackPressed();
        }
        if (isFullScreen) {
            Events.FullScreen fullScreen = new Events.FullScreen();
            fullScreen.setFullScreen(false);
            GlobalBus.getBus().post(fullScreen);
        } else {
            if (isFromNotification) {
                Intent intent = new Intent(TVDetailsActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//            casty.addMediaRouteMenuItem(menu);

        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    private void playViaCast() {
        if (itemTV.getTvType().equals(Constant.VIDEO_TYPE_HLS)) {
            casty.getPlayer().loadMediaAndPlay(createSampleMediaData(itemTV.getTvURL(), itemTV.getTvName(), itemTV.getTvImage()));
        } else {
            showToast(getResources().getString(R.string.cast_youtube));
        }
    }

    private MediaData createSampleMediaData(String videoUrl, String videoTitle, String videoImage) {
        return new MediaData.Builder(videoUrl)
                .setStreamType(MediaData.STREAM_TYPE_BUFFERED)
                .setContentType(getType(videoUrl))
                .setMediaType(MediaData.MEDIA_TYPE_MOVIE)
                .setTitle(videoTitle)
                .setSubtitle(getString(R.string.app_name))
                .addPhotoUrl(videoImage)
                .build();
    }

    private String getType(String videoUrl) {
        if (videoUrl.endsWith(".mp4")) {
            return "videos/mp4";
        } else if (videoUrl.endsWith(".m3u8")) {
            return "application/x-mpegurl";
        } else {
            return "application/x-mpegurl";
        }
    }

    private void castScreen() {
        ChromecastScreenFragment chromecastScreenFragment = new ChromecastScreenFragment();
        fragmentManager.beginTransaction().replace(R.id.playerSection, chromecastScreenFragment).commitAllowingStateLoss();
        chromecastScreenFragment.setOnItemClickListener(new RvOnClickListener() {
            @Override
            public void onItemClick(int position) {
                playViaCast();
            }
        });
    }

    private void otherServerUrls() {
        if (itemTV.getTvType().equals(Constant.VIDEO_TYPE_HLS)) {
            if (!itemTV.getTvURL2().isEmpty()) {
                btnServer2.setVisibility(View.VISIBLE);
                btnServer1.setVisibility(View.VISIBLE);
            }

            if (!itemTV.getTvURL3().isEmpty()) {
                btnServer3.setVisibility(View.VISIBLE);
                btnServer1.setVisibility(View.VISIBLE);
            }
        }
    }
    public interface OnNextVideoListener {
        void onNextVideo();
    }
}

