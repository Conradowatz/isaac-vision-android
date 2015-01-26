package de.conradowatz.isaacvision;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class FilterParams {

    public static final int SORT_ORDER_ID = 1;
    public static final int SORT_ORDER_COLOR = 2;
    public static final int SORT_ORDER_ALPHABETICAL = 3;


    private String searchText;
    private int itemPool;

    public static ArrayList<Item> sortItemList(ArrayList<Item> items, int sortOrderId) {

        if (sortOrderId == SORT_ORDER_ID) {
            Collections.sort(items, new Comparator<Item>() {
                @Override
                public int compare(Item item1, Item item2) {
                    Integer id1 = item1.getGameID();
                    Integer id2 = item2.getGameID();
                    return id1.compareTo(id2);
                }
            });
        } else if (sortOrderId == SORT_ORDER_COLOR) {
            Collections.sort(items, new Comparator<Item>() {
                @Override
                public int compare(Item item1, Item item2) {
                    String color1 = item1.getColorID();
                    String color2 = item2.getColorID();
                    return color1.compareTo(color2);
                }
            });
        } else if (sortOrderId == SORT_ORDER_ALPHABETICAL) {
            Collections.sort(items, new Comparator<Item>() {
                @Override
                public int compare(Item item1, Item item2) {
                    Float alpha1 = item1.getAlphabetID();
                    Float alpha2 = item2.getAlphabetID();
                    return alpha1.compareTo(alpha2);
                }
            });
        }

        return items;
    }

    public static ArrayList<Item> filterItemList(ArrayList<Item> items, FilterParams filterParams) {
        ArrayList<Item> filteredItems = new ArrayList<>();
        if (filterParams.getSearchText() != null && !filterParams.getSearchText().isEmpty()) {
            String searchtext = filterParams.getSearchText().toLowerCase();
            for (Item item : items) {
                if (item.getTitle().toLowerCase().contains(searchtext) ||
                        item.getTags().toLowerCase().contains(searchtext) ||
                        item.getPickup().toLowerCase().contains(searchtext) ||
                        item.getDescription().toLowerCase().contains(searchtext)) {

                    filteredItems.add(item);
                }
            }
        } else {
            filteredItems = items;
        }

        ArrayList<Item> filteredItems2 = new ArrayList<>();
        int itemPool = filterParams.getItemPool();
        if (itemPool!=0) {
            String[] poolTags = {"item room pool", "shop room pool", "boss room pool",
                    "devil room pool", "angel room pool", "secret room pool",
                    "library pool", "golden chest pool", "red chest pool",
                    "curse room pool", "normal beggar pool", "demon beggar pool",
                    "key beggar", "challenge room pool"};

            for (Item item : filteredItems) {
                if (item.getTags().toLowerCase().contains(poolTags[itemPool-1])) {
                    filteredItems2.add(item);
                }
            }
        } else {
            return filteredItems;
        }

        return filteredItems2;
    }

    public FilterParams(String searchText, int itemPool) {
        this.searchText = searchText;
        this.itemPool = itemPool;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public int getItemPool() {
        return itemPool;
    }

    public void setItemPool(int itemPool) {
        this.itemPool = itemPool;
    }

    public static int getSavedSortOrder(Context context) {
        return PreferenceReader.readIntFromPreferences(context, "item-sortorder", -1);
    }

    public static void setSavedSortOrder(Context context, int sortOrderId) {
        PreferenceReader.saveIntToPreferences(context, "item-sortorder", sortOrderId);
    }
}
