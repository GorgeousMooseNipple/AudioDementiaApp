package lab.android.audiodementia.background;

public class RefreshTokenEvent {
    boolean successful;
    String message;
    String accessToken;

    public RefreshTokenEvent(boolean successful, String message, String accessToken) {
        this.successful = successful;
        this.message = message;
        this.accessToken = accessToken;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getMessage() {
        return message;
    }

    public  String getAccessToken() {
        return accessToken;
    }
}
