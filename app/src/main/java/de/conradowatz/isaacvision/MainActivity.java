package de.conradowatz.isaacvision;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import br.liveo.interfaces.NavigationLiveoListener;
import br.liveo.navigationliveo.NavigationLiveo;


public class MainActivity extends NavigationLiveo implements NavigationLiveoListener {

    private ItemsFragment itemsFragment;

    private void setUpDrawer() {

        //set shadow
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawerLayout.setDrawerShadow(R.drawable.shadow_right, GravityCompat.START);

        // set listener {required}
        this.setNavigationListener(this);

        List<String> mListNameItem = new ArrayList<>();
        mListNameItem.add(0, "Items");
        mListNameItem.add(1, "Wiki");
        mListNameItem.add(2, "coming soon...");
        //Seeds?
        //Babys?
        //Rooms?

        // icons list items
        List<Integer> mListIconItem = new ArrayList<>();
        mListIconItem.add(0, R.drawable.icon_eye);
        mListIconItem.add(1, 0);
        mListIconItem.add(2, R.drawable.icon_dots);

        //{optional} - Among the names there is some subheader, you must indicate it here
        List<Integer> mListHeaderItem = new ArrayList<>();
        mListHeaderItem.add(1);

        setDefaultStartPositionNavigation(0);
        this.setNavigationAdapter(mListNameItem, mListIconItem, mListHeaderItem, null);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onInt(Bundle bundle) {
        //write default update channel --> BETA
        if (VersionUpdater.getUpdateChannel(this)==-1) {
            VersionUpdater.setUpdateChannel(this, VersionUpdater.CHANNEL_BETA);
        }
        //set up navigation drawer
        setUpDrawer();
        //start version updater
        new VersionUpdater().start(this);

    }

    @Override
    public void onUserInformation() {

        FragmentManager mFragmentManager = getSupportFragmentManager();
        View view = LayoutInflater.from(this).inflate(R.layout.nav_draw_header, null);
        this.mUserBackground.setImageDrawable(getResources().getDrawable(R.drawable.nav_draw_header));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onItemClickNavigation(int position, int containerID) {

        FragmentManager mFragmentManager = getSupportFragmentManager();
        Fragment mFragment = null;
        String fragmentTag = null;

        switch (position) {
            case 0:
                fragmentTag = "item-fragment";
                if (getSupportFragmentManager().findFragmentByTag(fragmentTag)!=null) {
                    mFragment = getSupportFragmentManager().findFragmentByTag(fragmentTag);
                } else {
                    mFragment = new ItemsFragment();
                }
                break;
        }

        if (mFragment!=null && fragmentTag!=null) {
            mFragmentManager.beginTransaction().replace(containerID, mFragment, fragmentTag).commit();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (position == 0) {
                getToolbar().setElevation(0);
            } else {
                getToolbar().setElevation(7);
            }

        }


    }

    @Override
    public void onPrepareOptionsMenuNavigation(Menu menu, int i, boolean b) {

    }

    @Override
    public void onClickFooterItemNavigation(View view) {

    }

    @Override
    public void onClickUserPhotoNavigation(View view) {

    }
}
