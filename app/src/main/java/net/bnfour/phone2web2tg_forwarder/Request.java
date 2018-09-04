package net.bnfour.phone2web2tg_forwarder;

/**
 * Represents requests to the API
 */
public class Request {

    String token;
    String message;
    // this is why i stick to .net most of the time
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
