package info.kgeorgiy.ja.kim.hello;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Task {
    private final int CAPACITY = 1 << 8;
    private SocketAddress socketAddress;
    private ByteBuffer byteBuffer = ByteBuffer.allocate(CAPACITY);

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int index = 0;

    public int getCurRequest() {
        return curRequest;
    }

    public void setCurRequest(int curRequest) {
        this.curRequest = curRequest;
    }

    public int curRequest = 0;

    public Task() {
    }

    public Task(int id) {
        this.index = id;
    }

    protected void setByteBuffer(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }

    protected void setSocketAddress(SocketAddress socketAddress) {
        this.socketAddress = socketAddress;
    }

    public SocketAddress getSocketAddress() {
        return this.socketAddress;
    }

    public ByteBuffer getByteBuffer() {
        return this.byteBuffer;
    }

    protected ByteBuffer flip() {
        return this.byteBuffer.flip();
    }

    public void configure() {
        final String message = "Hello, ".concat(String.valueOf(UTF_8.decode(flip())));
        setByteBuffer(getByteBuffer().clear().put(message.getBytes(UTF_8)).flip());
    }
}
