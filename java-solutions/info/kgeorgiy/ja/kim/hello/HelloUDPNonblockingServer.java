package info.kgeorgiy.ja.kim.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Channel;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.System.err;
import static java.nio.channels.SelectionKey.OP_READ;

public class HelloUDPNonblockingServer extends HelloUDPServer implements HelloServer {
    private ExecutorService executorService;
    private Selector selector;
    private DatagramChannel dc;
    private Queue<Task> queue;

    @Override
    public void start(final int port, final int threads) {
        try {
            queue = new ConcurrentLinkedQueue<>();
            selector = Selector.open();
            initDatagramChannel(port);
            initExecutorService();
        } catch (IOException e) {
            err.println("Error while starting server: " + e.getMessage());
        }
    }

    private void initExecutorService() {
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(getRunnable());
    }

    protected Channel getChannel(InetSocketAddress socket, Selector selector, int index) throws IOException {
        DatagramChannel channel = DatagramChannel.open();
        channel.configureBlocking(false);
        if (socket == null || index < 0) {
            channel.bind(new InetSocketAddress(index)).register(selector, OP_READ, new Task());
        } else {
            channel.connect(socket);
            channel.register(selector, SelectionKey.OP_WRITE, new Task(index));
        }
        return channel;
    }

    private void initDatagramChannel(final int port) throws IOException {
        dc = (DatagramChannel) getChannel(null, selector, port);
    }

    private Runnable getRunnable() {
        return () -> {
            while (!Thread.interrupted()) {
                try {
                    selector.select(250);
                    final Iterator<SelectionKey> i = selector.selectedKeys().iterator();
                    while (i.hasNext()) {
                        var key = i.next();
                        i.remove();
                        if (key.isReadable()) {
                            addTaskToQueue(key);
                            doTaskFromQueue();
                        }
                    }
                } catch (IOException e) {
                    err.println("Error while getting runnable instance: " + e.getMessage());
                    close();
                }
            }
        };
    }

    private void doTaskFromQueue() {
        var t = queue.poll();
        if (t != null) {
            try {
                dc.send(t.getByteBuffer(), t.getSocketAddress());
            } catch (IOException e) {
                err.println("I/O error while sending response: " + e.getMessage());
            }
        }
    }

    private void addTaskToQueue(final SelectionKey key) {
        final Task t = (Task) key.attachment();

        try {
            t.setSocketAddress(dc.receive(t.getByteBuffer().clear()));
            t.configure();
        } catch (IOException e) {
            err.println("Error while filling byteBuffer: " + e.getMessage());
        }

        queue.add(t);
    }

    @Override
    public void close() {
        try {
            selector.close();
            dc.close();
            closeExecutorService(executorService);
        } catch (Exception e) {
            err.println("Error while closing resources: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        go(args, true);
    }
}
