package com.technosale.atm;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Base64;
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
    private static String mFileName = null;
    Timer initTimer = new Timer();
    Timer secondTimer = new Timer();
    int hitTime = 0;
    boolean audioReceived = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle bundle = getIntent().getExtras();
        String title = bundle.getString("device_name");
        startFirstTimer(title);
    }

    private void startFirstTimer(String title) {
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
                if (response.code() == 200) {
                    if (response.body() != null) {
                        AudioResponseModel responseModel = response.body();
                        if (!responseModel.audio_base64_text.equals("")){
                            audioReceived = true;
                            hitTime = 0;
                            String base64Audio = responseModel.audio_base64_text;
                            File dir = Environment.getExternalStorageDirectory();
                            File target = new File(dir, "Audio captures");
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
                                        Uri uri = Uri.fromFile(uploadFile);
                                        uploadFile = new File(uri.getPath());
                                        File finalUploadFile = uploadFile;
                                        if (response.code() == 200) {
                                            finalUploadFile.delete();
                                        }
                                    }
                                } catch (Exception e) {
                                }
                            } catch (Exception e) {
                            }
                            stopFirstTimer();

                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    startSecondTimer(title);
                                }
                            }, 1000 * 10);
                        } else {
                            if (audioReceived) {
                                hitTime += 1;
                                if (hitTime == 2) {
                                    audioReceived = false;
                                    hitTime = 0;
                                    stopSecondTimer();
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
            }

            @Override
            public void onFailure(Call<AudioResponseModel> call, Throwable t) {
            }
        });
    }

    private void stopSecondTimer() {
        if (secondTimer != null) {
            secondTimer.cancel();
            secondTimer.purge();
        }
    }

    private void startSecondTimer(String title) {
        secondTimer = new Timer();
        secondTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                getAudioFromApi(title);
            }
        }, 0, 1000 * 10);
    }

    private void stopFirstTimer() {
        if (initTimer != null) {
            initTimer.cancel();
            initTimer.purge();
        }
    }
}