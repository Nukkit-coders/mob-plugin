package de.kniffo80.mobplugin;

import de.kniffo80.mobplugin.runnable.RouteFinderSearchTask;

import java.util.concurrent.*;

/**
 * @author zzz1999 @ MobPlugin
 */
public class RouteFinderThreadPool {

    public static ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

    public static void executeRouteFinderThread(RouteFinderSearchTask t) {
        executor.execute(t);
    }

    public static void shutDownNow() {
        executor.shutdownNow();
    }

}
