package com.technosale.atm.Connection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.technosale.atm.Constants;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class API {
    private static Retrofit retrofit = null;
    public static ApiService getClient() {

        if (retrofit == null ) {
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            retrofit = new Retrofit.Builder()
                    .client(HttpClientService.getUnsafeOkHttpClient())
                    .baseUrl(Constants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        ApiService api = retrofit.create(ApiService.class);
        return api; // return the APIInterface object
    }
}
