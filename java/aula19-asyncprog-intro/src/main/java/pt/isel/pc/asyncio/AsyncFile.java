
package pt.isel.pc.asyncio;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.Channel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.InvalidParameterException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;

/**
 * Class supporting open a file
 * for asynchronous sequential read or write
 * (binary and text)
 */
public class AsyncFile {
    private final static int BUFSIZE = 4096*16;

    private static final ExecutorService pool =
        Executors.newFixedThreadPool(4);

    // tells if file is open for read or open for write
    private enum Mode { Read, Write}

    // the transfer buffer size
    private static final int BUF_SIZE = 4096*16;

    // the nio associated file channel
    private AsynchronousFileChannel channel;

    // read or write
    private Mode mode;

    // current read or write position in file
    private long position;

    // auxiliary function to avoid treat close exceptions
    public static void closeChannel(Channel c) {
        try { c.close(); } catch(Exception e) {}
    }

    // file factory for read mode
    public static AsyncFile open(String path) {
        Path pathIn = Paths.get(path);
        try {
            return new AsyncFile(pathIn, Mode.Read);
        }
        catch(IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // file factory for write (creation) mode
    public static AsyncFile create(String path) {
        Path pathIn = Paths.get(path);
        try {
            return new AsyncFile(pathIn, Mode.Write);
        }
        catch(IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // private constructor just for internal use
    private  AsyncFile(Path path, Mode mode) throws IOException {
        if (mode == Mode.Read )
            channel = AsynchronousFileChannel.open(path, StandardOpenOption.READ);
        else
            channel = AsynchronousFileChannel.open(path,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );

    }

    //
    // Callback based operations
    //

    // asynchronous read chunk operation, callback based
    public void readBytes(byte[] data, int ofs, int size,
                          BiConsumer<Throwable, Integer> completed) {
        if (completed == null)
            throw new InvalidParameterException("callback can't be null!");
        if (mode == Mode.Write)
            throw new IllegalStateException("File is not readable");
        if (size + ofs > data.length)
            size = data.length - ofs;
        if (size ==0) {
            completed.accept(null, 0);
            return;
        }
        int s = size;
        ByteBuffer buf = ByteBuffer.wrap(data, ofs, size);
        CompletionHandler<Integer,Object> readCompleted =
                new CompletionHandler<Integer,Object>() {

                    @Override
                    public void completed(Integer result, Object attachment) {
                        if (result>0) position += result;
                        completed.accept(null,result);
                    }

                    @Override
                    public void failed(Throwable exc, Object attachment) {
                        completed.accept(exc, null);
                    }
                };
        channel.read(buf,position, null, readCompleted);
    }

    // auxiliary  asynchronous, callback based, read operation
    public  void readBytes(byte[] data, BiConsumer<Throwable, Integer> completed) {
        readBytes(data, 0, data.length, completed);
    }

    // asynchronous write chunk operation, callback based
    public void writeBytes( byte[] data, int ofs, int size,
                            BiConsumer<Throwable, Integer> completed) {
        if (completed == null)
            throw new InvalidParameterException("callback can't be null!");
        if (mode == Mode.Read)
            throw new IllegalStateException("File is not writable");
        if (ofs + size > data.length) size = data.length - ofs;

        ByteBuffer buf = ByteBuffer.wrap(data, ofs, size);
        CompletionHandler<Integer,Object> writeCompleted =
                new CompletionHandler<Integer,Object>() {

                    @Override
                    public void completed(Integer result, Object attachment) {
                        position += result;
                        completed.accept(null, result);
                    }

                    @Override
                    public void failed(Throwable exc, Object attachment) {
                        completed.accept(exc, null);
                    }
                };

        channel.write(buf,position,null,writeCompleted);
    }

    // asynchronous write chunk operation returning a Future
    public Future<Integer>
    writeBytes0(byte[] data, int ofs, int size) {
        if (mode == Mode.Read)
            throw new IllegalStateException("File is not writable");
        if (ofs + size > data.length) size = data.length - ofs;

        ByteBuffer buf = ByteBuffer.wrap(data, ofs, size);
        return channel.write(buf, position);

    }

    //
    // CompletableFuture based operations
    //

    // asynchronous write buffer operation returning a CompletableFuture
    public Future<Integer> writeBytes0(byte[] data) {

        return writeBytes(data, 0, data.length);
    }


    // asynchronous read chunk operation returning a CompletableFuture
    public Future<Integer> readBytes0(byte[] data, int ofs, int size) {
        if (mode == Mode.Read)
            throw new IllegalStateException("File is not readable");
        if (ofs + size > data.length) size = data.length - ofs;

        ByteBuffer buf = ByteBuffer.wrap(data, ofs, size);
        return channel.read(buf, position);
    }

    // asynchronous read buffer operation returning a CompletableFuture
    public Future<Integer> readBytes0(byte[] data) {

        return readBytes(data,0, data.length);
    }

    // asynchronous write chunk operation returning a CompletableFuture
    public CompletableFuture<Integer>
    writeBytes(byte[] data, int ofs, int size) {
        CompletableFuture<Integer> completed = new CompletableFuture<>();
        writeBytes(data,ofs, size,
            (t,i) -> {
                if (t== null) completed.complete(i);
                else completed.completeExceptionally(t);
            });
        return completed;
    }

    //
    // CompletableFuture based operations
    //

    // asynchronous write buffer operation returning a CompletableFuture
    public CompletableFuture<Integer> writeBytes(byte[] data) {
        return writeBytes(data, 0, data.length);
    }


    // asynchronous read chunk operation returning a CompletableFuture
    public CompletableFuture<Integer> readBytes(byte[] data, int ofs, int size) {
        CompletableFuture<Integer> completed = new CompletableFuture<>();

        readBytes(data, ofs, size,
            (t,i) -> {
                if (t== null) completed.complete(i);
                else completed.completeExceptionally(t);
            });
        return completed;
    }

    // asynchronous read buffer operation returning a CompletableFuture
    public CompletableFuture<Integer> readBytes(byte[] data) {
        return readBytes(data,0, data.length);
    }

    //
    // Stuff needed for asynchronous readline
    //

    // auxiliary function to get file size
    private int getSize() {
        try {
            long s = channel.size();
            if (s > Integer.MAX_VALUE) throw new RuntimeException("File too big to read!");
            return (int) s;
        }
        catch(IOException e) { throw new UncheckedIOException(e); }
    }

    //
    // public composed operations
    //

    // read all bytes to memory asynchronously

    public void close() {
        closeChannel(channel);
    }


    //async file copy
    /**
     * Now an async file copy using NIO2 AsynchronousFileChannel
     * as an async operation notifying success/error with callbacks
     *
     * @param fileIn
     * @param fileOut
     * @return
     * @throws IOException
     */
    public static void copyAsync(
        String fileIn, String fileOut,
        BiConsumer<Throwable, Long> completed) {

        long[] nBytes  = {0};

        AsyncFile fin = AsyncFile.open(fileIn);
        AsyncFile fOut = AsyncFile.create(fileOut);

        byte[] buffer = new byte[BUF_SIZE];

        BiConsumer<Throwable, Integer>[] writeCompletion =
            new BiConsumer[1];

        BiConsumer<Throwable, Integer> readCompletion = (t, n) -> {
            if (t != null) {
                fin.close();
                fOut.close();
                completed.accept(t, null);
                return;
            }
            if (n <=0) {
                fin.close();
                fOut.close();
                completed.accept(null, nBytes[0]);
                return;
            }
            nBytes[0] += n;
            fOut.writeBytes(buffer, 0, n, writeCompletion[0]);
        };

        writeCompletion[0] =  (t, __) -> {
            //System.out.println("On write completed, current thread: " +
            // Thread.currentThread().getId());
            if (t != null) {
                fin.close();
                fOut.close();
                completed.accept(t, null);
                return;
            }
            fin.readBytes(buffer,readCompletion);
        };

        fin.readBytes(buffer, readCompletion);
    }

    public static Future<Long> copyAsync( String fileIn, String fileOut) {
        return pool.submit(() -> 2L );
    }

    public static CompletableFuture<Long> copyAsync2( String fileIn, String fileOut) {
        CompletableFuture<Long> promise = new CompletableFuture<>();

        copyAsync(fileIn, fileOut, (t, l) -> {
            if (t != null) promise.completeExceptionally(t);
            else promise.complete(l);
        });
        return promise;
    }

    private static CompletableFuture<Long>
    copyTo(AsyncFile fIn, AsyncFile fOut, long total) {
        byte[] buffer = new byte[BUFSIZE];

        CompletableFuture<Long> res = fIn.readBytes(buffer)
            .thenCompose( i -> {
                if (i <= 0) {
                    fIn.close();
                    fOut.close();
                    return CompletableFuture.completedFuture(total);
                }
                else return fOut.writeBytes(buffer, 0, i)
                    .thenCompose( j -> copyTo(fIn, fOut, total + i));
            });

        return res;

    }



    /**
     * Now an async file copy using NIO2 AsynchronousFileChannel
     * as an async operation that returns a CompletableFuture enable composition of asynchronous copies
     *
     * @param fileIn
     * @param fileOut
     * @return
     */
    public static CompletableFuture<Long> copyFileAsync(
        String fileIn, String fileOut) {

        AsyncFile fin = AsyncFile.open(fileIn);
        AsyncFile fout = AsyncFile.create(fileOut);

        return copyTo(fin,fout,0);
    }


}