package lab.android.audiodementia.client;

public class HttpResponseWithData<Entity> extends HttpResponse {

    private Entity data;

    HttpResponseWithData(boolean successful, String message, Entity data) {
        super(successful, message);
        this.data = data;
    }

    public Entity getData() {
        return this.data;
    }
}
