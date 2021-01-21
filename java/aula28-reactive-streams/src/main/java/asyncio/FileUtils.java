package asyncio;

import io.reactivex.Observable;
import io.reactivex.Single;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

import static utils.ThreadUtils.showThread;

public class FileUtils {

    private static final ExecutorService pool =
        Executors.newFixedThreadPool(4);

    /**
     *  This version use Futures
     *	 So, it blocks ... ugh! :(
     */
    public static Future<Long> copy2Files(String fileIn1, String fileOut1,
                                          String fileIn2, String fileOut2) {
        Future<Long> fut1 = AsyncFile.copyAsync(fileIn1, fileOut1);

        Future<Long> fut2 = AsyncFile.copyAsync(fileIn2, fileOut2);

        return pool.submit(() -> {
            return fut1.get() + fut2.get();
        });

    }

    /**
     *  This version uses a CompletableFuture as a Future
	 *	 So, it blocks too... ugh! :(
     */
    public static CompletableFuture<Long> copy2FilesCfBlock(
        String fileIn1, String fileOut1, String fileIn2, String fileOut2) {

        CompletableFuture<Long> cf1 = AsyncFile.copyAsync2(fileIn1, fileOut1);
        CompletableFuture<Long> cf2 = AsyncFile.copyAsync2(fileIn2, fileOut2);

        CompletableFuture<Long> res = new CompletableFuture<>();

        pool.submit(() -> {
            try {
                long total = cf1.get() + cf2.get();
                res.complete(total);
            } catch (Exception e) {
                res.completeExceptionally(e);
            }
        });
        return res;
    }

    /**
     *  This version uses thenCombine operator
     *  to combine the two copies executed in parallel
     *  without blocking. Ok! :)
     */
    public static CompletableFuture<Long> copy2FilesCfParalell(
        String fileIn1, String fileOut1, String fileIn2, String fileOut2) {
        CompletableFuture<Long> cf1 = AsyncFile.copyAsync2(fileIn1, fileOut1);
        CompletableFuture<Long> cf2 = AsyncFile.copyAsync2(fileIn1, fileOut1);

        return cf1.thenCombine(cf2, (l1, l2) -> l1 + l2);
    }

    /**
     *  This version uses thenCombine operator
     *  to combine the two copies executed in serial mode
     *  without blocking.
     *  Ok! But no I/O Parallelism wich could be done in this case
     */
    public static CompletableFuture<Long> copy2FilesCfSerial(
        String fileIn1, String fileOut1, String fileIn2, String fileOut2) {

        CompletableFuture<Long>  cfRes =
                AsyncFile.copyAsync2(fileIn1, fileOut1)
                .thenCompose((l1) ->
                     AsyncFile.copyAsync2(fileIn2, fileOut2)
                     .thenApply(( l2) -> l1 + l2)
                );
        
        return cfRes;
    }

    /**
     *  This version copy two files in parallel via callbacks.
     *  In this case we need to do the necessary synchronization.
     *  Complicated :(
     */
    public static void copy2Files(String fileIn1, String fileOut1,
                                  String fileIn2, String fileOut2,
                                  BiConsumer<Throwable,Long> consumer) {

        final AtomicInteger counter = new AtomicInteger();
        final AtomicLong total = new AtomicLong();

        BiConsumer<Throwable, Long> cb = (t, l) -> {
            if (t != null)
                consumer.accept(t, 0L);
            else {
                total.addAndGet(l);
                if (counter.incrementAndGet() == 2)
                    consumer.accept(null, total.get());
            }
        };

        AsyncFile.copyAsync(fileIn1, fileOut1, cb);

        AsyncFile.copyAsync(fileIn2, fileOut2, cb);
    }

    /// AsyncFile with Observables

    public static Observable<byte[]> fromFile(String fileName) {
        // return an Observable<byte[]> that, when subscribed,
        // generates an asynchronous stream of byte[] representing the file content
        return Observable.create(source -> {
            byte[] buffer = new byte[4096];
            AsyncFile f = AsyncFile.open(fileName);

            // since the callback is "recursive" we need this trick
            // to satisfy the compiler
            BiConsumer<Throwable,Integer>[] readDone = new BiConsumer[1];

            readDone[0] = ( t,  i) -> {
                if (t != null) {
                    source.onError(t);
                    f.close();
                }
                else {
                    if (i < 0) {
                        source.onComplete();
                        f.close();
                    }
                    else {
                        source.onNext(Arrays.copyOf(buffer, i));
                        f.readBytes(buffer, readDone[0]);
                    }
                }
            };
            // start the emission
            f.readBytes(buffer, readDone[0]);
        });
    }


    public static Single<Long> copyFile(String fin, String fOut)
    {


        try {
            FileOutputStream fs = new FileOutputStream((fOut));

            return
                fromFile(fin)
                    .reduce(0L, (t, b) -> {
                        showThread("reduce " + b.length + "bytes");
                        fs.write(b);
                        return t + b.length;
                    })
                    .doOnEvent((l,t) -> {
                        showThread("close out file!");
                        fs.close();
                    });
        }
        catch(Exception e) {
            return Single.error(e);
        }

    }
}
