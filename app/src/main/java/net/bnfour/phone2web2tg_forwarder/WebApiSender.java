package net.bnfour.phone2web2tg_forwarder;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// TODO notify user on failures

/**
 * Fire and forget message helper
 */
public class WebApiSender implements Callback<Response> {

    private String _token;
    private String _endpoint;

    private int _retryCount = 0;
    private String _cachedMessage;

    public WebApiSender(String endpoint, String token) {
        _endpoint = endpoint;
        _token = token;
    }

    public void send(String message) {

        _cachedMessage = message;

        Request request = new Request(_token, message);

        // Retrofit stuff
        Gson gson = new GsonBuilder().setLenient().create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(_endpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        IDontnetTelegramForwarderApi api
                = retrofit.create(IDontnetTelegramForwarderApi.class);

        Call<Response> call = api.sendRequest(request);
        call.enqueue(this);
    }

    @Override
    public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
        if (response.isSuccessful()) {
            Response result = response.body();
            if (!result.ok) {
                // TODO notification
                Log.d("result", "not ok: " + result.details);
                // retrying on too many messages
                // enum for states?
                if (result.code == 2) {
                    _retryCount++;
                    if (_retryCount < 3) {
                        try {
                            Thread.sleep(65 * 1000);
                        } catch (InterruptedException ex) {
                            Log.wtf("error", "our peaceful sleep was interrupted :(");
                        }
                        send(_cachedMessage);
                    } else {
                        // notification
                    }
                }
            } else {
                Log.d("result", "ok");
            }
        } else {
            // TODO notification about failure
            try {
                Log.d("result", "not success " + response.errorBody().string());
            } catch (Exception ex) {}
        }
    }

    @Override
    public void onFailure(Call<Response> call, Throwable t) {
        // TODO notification
        Log.d("rip", t.toString());
    }
}
