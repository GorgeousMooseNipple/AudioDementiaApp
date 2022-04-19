package lab.android.audiodementia.client;

import android.content.Context;
import android.net.http.HttpResponseCache;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class CacheManager {

    public static void enableHttpCaching(Context appContext) {
        try {
            File httpCacheDir = new File(appContext.getCacheDir()
                    , "http");
            long httpCacheSize = 20 * 1024 * 1024; // 10 MiB
            HttpResponseCache.install(httpCacheDir, httpCacheSize);
        } catch (IOException e) {
            Log.i("CACHING_FAILED"
                    , "OVER ICS: HTTP response cache failed:" + e);
        }
    }

}
