package info.kgeorgiy.ja.kim.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ScalarIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;


/**
 * @author medvezhonok
 */
public class IterativeParallelism implements ScalarIP {
    private final ParallelMapper mapper;

    public IterativeParallelism(final ParallelMapper mapper) {
        this.mapper = mapper;
    }

    public IterativeParallelism() {
        this.mapper = null;
    }

    private <T, R> Stream<R> parallelism(int threads, List<? extends T> values,
                                         Function<Stream<? extends T>, R> consumer)
            throws InterruptedException {
        if (threads <= 0) {
            throw new InterruptedException("Invalid thread count: " + threads);
        }

        threads = Math.min(threads, values.size());

        List<List<? extends T>> parts = split(threads, values);
        final List<R> result = new ArrayList<>(Collections.nCopies(threads, null));
        final List<Thread> workers = new ArrayList<>();

        if (mapper == null) {
            IntStream.range(0, threads).forEach(i -> addWorker(consumer, workers, result, parts, i));
            joinAll(workers);
            return result.stream();
        } else {
            return mapper.map(list -> consumer.apply(list.stream()), parts).stream();
        }
    }

    public static void joinAll(List<Thread> workers) {
        workers.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public <T> List<List<? extends T>> split(final int n, final List<? extends T> values) {
        final List<List<? extends T>> result = new ArrayList<>(Collections.nCopies(n, null));

        int subListSize = values.size() / n, remainder = values.size() % n;
        int l = 0, r = subListSize;

        for (int i = 0; i < n; i++) {
            result.set(i, values.subList(l, r));
            l = r;
            r += subListSize;
            if (remainder > 0) {
                remainder -= 1;
                r += 1;
            }
        }

        return result;
    }

    private <T, R> void addWorker(Function<Stream<? extends T>, R> function,
                                  List<Thread> workers,
                                  List<R> result,
                                  List<List<? extends T>> parts,
                                  int index) {
        final Thread worker = new Thread(() -> result.set(index, function.apply(parts.get(index).stream())));
        worker.start();
        workers.add(worker);
    }

    @Override
    public <T> T maximum(int threads,
                         List<? extends T> values,
                         Comparator<? super T> comparator) throws InterruptedException {
        Function<Stream<? extends T>, T> maxi = stream -> stream.max(comparator).orElseThrow(AssertionError::new);
        return maxi.apply(parallelism(threads, values, maxi));
    }
    @Override
    public <T> T minimum(int threads,
                         List<? extends T> values,
                         Comparator<? super T> comparator) throws InterruptedException {
        Function<Stream<? extends T>, T> mini = stream -> stream.min(comparator).orElseThrow(AssertionError::new);
        return mini.apply(parallelism(threads, values, mini));
    }

    @Override
    public <T> boolean all(int threads,
                           List<? extends T> values,
                           Predicate<? super T> predicate) throws InterruptedException {
        return !any(threads, values, predicate.negate());
    }

    @Override
    public <T> boolean any(int threads,
                           List<? extends T> values,
                           Predicate<? super T> predicate) throws InterruptedException {
        Stream<Boolean> stream = parallelism(threads, values, s -> s.allMatch(Predicate.not(predicate)));
        return !stream.allMatch(Boolean::booleanValue);
    }

    @Override
    public <T> int count(int threads,
                         List<? extends T> values,
                         Predicate<? super T> predicate) throws InterruptedException {
        final Function<Stream<Integer>, Integer> sum = stream -> stream.reduce(0, Integer::sum);
        final Function<Stream<? extends T>, Integer> count = stream -> Math.toIntExact(stream.filter(predicate).count());

        return sum.apply(parallelism(threads, values, count));
    }
}

