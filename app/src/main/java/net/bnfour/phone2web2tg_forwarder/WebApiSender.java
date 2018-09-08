package net.bnfour.phone2web2tg_forwarder;

import android.content.Context;
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

    private Context _context;

    private String _token;
    private String _endpoint;

    private int _retryCount = 0;
    private String _cachedMessage;

    public WebApiSender(Context context, String endpoint, String token) {
        _context = context;
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
                if (result.code == 1) {
                    Notifier.showNotification(_context, _context.getString(R.string.no_token_fail));
                }
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
                        Notifier.showNotification(_context, _context.getString(R.string.retries_failed));
                    }
                }
            }
        } else {
            Notifier.showNotification(_context, _context.getString(R.string.another_failure));
        }
    }

    @Override
    public void onFailure(Call<Response> call, Throwable t) {
        Notifier.showNotification(_context, _context.getString(R.string.another_failure));
    }
}
