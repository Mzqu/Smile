package mq.org.smile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.RatingBar;

import com.parse.CountCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.FileOutputStream;
import java.util.LinkedList;

public class PlayActivity extends SwipeableActivity {
    private MediaPlayer m;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final ImageButton play = (ImageButton) findViewById(R.id.playButton);
        final FontButton request = (FontButton) findViewById(R.id.requestButton);
        final FontTextView noMessages = (FontTextView) findViewById(R.id.noMessagesText);

        noMessages.setVisibility(View.INVISIBLE);
        play.setVisibility(View.INVISIBLE);

        request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                request.setVisibility(View.INVISIBLE);
                final ProgressDialog progressBar = new ProgressDialog(view.getContext());
                progressBar.setCancelable(false);
                progressBar.setMessage("Requesting...");
                progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressBar.show();

                final ParseQuery<ParseObject> pointsQuery = ParseQuery.getQuery("Points");
                pointsQuery.whereEqualTo("author", ParseUser.getCurrentUser());
                pointsQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject obj, ParseException e) {
                        if (obj == null || (int) obj.get("amount") == 0) {
                            progressBar.dismiss();
                            alertNoPoints();
                            return;
                        } else {
                            final ParseObject object = obj;
                            final ParseQuery<ParseObject> query = ParseQuery.getQuery("Recording");
                            query.whereNotEqualTo("author", ParseUser.getCurrentUser());

                            try {
                                query.countInBackground(new CountCallback() {
                                    @Override
                                    public void done(int i, ParseException e) {
                                        if (i == 0) {
                                            progressBar.dismiss();
                                            noMessages.setVisibility(View.VISIBLE);
                                        } else {
                                            object.put("amount", (int) object.get("amount") - 1);
                                            object.saveInBackground();
                                            final String retrieved = Environment.getExternalStorageDirectory().getAbsolutePath() + "/recording2.3gp";
                                            play.setEnabled(false);

                                            query.getFirstInBackground(new GetCallback<ParseObject>() {
                                                @Override
                                                public void done(ParseObject object, ParseException e) {
                                                    play.setVisibility(View.VISIBLE);
                                                    progressBar.dismiss();
                                                    final ParseUser author = (ParseUser) object.get("author");
                                                    ParseFile audio = object.getParseFile("audio");

                                                    if(audio != null) {
                                                        play.setEnabled(true);
                                                        final String audioFileURL = audio.getUrl();
                                                        object.deleteInBackground();
                                                        final LinkedList rated = new LinkedList();
                                                        rated.push(false);

                                                        play.setOnClickListener(new View.OnClickListener() {

                                                            @Override
                                                            public void onClick(View view) {
                                                                play.setVisibility(View.INVISIBLE);
                                                                play.startAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.circle_exit));

                                                                m = new MediaPlayer();
                                                                m.setAudioStreamType(AudioManager.STREAM_MUSIC);

                                                                try {
                                                                    m.setDataSource(audioFileURL);
                                                                } catch (Exception ex) {
                                                                }

                                                                try {
                                                                    m.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                                                        @Override
                                                                        public void onPrepared(MediaPlayer m) {
                                                                            m.start();
                                                                            ((RecordingProgressCircle) findViewById(R.id.progressCircle)).start(m.getDuration());
                                                                            m.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                                                                @Override
                                                                                public void onCompletion(MediaPlayer mediaPlayer) {
                                                                                    play.setVisibility(View.VISIBLE);
                                                                                    play.startAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.circle_enter));
                                                                                    ((RecordingProgressCircle) findViewById(R.id.progressCircle)).clear();
                                                                                    ((RecordingProgressCircle) findViewById(R.id.progressCircle)).startAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.fade_out));

                                                                                    final Dialog rankDialog = new Dialog(PlayActivity.this, R.style.FullHeightDialog);
                                                                                    rankDialog.setContentView(R.layout.rank_dialog);
                                                                                    rankDialog.setCancelable(false);
                                                                                    final RatingBar ratingBar = (RatingBar) rankDialog.findViewById(R.id.dialog_ratingbar);

                                                                                    FontTextView text = (FontTextView) rankDialog.findViewById(R.id.rank_dialog_text1);
                                                                                    text.setText("Rate This Message");

                                                                                    FontButton updateButton = (FontButton) rankDialog.findViewById(R.id.rank_dialog_button);
                                                                                    updateButton.setOnClickListener(new View.OnClickListener() {
                                                                                        @Override
                                                                                        public void onClick(View v) {
                                                                                            final ParseQuery<ParseObject> query = ParseQuery.getQuery("Rating");
                                                                                            query.whereEqualTo("author", author);
                                                                                            query.getFirstInBackground(new GetCallback<ParseObject>() {
                                                                                                @Override
                                                                                                public void done(ParseObject obj, ParseException e) {
                                                                                                    final ParseObject object = obj;
                                                                                                    if (object == null) {
                                                                                                        final ParseObject rating = new ParseObject("Rating");
                                                                                                        rating.put("total", 1);
                                                                                                        rating.put("rating", "" + ratingBar.getRating());
                                                                                                        rating.put("author", author);
                                                                                                        ratingBar.getRating();
                                                                                                        rating.saveInBackground();
                                                                                                    } else {
                                                                                                        int total = (int) object.get("total");
                                                                                                        float rate = Float.parseFloat((String) object.get("rating"));
                                                                                                        object.put("rating", "" + ((total * rate + ratingBar.getRating()) / (total + 1)));
                                                                                                        object.put("total", (int) object.get("total") + 1);
                                                                                                        object.saveInBackground();
                                                                                                    }
                                                                                                }
                                                                                            });
                                                                                            rankDialog.dismiss();
                                                                                        }
                                                                                    });
                                                                                    //now that the dialog is set up, it's time to show it
                                                                                    if (!(boolean) rated.get(0)) {
                                                                                        rankDialog.show();
                                                                                        rated.set(0, true);
                                                                                    }
                                                                                }
                                                                            });
                                                                        }
                                                                    });
                                                                    m.prepareAsync();
                                                                } catch (Exception e) {
                                                                    e.printStackTrace();
                                                                }
                                                            }
                                                        });
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            } catch (Exception ex) {
                            }
                        }
                    }
                });
            }
        });
    }

    private void alertNoPoints(){
        AlertDialog alertDialog = new AlertDialog.Builder(PlayActivity.this).create();
        alertDialog.setTitle("No Points!");
        alertDialog.setMessage("You have no points. Please go record a message first!");

        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Got it!",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        onBackPressed();
                    }
                });
        alertDialog.show();
    }

    private void bytesToAudio(String pathname, byte[] myByteArray){
        try{
            FileOutputStream fos = new FileOutputStream(pathname);
            fos.write(myByteArray);
            fos.close();
        }
        catch(Exception e){}
    }

    @Override
    public void onBackPressed(){
        if(m != null)
            m.stop();
        this.finish();
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
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
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}