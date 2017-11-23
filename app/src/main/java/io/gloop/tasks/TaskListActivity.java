package io.gloop.tasks;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.gloop.Gloop;
import io.gloop.permissions.GloopUser;
import io.gloop.tasks.dialogs.DayNightSettingsDialog;
import io.gloop.tasks.dialogs.UserProfileDialog;
import io.gloop.tasks.model.UserInfo;
import io.gloop.tasks.utils.SharedPreferencesStore;

import static io.gloop.tasks.ListFragment.VIEW_CLOSED_TASKS;
import static io.gloop.tasks.ListFragment.VIEW_OPEN_TASKS;


public class TaskListActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout mDrawerLayout;

    private CircleImageView userImage;
    private TextView username;
    private TextView navHeaderUsername;
    private ViewPager viewPager;
    private CircleImageView navHeaderUserImage;
    private FloatingActionButton floatingActionButton;


    private GloopUser owner;
    private UserInfo userInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        TasksApplication application = (TasksApplication) getApplication();
        Tracker mTracker = application.getDefaultTracker();
        mTracker.setScreenName("Image~" + "TaskListActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setHomeAsUpIndicator(R.drawable.ic_menu);
            ab.setDisplayHomeAsUpEnabled(true);
        }

        // Load the currently logged in GloopUser of the app.
        this.owner = Gloop.getOwner();
        // Load user info
        userInfo = Gloop.allLocal(UserInfo.class)
                .where()
                .equalsTo("email", owner.getName())
                .first();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        if (viewPager != null) {
            setupViewPager(viewPager);
        }

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        username = (TextView) findViewById(R.id.username);
        View navigationHeader = navigationView.getHeaderView(0);
        navHeaderUsername = (TextView) navigationHeader.findViewById(R.id.nav_header_username);
        navHeaderUserImage = (CircleImageView) navigationHeader.findViewById(R.id.nav_header_user_image);


        userImage = (CircleImageView) findViewById(R.id.user_image);
        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new UserProfileDialog(TaskListActivity.this, userImage, userInfo);
            }
        });

        floatingActionButton = (FloatingActionButton) findViewById(R.id.fab_menu_item_new);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = view.getContext();
                Intent intent = new Intent(context, TaskDetailActivity.class);
                intent.putExtra(TaskDetailFragment.ARG_USER_INFO, userInfo);
                context.startActivity(intent);
            }
        });

        AppCompatDelegate.setDefaultNightMode(SharedPreferencesStore.getNightMode());

    }

    public void setFABVisibility(int enable) {
        if (enable == View.INVISIBLE)
            floatingActionButton.hide(true);
        else
            floatingActionButton.show(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sample_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_my_tasks:
                viewPager.setCurrentItem(0);
                break;
            case R.id.nav_groups:
                viewPager.setCurrentItem(1);
                break;
            case R.id.nav_user:
                new UserProfileDialog(TaskListActivity.this, userImage, userInfo);
                break;
            case R.id.nav_night_mode:
                new DayNightSettingsDialog(TaskListActivity.this);
                break;
            case R.id.nav_logout:
                logout();
                break;
        }

        item.setChecked(true);
        mDrawerLayout.closeDrawers();
        return true;
    }

    private void logout() {
        Gloop.logout();
        SharedPreferencesStore.clearUser();
        finish();
    }

    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getSupportFragmentManager());
        final ListFragment frag1 = ListFragment.newInstance(VIEW_OPEN_TASKS, userInfo, owner);
        final ListFragment frag2 = ListFragment.newInstance(VIEW_CLOSED_TASKS, userInfo, owner);
        adapter.addFragment(frag1, "Open");
        adapter.addFragment(frag2, "Closed");
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                frag1.setupRecyclerView();
                frag2.setupRecyclerView();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        Adapter(FragmentManager fm) {
            super(fm);
        }

        void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        new Thread(new Runnable() {
            @Override
            public void run() {
                setUserInfo();
            }
        }).start();
    }

    private void setUserInfo() {

        if (userInfo != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Uri imageURL = userInfo.getImageURL();
                    if (imageURL != null) {
                        Picasso.with(getApplicationContext())
                                .load(imageURL)
                                .into(userImage);

                        Picasso.with(getApplicationContext())
                                .load(imageURL)
                                .into(navHeaderUserImage);
                    }

                    username.setText(userInfo.getUserName());
                    navHeaderUsername.setText(userInfo.getUserName());
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}