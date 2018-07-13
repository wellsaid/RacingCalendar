package wellsaid.it.racingcalendar;

import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import butterknife.BindView;
import butterknife.ButterKnife;
import wellsaid.it.racingcalendardata.RacingCalendar;

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

    /* The FragmentPagerAdapter that will provide fragments for each of the sections */
    private TabsAdapter tabsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* inflate layout */
        setContentView(R.layout.activity_main);

        /* bind the views to corresponding variables */
        ButterKnife.bind(this);

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Inflate the menu (adds items to the action bar) */
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /* get the option selected */
        int id = item.getItemId();
        switch (id){
            case R.id.action_settings:
                /* TODO: Start the settings activity */
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
