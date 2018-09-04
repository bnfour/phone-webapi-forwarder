package net.bnfour.phone2web2tg_forwarder;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface IDontnetTelegramForwarderApi {
    @POST("")
    Call<Response> sendRequest(@Body Request request);
}
