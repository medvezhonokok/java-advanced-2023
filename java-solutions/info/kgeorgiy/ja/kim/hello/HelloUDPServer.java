package info.kgeorgiy.ja.kim.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static java.lang.System.err;
import static java.nio.charset.StandardCharsets.UTF_8;

public class HelloUDPServer extends AbstractHelloUDP implements HelloServer {
    private DatagramSocket serverSocket;
    private ExecutorService executorService;

    public static void main(final String... args) {
        go(args, false);
    }

    protected static void go(String[] args, boolean isNonBlocking) {
        checkOnLength(2, args);
        final int port = get(args, 0, 8080);
        final int threads = get(args, 1, 10);

        try (HelloUDPServer server = isNonBlocking ? new HelloUDPServer() : new HelloUDPNonblockingServer()) {
            server.start(port, threads);
            do {
                String line;
                try (final Scanner in = new Scanner(System.in)) {
                    line = in.nextLine();
                    if (line.equals("quit")) {
                        break;
                    } else {
                        System.out.println(line);
                    }
                }
            } while (true);
        } catch (Exception e) {
            err.println("Unexpected error: " + e.getMessage());
        }
    }

    protected static int get(final String[] args,
                             final int index,
                             final int defaultValue) {
        try {
            return Integer.parseInt(args[index]);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Override
    public void start(int port, int threads) {
        try {
            serverSocket = new DatagramSocket(port);
            executorService = Executors.newFixedThreadPool(threads);
            IntStream.range(0, threads).forEach(i -> executorService.execute(getRunnable()));
        } catch (SocketException e) {
            // No operations.
        }
    }

    private Runnable getRunnable() {
        return () -> {
            try {
                while (!serverSocket.isClosed()) {
                    byte[] requestedBytes = new byte[SIZE];
                    DatagramPacket request = new DatagramPacket(requestedBytes, SIZE);

                    try {
                        serverSocket.receive(request);
                    } catch (IOException e) {
                        // No operations.
                    }

                    byte[] respondedBytes = ("Hello, ".concat(getStringFromUDPPacket(request))).getBytes(UTF_8); // :NOTE: Utf8
                    serverSocket.send(
                            new DatagramPacket(
                                    respondedBytes,
                                    respondedBytes.length,
                                    request.getSocketAddress()
                            ));
                }
            } catch (IOException e) {
                err.println("Failed at creating runnable instance. Caused by: " + e.getMessage());
            }
        };
    }

    @Override
    public void close() {
        serverSocket.close();
        executorService.shutdown();
        closeExecutorService(executorService);
    }
}
