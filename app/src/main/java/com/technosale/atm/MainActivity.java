package com.technosale.atm;

import static android.content.ContentValues.TAG;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.technosale.atm.Connection.API;
import com.technosale.atm.Model.AudioResponseModel;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Timer;
import java.util.TimerTask;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private MediaPlayer mPlayer;
    private static final String LOG_TAG = "AudioRecording";
    private static String mFileName = null;
    Timer initTimer = new Timer();
    Timer secondTimer = new Timer();
    int hitTime = 0;
    boolean audioReceived = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // getting bundle from recyclerview adapter
        Bundle bundle = getIntent().getExtras();
        String title = bundle.getString("device_name");
        System.out.println("pass gareko bundle ko value" + title);
        startFirstTimer(title);
    }

    private void startFirstTimer(String title) {
        System.out.println("first timer started");
        initTimer = new Timer();
        initTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                getAudioFromApi(title);
            }
        }, 0, 1000 * 25);
    }

    private void getAudioFromApi(String title) {
        Call<AudioResponseModel> call = API.getClient().getAudio(title); /*replace device name with value here*/
        call.enqueue(new Callback<AudioResponseModel>() {
            @Override
            public void onResponse(Call<AudioResponseModel> call, Response<AudioResponseModel> response) {
                System.out.println("response code is" + response.code());
                System.out.println("response is " + response.body());
                if (response.code() == 200) {
                    if (response.body() != null) {
                        System.out.println("is not null");
                        AudioResponseModel responseModel = response.body();
                        System.out.println("audio received ko value " + audioReceived);

                        /*200 + audio file received from server*/
                        if (!responseModel.audio_base64_text.equals("")){
                            System.out.println("else ma aayo");
                            audioReceived = true;
                            hitTime = 0;
                            System.out.println("hit time ko value" + hitTime);
                            String base64Audio = responseModel.audio_base64_text;
                            File dir = Environment.getExternalStorageDirectory();
                            File target = new File(dir, "Audio captures");
                            System.out.println("target location is " + target.getAbsolutePath().toString());
                            if (!target.exists())
                                target.mkdirs();
                            mPlayer = new MediaPlayer();
                            try {
                                mFileName = target.getAbsolutePath() + "/" + System.currentTimeMillis() + "audio" + ".mp3";
                                FileOutputStream fos = new FileOutputStream(mFileName);
                                fos.write(Base64.decode(base64Audio.getBytes(), Base64.DEFAULT));
                                fos.close();
                                Toast.makeText(getApplicationContext(), "Audio Started Playing", Toast.LENGTH_LONG).show();
                                try {
                                    mPlayer = new MediaPlayer();
                                    mPlayer.setDataSource(mFileName);
                                    mPlayer.prepare();
                                    mPlayer.start();
                                    File[] files = target.listFiles();
                                    if (files.length > 0) {
                                        File uploadFile = new File(target, files[0].getName());
                                        System.out.println("upload file is " + uploadFile);
                                        Uri uri = Uri.fromFile(uploadFile);
                                        uploadFile = new File(uri.getPath());
                                        File finalUploadFile = uploadFile;
                                        if (response.code() == 200) {
                                            finalUploadFile.delete();
                                            Log.i(TAG, "decoded file deleted: " + finalUploadFile);
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e(LOG_TAG, "prepare() failed");
                                }
                            } catch (Exception e) {
                                Log.e(LOG_TAG, "prepare() failed");
                            }
                            stopFirstTimer("from 200 + message");

                            /*Second timer initialised with delay of 10 sec*/
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    startSecondTimer(title, "from 200 + message");
                                }
                            }, 1000 * 10);
                        } else {
                            /*response code is 200 but audio chaina*/
                            /*Audio first time receive bhaye pachhi hit time ma jane ho*/
                            if (audioReceived) {
                                System.out.println("audio empty");
                                hitTime += 1;
                                System.out.println("hit time" + hitTime);
                                if (hitTime == 2) {
                                    audioReceived = false;
                                    hitTime = 0;
                                    stopSecondTimer("khali audio");
                                    /*Second timer initialised with delay of 10 sec*/
                                    final Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            startFirstTimer(title);
                                        }
                                    }, 1000 * 25);
                                }
                            }
                        }
                    }
                }
                /*Response code 200 nabhayeko case
                else {

                }
                */

            }

            @Override
            public void onFailure(Call<AudioResponseModel> call, Throwable t) {
                System.out.println("failure ko case " + t.getMessage());
                System.out.println("failure ko case " + t.getLocalizedMessage());

            }
        });
    }

    private void stopSecondTimer(String msg) {
        System.out.println("second timer stopped" + msg);
        if (secondTimer != null) {
            secondTimer.cancel();
            secondTimer.purge();
        }
    }

    /*10 sec Timer*/
    private void startSecondTimer(String title, String msg) {
        System.out.println("second timer started" + msg);
        secondTimer = new Timer();
        secondTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                getAudioFromApi(title);
            }
        }, 0, 1000 * 10);
    }

    private void stopFirstTimer(String msg) {
        System.out.println("first timer stopped" + msg);
        if (initTimer != null) {
            initTimer.cancel();
            initTimer.purge();
        }
    }
}