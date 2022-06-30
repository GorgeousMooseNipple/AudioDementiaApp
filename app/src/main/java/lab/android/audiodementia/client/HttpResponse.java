package lab.android.audiodementia.client;
import java.net.HttpURLConnection;

public class HttpResponse {

    private boolean successful;
    private int statusCode;
    private String message;

    HttpResponse(boolean successful, int statusCode, String message) {
        this.successful = successful;
        this.message = message;
        this.statusCode = statusCode;
    }

    public boolean isSuccess() {
        return  successful;
    }

    public String getMessage() {
        return message;
    }

    public boolean isUnauthorized() {
        return this.statusCode == HttpURLConnection.HTTP_UNAUTHORIZED;
    }
}
