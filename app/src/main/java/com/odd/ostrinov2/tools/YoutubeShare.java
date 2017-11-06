package com.odd.ostrinov2.tools;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.Toast;

import com.odd.ostrinov2.Constants;
import com.odd.ostrinov2.Ost;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class YoutubeShare {

    private String youtube, url;
    private Activity activity;
    private DBHandler db;

    public YoutubeShare(Activity activity, String url, DBHandler db){
        this.db = db;
        this.activity = activity;
        this.url = url;
        YoutubeGetInfo youtubeGetInfo = new YoutubeGetInfo();
        youtubeGetInfo.execute();
    }
    public String getTitle(){
        return youtube;
    }

    private class YoutubeGetInfo extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(final Void... arg0) {
            HttpHandler sh = new HttpHandler();
            String jsonUrl = "https://www.youtube.com/oembed?format=json&amp;url=" + url
                    + "&key=" + Constants.YDATA_API_TOKEN;

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(jsonUrl);

            //  Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    System.out.println(jsonObj);
                    youtube = jsonObj.get("title").toString();
                    parseOst(youtube, db);

                } catch (final JSONException e) {
                    // Log.e(TAG, "Json parsing error: " + e.getMessage());
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity,
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                //Log.e(TAG, "Couldn't get json from server.");
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity,
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }

        private void parseOst(String title, DBHandler db){
            List<String> shows = db.getAllShows();
            String lcTitle = youtube.toLowerCase();
            String ostShow = "";
            for (String show: shows){
                System.out.println("Title: " + lcTitle + " show: " + show);
                if(show.contains("(")){
                    System.out.println("show contains (");
                    String wholeShowString = show;
                    String[] lineArray = show.split("\\(");
                    show = lineArray[0];
                    String showEnglish = lineArray[1].replace(")", "").trim();
                    System.out.println("show: " + show + " showEnglish: " + showEnglish);
                    if(lcTitle.contains(show.toLowerCase()) || lcTitle.contains(showEnglish.toLowerCase())){
                        System.out.println("setting to show: " + show);
                        ostShow = wholeShowString;
                        if(lcTitle.contains(show.toLowerCase())){
                            title = title.replace(show,"").replace("-", "").trim();
                        }else{
                            title = title.replace(showEnglish, "").replace("-", "").trim();
                        }
                    }
                }
                else if(lcTitle.contains(show.toLowerCase()) && !show.equals("")){
                    ostShow = show;
                    title = title.replace(show, "").replace("-", "").trim();
                }
            }
            db.addNewOst(new Ost(title, ostShow, "",  url));
        }
    }
}