package net.bnfour.phone2web2tg_forwarder;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

// lol, what a typo in "dotnet". Stays this way
public interface IDontnetTelegramForwarderApi {
    // so seems like retrofit doesn't like empty strings here
    // it also add slash between base url and request path by itself
    @POST("api")
    Call<Response> sendRequest(@Body Request request);
}
