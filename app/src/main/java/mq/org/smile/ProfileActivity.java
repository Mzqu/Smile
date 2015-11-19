package mq.org.smile;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class ProfileActivity extends SwipeableActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        update();
    }

    public void update(){
        final ProgressDialog progressBar = new ProgressDialog(this);
        progressBar.setCancelable(false);
        progressBar.setMessage("Loading...");
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.show();

        ((TextView)findViewById(R.id.username)).setText(ParseUser.getCurrentUser().getUsername());
        ((TextView)findViewById(R.id.userSince)).setText("User since " + ParseUser.getCurrentUser().getCreatedAt());
        final ParseQuery<ParseObject> pointsQuery = ParseQuery.getQuery("Points");
        pointsQuery.whereEqualTo("author", ParseUser.getCurrentUser());
        pointsQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                progressBar.dismiss();
                if (parseObject == null)
                    ((TextView) findViewById(R.id.points)).setText("0 points");
                else
                    ((TextView) findViewById(R.id.points)).setText(parseObject.get("amount") + " points");
                final ParseQuery<ParseObject> ratingQuery = ParseQuery.getQuery("Rating");
                ratingQuery.whereEqualTo("author", ParseUser.getCurrentUser());
                ratingQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {
                        if (parseObject == null)
                            ((TextView) findViewById(R.id.rating)).setText("No rating yet.");
                        else {
                            float temp = Float.parseFloat((String) parseObject.get("rating"));
                            int temp2 = (int) (temp * 10);
                            double fin = temp2 / 10;
                            ((TextView) findViewById(R.id.rating)).setText("Rating: " + fin);
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onResume(){
        update();
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.sign_up, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }
}
