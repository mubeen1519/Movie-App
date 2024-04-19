package com.example.videostreamingapp;

import android.annotation.SuppressLint;
import android.app.UiModeManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.fragment.HomeFragment;
import com.example.fragment.MyAccountFragment;
import com.example.fragment.SettingFragment;
import com.example.fragment.ShowsTabFragment;
import com.example.fragment.SportCategoryFragment;
import com.example.fragment.TVCategoryFragment;
import com.example.fragment.WatchListFragment;
import com.example.item.ItemBottomBar;
import com.example.util.BannerAds;
import com.example.util.Constant;
import com.example.util.GDPRChecker;
import com.example.util.IsRTL;
import com.example.util.NotificationTiramisu;
import com.example.util.StatusBarUtil;
import com.google.android.material.navigation.NavigationView;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class MainActivity extends BaseActivity {

    private DrawerLayout drawerLayout;
    ConstraintLayout relativeLayout;
    NavigationView navigationView;
    SearchView searchBar;
    Toolbar toolbar;
    private FragmentManager fragmentManager;
    boolean doubleBackToExitPressedOnce = false;
    MyApplication myApplication;
    int versionCode;
    ArrayList<ItemBottomBar> bottomBarList;
    LinearLayout llHome, llWatchlist, llAccount, llSettings;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StatusBarUtil.setStatusBarGradiant(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IsRTL.ifSupported(this);

        navigationView = findViewById(R.id.navigation_view);

        relativeLayout = findViewById(R.id.relative_layout);


        fragmentManager = getSupportFragmentManager();
        myApplication = MyApplication.getInstance();

        if (!isAndroidTV()) {
            toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            drawerLayout = findViewById(R.id.drawer_layout);
            llHome = findViewById(R.id.llHome);
            llWatchlist = findViewById(R.id.llWatchlist);
            llAccount = findViewById(R.id.llAccount);
            llSettings = findViewById(R.id.llSetting);
            bottomBarList = ItemBottomBar.listOfBottomBarItem(drawerLayout);
            navigationView = findViewById(R.id.navigation_view);

        }


        try {
            versionCode = getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (Constant.adNetworkType != null && Constant.adNetworkType.equals(Constant.admobAd)) {
            new GDPRChecker()
                    .withContext(MainActivity.this)
                    .check();
        }

        LinearLayout mAdViewLayout = findViewById(R.id.adView);
        BannerAds.showBannerAds(this, mAdViewLayout);

        HomeFragment homeFragment = new HomeFragment();
        loadFrag(homeFragment, getString(R.string.menu_home), fragmentManager);


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if (isAndroidTV()) {
                    switch (menuItem.getItemId()) {
                        case R.id.menu_go_home:
                            HomeFragment homeFragment = new HomeFragment();
                            loadFrag(homeFragment, getString(R.string.menu_home), fragmentManager);
                            return true;
                        case R.id.menu_go_movie:
                            ShowsTabFragment movieTabFragment = new ShowsTabFragment();
                            Bundle bundleMovie = new Bundle();
                            bundleMovie.putBoolean("isShow", false);
                            movieTabFragment.setArguments(bundleMovie);
                            loadFrag(movieTabFragment, getString(R.string.menu_movie), fragmentManager);
                            return true;
                        case R.id.menu_go_tv_show:
                            ShowsTabFragment showsTabFragment = new ShowsTabFragment();
                            Bundle bundleShow = new Bundle();
                            bundleShow.putBoolean("isShow", true);
                            showsTabFragment.setArguments(bundleShow);
                            loadFrag(showsTabFragment, getString(R.string.menu_tv_show), fragmentManager);
                            return true;
                        case R.id.menu_go_sport:
                            SportCategoryFragment sportCategoryFragment = new SportCategoryFragment();
                            loadFrag(sportCategoryFragment, getString(R.string.menu_sport), fragmentManager);
                            return true;
                        case R.id.menu_go_tv:
                            TVCategoryFragment tvCategoryFragment = new TVCategoryFragment();
                            loadFrag(tvCategoryFragment, getString(R.string.menu_tv), fragmentManager);
                            return true;
                        case R.id.menu_go_watch_list:
                            WatchListFragment watchListFragment = new WatchListFragment();
                            loadFrag(watchListFragment, getString(R.string.my_watch_list), fragmentManager);
                            return true;
                        case R.id.menu_go_profile:
                            Intent intentProfile = new Intent(MainActivity.this, EditProfileActivity.class);
                            intentProfile.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intentProfile);
                            return true;
                        case R.id.menu_go_setting:
                            SettingFragment settingFragment = new SettingFragment();
                            loadFrag(settingFragment, getString(R.string.menu_setting), fragmentManager);
                            return true;
                        case R.id.menu_go_logout:
                            new LogoutOnline(MainActivity.this);
                            return true;
                        case R.id.menu_go_login:
                            Intent intentSignIn = new Intent(MainActivity.this, SignInActivity.class);
                            intentSignIn.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intentSignIn);
                            finish();
                            return true;
                        case R.id.menu_go_dashboard:
                            Intent intentDashBoard = new Intent(MainActivity.this, DashboardActivity.class);
                            intentDashBoard.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intentDashBoard);
                            return true;
                        default:
                            return true;
                    }
                } else {
                    drawerLayout.closeDrawers();
                    switch (menuItem.getItemId()) {
                        case R.id.menu_go_home:
                            selectBottomBar(0);
                            HomeFragment homeFragment = new HomeFragment();
                            loadFrag(homeFragment, getString(R.string.menu_home), fragmentManager);
                            return true;
                        case R.id.menu_go_movie:
                            resetBottomBar();
                            ShowsTabFragment movieTabFragment = new ShowsTabFragment();
                            Bundle bundleMovie = new Bundle();
                            bundleMovie.putBoolean("isShow", false);
                            movieTabFragment.setArguments(bundleMovie);
                            loadFrag(movieTabFragment, getString(R.string.menu_movie), fragmentManager);
                            return true;
                        case R.id.menu_go_tv_show:
                            resetBottomBar();
                            ShowsTabFragment showsTabFragment = new ShowsTabFragment();
                            Bundle bundleShow = new Bundle();
                            bundleShow.putBoolean("isShow", true);
                            showsTabFragment.setArguments(bundleShow);
                            loadFrag(showsTabFragment, getString(R.string.menu_tv_show), fragmentManager);
                            return true;
                        case R.id.menu_go_sport:
                            resetBottomBar();
                            SportCategoryFragment sportCategoryFragment = new SportCategoryFragment();
                            loadFrag(sportCategoryFragment, getString(R.string.menu_sport), fragmentManager);
                            return true;
                        case R.id.menu_go_tv:
                            resetBottomBar();
                            TVCategoryFragment tvCategoryFragment = new TVCategoryFragment();
                            loadFrag(tvCategoryFragment, getString(R.string.menu_tv), fragmentManager);
                            return true;
                        case R.id.menu_go_watch_list:
                            selectBottomBar(1);
                            WatchListFragment watchListFragment = new WatchListFragment();
                            loadFrag(watchListFragment, getString(R.string.my_watch_list), fragmentManager);
                            return true;
                        case R.id.menu_go_profile:
                            Intent intentProfile = new Intent(MainActivity.this, EditProfileActivity.class);
                            intentProfile.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intentProfile);
                            return true;
                        case R.id.menu_go_setting:
                            selectBottomBar(4);
                            SettingFragment settingFragment = new SettingFragment();
                            loadFrag(settingFragment, getString(R.string.menu_setting), fragmentManager);
                            return true;
                        case R.id.menu_go_logout:
                            new LogoutOnline(MainActivity.this);
                            return true;
                        case R.id.menu_go_login:
                            Intent intentSignIn = new Intent(MainActivity.this, SignInActivity.class);
                            intentSignIn.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intentSignIn);
                            finish();
                            return true;
                        case R.id.menu_go_dashboard:
                            Intent intentDashBoard = new Intent(MainActivity.this, DashboardActivity.class);
                            intentDashBoard.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intentDashBoard);
                            return true;
                        default:
                            return true;
                    }
                }
            }
        });

        if (!isAndroidTV()) {
            ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
                @Override
                public void onDrawerClosed(View drawerView) {
                    super.onDrawerClosed(drawerView);
                }

                @Override
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                }
            };

            drawerLayout.addDrawerListener(actionBarDrawerToggle);
            actionBarDrawerToggle.syncState();
            toolbar.setNavigationIcon(R.drawable.ic_side_nav);
        }

        if (versionCode != Constant.appUpdateVersion && Constant.isAppUpdate) {
            newUpdateDialog();
        }

        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    getPackageName(), //Insert your own package name.
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
        setBottomNavBar();
        selectBottomBar(0);
        hideShowMenu();
        NotificationTiramisu.takePermission(this);
    }

    public void loadFrag(Fragment f1, String name, FragmentManager fm) {
        for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();
        }
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.Container, f1, name);
        ft.commit();
        setToolbarTitle(name);
    }

    public void setToolbarTitle(String Title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(Title);
        }
    }

    public void setHeader() {
        if (isAndroidTV()) {
            if (myApplication.getIsLogin()) {
                navigationView.getMenu().findItem(R.id.menu_go_login).setVisible(false);
                navigationView.getMenu().findItem(R.id.menu_go_profile).setVisible(true);
                navigationView.getMenu().findItem(R.id.menu_go_logout).setVisible(true);
                navigationView.getMenu().findItem(R.id.menu_go_dashboard).setVisible(true);
                navigationView.getMenu().findItem(R.id.menu_go_watch_list).setVisible(true);
            } else {
                navigationView.getMenu().findItem(R.id.menu_go_login).setVisible(true);
                navigationView.getMenu().findItem(R.id.menu_go_profile).setVisible(false);
                navigationView.getMenu().findItem(R.id.menu_go_logout).setVisible(false);
                navigationView.getMenu().findItem(R.id.menu_go_dashboard).setVisible(false);
                navigationView.getMenu().findItem(R.id.menu_go_watch_list).setVisible(false);

            }
        } else {
            if (myApplication.getIsLogin() && navigationView != null) {
                View header = navigationView.getHeaderView(0);
                TextView txtHeaderName = header.findViewById(R.id.nav_name);
                TextView txtHeaderEmail = header.findViewById(R.id.nav_email);
                txtHeaderName.setText(myApplication.getUserName());
                txtHeaderEmail.setText(myApplication.getUserEmail());
                txtHeaderName.setSelected(true);
                txtHeaderEmail.setSelected(true);
                header.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intentDashBoard = new Intent(MainActivity.this, DashboardActivity.class);
                        intentDashBoard.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intentDashBoard);
                    }
                });
            }
            if (myApplication.getIsLogin()) {
                navigationView.getMenu().findItem(R.id.menu_go_login).setVisible(false);
                navigationView.getMenu().findItem(R.id.menu_go_profile).setVisible(true);
                navigationView.getMenu().findItem(R.id.menu_go_logout).setVisible(true);
                navigationView.getMenu().findItem(R.id.menu_go_dashboard).setVisible(true);
                navigationView.getMenu().findItem(R.id.menu_go_watch_list).setVisible(true);
            } else {
                navigationView.getMenu().findItem(R.id.menu_go_login).setVisible(true);
                navigationView.getMenu().findItem(R.id.menu_go_profile).setVisible(false);
                navigationView.getMenu().findItem(R.id.menu_go_logout).setVisible(false);
                navigationView.getMenu().findItem(R.id.menu_go_dashboard).setVisible(false);
                navigationView.getMenu().findItem(R.id.menu_go_watch_list).setVisible(false);
                View header = navigationView.getHeaderView(0);
                TextView txtHeaderName = header.findViewById(R.id.nav_name);
                TextView txtHeaderEmail = header.findViewById(R.id.nav_email);
                txtHeaderName.setSelected(true);
                txtHeaderEmail.setSelected(true);
                header.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intentSignIn = new Intent(MainActivity.this, SignInActivity.class);
                        intentSignIn.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intentSignIn);
                        finish();
                    }
                });
            }
        }
    }

    private void hideShowMenu() {
        navigationView.getMenu().findItem(R.id.menu_go_tv_show).setVisible(Constant.isShowMenu);
        navigationView.getMenu().findItem(R.id.menu_go_movie).setVisible(Constant.isMovieMenu);
        navigationView.getMenu().findItem(R.id.menu_go_sport).setVisible(Constant.isSportMenu);
        navigationView.getMenu().findItem(R.id.menu_go_tv).setVisible(Constant.isTvMenu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setHeader();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

//        if (isAndroidTV()) {
//            inflater.inflate(R.menu.tv_search,menu);
//            final MenuItem tvSearch = menu.findItem(R.id.action_search);
//            searchBar = (SearchView) tvSearch.getActionView();
//
//        } else {
            inflater.inflate(R.menu.menu_search, menu);
            final MenuItem searchMenuItem = menu.findItem(R.id.search);
            final SearchView searchView = (SearchView) searchMenuItem.getActionView();

            searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {

                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    // TODO Auto-generated method stub
                    if (!hasFocus) {
                        searchMenuItem.collapseActionView();
                        searchView.setQuery("", false);
                    }
                }
            });

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

                @Override
                public boolean onQueryTextSubmit(String arg0) {
                    // TODO Auto-generated method stub
                    Intent intent = new Intent(MainActivity.this, SearchHorizontalActivity.class);
                    intent.putExtra("search", arg0);
                    startActivity(intent);
                    searchView.clearFocus();
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String arg0) {
                    // TODO Auto-generated method stub
                    return false;
                }
            });

