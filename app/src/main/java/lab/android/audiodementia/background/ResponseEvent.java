package lab.android.audiodementia.background;


public class ResponseEvent<Data> {

    boolean successful;
    String message;
    Data data;

    public boolean isSuccessful() {
        return  successful;
    }

    public String getMessage() {
        return message;
    }

    public Data getEntity() {
        return data;
    }
}
