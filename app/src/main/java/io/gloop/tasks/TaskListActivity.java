package io.gloop.tasks;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.gloop.Gloop;
import io.gloop.permissions.GloopUser;
import io.gloop.tasks.dialogs.DayNightSettingsDialog;
import io.gloop.tasks.dialogs.NewTaskDialog;
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
    private FloatingActionMenu floatingActionMenu;


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
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);

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


        LinearLayout header = (LinearLayout) findViewById(R.id.header);
        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new UserProfileDialog(TaskListActivity.this, userImage, userInfo);
            }
        });
        userImage = (CircleImageView) findViewById(R.id.user_image);
        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new UserProfileDialog(TaskListActivity.this, userImage, userInfo);
            }
        });

        floatingActionMenu = (FloatingActionMenu) findViewById(R.id.fab_menu);

        final FloatingActionButton fabSearch = (FloatingActionButton) findViewById(R.id.fab_menu_item_search);
        fabSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                new SearchDialog(TaskListActivity.this, floatingActionMenu, owner, userInfo);
                floatingActionMenu.close(false);
            }
        });

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_menu_item_new);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new NewTaskDialog(TaskListActivity.this, owner, view, TaskListActivity.this.getSupportFragmentManager(), floatingActionMenu, userInfo);
                floatingActionMenu.close(false);
            }
        });

//        final FloatingActionButton fabNewGroup = (FloatingActionButton) findViewById(R.id.fab_menu_item_new_group);
//        fabNewGroup.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                new NewGroupDialog(TaskListActivity.this, owner, view, TaskListActivity.this.getSupportFragmentManager(), floatingActionMenu, userInfo);
//                floatingActionMenu.close(false);
//            }
//        });

//        FloatingActionButton fabScan = (FloatingActionButton) findViewById(R.id.fab_menu_item_scan);
//        fabScan.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                launchScanner(QRCodeScannerActivity.class);
//                floatingActionMenu.close(false);
//            }
//        });

        AppCompatDelegate.setDefaultNightMode(SharedPreferencesStore.getNightMode());

    }

    public void setFABVisibility(int enable) {
        if (enable == View.INVISIBLE)
            floatingActionMenu.hideMenuButton(true);
        else
            floatingActionMenu.showMenuButton(true);
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
        adapter.addFragment(ListFragment.newInstance(VIEW_OPEN_TASKS, userInfo, owner), "Open");
        adapter.addFragment(ListFragment.newInstance(VIEW_CLOSED_TASKS, userInfo, owner), "Closed");
        viewPager.setAdapter(adapter);
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

    private static final int ZBAR_CAMERA_PERMISSION = 1;
    private Class<?> mClss;


    public void launchScanner(Class<?> clss) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            mClss = clss;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, ZBAR_CAMERA_PERMISSION);
        } else {
            Intent intent = new Intent(this, clss);
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case ZBAR_CAMERA_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (mClss != null) {
                        Intent intent = new Intent(this, mClss);
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(this, R.string.grant_camera_permissions, Toast.LENGTH_SHORT).show();
                }
                break;
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, SELECT_PHOTO);

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                break;
            }
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        new Thread(new Runnable() {
            @Override
            public void run() {
                setUserInfo();
//                checkForPrivateBoardAccessRequests();
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

//        checkForPrivateBoardAccessRequests();
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


//    public void checkForPrivateBoardAccessRequests() {
//        final GloopList<TaskAccessRequest> accessRequests = Gloop
//                .all(TaskAccessRequest.class)
//                .where()
//                .equalsTo("boardCreator", owner.getUserId())
//                .all();
//        for (final TaskAccessRequest accessRequest : accessRequests) {
////            NotificationUtil.show(TaskListActivity.this, accessRequest);
//            runOnUiThread(new Runnable() {
//                              @Override
//                              public void run() {
//                                  new AcceptBoardAccessDialog(TaskListActivity.this, accessRequest).show();
//                              }
//                          }
//            );
//        }
//
//
////        GloopList<TaskAccessRequest> all = Gloop.all(TaskAccessRequest.class);
//        accessRequests.removeOnChangeListeners();
//        accessRequests.addOnChangeListener(new GloopOnChangeListener() {
//            @Override
//            public void onChange() {
//                for (TaskAccessRequest accessRequest : accessRequests) {
////                    if (accessRequest.getBoardCreator().equals(owner.getUserId()))
//                    new AcceptBoardAccessDialog(TaskListActivity.this, accessRequest).show();
//                }
//            }
//        });
//    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public static final int SELECT_PHOTO = 1;


    private final static int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 2;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent
            imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                                    != PackageManager.PERMISSION_GRANTED) {

                                // Should we show an explanation?
                                if (shouldShowRequestPermissionRationale(
                                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                    // Explain to the user why we need to read the contacts
                                }

                                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                                // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
                                // app-defined int constant that should be quite unique

                                return;
                            }
                        }

                        final Uri imageUri = imageReturnedIntent.getData();
                        final InputStream imageStream;
                        if (imageUri != null) {
                            imageStream = getContentResolver().openInputStream(imageUri);
                            Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                            selectedImage = ThumbnailUtils.extractThumbnail(selectedImage, 200, 200);
                            userImage.setImageBitmap(selectedImage);
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                }
        }
    }
}