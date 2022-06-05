package edu.cmu.project4;
/**
 * @author Xinyuan Xu
 * @andrewID xinyuanx
 */
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.List;

public class MakeupSearch extends AppCompatActivity {

    MakeupSearch me = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*
         * The click listener will need a reference to this object, so that upon successfully finding a picture from Flickr, it
         * can callback to this object with the resulting picture Bitmap.  The "this" of the OnClick will be the OnClickListener, not
         * this InterestingPicture.
         */
        final MakeupSearch ma = this;
        /*
         * Find the "submit" button, and add a listener to it
         */
        Button submitButton = (Button)findViewById(R.id.submit);


        // Add a listener to the send button
        submitButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View viewParam) {
                String[] searchTerm = new String[2];
                searchTerm[0] = ((EditText)findViewById(R.id.searchBrand)).getText().toString().toLowerCase();
                searchTerm[1] = ((EditText)findViewById(R.id.searchType)).getText().toString().toLowerCase();
                System.out.println("searchTerm = " + Arrays.toString(searchTerm));
                GetMakeupInfo gp = new GetMakeupInfo();
                gp.search(searchTerm, me, ma); // Done asynchronously in another thread.  It calls ms.infoReady() in this thread when complete.
            }
        });
    }

    /*
     * This is called by the GetPicture object when the picture is ready.  This allows for passing back the Bitmap picture for updating the ImageView
     */
    public void infoReady(List result) {
        ImageView pictureView = (ImageView)findViewById(R.id.makeupPicture);
        TextView title = (TextView)findViewById(R.id.title);
        TextView searchViewBrand = (EditText)findViewById(R.id.searchBrand);
        TextView searchViewType = (EditText)findViewById(R.id.searchType);
        TextView name = (TextView)findViewById(R.id.name);
        TextView price = (TextView)findViewById(R.id.price);
        TextView link = (TextView)findViewById(R.id.link);
        // if information of the makeup is found
        if (result != null && (Bitmap)result.get(0) != null) {
            pictureView.setImageBitmap((Bitmap) result.get(0));
            System.out.println("picture");
            pictureView.setVisibility(View.VISIBLE);
            name.setText("Name: " + (String) result.get(1));
            price.setText("Price: " + (String)result.get(2));
            link.setText("Link to buy: " + (String)result.get(3));
            title.setText("What we've got for you...");
            title.setVisibility(View.VISIBLE);
            name.setVisibility(View.VISIBLE);
            price.setVisibility(View.VISIBLE);
            link.setVisibility(View.VISIBLE);
        } else {
            pictureView.setImageResource(R.mipmap.ic_launcher);
            System.out.println("No picture");
            pictureView.setVisibility(View.INVISIBLE);
            title.setText("Cannot find it... Try again!");
            title.setVisibility(View.VISIBLE);
            name.setVisibility(View.INVISIBLE);
            price.setVisibility(View.INVISIBLE);
            link.setVisibility(View.INVISIBLE);
        }
        searchViewBrand.setText("");
        searchViewType.setText("");
        pictureView.invalidate();
    }
}
