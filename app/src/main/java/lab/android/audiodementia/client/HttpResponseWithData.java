package lab.android.audiodementia.client;

public class HttpResponseWithData<Data> extends HttpResponse {

    private Data data;

    HttpResponseWithData(boolean successful, int statusCode, String message, Data data) {
        super(successful, statusCode, message);
        this.data = data;
    }

    public Data getData() {
        return this.data;
    }
}
