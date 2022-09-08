package com.technosale.atm.Connection;

import com.technosale.atm.Constants;
import com.technosale.atm.Model.AudioResponseModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {

    @GET(Constants.GET_AUDIO_URL)
    Call<AudioResponseModel> getAudio (@Query("device_name") String device_name);

    @GET(Constants.ADD_DEVICE)
    Call<AudioResponseModel> registerDevices (@Query("device_name") String device_name);



//    Call<String> uploadVideo (@Query("device_name") String device_name);
}
