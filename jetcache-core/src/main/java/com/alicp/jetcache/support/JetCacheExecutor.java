package com.alicp.jetcache.support;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created on 2017/5/3.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class JetCacheExecutor {
    protected static ScheduledExecutorService defaultExecutor;
    protected static ScheduledExecutorService heavyIOExecutor;

    // init a lock,for reducing lock granularity,we can use tow ReentrantLock for defaultExecutor and heavyIOExecutor initializing
    private static final ReentrantLock lock = new ReentrantLock();

    // counter for thread naming
    private static final AtomicInteger threadCount = new AtomicInteger(0);

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (defaultExecutor != null) {
                    defaultExecutor.shutdownNow();
                }
                if (heavyIOExecutor != null) {
                    heavyIOExecutor.shutdownNow();
                }
            }
        });
    }

    public static ScheduledExecutorService defaultExecutor() {
        if (defaultExecutor != null) {
            return defaultExecutor;
        }
//        synchronized (JetCacheExecutor.class) {
//            if (defaultExecutor == null) {
//                ThreadFactory tf = r -> {
//                    Thread t = new Thread(r, "JetCacheDefaultExecutor");
//                    t.setDaemon(true);
//                    return t;
//                };
//                defaultExecutor = new ScheduledThreadPoolExecutor(
//                        1, tf, new ThreadPoolExecutor.DiscardPolicy());
//            }
//        }
        try {
            lock.lock();
            if (defaultExecutor == null) {
                ThreadFactory tf = r -> {
                    Thread t = new Thread(r, "JetCacheDefaultExecutor");
                    t.setDaemon(true);
                    return t;
                };
                defaultExecutor = new ScheduledThreadPoolExecutor(
                        1, tf, new ThreadPoolExecutor.DiscardPolicy());
            }
            return defaultExecutor;
        } finally {
            lock.unlock();
        }
    }

    public static ScheduledExecutorService heavyIOExecutor() {
        if (heavyIOExecutor != null) {
            return heavyIOExecutor;
        }
//        synchronized (JetCacheExecutor.class) { // this may throw IllegalMonitorStateException
//            if (heavyIOExecutor == null) {
//                ThreadFactory tf = r -> {
//                    Thread t = new Thread(r, "JetCacheHeavyIOExecutor" + threadCount.incrementAndGet());
//                    t.setDaemon(true);
//                    return t;
//                };
//                heavyIOExecutor = new ScheduledThreadPoolExecutor(
//                        10, tf, new ThreadPoolExecutor.DiscardPolicy());
//            }
//        }
        try {
            lock.lock();
            if (heavyIOExecutor == null) {
                ThreadFactory tf = r -> {
                    Thread t = new Thread(r, "JetCacheHeavyIOExecutor" + threadCount.incrementAndGet());
                    t.setDaemon(true);
                    return t;
                };
                heavyIOExecutor = new ScheduledThreadPoolExecutor(
                        10, tf, new ThreadPoolExecutor.DiscardPolicy());
            }
            return heavyIOExecutor;
        } finally {
            lock.unlock();
        }
    }

    public static void setDefaultExecutor(ScheduledExecutorService executor) {
        JetCacheExecutor.defaultExecutor = executor;
    }

    public static void setHeavyIOExecutor(ScheduledExecutorService heavyIOExecutor) {
        JetCacheExecutor.heavyIOExecutor = heavyIOExecutor;
    }
}
