package lab.android.audiodementia.client;

public class HttpResponse {

    private boolean successful;
    private String message;

    HttpResponse(boolean successful, String message) {
        this.successful = successful;
        this.message = message;
    }

    public boolean isSuccess() {
        return  successful;
    }

    public String getMessage() {
        return message;
    }
}
