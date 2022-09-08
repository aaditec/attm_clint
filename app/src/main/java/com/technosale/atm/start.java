package com.technosale.atm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.technosale.atm.Connection.API;
import com.technosale.atm.Model.AudioResponseModel;
import com.technosale.atm.Model.Device_model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class start extends AppCompatActivity {
    private Apppref apppref;
    EditText regDevice;
    Button register, btnStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        regDevice = findViewById(R.id.regDevice);
        register = findViewById(R.id.proceed);
        btnStatus = findViewById(R.id.btnStatus);
        apppref = new Apppref(this);
//        regDevice.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showNewDeviceRegistrationDialog();
//            }
//        });

        btnStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNameEntered()) {
                    connectRegisterApi();

                } else {
                    Toast.makeText(start.this, "Please enter the device name first", Toast.LENGTH_SHORT).show();
                }
            }
        });


        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNameEntered()) {
                    apppref.setDeviceName(regDevice.getText().toString().trim());
                    connectRegisterApi();
                } else {
                    Toast.makeText(start.this, "Please enter the device name first", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }



    private boolean isNameEntered() {
        return !regDevice.getText().toString().isEmpty();
    }

    private void connectRegisterApi() {
        String deviceName = regDevice.getText().toString().trim();
        Call<AudioResponseModel> call = API.getClient().registerDevices(deviceName);
        call.enqueue(new Callback<AudioResponseModel>() {
            @Override
            public void onResponse(Call<AudioResponseModel> call, Response<AudioResponseModel> response) {
                System.out.println("response code is" + response.code());
                System.out.println("response is " + response.body());
                if (response.code() == 200) {
                    AudioResponseModel responseModel = response.body();
                    /*First time registration ma acknowledge ko value received*/
                    if (responseModel.acknowledge != null) {
                        showToast(start.this, "Registration Requested Successfully");
                        System.out.println("device registered api hit success");
                        System.out.println("acknowledgement ko value is " + responseModel.acknowledge);
                    }
                    /*Approve nabhaye samma empty json aauchha so jun json element check garda pani null aauchha*/
                    else if (responseModel.audio_base64_text == null) {
                        showToast(start.this, "Pending Approval");
                        System.out.println("device registered but pending approval");
                        System.out.println("cannot proceed to next page");
                    }
                    /*top ko both cases fail bhayeko case ma device has been registered bhanera bujhinchha*/
                    else {
                        showToast(start.this, "Device already Registered");
                        System.out.println("audio file response received (in this case approved)");

                        Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                        Bundle bundle = new Bundle();
                        Device_model deviceModel = new Device_model();
                        bundle.putString("device_name",deviceName);
                        System.out.println("device name is" + deviceName);
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
                Log.d("message", "onFailure: " + t);
            }
        });
    }

    private void showToast(start start, String message) {
        Toast.makeText(start.this, message, Toast.LENGTH_SHORT).show();
    }


    private void showNewDeviceRegistrationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_new_device_reg, null, false);
        EditText etNewDeviceName = dialogView.findViewById(R.id.et_new_device_name);
        MaterialButton btnOk = dialogView.findViewById(R.id.btn_ok);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
        builder.setView(dialogView);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        btnOk.setOnClickListener(v -> {
            apppref.setDeviceName(etNewDeviceName.getText().toString().trim());
            regDevice.setText(apppref.getDeviceName());
            alertDialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> {
            alertDialog.dismiss();
        });
    }


}