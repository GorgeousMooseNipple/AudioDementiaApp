package lab.android.audiodementia.activities;

import es.claucookie.miniequalizerlibrary.EqualizerView;
import lab.android.audiodementia.R;
import lab.android.audiodementia.background.Background;
import lab.android.audiodementia.client.RestClient;
import lab.android.audiodementia.fragments.MainFragment;
import lab.android.audiodementia.fragments.MusicFragment;
import lab.android.audiodementia.fragments.PlayerFragment;
import lab.android.audiodementia.fragments.PlaylistsFragment;
import lab.android.audiodementia.fragments.SongListFragment;
import lab.android.audiodementia.service.MusicPlayerService;
import lab.android.audiodementia.user.UserSession;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.Manifest;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    protected Toolbar toolbar;
    protected NavigationView navigationView;
    protected DrawerLayout drawerLayout;
    protected ActionBarDrawerToggle drawerToggle;
    private UserSession session;
    private MusicPlayerService.PlayerServiceBinder binder;
    private MusicPlayerService playerService;
    private MediaControllerCompat mediaController;
    private MenuItem navigationPlayer;
    private EqualizerView equalizerView;
    private Background background;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_main);
        getPermissions();

        session = new UserSession(this);
        background = Background.getInstance();

        toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.bringToFront();
        drawerLayout = findViewById(R.id.drawer_layout);

        navigationPlayer = navigationView.getMenu().findItem(R.id.nav_menu_player);

        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.main_header_title,
                R.string.main_layout_genres
        ) {
            public void onDrawerClosed(View view) {
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu();
            }
        };

        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerToggle.syncState();
        drawerToggle.getDrawerArrowDrawable().setColor(getColor(R.color.colorText));

        equalizerView = findViewById(R.id.equalizer_view);

        bindService(new Intent(this, MusicPlayerService.class), new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                binder = (MusicPlayerService.PlayerServiceBinder) service;
                playerService = binder.getService();

                try {
                    mediaController = new MediaControllerCompat(
                            BaseActivity.this, binder.getMediaSessionToken());
                    mediaController.registerCallback(
                            new MediaControllerCompat.Callback() {
                                @Override
                                public void onPlaybackStateChanged(PlaybackStateCompat state) {
                                    if (state == null)
                                        return;
                                    if (state.getState() == PlaybackStateCompat.STATE_PLAYING) {
                                        if (!navigationPlayer.isEnabled())
                                            navigationPlayer.setEnabled(true);
                                        if (equalizerView.getVisibility() == View.GONE) {
                                            equalizerView.setVisibility(View.VISIBLE);
                                        }
                                        equalizerView.animateBars();
                                    }
                                    if (state.getState() == PlaybackStateCompat.STATE_STOPPED) {
                                        if (navigationPlayer.isEnabled())
                                            navigationPlayer.setEnabled(false);
                                        equalizerView.stopBars();
                                    }
                                    if (state.getState() == PlaybackStateCompat.STATE_PAUSED) {
                                        equalizerView.stopBars();
                                    }
                                    if (state.getState() == PlaybackStateCompat.STATE_CONNECTING) {
                                        if (equalizerView.getVisibility() == View.VISIBLE) {
                                            equalizerView.stopBars();
                                        }
                                    }
                                }
                            }
                    );
                }
                catch (RemoteException e) {
                    mediaController = null;
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                binder = null;
            }
        }, BIND_AUTO_CREATE);


        try {
            if (getIntent().getExtras().getString("SHOW_PLAYLISTS") != null)
                startFragment(new PlaylistsFragment());
            getSupportActionBar().setTitle("Playlists");
            return;
        }
        catch (NullPointerException ex) {
            ex.printStackTrace();
        }
        startFragment(new MainFragment());
        getSupportActionBar().setTitle("Main page");
    }

    public MusicPlayerService.PlayerServiceBinder getBinder() {
        return binder;
    }

    private void getPermissions() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_PHONE_STATE}, 1);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
    }

    private void startFragment(Fragment fragment) {
        if(fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.fragment_placeholder, fragment).addToBackStack(null).commit();
        }
    }

    public void startSongList(Bundle bundle, String title) {
        Fragment fragment = new SongListFragment();
        fragment.setArguments(bundle);
        setToolbarTitle(title);
        startFragment(fragment);
    }

    private void setToolbarTitle(String title)
    {
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(title);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Fragment fragment = null;
        switch (menuItem.getItemId())
        {
            case R.id.nav_menu_main:
                fragment = new MainFragment();
                setToolbarTitle(menuItem.getTitle().toString());
                break;
            case R.id.nav_menu_player:
                fragment = new PlayerFragment();
                setToolbarTitle(menuItem.getTitle().toString());
                break;
            case R.id.nav_menu_music:
                fragment = new MusicFragment();
                setToolbarTitle(menuItem.getTitle().toString());
                break;
            case R.id.nav_menu_playlists:
                fragment = new PlaylistsFragment();
                setToolbarTitle(menuItem.getTitle().toString());
                break;
            case R.id.nav_menu_exit:
                Map<String, String> params = new HashMap<>();
                params.put("refresh_token", session.getRefresh());
                background.execute(new Runnable() {
                    @Override
                    public void run() {
                        background.postEvent(RestClient.signOut(params));
                    }
                });
                session.logout();
                Intent intent = new Intent(BaseActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                break;
        }

        menuItem.setChecked(true);

        if (fragment != null)
            startFragment(fragment);

        if(drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }
}
