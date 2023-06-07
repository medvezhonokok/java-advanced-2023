package info.kgeorgiy.ja.kim.hello;

import java.net.DatagramPacket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;

public abstract class AbstractHelloUDP {
    protected static final int SIZE = 4096;

    protected static void checkOnLength(final int size, final String... args) {
        if (args.length != size) {
            throw new IllegalArgumentException(
                    "Invalid arguments length. Excepted : %d, found: %d"
                            .formatted(size, args.length));
        }
    }

    protected String getStringFromUDPPacket(DatagramPacket packet) {
        return getStringFromUDPPacket(packet.getData(),
                packet.getOffset(),
                packet.getLength());
    }

    protected String getStringFromUDPPacket(byte[] data, int offset, int length) {
        return new String(data, offset, length, UTF_8);
    }

    protected void closeExecutorService(final ExecutorService service) {
        service.shutdown();
        try {
            service.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // No operations.
        }
    }
}

