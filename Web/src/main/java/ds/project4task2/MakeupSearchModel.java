package ds.project4task2;
/*
 * @author Xinyuan Xu
 * @andrew id: xinyuanx
 *
 * This file is the Model component of the MVC, and it models the business
 * logic for the web application.  In this case, the business logic involves
 * making a request to Makeup API and then returning the information of Makeup
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import com.mongodb.client.MongoCollection;
import jakarta.servlet.http.HttpServletRequest;
import org.bson.Document;
import org.json.*;

public class MakeupSearchModel {
    /**
     * Arguments.
     *
     * @param searchBrand The brand of the makeup to be searched for.
     * @param searchType The type of the makeup to be searched for.
     * photo requested.
     */
    public List<String> doMakeupSearch(String searchBrand, String searchType)
            throws UnsupportedEncodingException  {

        searchBrand = URLEncoder.encode(searchBrand, "UTF-8");
        searchType = URLEncoder.encode(searchType, "UTF-8");

        String response = "";

        // Create a URL for the page to be screen scraped
        String makeupURL =
                "http://makeup-api.herokuapp.com/api/v1/products.json?brand="+searchBrand+"&product_type="
                        + searchType;

        // Fetch the page
        response = fetch(makeupURL);

        if (response.length()==2) {
            return null;
        }
        if (response.contains("},{\"id\":")) {
            response = response.substring(1,response.indexOf(",{\"id\":"));
        } else {
            response = response.substring(1,response.length()-1);
        }

        // create JSON object using the response
        JSONObject obj = new JSONObject(response);
        String name = (String) obj.get("name");
        String price = (String) obj.get("price");
        String product_link = (String) obj.get("product_link");
        String image_link = (String) obj.get("image_link");

        // return an array of the information
        List<String> result = new ArrayList<>();
        result.add(image_link);
        result.add(name);
        result.add(price);
        result.add(product_link);
        return result;
    }

    /*
     * Make an HTTP request to a given URL
     *
     * @param urlString The URL of the request
     * @return A string of the response from the HTTP GET.  This is identical
     * to what would be returned from using curl on the command line.
     */
    private String fetch(String urlString) {
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
            System.out.println("Eeek, an exception");
            // Do something reasonable.  This is left for students to do.
        }
        return response;
    }
    public void logData(MongoCollection collection, HttpServletRequest request, String name, String price, String product_link, String image_link, Timestamp timestamp, long search_time, String searchBrand, String searchType) {
        // https://www.mongodb.com/docs/guides/server/read/
        // save log data to database
        Document document = new Document()
                .append("search_brand", searchBrand)
                .append("search_type", searchType)
                .append("name", name)
                .append("price", price)
                .append("product_link", product_link)
                .append("image_link", image_link)
                .append("device",request.getHeader("User-Agent").split("/")[0])
                .append("timestamp", timestamp)
                .append("search_time", search_time);

        collection.insertOne(document);
    }
}
