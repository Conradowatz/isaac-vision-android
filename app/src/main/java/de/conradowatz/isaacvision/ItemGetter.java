package de.conradowatz.isaacvision;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ItemGetter {

    String itemImageFilePath;
    String trinketImageFilePath;
    String cardImageFilePath;
    String jsonTextFilePath;

    public class ItemDownLoadTask extends AsyncTask<ItemsFragment, Void, ArrayList<Item>[]> {

        private ItemsFragment parent;

        @Override
        protected ArrayList<Item>[] doInBackground(ItemsFragment... params) {

            parent = params[0];

            String jsonData = downloadString("http://conradowatz.de/apps/isaacvision/isaacdatabase.html");
            //json encoding

            String itemImagedownload = null;
            String trinketImageDownload = null;
            String cardImageDownload = null;
            try {
                JSONObject fullJson = new JSONObject(jsonData);
                JSONObject imagelinks = fullJson.getJSONObject("imagedownloads");
                itemImagedownload = imagelinks.getString("items");
                trinketImageDownload = imagelinks.getString("trinkets");
                cardImageDownload = imagelinks.getString("cards");

            } catch (JSONException e) {
                e.printStackTrace();
            }

            //download images
            Bitmap itemsImage = downloadImage(itemImagedownload);
            Bitmap trinketsImage = downloadImage(trinketImageDownload);
            Bitmap cardsImage = downloadImage(cardImageDownload);

            if (itemsImage==null || trinketsImage==null || cardsImage==null) {
                return null;
            }

            //save them
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(itemImageFilePath);
                itemsImage.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.close();

                out = new FileOutputStream(trinketImageFilePath);
                trinketsImage.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.close();

                out = new FileOutputStream(cardImageFilePath);
                cardsImage.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            //save json data
            try {
                FileWriter outw = new FileWriter(new File(jsonTextFilePath));
                outw.write(jsonData);
                outw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            ArrayList<Item>[] itemList = makeItemList(jsonData, itemsImage, trinketsImage, cardsImage);

            return itemList;
        }

        @Override
        protected void onPostExecute(ArrayList<Item>[] items) {

            if (items==null) {
                parent.onDownloadError();
                return;
            }

            //sort items
            int sortOrder = FilterParams.getSavedSortOrder(parent.getActivity());
            if (sortOrder!=FilterParams.SORT_ORDER_ALPHABETICAL) {
                for (ArrayList<Item> item : items) {
                    FilterParams.sortItemList(item, sortOrder);
                }
            }

            parent.gotItems(items);
        }
    }

    public class ItemStorageTask extends AsyncTask<ItemsFragment, Void, ArrayList<Item>[]> {

        private ItemsFragment parent;

        @Override
        protected ArrayList<Item>[] doInBackground(ItemsFragment... params) {

            Bitmap itemImage = null;
            Bitmap trinketImage = null;
            Bitmap cardImage = null;
            String jsonText = null;

            parent = params[0];
            Context context = parent.getActivity().getApplicationContext();

            //load item image
            FileInputStream inputStream;
            try {
                inputStream = context.openFileInput("rebirth-items-final.png");
                itemImage = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            //load trinket image
            try {
                inputStream = context.openFileInput("rebirth-trinkets-final.png");
                trinketImage = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            //load card image
            try {
                inputStream = context.openFileInput("rebirth-cards-final.png");
                cardImage = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            //load jsonText
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            BufferedReader in = null;

            try {
                in = new BufferedReader(new FileReader(new File(context.getFilesDir() + "/items-json.txt")));
                while ((line = in.readLine()) != null) stringBuilder.append(line);
                jsonText = stringBuilder.toString();
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            ArrayList<Item>[] itemList = makeItemList(jsonText, itemImage, trinketImage, cardImage);
            return itemList;
        }

        @Override
        protected void onPostExecute(ArrayList<Item>[] items) {
            //sort items
            int sortOrder = FilterParams.getSavedSortOrder(parent.getActivity());
            if (sortOrder!=FilterParams.SORT_ORDER_ALPHABETICAL) {
                for (ArrayList<Item> item : items) {
                    FilterParams.sortItemList(item, sortOrder);
                }
            }

            parent.gotItems(items);
        }
    }

    public void start(ItemsFragment context) {

        Context applicationContext = context.getActivity().getApplicationContext();

        itemImageFilePath = applicationContext.getFilesDir() + "/rebirth-items-final.png";
        trinketImageFilePath = applicationContext.getFilesDir() + "/rebirth-trinkets-final.png";
        cardImageFilePath = applicationContext.getFilesDir() + "/rebirth-cards-final.png";
        jsonTextFilePath = applicationContext.getFilesDir() + "/items-json.txt";

        File itemImageFile = new File(itemImageFilePath);
        File trinketImageFile= new File(trinketImageFilePath);
        File cardImageFile = new File(cardImageFilePath);
        File jsonTextFile = new File(jsonTextFilePath);

        Boolean exists = (itemImageFile.exists() && trinketImageFile.exists() && cardImageFile.exists() && jsonTextFile.exists());

        if (exists) {
            new ItemStorageTask().execute(context);
            Log.d("IG", "load from save data...");
        } else {
            new ItemDownLoadTask().execute(context);
            Log.d("IG", "start downloading data...");
        }
    }



    public static String downloadString(String downloadLink) {
        String resultString = null;
        try {
            InputStream in = new java.net.URL(downloadLink).openStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();
            sb.append(reader.readLine() + "\n");

            String line = "0";
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            in.close();
            resultString = sb.toString();
        } catch (Exception e) {
            Log.d("STRING", "String download Error:");
            Log.e("STRING", e.getMessage());
        }
        return resultString;
    }

    public static Bitmap downloadImage(String downloadLink) {
        Bitmap resultBitmap = null;
        try {
            InputStream in = new java.net.URL(downloadLink).openStream();
            resultBitmap = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.d("IMAGE", "Image download Error:");
            Log.e("IMAGE", e.getMessage());
        }
        return resultBitmap;
    }

    public static ArrayList<Item>[] makeItemList(String jsonData, Bitmap itemsImage, Bitmap trinketsImage, Bitmap cardsImage) {
        //json encoding

        ArrayList<Item> itemList = new ArrayList<>();
        ArrayList<Item> trinketList = new ArrayList<>();
        ArrayList<Item> cardList = new ArrayList<>();

        try {
            JSONObject fullJson = new JSONObject(jsonData);
            JSONObject itemInfo = fullJson.getJSONObject("iteminfo");

            JSONArray items = itemInfo.getJSONArray("items");
            for (int i=0; i<items.length(); i++) {
                JSONObject currentJSONItem = items.getJSONObject(i);
                Bitmap currentImage = Bitmap.createBitmap(itemsImage, currentJSONItem.getInt("startX"), 0, currentJSONItem.getInt("width"), 50);
                Item currentItem = new Item(currentJSONItem.getString("title"),
                                            currentJSONItem.getString("pickup"),
                                            currentJSONItem.getString("description"),
                                            currentJSONItem.getString("extraInfo"),
                                            currentJSONItem.getString("tags"),
                                            currentJSONItem.getBoolean("specialItem"),
                                            currentImage, currentJSONItem.getString("colorID"),
                                            Float.valueOf((float) currentJSONItem.getDouble("alphabetID")),
                                            currentJSONItem.getInt("gameID"));

                itemList.add(currentItem);
            }

            JSONArray trinkets = itemInfo.getJSONArray("trinkets");
            for (int i=0; i<trinkets.length(); i++) {
                JSONObject currentJSONItem = trinkets.getJSONObject(i);
                Bitmap currentImage = Bitmap.createBitmap(trinketsImage, currentJSONItem.getInt("startX"), 0, currentJSONItem.getInt("width"), 50);
                Item currentItem = new Item(currentJSONItem.getString("title"),
                        currentJSONItem.getString("pickup"),
                        currentJSONItem.getString("description"),
                        currentJSONItem.getString("extraInfo"),
                        currentJSONItem.getString("tags"),
                        currentJSONItem.getBoolean("specialItem"),
                        currentImage, currentJSONItem.getString("colorID"),
                        Float.valueOf((float) currentJSONItem.getDouble("alphabetID")),
                        currentJSONItem.getInt("gameID"));

                trinketList.add(currentItem);
            }

            JSONArray cards = itemInfo.getJSONArray("cards");
            for (int i=0; i<cards.length(); i++) {
                JSONObject currentJSONItem = cards.getJSONObject(i);
                Bitmap currentImage = Bitmap.createBitmap(cardsImage, currentJSONItem.getInt("startX"), 0, currentJSONItem.getInt("width"), 50);
                Item currentItem = new Item(currentJSONItem.getString("title"),
                        currentJSONItem.getString("pickup"),
                        currentJSONItem.getString("description"),
                        currentJSONItem.getString("extraInfo"),
                        currentJSONItem.getString("tags"),
                        currentJSONItem.getBoolean("specialItem"),
                        currentImage, currentJSONItem.getString("colorID"),
                        Float.valueOf((float) currentJSONItem.getDouble("alphabetID")),
                        currentJSONItem.getInt("gameID"));

                cardList.add(currentItem);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        ArrayList<Item>[] mainArray = new ArrayList[]{itemList, trinketList, cardList};

        return mainArray;
    }
}