//        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (fragmentManager.getBackStackEntryCount() != 0) {
            String tag = fragmentManager.getFragments().get(fragmentManager.getBackStackEntryCount() - 1).getTag();
            setToolbarTitle(tag);
            super.onBackPressed();
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, getString(R.string.back_key), Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment fragment = fragmentManager.findFragmentByTag(getString(R.string.menu_profile));
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void newUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(getString(R.string.app_update_title));
        builder.setCancelable(false);
        builder.setMessage(Constant.appUpdateDesc);
        builder.setPositiveButton(getString(R.string.app_update_btn), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(Constant.appUpdateUrl)));
            }
        });
        if (Constant.isAppUpdateCancel) {
            builder.setNegativeButton(getString(R.string.app_cancel_btn), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                }
            });
        }
        builder.setIcon(R.mipmap.ic_launcher);
        builder.show();
    }

    private void setBottomNavBar() {
        if (isAndroidTV()) {
            return;
        } else {
            llHome.setOnClickListener(view -> {
                selectBottomBar(0);
                unCheckND();
                selectNd(R.id.menu_go_home);
                HomeFragment homeFragment = new HomeFragment();
                loadFrag(homeFragment, getString(R.string.menu_home), fragmentManager);
            });

            llWatchlist.setOnClickListener(view -> {
                selectBottomBar(1);
                unCheckND();
                selectNd(R.id.menu_go_watch_list);
                WatchListFragment watchListFragment = new WatchListFragment();
                loadFrag(watchListFragment, getString(R.string.my_watch_list), fragmentManager);
            });

            llAccount.setOnClickListener(view -> {
                selectBottomBar(2);
                unCheckND();
                MyAccountFragment myAccountFragment = new MyAccountFragment();
                loadFrag(myAccountFragment, getString(R.string.account), fragmentManager);
            });

            llSettings.setOnClickListener(view -> {
                selectBottomBar(3);
                unCheckND();
                selectNd(R.id.menu_go_setting);
                SettingFragment settingFragment = new SettingFragment();
                loadFrag(settingFragment, getString(R.string.menu_setting), fragmentManager);
            });
        }
    }

    private boolean isAndroidTV() {
        UiModeManager uiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);
        return uiModeManager != null && uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION;
    }

    private void unCheckND() {
        int size = navigationView.getMenu().size();
        for (int i = 0; i < size; i++) {
            navigationView.getMenu().getItem(i).setChecked(false);
        }
    }

    private void selectNd(int idName) {
        navigationView.getMenu().findItem(idName).setChecked(true);
    }

    public void resetBottomBar() {
        selectBottomBar(-1);
    }

    private void selectBottomBar(int posClick) {
        if (isAndroidTV()) {
            return;
        }
        for (int i = 0; i < bottomBarList.size(); i++) {
            ItemBottomBar bottomBar = bottomBarList.get(i);
            if (posClick == i) {
                bottomBar.getTextView().setTextColor(getResources().getColor(R.color.bottom_hover_item));
                bottomBar.getImageView().setColorFilter(getResources().getColor(R.color.bottom_hover_item), PorterDuff.Mode.SRC_IN);
            } else {
                bottomBar.getTextView().setTextColor(getResources().getColor(R.color.bottom_text));
                bottomBar.getImageView().setColorFilter(getResources().getColor(R.color.bottom_text), PorterDuff.Mode.SRC_IN);
            }
        }
    }
}
