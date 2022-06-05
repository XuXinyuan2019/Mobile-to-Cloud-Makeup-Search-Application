package ds.project4task2;
/**
 * @author Xinyuan Xu
 * @andrewID xinyuanx
 */
import java.io.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.*;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import org.bson.Document;

import static com.mongodb.client.model.Accumulators.sum;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Indexes.ascending;
import static com.mongodb.client.model.Indexes.descending;

@WebServlet(name = "MakeupSearchServlet", value = {"/getMakeupInformation","/dashboard"})
public class MakeupSearchServlet extends HttpServlet {
    MakeupSearchModel msm = null;  // The "business model" for this app

    // Initiate this servlet by instantiating the model that it will use.
    @Override
    public void init() {
        msm = new MakeupSearchModel();
    }

    // This servlet will reply to HTTP GET requests via this doGet method
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,IOException {
        // record the time of the search process
        long startTime = System.currentTimeMillis();
        long endTime = 0;
        // connect to mongodb
        // reference: https://www.mongodb.com/docs/guides/server/read/
        ConnectionString connectionString = new ConnectionString("mongodb://xinyuanx:Xu990706@cluster0-shard-00-00.80qvd.mongodb.net:27017,cluster0-shard-00-01.80qvd.mongodb.net:27017,cluster0-shard-00-02.80qvd.mongodb.net:27017/test?w=majority&retryWrites=true&tls=true&authMechanism=SCRAM-SHA-1");
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .serverApi(ServerApi.builder()
                        .version(ServerApiVersion.V1)
                        .build())
                .build();
        MongoClient mongoClient = MongoClients.create(settings);
        MongoDatabase database = mongoClient.getDatabase("dbProject4");
        MongoCollection<Document> collection = database.getCollection("logData");

        // the dashboard page
        if (request.getServletPath().equals("/dashboard")) {
            String nextView = "dashboard.jsp";
            RequestDispatcher view = request.getRequestDispatcher(nextView);

            // use a array to store the log data, set the logData attribute
            List<Document> logData = new ArrayList<>();
            for (Document d: collection.find()) {
                logData.add(d);
            }
            request.setAttribute("logData", logData);
            List<Document> searchTimeList = new ArrayList<>();

            // compute the shortest search time
            collection.find().sort(ascending("search_time")).into(searchTimeList);
            Document d1 = searchTimeList.get(0);
            String shortestSearchTime = String.valueOf((long) d1.get("search_time"));
            request.setAttribute("shortestSearchTime", shortestSearchTime);

            // compute the brand with the largest number of search times
            // reference: https://www.programcreek.com/java-api-examples/?api=com.mongodb.client.AggregateIterable
            List list = Arrays.asList(group("$search_brand", sum("count", 1)),
                    sort(descending("count")), limit(1));
            AggregateIterable<Document> topBrand = collection.aggregate(list);
            Document d2 = topBrand.first();
            String top1Brand =  d2.get("_id").toString();
            request.setAttribute("top1Brand", top1Brand);

            // compute the total numbers of search
            long estimatedCount = collection.estimatedDocumentCount();
            String totalSearchTimes = String.valueOf(estimatedCount);
            request.setAttribute("totalSearchTimes", totalSearchTimes);

            view.forward(request, response);

        } else {
            // get the search parameter if it exists
            String searchBrand = request.getParameter("searchBrand");
            String searchType = request.getParameter("searchType");

            String nextView;
            String name = null;
            String pictureURL = null;
            String price = null;
            String link = null;
            Timestamp timestamp = null;
            /*
             * Check if the search parameter is present.
             * If not, then give the user instructions and prompt for a search string.
             * If there is a search parameter, then do the search and return the result.
             */
            if (searchBrand != null || searchType != null) {
                // use model to do the search and choose the result view
                List<String> result = msm.doMakeupSearch(searchBrand, searchType);
                if (result != null) {
                    pictureURL = result.get(0);
                    name = result.get(1);
                    price = result.get(2);
                    link = result.get(3);
                }
                timestamp = new Timestamp(System.currentTimeMillis());
                endTime = System.currentTimeMillis();
            }

            // set response to a JSON format string
            String res = "{\"image_link\":\"" + pictureURL + "\",\"name\":\"" + name + "\",\"price\":\"" + price + "\",\"product_link\":\"" + link + "\"}";
            response.setStatus(200);
            response.setContentType("text/plain;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println(res);

            // record the data to mongodb
            msm.logData(collection, request, name, price, link, pictureURL, timestamp, endTime - startTime, searchBrand, searchType);
        }

        mongoClient.close();
    }
}