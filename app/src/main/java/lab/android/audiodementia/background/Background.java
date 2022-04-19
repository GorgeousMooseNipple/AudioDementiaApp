package lab.android.audiodementia.background;


import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import org.greenrobot.eventbus.EventBus;

public class Background {

    private static Background instance;
    private final ExecutorService executorService = new ScheduledThreadPoolExecutor(4);
    private final EventBus eventBus = EventBus.getDefault();

    private Background() {}

    public static Background getInstance()
    {
        if (instance == null)
            instance = new Background();
        return instance;
    }

    public Future<?> execute(Runnable runnable) {
        return executorService.submit(runnable);
    }

    public <T> Future<T> submit(Callable<T> runnable) {
        return executorService.submit(runnable);
    }

    public void postEvent(final Object event) {
        eventBus.post(event);
    }

    public void register(final Object subscriber) {
        eventBus.register(subscriber);
    }

    public void unregister(final Object subscriber) {
        eventBus.unregister(subscriber);
    }

}
