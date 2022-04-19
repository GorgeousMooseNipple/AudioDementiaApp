package lab.android.audiodementia.background;

public class UserRegisterEvent {

    private boolean success;
    private String message;

    public UserRegisterEvent(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
