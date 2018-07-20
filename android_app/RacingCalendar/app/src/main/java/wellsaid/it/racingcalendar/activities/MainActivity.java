package wellsaid.it.racingcalendar.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import butterknife.BindView;
import butterknife.ButterKnife;
import wellsaid.it.racingcalendar.R;
import wellsaid.it.racingcalendar.settings.SettingsActivity;
import wellsaid.it.racingcalendardata.RacingCalendar;
import wellsaid.it.racingcalendardata.RacingCalendarNotifier;

public class MainActivity extends AppCompatActivity {

    /* A FragmentPagerAdapter which will contain all the tabs */
    public class TabsAdapter extends FragmentPagerAdapter {

        /* The desired tabs position */
        static final int HOME_TAB_POS = 0;
        static final int FAVORITES_TAB_POS = 1;
        static final int ALL_TAB_POS = 2;

        /* The number of tabs we want */
        private static final int numTabs = 3;

        /* they will contain the instances of the tab fragments needed */
        AllSeriesTab allSeriesTab = null;
        FavoritesTab favoritesTab = null;
        HomeTab homeTab = null;

        TabsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case HOME_TAB_POS:
                    homeTab = new HomeTab();
                    return homeTab;
                case FAVORITES_TAB_POS:
                    favoritesTab = new FavoritesTab();
                    favoritesTab.setListener(new FavoritesTab.FavoritesChangeListener() {
                        @Override
                        public void onFavoritesChanged(RacingCalendar.Series series) {
                            /* when a series changed favorite status in favorite series tab ...
                             * inform the all series and home tabs
                             */
                            if(allSeriesTab != null){
                                allSeriesTab.notifyChangeFavoriteStatus(series);
                                homeTab.notifyChangeFavoriteStatus(series);
                            }
                        }
                    });
                    return favoritesTab;
                case ALL_TAB_POS:
                    allSeriesTab = new AllSeriesTab();
                    allSeriesTab.setListener(new AllSeriesTab.FavoritesChangeListener() {
                        @Override
                        public void onFavoritesChanged(RacingCalendar.Series series) {
                            /* when a series changed favorite status in the all series tab ...
                             * inform the favorite series and home tabs
                             */
                            if(favoritesTab != null){
                                favoritesTab.notifyChangeFavoriteStatus(series);
                                homeTab.notifyChangeFavoriteStatus(series);
                            }
                        }
                    });
                    return allSeriesTab;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return numTabs;
        }
    }

    /* The ViewPager that will host the section contents. */
    @BindView(R.id.container)
    ViewPager viewPager;

    /* The toolbar of the activity */
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    /* The tablayout of the activity */
    @BindView(R.id.tab_layout)
    TabLayout tabLayout;

    /* The ad view of the activity */
    @BindView(R.id.ad_view)
    AdView adView;

    /* The FragmentPagerAdapter that will provide fragments for each of the sections */
    private TabsAdapter tabsAdapter;

    private static final int RC_SIGN_IN = 1;

    private GoogleApiClient googleApiClient;

    private FirebaseAuth auth;

    private FirebaseAuth.AuthStateListener authListener;

    private FirebaseUser user;

    private MenuItem actionAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* inflate layout */
        setContentView(R.layout.activity_main);

        /* bind the views to corresponding variables */
        ButterKnife.bind(this);

        /* load the configuration for the app */
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        Resources resources = getResources();
        int minBefore = Integer.valueOf(sharedPref.getString(getString(R.string.min_before_key),
                String.valueOf(resources.getInteger(R.integer.min_before_default))));
        int subMode = Integer.valueOf(sharedPref.getString(getString(R.string.sub_mode_key),
                String.valueOf(resources.getInteger(R.integer.sub_mode_default))));
        RacingCalendarNotifier.getInstance().setConfiguration(minBefore, subMode);

        /* initialize the action bar */
        setSupportActionBar(toolbar);

        /* add tabs to the tab layout */
        tabLayout.addTab(tabLayout.newTab().setText(R.string.home_tab_name));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.favorite_tab_name));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.all_series_tab_name));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        /* Create the SectionsPagerAdapter */
        tabsAdapter = new TabsAdapter(getSupportFragmentManager());

        /* Set up the ViewPager with the just created SectionsPagerAdapter */
        viewPager.setAdapter(tabsAdapter);

        /* Add on page change listener to sync view pager and tab layout */
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        /* Set on tab selected listener to change tab on a tap on the tab layout */
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab){}

            @Override
            public void onTabReselected(TabLayout.Tab tab){}
        });

        /* initialize the AdMob app */
        MobileAds.initialize(this, getString(R.string.admob_app_id));

        /* load the add */
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        adView.loadAd(adRequest);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this,
                        new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(MainActivity.this, "Error starting Google Log In", Toast.LENGTH_LONG).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        auth = FirebaseAuth.getInstance();
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() != null){ }
            }
        };

        // Get current user
        user = auth.getCurrentUser();
    }

    @Override
    protected void onStart() {
        super.onStart();

        auth.addAuthStateListener(authListener);
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                e.printStackTrace();;
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            user = auth.getCurrentUser();
                            actionAccount.setTitle(R.string.action_logout);
                        }
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Inflate the menu (adds items to the action bar) */
        getMenuInflater().inflate(R.menu.menu_main, menu);
        actionAccount = menu.getItem(1);
        actionAccount.setTitle((user == null)?R.string.action_login:R.string.action_logout);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /* get the option selected */
        int id = item.getItemId();
        switch (id){
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_account:
                if(user == null) {
                    signIn();
                } else {
                    auth.signOut();
                    user = null;
                    item.setTitle(R.string.action_login);
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
