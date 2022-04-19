package lab.android.audiodementia.client;

public class HttpResponseWithData<Data> extends HttpResponse {

    private Data data;

    HttpResponseWithData(boolean successful, String message, Data data) {
        super(successful, message);
        this.data = data;
    }

    public Data getData() {
        return this.data;
    }
}
