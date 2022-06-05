package edu.cmu.project4;
/**
 * @author Xinyuan Xu
 * @andrewID xinyuanx
 */
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.annotation.RequiresApi;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class GetMakeupInfo {
    MakeupSearch ms = null;   // for callback
    String[] searchTerm = null;       // search Flickr for this word

    List result = null;          // returned from Flickr

    // search( )
    // Parameters:
    // String searchTerm: the thing to search for on flickr
    // Activity activity: the UI thread activity
    // InterestingPicture ip: the callback method's class; here, it will be ip.pictureReady( )
    public void search(String[] searchTerm, Activity activity, MakeupSearch ms) {
        this.ms = ms;
        this.searchTerm = searchTerm;
        new BackgroundTask(activity).execute();
    }

    // class BackgroundTask
    // Implements a background thread for a long running task that should not be
    //    performed on the UI thread. It creates a new Thread object, then calls doInBackground() to
    //    actually do the work. When done, it calls onPostExecute(), which runs
    //    on the UI thread to update some UI widget (***never*** update a UI
    //    widget from some other thread!)
    //
    // Adapted from one of the answers in
    // https://stackoverflow.com/questions/58767733/the-asynctask-api-is-deprecated-in-android-11-what-are-the-alternatives
    // Modified by Barrett
    //
    // Ideally, this class would be abstract and parameterized.
    // The class would be something like:
    //      private abstract class BackgroundTask<InValue, OutValue>
    // with two generic placeholders for the actual input value and output value.
    // It would be instantiated for this program as
    //      private class MyBackgroundTask extends BackgroundTask<String, Bitmap>
    // where the parameters are the String url and the Bitmap image.
    //    (Some other changes would be needed, so I kept it simple.)
    //    The first parameter is what the BackgroundTask looks up on Flickr and the latter
    //    is the image returned to the UI thread.
    // In addition, the methods doInBackground() and onPostExecute( ) could be
    //    abstract methods; would need to finesse the input and ouptut values.
    // The call to activity.runOnUiThread( ) is an Android Activity method that
    //    somehow "knows" to use the UI thread, even if it appears to create a
    //    new Runnable.

    private class BackgroundTask {

        private Activity activity; // The UI thread

        public BackgroundTask(Activity activity) {
            this.activity = activity;
        }

        private void startBackground() {
            new Thread(new Runnable() {
                public void run() {

                    try {
                        doInBackground();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // This is magic: activity should be set to MainActivity.this
                    //    then this method uses the UI thread
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            onPostExecute();
                        }
                    });
                }
            }).start();
        }

        private void execute(){
            // There could be more setup here, which is why
            //    startBackground is not called directly
            startBackground();
        }

        // doInBackground( ) implements whatever you need to do on
        //    the background thread.
        // Implement this method to suit your needs
        private void doInBackground() throws IOException {
            result = search(searchTerm);
        }

        // onPostExecute( ) will run on the UI thread after the background
        //    thread completes.
        // Implement this method to suit your needs
        public void onPostExecute() {
            ms.infoReady(result);
        }

        /*
         * Search Flickr.com for the searchTerm argument, and return a Bitmap that can be put in an ImageView
         */
        private List search(String[] searchTerm) throws IOException {
            List result = new ArrayList();
            String pictureURL = null;
            String name = null;
            String price = null;
            String link = null;

            // Call Flickr to get the web page containing image URL's of the search term
            // Call the web site on heroku
            JSONObject jsonObj =
                    getRemoteJSON("https://cryptic-wildwood-22514.herokuapp.com/getMakeupInformation?searchBrand="+searchTerm[0]+"&searchType="+searchTerm[1]);

            // Get the photo element
            if (jsonObj == null) {
                return null; // no pictures found

            } else {
                try {
                    pictureURL = (String) jsonObj.get("image_link");
                    name = (String) jsonObj.get("name");
                    price = (String) jsonObj.get("price");
                    link = (String) jsonObj.get("product_link");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            // At this point, we have the URL of the picture that resulted from the search.  Now load the image itself.
            try {
                URL u = new URL(pictureURL);
                result.add(getRemoteImage(u));
            } catch (Exception e) {
                e.printStackTrace();
                result.add(null);
            }
            result.add(name);
            result.add(price);
            result.add(link);

            return result;
        }


        /*
         * Given a url that will request JSON, return a JSONObject, else null
         */

        private JSONObject getRemoteJSON(String urlString) {
            // code from InterestingPicture lab
            String response = "";
            try {
                URL url = new URL(urlString);
                /*
                 * Create an HttpURLConnection.  This is useful for setting headers
                 * and for getting the path of the resource that is returned (which
                 * may be different than the URL above if redirected).
                 * HttpsURLConnection (with an "s") can be used if required by the site.
                 */
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                // Read all the text returned by the server
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                String str;
                // Read each line of "in" until done, adding each to "response"
                while ((str = in.readLine()) != null) {
                    // str is one line of text readLine() strips newline characters
                    response += str;
                }
                in.close();
            } catch (IOException e) {
                System.out.println("Eeek, an exception: "+e);
                // Do something reasonable.  This is left for students to do.
            }

            try{
                if (response == null || response.length()==0 ) {
                    System.out.println("No such product.");
                    return null;
                }
                JSONObject object = new JSONObject(response);
                return object;
            } catch (Exception e) {
                System.out.print("Yikes, hit the error: "+e);
                return null;
            }
        }


        /*
         * Given a URL referring to an image, return a bitmap of that image
         */
        @RequiresApi(api = Build.VERSION_CODES.P)
        private Bitmap getRemoteImage(final URL url) {
            try {
                final URLConnection conn = url.openConnection();
                conn.connect();
                BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
                Bitmap bm = BitmapFactory.decodeStream(bis);
                return bm;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}


