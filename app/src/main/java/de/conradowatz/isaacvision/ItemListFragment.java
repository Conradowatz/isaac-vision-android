package de.conradowatz.isaacvision;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.util.ArrayList;


public class ItemListFragment extends android.support.v4.app.Fragment {

    private View thisView;
    private ArrayList<Item> itemList;
    private int lastTouch;
    private ArrayList<Item> filterSavedItemList;
    private int checkedItemPool;
    private FilterParams filterParams;

    private RecyclerView itemRecycler;
    private ProgressBar loadingProgressBar;
    private RecyclerItemsAdapter mAdapter;
    AlertDialog itemPoolDialog;
    private int itemMode;


    public ItemListFragment() {
        // Required empty public constructor
    }

    //gets called by the ItemsFragment passing the item data
    public void setUp(ArrayList<Item> itemList, int itemMode) {

        this.itemList = itemList;
        this.itemMode = itemMode;

        updateItemList(itemList);
        itemRecycler.setVisibility(View.VISIBLE);
        loadingProgressBar.setVisibility(View.INVISIBLE);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //save all item data
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("itemList", itemList);
        if (filterSavedItemList != null) {
            outState.putParcelableArrayList("filterSavedItemList", filterSavedItemList);
        }
        outState.putInt("selectedItem", lastTouch);
        outState.putInt("itemMode", itemMode);
        if (itemMode==0) outState.putInt("checkedItemPool", checkedItemPool);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //get back the item data!
        if (savedInstanceState != null) {
            itemList = savedInstanceState.getParcelableArrayList("itemList");
            filterSavedItemList = savedInstanceState.getParcelableArrayList("filterSavedItemList");
            lastTouch = savedInstanceState.getInt("selectedItem", lastTouch);
            itemMode = savedInstanceState.getInt("itemMode");
            if (itemMode==0) checkedItemPool = savedInstanceState.getInt("checkedItemPool");

            //because its called after onCreateView
            updateItemList(itemList);
            itemRecycler.setVisibility(View.VISIBLE);
            loadingProgressBar.setVisibility(View.INVISIBLE);

            mAdapter.selectItem(lastTouch);

        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        //set nothing is selected at first
        lastTouch = -1;
        //set all item pools enabled
        if (itemMode==0) {
            filterParams = new FilterParams(null, 0);
            checkedItemPool = 0;
        }
    }

    public void setFilterParams(FilterParams setFilterParams) {

        filterParams = setFilterParams;

        if (filterSavedItemList != null) {
            itemList = filterSavedItemList;
        } else {
            filterSavedItemList = itemList;
        }

        if (itemList != null) {
            new AsyncTask<FilterParams, Void, ArrayList<Item>>() {
                @Override
                protected ArrayList<Item> doInBackground(FilterParams... params) {
                    FilterParams filterParamsInside = params[0];
                    return FilterParams.filterItemList(itemList, filterParamsInside);
                }

                @Override
                protected void onPostExecute(ArrayList<Item> items) {
                    itemList = items;
                    if (loadingProgressBar != null) {
                        if (loadingProgressBar.getVisibility() == View.INVISIBLE) {
                            updateItemList(itemList);
                        }
                    }
                }
            }.execute(filterParams);
        }
    }

    public void resetFilter() {

        this.filterParams = null;

        if (filterSavedItemList != null) {
            itemList = filterSavedItemList;

            if (loadingProgressBar != null) {
                if (loadingProgressBar.getVisibility() == View.INVISIBLE) {
                    updateItemList(itemList);
                }
            }
        }

    }

    public FilterParams getFilterParams() {
        return this.filterParams;
    }

    public void sortItemList(final int sortOrderId) {
        if (itemList != null) {
            new AsyncTask<Integer, Void, ArrayList<Item>>() {
                @Override
                protected ArrayList<Item> doInBackground(Integer... params) {
                    int sortOrderInside = params[0];
                    return FilterParams.sortItemList(itemList, sortOrderInside);
                }

                @Override
                protected void onPostExecute(ArrayList<Item> items) {
                    itemList = items;
                    if (loadingProgressBar != null) {
                        if (loadingProgressBar.getVisibility() == View.INVISIBLE) {
                            updateItemList(itemList);
                        }
                    }
                }
            }.execute(sortOrderId);
        }
    }

    public void selectItem(int selectedItem) {
        lastTouch = selectedItem;
        //because this can get called before onCreateView
        if (mAdapter != null) {
            mAdapter.selectItem(selectedItem);
        }
    }

    private void updateItemList(ArrayList<Item> items) {

        if (!isAdded()) {
            return;
        }

        //must store items in private value, because the onItemClickListener uses them
        itemList = items;

        //calculate imageHeight based on screen dpi, to use for RecyclerView
        final float imageHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics());

        Bitmap[] images = new Bitmap[items.size()];
        for (int i = 0; i < items.size(); i++) {
            images[i] = items.get(i).getImage();
        }

        //pass the adapter its images
        mAdapter = new RecyclerItemsAdapter(images);
        //do not select anything at start
        mAdapter.selectItem(-1);
        itemRecycler.setAdapter(mAdapter);
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 50, LinearLayoutManager.VERTICAL, false);
        //here we need the imageHeight, because we calculate how many items fit on one row. the 50 is the accuracy
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int i) {
                Item currentItem = itemList.get(i);
                float scale = itemRecycler.getWidth() / 50;
                int spanSize = (int) Math.ceil((float) currentItem.getImage().getWidth() / currentItem.getImage().getHeight() * imageHeight / scale);

                return spanSize;
            }
        });
        itemRecycler.setLayoutManager(layoutManager);

        //set item TouchListener to to ItemClickListener
        final ItemListFragment context = this;
        itemRecycler.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                //saves lastTouch so panel doesn't swipe up iff already selected
                if (lastTouch != position) {
                    //get ItemsFragment
                    ItemsFragment thisparentFragment = (ItemsFragment) getParentFragment();
                    //tell it to slide up/change the item info panel
                    thisparentFragment.setInfoPanelContent(itemList.get(position), context);
                    mAdapter.selectItem(position);
                }
                lastTouch = position;
            }
        }));

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        thisView = inflater.inflate(R.layout.fragment_itemlist, container, false);

        itemRecycler = (RecyclerView) thisView.findViewById(R.id.itemList_items_RecyclerView);
        loadingProgressBar = (ProgressBar) thisView.findViewById(R.id.itemList_loading_ProgressBar);

        //at first onCreateView the items will be still loading, so we have to display a loading bar
        if (itemList != null) {
            updateItemList(itemList);
            selectItem(lastTouch);

            itemRecycler.setVisibility(View.VISIBLE);
            loadingProgressBar.setVisibility(View.INVISIBLE);
        } else {
            //because RecyclerViews cant be empty we pass it an empty list
            updateItemList(new ArrayList<Item>());

            itemRecycler.setVisibility(View.INVISIBLE);
            loadingProgressBar.setVisibility(View.VISIBLE);
        }


        return thisView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (itemMode==0) inflater.inflate(R.menu.menu_itemlist_items, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_select_rooms) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Select item pools");
            String[] itemPools = {"All", "Item Room", "Shop", "Boss Room", "Devil Room", "Angle Room", "Secret Room", "Library", "Golden Chest", "Red Chest", "Curse Room", "Beggar", "Demon Beggar", "Key Beggar", "Challenge Room"};
            builder.setSingleChoiceItems(itemPools, checkedItemPool, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    checkedItemPool = which;
                    FilterParams newFilterParams = getFilterParams();
                    newFilterParams.setItemPool(checkedItemPool);
                    setFilterParams(newFilterParams);
                    itemPoolDialog.cancel();
                }
            });
            builder.setNeutralButton("Cancel", null);
            itemPoolDialog = builder.create();
            itemPoolDialog.show();
        }
        return super.onOptionsItemSelected(item);
    }
}
