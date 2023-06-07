package info.kgeorgiy.ja.kim.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static java.lang.System.err;
import static java.lang.System.out;
import static java.nio.charset.StandardCharsets.UTF_8;

public class HelloUDPClient extends AbstractHelloUDP implements HelloClient {

    public static void main(final String... args) {
        checkOnLength(5, args);

        try {
            final String host = args[0];
            final String prefix = args[2];

            final int port = Integer.parseInt(args[1]);
            final int requests = Integer.parseInt(args[3]);
            final int threads = Integer.parseInt(args[4]);

            new HelloUDPClient().run(host, port, prefix, threads, requests);
        } catch (Exception e) {
            err.println("Unknown error: " + e.getMessage());
        }
    }

    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        try {
            InetSocketAddress address = new InetSocketAddress(InetAddress.getByName(host), port);
            ExecutorService executorService = Executors.newFixedThreadPool(threads);
            IntStream.range(0, threads)
                    .forEach(i -> executorService.execute(getRunnable(address, prefix, i + 1, requests)));
            executorService.shutdown();
            if (!executorService.awaitTermination(requests * 10L, TimeUnit.SECONDS)) {
                err.println("Error while terminating executorService.");
            }
        } catch (UnknownHostException e) {
            err.println("Bad host error: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            err.println("Error: InterruptedException, caused by " + e.getMessage());
        }
    }

    private Runnable getRunnable(InetSocketAddress ipAddress, String prefix, int number, int requests) {
        return () -> {
            try (DatagramSocket clientSocket = new DatagramSocket()) {
                byte[] respondedBytes = new byte[SIZE];
                DatagramPacket response = new DatagramPacket(respondedBytes, SIZE);

                try {
                    clientSocket.setSoTimeout(250);
                } catch (SocketException e) {
                    err.println("Failed at setting timeout: " + e.getMessage());
                }

                IntStream.range(0, requests)
                        .mapToObj(r -> String.format("%s%s_%s", prefix, number, r + 1))
                        .forEach(m -> {
                            byte[] requestedBytes = m.getBytes(UTF_8);

                            DatagramPacket request = new DatagramPacket(
                                    requestedBytes,
                                    m.length(),
                                    ipAddress);

                            while (!clientSocket.isClosed() && !Thread.interrupted()) {
                                try {
                                    clientSocket.send(request);
                                    clientSocket.receive(response);
                                    String message = getStringFromUDPPacket(response);
                                    if (message.contains(m)) {
                                        out.println(message);
                                        break;
                                    }
                                } catch (IOException e) {
                                    err.println("I/O error: " + e.getMessage());
                                }
                            }
                        });
            } catch (SocketException e) {
                err.println("Error: SocketException, caused by: " + e.getMessage());
            }
        };
    }
}
