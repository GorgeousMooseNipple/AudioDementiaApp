package lab.android.audiodementia.background;

import android.os.Handler;
import java.lang.ref.WeakReference;

import lab.android.audiodementia.client.HttpResponse;
import lab.android.audiodementia.client.HttpResponseWithData;

public class BackgroundHttpExecutor {

    private Handler handler;
    private Background background;

    public BackgroundHttpExecutor() {
        this.handler = new Handler();
        this.background = Background.getInstance();
    }

    public void execute(Provider<HttpResponse> provider, Consumer<HttpResponse> consumer) {
        WeakReference<Consumer<HttpResponse>> consumerRef = new WeakReference<>(consumer);
        this.background.execute(() -> {
            final HttpResponse data = provider.call();
            this.handler.post(() -> {
                try {
                    consumerRef.get().call(data);
                }
                catch (NullPointerException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    public <Data> void executeWithReturn(Provider<HttpResponseWithData<Data>> provider, Consumer<HttpResponseWithData<Data>> consumer) {
        WeakReference<Consumer<HttpResponseWithData<Data>>> consumerRef = new WeakReference<>(consumer);
        this.background.execute(() -> {
            final HttpResponseWithData<Data> data = provider.call();
            this.handler.post(() -> {
                try {
                    consumerRef.get().call(data);
                }
                catch (NullPointerException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    public <ArgType> void execute(
            Function<HttpResponse, ArgType> provider,
            ArgType args,
            Consumer<HttpResponse> consumer
    ) {
        WeakReference<Consumer<HttpResponse>> consumerRef = new WeakReference<>(consumer);
        this.background.execute(() -> {
            final HttpResponse data = provider.call(args);
            this.handler.post(() -> {
                try {
                    consumerRef.get().call(data);
                }
                catch (NullPointerException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    public <ArgType, Data> void executeWithReturn(
            Function<HttpResponseWithData<Data>, ArgType> provider,
            ArgType args,
            Consumer<HttpResponseWithData<Data>> consumer
    ) {
        WeakReference<Consumer<HttpResponseWithData<Data>>> consumerRef = new WeakReference<>(consumer);
        this.background.execute(() -> {
            final HttpResponseWithData<Data> data = provider.call(args);
            this.handler.post(() -> {
                try {
                    consumerRef.get().call(data);
                }
                catch (NullPointerException e) {
                    e.printStackTrace();
                }
            });
        });
    }
}
