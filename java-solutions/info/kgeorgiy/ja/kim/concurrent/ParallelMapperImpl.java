package info.kgeorgiy.ja.kim.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Implementation of the ParallelMapper interface that allows
 * parallel execution of tasks using a pool of worker threads.
 *
 * @author medvezhonok
 */
public class ParallelMapperImpl implements ParallelMapper {

    private final List<Thread> workers;
    private final Deque<Runnable> tasks;


    /**
     * Constructs the ParallelMapperImpl object with the specified number of threads.
     *
     * @param threads the number of worker threads to use
     */
    public ParallelMapperImpl(final int threads) {
        workers = new ArrayList<>(threads);
        tasks = new ArrayDeque<>();

        IntStream.range(0, threads).forEach(i -> {
            Thread t = new Thread(() -> {
                try {
                    while (!Thread.interrupted()) {
                        Runnable task;
                        synchronized (tasks) {
                            while (tasks.isEmpty()) {
                                tasks.wait(); // Пассивное ожидание
                            }
                            task = tasks.poll();
                        }
                        task.run();
                    }
                } catch (InterruptedException ignored) {
                    // No operations.
                } finally {
                    Thread.currentThread().interrupt();
                }
            });
            workers.add(t);
        });

        workers.forEach(Thread::start);
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        AtomicInteger counter = new AtomicInteger(args.size());
        List<R> result = IntStream.range(0, args.size()).<R>mapToObj(i -> null).collect(Collectors.toList());

        for (int i = 0; i < args.size(); i++) {
            T arg = args.get(i);
            synchronized (tasks) {
                tasks.add(getRunnable(f, result, arg, counter, i));
                tasks.notify();
            }
        }

        synchronized (result) {
            while (counter.get() != 0) {
                result.wait();
            }
        }

        return result;
    }

    /**
     * Returns a {@code Runnable} that applies the provided {@code Function} to the given argument and stores the result
     * in the specified index of the given {@code List} of results. Also decrements the provided {@code Counter} and
     * notifies any waiting threads.
     *
     * @param f      the {@code Function} to apply to the given argument
     * @param result the {@code List} of results to store the computed value in
     * @param arg    the argument to apply the {@code Function} to
     * @param t      the {@code Counter} to decrement
     * @param i      the index in the {@code List} to store the result in
     * @param <T>    the type of the input to the {@code Function}
     * @param <R>    the type of the output of the {@code Function}
     * @return a {@code Runnable} that applies the provided {@code Function} to the given argument and stores the result
     * in the specified index of the given {@code List} of results. Also decrements the provided {@code Timer} and
     * notifies any waiting threads
     */
    private <T, R> Runnable getRunnable(Function<? super T, ? extends R> f, List<R> result, T arg, AtomicInteger t, int i) {
        return () -> {
            try {
                R res = f.apply(arg);
                synchronized (result) {
                    result.set(i, res);
                    t.decrementAndGet();
                    result.notifyAll();
                }
            } catch (RuntimeException e) {
                // ?
            }
        };
    }

    @Override
    public void close() {
        workers.forEach(Thread::interrupt);
        workers.forEach(w -> {
            while (true) {
                try {
                    w.join();
                    break;
                } catch (InterruptedException ignored) {
                    // No operations.
                }
            }
        });
    }
}