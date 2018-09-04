package net.bnfour.phone2web2tg_forwarder;

/**
 * Represents requests to the API
 */
public class Request {

    String token;
    String message;

    public Request(String token, String message) {
        this.token = token;
        this.message = message;
    }
}
