package de.conradowatz.isaacvision;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;
import it.neokree.materialnavigationdrawer.elements.listeners.MaterialSectionListener;


public class MainActivity extends MaterialNavigationDrawer {


    private void setUpDrawer() {

        setDrawerHeaderImage(this.getResources().getDrawable(R.drawable.nav_draw_header));


        addSection(newSection("Items", R.drawable.icon_eye, new ItemsFragment()));
        addSubheader("Wiki");
        addSection( newSection("Coming soon", R.drawable.icon_dots ,(MaterialSectionListener)null));

        Intent startSettings = new Intent(this, SettingsActivity.class);
        addBottomSection(newSection("Settings", R.drawable.icon_settings, startSettings));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void init(Bundle savedInstanceState) {
        //set default preference values
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        //set up navigation drawer
        setUpDrawer();
        //start version updater
        new VersionUpdater(this){
            @Override
            void onFinish(boolean newVersionAvailable) {}
            @Override
            void onError(String message) {}
        }.start();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
