package mq.org.smile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class MainActivity extends AppCompatActivity {
    private boolean loadBar = true;
    ProgressDialog progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            loadLoginView();
        }
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);

        ((Button)findViewById(R.id.toRecord)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RecordActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
            }
        });

        ((Button)findViewById(R.id.toPlay)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, PlayActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
            }
        });
    }

    @Override
    protected void onResume(){
        loadBar = true;
        super.onResume();
        updateStuff();
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(progressBar != null)
            progressBar.dismiss();
        loadBar = false;
    }

    private void updateStuff(){
        progressBar = new ProgressDialog(this);
        progressBar.setCancelable(false);
        progressBar.setMessage("Updating...");
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.show();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Points");
        query.whereEqualTo("author", ParseUser.getCurrentUser());
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject obj, ParseException e) {
                if(loadBar) {
                    progressBar.dismiss();
                    progressBar = null;
                }

                if (obj == null)
                    ((TextView) findViewById(R.id.points)).setText("0 points");
                else
                    ((TextView) findViewById(R.id.points)).setText(obj.get("amount") + " points");
            }
        });
        if(ParseUser.getCurrentUser() != null)
            ((TextView)findViewById(R.id.user)).setText(ParseUser.getCurrentUser().getUsername());
    }

    private void loadLoginView() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {

            case R.id.action_refresh: {
                updateStuff();
                break;
            }

            case R.id.action_new: {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
                break;
            }
            case R.id.action_settings: {
                // Do something when user selects Settings from Action Bar overlay
                break;
            }
            case R.id.action_logout:
                ParseUser.logOut();
                loadLoginView();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
