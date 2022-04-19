package lab.android.audiodementia.background;

public class UserSignInEvent {
    private String message;
    private long id;
    private boolean success;
    private String login;
    private String pass;
    private String token;
    private String refresh;

    public UserSignInEvent(boolean success, String message, long id, String login, String pass, String token, String refresh) {
        this.message = message;
        this.id = id;
        this.success = success;
        this.login = login;
        this.pass = pass;
        this.token = token;
        this.refresh = refresh;
    }

    public UserSignInEvent(boolean success, String message, String login, String pass, String token, String refresh) {
        this.message = message;
        this.success = success;
        this.login = login;
        this.pass = pass;
        this.token = token;
        this.refresh = refresh;
    }

    public UserSignInEvent(boolean success, String message) {
        this.message = message;
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public long getId() {
        return id;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getLogin() {
        return login;
    }

    public String getPass() { return pass; }

    public String getToken() { return token; }

    public String getRefresh() { return refresh; }
}
