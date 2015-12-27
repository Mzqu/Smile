package mq.org.smile;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


public class RecordActivity extends SwipeableActivity{
    ImageButton play, record, send,retry;
    boolean recording = false;
    RecordingProgressCircle circle;
    ParseFile file;
    Handler h = new Handler();
    private MediaRecorder myAudioRecorder;
    private MediaPlayer m;
    private String outputFile = null, testFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        play=(ImageButton)findViewById(R.id.button3);
        record=(ImageButton)findViewById(R.id.button);
        send=(ImageButton)findViewById(R.id.button4);
        retry=(ImageButton)findViewById(R.id.button5);
        circle=(RecordingProgressCircle)findViewById(R.id.progressCircle);

        play.setVisibility(View.INVISIBLE);
        retry.setEnabled(false);
        send.setEnabled(false);
        outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/recording.mp3";

        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!recording) {
                    try {
                        myAudioRecorder = new MediaRecorder();
                        myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
                        myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
                        myAudioRecorder.setOutputFile(outputFile);
                        myAudioRecorder.prepare();
                        myAudioRecorder.start();
                    } catch (IllegalStateException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                // code runs in a thread
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        record.setVisibility(View.INVISIBLE);
                                        circle.start(30000);
                                        record.startAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.circle_pulse));
                                        record.setEnabled(false);

                                        h.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                recording = true;
                                                record.setEnabled(true);
                                            }
                                        }, 2000);

                                        h.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                stopRecord();
                                            }
                                        }, 30000);
                                    }
                                });
                            } catch (final Exception ex) {
                                Log.i("---", "Exception in thread");
                            }
                        }
                    }.start();
                } else {
                    new Thread() {
                        @Override
                        public void run() {
                            stopRecord();
                        }
                    }.start();
                }
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                play.setVisibility(View.INVISIBLE);
                play.startAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.circle_exit));


                m = new MediaPlayer();

                try {
                    m.setDataSource(outputFile);
                }

                catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    m.prepare();
                }

                catch (IOException e) {
                    e.printStackTrace();
                }

                m.start();
                circle.start(m.getDuration());
                m.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    // code runs in a thread
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            play.setVisibility(View.VISIBLE);
                                            play.startAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.circle_enter));
                                            circle.clear();
                                            circle.startAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.fade_out));
                                        }
                                    });
                                } catch (final Exception ex) {
                                    Log.i("---","Exception in thread");
                                }
                            }
                        }.start();
                    }
                });
            }
        });

        send.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(m != null)
                    m.stop();
                play.setVisibility(View.INVISIBLE);
                record.setVisibility(View.INVISIBLE);
                send.setEnabled(false);
                retry.setEnabled(false);

                final ProgressDialog progressBar = new ProgressDialog(v.getContext());
                progressBar.setCancelable(false);
                progressBar.setMessage("Sending...");
                progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressBar.show();

                FileInputStream fileInputStream = null;
                File fileObj = new File(outputFile);
                byte[] data = new byte[(int) fileObj.length()];

                try {
                    //convert file into array of bytes
                    fileInputStream = new FileInputStream(fileObj);
                    fileInputStream.read(data);
                    fileInputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                file = new ParseFile("recording.mp3", data);
                file.saveInBackground();

                final ParseObject recording = new ParseObject("Recording");
                recording.put("audio", file);
                recording.put("author", ParseUser.getCurrentUser());
                recording.saveInBackground(new SaveCallback() {
                    public void done(ParseException e) {
                        progressBar.dismiss();
                        if (e == null) {
                            // Saved successfully.
                        } else {
                            // The save failed.
                            Toast.makeText(getApplicationContext(), "Failed to Save", Toast.LENGTH_SHORT).show();
                            Log.d(getClass().getSimpleName(), "User update error: " + e);
                        }
                    }
                });

                final ParseQuery<ParseObject> query = ParseQuery.getQuery("Points");
                query.whereEqualTo("author", ParseUser.getCurrentUser());
                query.getFirstInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject obj, ParseException e) {
                        final ParseObject object = obj;
                        if (object == null) {
                            final ParseObject points = new ParseObject("Points");
                            points.put("amount", 1);
                            points.put("author", ParseUser.getCurrentUser());
                            points.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    displayPoints(points);
                                }
                            });
                        } else {
                            object.put("amount", (int) object.get("amount") + 1);
                            object.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    displayPoints(object);
                                }
                            });
                        }
                    }
                });
            }
        });
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(m != null)
                    m.stop();
                record.setVisibility(View.VISIBLE);
                play.setVisibility(View.INVISIBLE);
                retry.setEnabled(false);
                circle.clear();
                send.setEnabled(false);
                recording = false;
            }
        });
    }

    private void displayPoints(ParseObject object){
        AlertDialog alertDialog = new AlertDialog.Builder(RecordActivity.this).create();
        alertDialog.setTitle("Points");
        alertDialog.setMessage("You earned 1 point! You have " + object.get("amount") + " points.");

        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        onBackPressed();
                    }
                });
        alertDialog.show();
    }

    private void stopRecord(){
        new Thread() {
            @Override
            public void run() {
                recording = false;

                if(h != null){
                    h.removeCallbacksAndMessages(null);
                }
                if(myAudioRecorder != null) {
                    myAudioRecorder.stop();
                    myAudioRecorder.reset();
                    myAudioRecorder.release();
                    myAudioRecorder = null;
                }

                record.post(new Runnable() {
                    @Override
                    public void run() {
                        record.clearAnimation();
                    }
                });

                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        play.post(new Runnable() {
                            @Override
                            public void run() {
                                play.startAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.circle_enter));
                                play.setVisibility(View.VISIBLE);
                            }
                        });

                        send.post(new Runnable() {
                            @Override
                            public void run() {
                                send.setEnabled(true);
                            }
                        });

                        retry.post(new Runnable() {
                            @Override
                            public void run() {
                                retry.setEnabled(true);
                            }
                        });
                    }
                }, 400);

                circle.post(new Runnable() {
                    @Override
                    public void run() {
                        circle.clear();
                        circle.startAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.fade_out));
                    }
                });

            }
        }.start();
    }

    @Override
    protected void onPause(){
        if(h != null)
            h.removeCallbacksAndMessages(null);
        if(m != null)
            m.stop();
        if(myAudioRecorder != null) {
            myAudioRecorder.release();
            myAudioRecorder = null;
        }
        super.onPause();
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

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }
}

