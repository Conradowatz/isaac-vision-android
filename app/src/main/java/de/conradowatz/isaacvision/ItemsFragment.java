package de.conradowatz.isaacvision;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;


public class ItemsFragment extends android.support.v4.app.Fragment {

    private View thisView;
    private SlidingUpPanelLayout panelLayout;
    private ViewPager viewPager;
    private String searchQueryText;

    private Item openItem;
    private Boolean panelIsUp;

    public ItemsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        thisView = inflater.inflate(R.layout.fragment_items, container, false);
        setHasOptionsMenu(true);

        panelLayout = (SlidingUpPanelLayout) thisView.findViewById(R.id.item_slidingPanel_SlidingUpPanelLayout);

        //create fragments for the viewpager and store them in an array
        ItemListFragment itemFragment = new ItemListFragment();
        ItemListFragment trinketFragment = new ItemListFragment();
        ItemListFragment cardsFragment = new ItemListFragment();

        ItemListFragment[] itemListFragments = new ItemListFragment[]{itemFragment, trinketFragment, cardsFragment};


        //set the pager adapter
        viewPager = (ViewPager) thisView.findViewById(R.id.items_ViewPager);
        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(new ItemPageAdapter(getChildFragmentManager(), itemListFragments));

        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) thisView.findViewById(R.id.item_PagerSlidingTabStrip);
        tabs.setViewPager(viewPager);

        //get the itemLists via AsyncTask
        if (savedInstanceState == null) { //makes sure they are not again called
            ItemGetter itemGetter = new ItemGetter();
            itemGetter.start(this);
        }

        if (panelIsUp != null) { //set state of the slideUpPanel
            if (!panelIsUp) panelLayout.hidePanel();
        } else panelLayout.hidePanel();

        return thisView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //get that store values back!
        if (savedInstanceState != null) {
            panelIsUp = savedInstanceState.getBoolean("isPanelUp");
            searchQueryText = savedInstanceState.getString("searchQueryText");
            if (panelIsUp) {
                openItem = savedInstanceState.getParcelable("openItem");
                setInfoPanelContent(openItem, null);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //save if the panel is up and if, the panel content
        outState.putBoolean("isPanelUp", panelIsUp);
        outState.putString("searchQueryText", searchQueryText);
        if (panelIsUp) {
            outState.putParcelable("openItem", openItem);
        }
    }

    //is called when the ItemGetter finished
    public void gotItems(ArrayList<Item>[] itemList) {
        //pass the fragments their content
        for (int i = 0; i < 3; i++) {
            //this is a trick to get the fragments in the adapter
            ItemListFragment itemListFragment = (ItemListFragment) viewPager.getAdapter().instantiateItem(viewPager, i);
            itemListFragment.setUp(itemList[i], i);
        }

    }

    //is called when the ItemGetter throws a download error
    public void onDownloadError() {

    }

    //will be called from the ItemListFragment when an item is selected
    public void setInfoPanelContent(Item item, ItemListFragment context) {

        panelIsUp = true;
        openItem = item;

        //deselect
        for (int i = 0; i < 3; i++) {
            ItemListFragment itemListFragment = (ItemListFragment) viewPager.getAdapter().instantiateItem(viewPager, i);
            if (context != itemListFragment) {
                itemListFragment.selectItem(-1);
            }
        }


        if (panelLayout.isPanelHidden()) {
            panelLayout.showPanel();
        }
        TextView vTitle = (TextView) thisView.findViewById(R.id.infoPanel_title_TextView);
        TextView vPickup = (TextView) thisView.findViewById(R.id.infoPanel_pickup_TextView);
        TextView vID = (TextView) thisView.findViewById(R.id.infoPanel_id_TextView);
        ImageView vImage = (ImageView) thisView.findViewById(R.id.infoPanel_image_ImageView);
        TextView vSpecial = (TextView) thisView.findViewById(R.id.infoPanel_special_TextView);
        TextView vDescription = (TextView) thisView.findViewById(R.id.infoPanel_desc_TextView);
        TextView vExtra = (TextView) thisView.findViewById(R.id.infoPanel_extra_TextView);

        vTitle.setText(Html.fromHtml(item.getTitle()));
        if (item.getPickup() != null) {
            vPickup.setText(Html.fromHtml(item.getPickup()));
        } else {
            vPickup.setText("");
        }
        if (item.getGameID() != null) {
            vID.setText("ID: " + String.valueOf(item.getGameID()));
        } else {
            vID.setText("");
        }
        vImage.setImageBitmap(item.getImage());
        if (item.getSpecialItem()) {
            vSpecial.setVisibility(View.VISIBLE);
        } else {
            vSpecial.setVisibility(View.INVISIBLE);
        }
        vDescription.setText(Html.fromHtml(item.getDescription()));
        if (item.getExtraInfo() != null) {
            vExtra.setText(Html.fromHtml(item.getExtraInfo()));
        } else {
            vExtra.setText("");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        panelIsUp = false;
        //write default sort order
        if (FilterParams.getSavedSortOrder(getActivity()) == -1) {
            FilterParams.setSavedSortOrder(getActivity(), FilterParams.SORT_ORDER_ALPHABETICAL);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_items, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);

        // Search View
        SearchView mSearchView = (SearchView) searchItem.getActionView();
        mSearchView.setOnQueryTextListener(new ItemTextFilterListener());
        mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                searchQueryText = null;
                for (int i = 0; i < 3; i++) {
                    ItemListFragment itemListFragment = (ItemListFragment) viewPager.getAdapter().instantiateItem(viewPager, i);
                    FilterParams filterParams = itemListFragment.getFilterParams();
                    filterParams.setSearchText(null);
                    itemListFragment.setFilterParams(filterParams);
                }
                return false;

            }
        });
        if (searchQueryText != null) {
            mSearchView.setQuery(searchQueryText, false);
            mSearchView.setIconified(false);
            mSearchView.clearFocus();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_sort_id) {
            changeItemSortOrder(FilterParams.SORT_ORDER_ID);
            return true;
        } else if (id == R.id.action_sort_color) {
            changeItemSortOrder(FilterParams.SORT_ORDER_COLOR);
            return true;
        } else if (id == R.id.action_sort_alphabetical) {
            changeItemSortOrder(FilterParams.SORT_ORDER_ALPHABETICAL);
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    private void changeItemSortOrder(int sortOrderId) {
        //dont update if the same is selected
        int lastSortOrder = FilterParams.getSavedSortOrder(getActivity());
        if (lastSortOrder == sortOrderId) {
            return;
        }

        //save new sort order
        FilterParams.setSavedSortOrder(getActivity(), sortOrderId);
        //change itemlist for every fragment
        for (int i = 0; i < 3; i++) {
            ItemListFragment itemListFragment = (ItemListFragment) viewPager.getAdapter().instantiateItem(viewPager, i);
            itemListFragment.sortItemList(sortOrderId);
        }

    }

    public class ItemPageAdapter extends FragmentPagerAdapter {

        private ItemListFragment[] fragments;

        public ItemPageAdapter(FragmentManager fm, ItemListFragment[] fragments) {
            super(fm);

            this.fragments = fragments;

        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            android.support.v4.app.Fragment switchFragment = null;
            switch (position) {
                case 0:
                    switchFragment = fragments[0];
                    break;
                case 1:
                    switchFragment = fragments[1];
                    break;
                case 2:
                    switchFragment = fragments[2];
                    break;
            }
            return switchFragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            CharSequence[] titles = new CharSequence[]{"Items", "Trinkets", "Other"};
            return titles[position];
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

    public class ItemTextFilterListener implements SearchView.OnQueryTextListener {

        @Override
        public boolean onQueryTextSubmit(String s) {
            searchQueryText = s;
            for (int i = 0; i < 3; i++) {
                ItemListFragment itemListFragment = (ItemListFragment) viewPager.getAdapter().instantiateItem(viewPager, i);
                FilterParams filterParams = itemListFragment.getFilterParams();
                filterParams.setSearchText(s);
                itemListFragment.setFilterParams(filterParams);
            }
            return false;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            return false;
        }
    }

}