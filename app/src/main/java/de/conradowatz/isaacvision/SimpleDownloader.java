package de.conradowatz.isaacvision;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SimpleDownloader {

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
            Log.d("IMAGE", "Image download Error");
            Log.d("IMAGE", e.getMessage());
        }
        return resultBitmap;
    }
}
