package com.technosale.atm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.technosale.atm.Connection.API;
import com.technosale.atm.Model.AudioResponseModel;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class StartActivity extends AppCompatActivity {
    private Apppref apppref;
    EditText edtDeviceName;
    Button btnRegistration, btnStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        edtDeviceName = findViewById(R.id.edtDeviceName);
        btnRegistration = findViewById(R.id.btnRegistration);
        btnStatus = findViewById(R.id.btnStatus);
        apppref = new Apppref(this);

        btnStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNameEntered()) {
                    connectRegisterApi();

                } else {
                    Toast.makeText(StartActivity.this, "Please enter the device name first", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNameEntered()) {
                    apppref.setDeviceName(edtDeviceName.getText().toString().trim());
                    connectRegisterApi();
                } else {
                    Toast.makeText(StartActivity.this, "Please enter the device name first", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }



    private boolean isNameEntered() {
        return !edtDeviceName.getText().toString().isEmpty();
    }

    private void connectRegisterApi() {
        String deviceName = edtDeviceName.getText().toString().trim();
        Call<AudioResponseModel> call = API.getClient().registerDevices(deviceName);
        call.enqueue(new Callback<AudioResponseModel>() {
            @Override
            public void onResponse(Call<AudioResponseModel> call, Response<AudioResponseModel> response) {
                if (response.code() == 200) {
                    AudioResponseModel responseModel = response.body();
                    if (responseModel.acknowledge != null) {
                        showToast(StartActivity.this, "Registration Requested Successfully");
                    }
                    else if (responseModel.audio_base64_text == null) {
                        showToast(StartActivity.this, "Pending Approval");
                    }
                    else {
                        showToast(StartActivity.this, "Device already Registered");
                        Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("device_name",deviceName);
                        intent.putExtras(bundle);

                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                } else if (response.code() == 400) {
                    JSONObject error1 = null;
                    try {
                        error1 = new JSONObject(response.errorBody().string());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (error1.has("message")) {
                        try {
                            String resultDescription = error1.getString("message");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else {

                }
            }

            @Override
            public void onFailure(Call<AudioResponseModel> call, Throwable t) {
            }
        });
    }

    private void showToast(StartActivity start, String message) {
        Toast.makeText(StartActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}