package lab.android.audiodementia.background;

public class ConnectionCheckEvent {

    private boolean success;
    private String message;

    public ConnectionCheckEvent(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }
    public String getMessage() { return message; }
}
