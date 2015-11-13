package mq.org.smile;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import java.io.FileNotFoundException;
import java.io.IOException;


public class RecordActivity extends SwipeableActivity{
    Button play,stop,record,send,retry;
    RecordingProgressCircle circle;
    ParseFile file;
    Handler h;
    private MediaRecorder myAudioRecorder;
    private MediaPlayer m;
    private String outputFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_record);


        play=(Button)findViewById(R.id.button3);
        stop=(Button)findViewById(R.id.button2);
        record=(Button)findViewById(R.id.button);
        send=(Button)findViewById(R.id.button4);
        retry=(Button)findViewById(R.id.button5);
        circle=(RecordingProgressCircle)findViewById(R.id.progressCircle);

        stop.setVisibility(View.INVISIBLE);
        play.setVisibility(View.INVISIBLE);
        retry.setEnabled(false);
        send.setEnabled(false);
        outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/recording.3gp";

        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    myAudioRecorder=new MediaRecorder();
                    myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
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

                record.setVisibility(View.INVISIBLE);
                circle.start(30000);

                h = new Handler();
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        stop.setVisibility(View.VISIBLE);
                    }
                }, 2000);

                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        h.removeCallbacksAndMessages(null);
                        stopRecord();
                    }
                }, 30000);
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                h.removeCallbacksAndMessages(null);
                stopRecord();
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) throws IllegalArgumentException,SecurityException,IllegalStateException {
                play.setVisibility(View.INVISIBLE);
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
                        play.setVisibility(View.VISIBLE);
                        circle.clear();
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
                stop.setVisibility(View.INVISIBLE);

                final ProgressDialog progressBar = new ProgressDialog(v.getContext());
                progressBar.setCancelable(false);
                progressBar.setMessage("Sending...");
                progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressBar.show();

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
                stop.setVisibility(View.INVISIBLE);
                play.setVisibility(View.INVISIBLE);
                retry.setEnabled(false);
                circle.clear();
                send.setEnabled(false);
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
        myAudioRecorder.stop();
        myAudioRecorder.release();
        myAudioRecorder  = null;

        stop.setVisibility(View.INVISIBLE);
        play.setVisibility(View.VISIBLE);
        circle.clear();
        send.setEnabled(true);
        retry.setEnabled(true);

        byte[] data = AudioFileToBytes(outputFile);
        file = new ParseFile("recording.3gpp", data);
        file.saveInBackground(new SaveCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    // Saved successfully.
                    Toast.makeText(getApplicationContext(), "Audio recorded successfully", Toast.LENGTH_SHORT).show();
                } else {
                    // The save failed.
                    Toast.makeText(getApplicationContext(), "Failed to Save", Toast.LENGTH_SHORT).show();
                    Log.d(getClass().getSimpleName(), "User update error: " + e);
                }
            }
        });
    }

    public byte[] AudioFileToBytes(final String filePath){
        File file = new File(filePath);
        FileInputStream fin = null;
        try {
            // create FileInputStream object
            fin = new FileInputStream(file);

            byte fileContent[] = new byte[(int)file.length()];

            // Reads up to certain bytes of data from this input stream into an array of bytes.
            fin.read(fileContent);
            //create string from byte array
            String s = new String(fileContent);
            return fileContent;
        }
        catch (FileNotFoundException e) {
            System.out.println("File not found" + e);
        }
        catch (IOException ioe) {
            System.out.println("Exception while reading file " + ioe);
        }
        finally {
            // close the streams using close method
            try {
                if (fin != null) {
                    fin.close();
                }
            }
            catch (IOException ioe) {
                System.out.println("Error while closing stream: " + ioe);
            }
        }
        return null;
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

