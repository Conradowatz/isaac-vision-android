package de.conradowatz.isaacvision;


import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public abstract class VersionUpdater {

    public static String CHANNEL_STABLE = "stable";
    public static String CHANNEL_BETA = "beta";

    private Context context;
    private long downloadID = -1L;
    private String downloadCompleteIntentName = DownloadManager.ACTION_DOWNLOAD_COMPLETE;
    private IntentFilter downloadCompleteIntentFilter = new IntentFilter(downloadCompleteIntentName);

    class UpdateCheckerTask extends AsyncTask<Void, Void, Boolean> {

        String downloadPath;
        boolean downloadError = false;

        @Override
        protected Boolean doInBackground(Void... params) {
            String jsonData = SimpleDownloader.downloadString("http://conradowatz.de/apps/isaacvision/versioninfo.php");

            if (jsonData!=null) {
                try {
                    JSONObject fullJson = new JSONObject(jsonData);
                    int latestVersion;
                    if (getUpdateChannel(context).equals(CHANNEL_STABLE)) {
                        //STABLE CHANNEL
                        latestVersion = fullJson.getInt("lateststable");
                        downloadPath = fullJson.getString("stabledownload");
                    } else {
                        //BETA CHANNEL
                        latestVersion = fullJson.getInt("latestbeta");
                        downloadPath = fullJson.getString("betadownload");
                    }
                    PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                    int versionNumber = pInfo.versionCode;

                    if (latestVersion>versionNumber) return true;
                    else return false;

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }

            downloadError = true;
            return  false;
        }

        @Override
        protected void onPostExecute(Boolean newVersionAvailable) {
            if (!downloadError) {
                onFinish(newVersionAvailable);
                if (newVersionAvailable) showUpdateDialog(downloadPath);
            } else {
                onError("Connection error!");
            }
        }
    }

    public void start() {
        new UpdateCheckerTask().execute();
    }

    public VersionUpdater(Context context) {
        this.context = context;
    }

    abstract void onFinish(boolean newVersionAvailable);

    abstract void onError(String message);

    private void showUpdateDialog(final String downloadPath) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle("New version available!");
        String updateChannel = "STABLE";
        if (getUpdateChannel(context).equals(CHANNEL_BETA)) {
            updateChannel = "BETA";
        }
        String updateMessage = "A new version of Isaac Vision is available for download." + "\n" + "Update channel: " + updateChannel + "\n" + "Do you wan't to download it now?";
        builder.setMessage(updateMessage);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                startDownload(downloadPath);

            }
        });


        builder.setNegativeButton("No, later", null);
        builder.show();


    }

    private void startDownload(String downloadLink) {
        Uri uri = Uri.parse(downloadLink);
        Log.d("UM", downloadLink);

        Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .mkdirs();

        File file = new File(Environment.getExternalStorageDirectory() + "/download/" + "ivupdate.apk");
        file.delete();

        if (file.exists()) {
            startDownload(downloadLink);
            return;
        }

        context.registerReceiver(downloadCompleteReceiver, downloadCompleteIntentFilter);

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        downloadID = downloadManager.enqueue(new DownloadManager.Request(uri)
                        .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                                DownloadManager.Request.NETWORK_MOBILE)
                        .setAllowedOverRoaming(false)
                        .setTitle("Isaac Vision Version Updater")
                        .setDescription("Downloading new version...")
                        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                                "ivupdate.apk"));


    }

    private BroadcastReceiver downloadCompleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L);
            if (id != downloadID) {
                return;
            }
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(id);
            Cursor cursor = downloadManager.query(query);
            if (!cursor.moveToFirst()) {
                return;
            }

            File downloadFile = new File(Environment.getExternalStorageDirectory() + "/download/" + "ivupdate.apk");
            if (downloadFile.exists()) {
                Intent openIntent = new Intent(Intent.ACTION_VIEW);
                openIntent.setDataAndType(Uri.fromFile(downloadFile), "application/vnd.android.package-archive");
                openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(openIntent);
            } else {
                Toast.makeText(context, "Download error!", Toast.LENGTH_LONG).show();
            }


        }
    };

    public static String getUpdateChannel(Context context) {
        return PreferenceReader.readStringFromPreferences(context, SettingsFragment.KEY_UPDATE_CHANNEL, CHANNEL_BETA);

    }

}
