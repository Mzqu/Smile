package mq.org.smile;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
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

public class MainActivity extends Activity {
    private boolean loadBar = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            loadLoginView();
        }
        setContentView(R.layout.activity_main);

        ((Button)findViewById(R.id.toRecord)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RecordActivity.class);
                startActivity(intent);

            }
        });

        ((Button)findViewById(R.id.toPlay)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, PlayActivity.class);
                startActivity(intent);
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
        loadBar = false;
    }

    private void updateStuff(){
        final ProgressDialog progressBar = new ProgressDialog(this);
        progressBar.setCanceledOnTouchOutside(false);
        progressBar.setMessage("Updating...");
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.show();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Points");
        query.whereEqualTo("author", ParseUser.getCurrentUser());
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject obj, ParseException e) {
                if(loadBar)
                    progressBar.dismiss();

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
