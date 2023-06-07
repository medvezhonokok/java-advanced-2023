package info.kgeorgiy.ja.kim.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;


public class HelloUDPNonBlockingClient extends HelloUDPNonblockingServer implements HelloClient {


    @Override
    public void run(final String host, final int port, final String prefix, final int threads, final int requests) {
        List<Channel> list = new ArrayList<>();

        try {
            final Selector selector = Selector.open();
            InetSocketAddress address = new InetSocketAddress(InetAddress.getByName(host), port);

            IntStream.range(0, threads).forEach(i -> {
                try {
                    list.add(getChannel(address, selector, i));
                } catch (IOException e) {
                    // No operations.
                }
            });


            while (true) {
                selector.select(250);
                if (selector.selectedKeys().isEmpty()) {
                    selector.keys().forEach(k -> k.interestOps(SelectionKey.OP_WRITE));
                }
                for (final Iterator<SelectionKey> it = selector.selectedKeys().iterator(); it.hasNext(); ) {
                    final var key = it.next();

                    DatagramChannel channel = (DatagramChannel) key.channel();
                    Task t = (Task) key.attachment();
                    ByteBuffer buffer = ByteBuffer.allocate(2048);
                    if (key.isReadable()) {
                        buffer.flip();
                        channel.receive(buffer);
                        String ans = new String(buffer.array(), StandardCharsets.UTF_8);
                        System.out.println(ans);
                    } else if (key.isWritable()) {
                        buffer.clear();
                        buffer.put((prefix + (t.getIndex() + 1) + "_" + (t.getCurRequest() + 1)).getBytes(StandardCharsets.UTF_8));
                        buffer.flip();
                        channel.write(buffer);
                        key.interestOps(SelectionKey.OP_WRITE);
                    }

                    it.remove();
                    if (!channel.isOpen()) {
                        key.cancel();
                        if (selector.keys().isEmpty()) {
                            break;
                        }
                    }
                }
            }
        } catch (final IOException ignored) {
            // No operations.
        }
    }
}