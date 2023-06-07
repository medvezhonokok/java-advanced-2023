package info.kgeorgiy.ja.kim.arrayset;

import java.util.*;

@SuppressWarnings("unused")
public class ArraySet<T> extends AbstractSet<T> implements SortedSet<T> {
    private final List<T> source;
    private final Comparator<T> c;

    public ArraySet(final List<T> source, final Comparator<T> comparator) {
        this.source = source;
        this.c = comparator;
    }

    /**
     * A constructor with one argument of type Collection, and one argument of type Comparator,
     * which creates a new sorted set with the same elements as first argument,
     * sorted  according to the specified comparator.
     */
    public ArraySet(final Collection<T> collection, final Comparator<T> comparator) {
        final TreeSet<T> treeSet = new TreeSet<>(comparator);

        if (collection != null) {
            treeSet.addAll(collection);
        }

        this.source = List.copyOf(treeSet);
        this.c = comparator;
    }

    /**
     * A void (no arguments) constructor,
     * which creates an empty sorted set
     * sorted according to the natural ordering of its elements.
     */
    public ArraySet() {
        this(Collections.emptyList(), null);
    }

    /**
     * A constructor with a single argument of type Comparator,
     * which creates an empty sorted set sorted according to the specified comparator.
     */
    public ArraySet(final Comparator<T> comparator) {
        this(Collections.emptyList(), comparator);
    }

    /**
     * A constructor with a single argument of type Collection,
     * which creates a new sorted set with the same elements as its argument,
     * sorted according to the natural ordering of the elements.
     */
    public ArraySet(final Collection<T> collection) {
        this(collection, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object o) {
        return binarySearch((T) o) > -1;
    }

    @Override
    public Iterator<T> iterator() {
        return source.iterator();
    }

    @Override
    public int size() {
        return source.size();
    }

    @Override
    public Comparator<T> comparator() {
        return c;
    }

    /**
     * @param fromElement low endpoint (inclusive) of the returned set
     * @param toElement   high endpoint (exclusive) of the returned set
     * @return the ArraySet, which all elements are greater or equals fromElement, and lower than toElement
     */
    @Override
    public SortedSet<T> subSet(final T fromElement, final T toElement) {
        requireNonNulls(fromElement, toElement);

        final int indexFrom = binarySearchImpl(fromElement);
        final int indexTo = binarySearchImpl(toElement);

        return subArraySet(indexFrom, indexTo);
    }

    /**
     * @param toElement high endpoint (exclusive) of the returned set
     * @return the ArraySet, which all elements are lower than toElement
     */
    @Override
    public SortedSet<T> headSet(final T toElement) {
        final int indexTo = binarySearchImpl(toElement);
        return subArraySet(0, indexTo);
    }

    /**
     * @param fromElement low endpoint (inclusive) of the returned set
     * @return the ArraySet, which all elements are greater than fromElement or equals fromElement
     */
    @Override
    public SortedSet<T> tailSet(final T fromElement) {
        final int indexFrom = binarySearchImpl(fromElement);
        return subArraySet(indexFrom, source.size());
    }

    /**
     * @return the first element in ArraySet
     * @throws NoSuchElementException in case, if ArraySet is empty.
     */
    @Override
    public T first() {
        return get(0);
    }

    /**
     * @return the last element in ArraySet
     * @throws NoSuchElementException in case, if ArraySet is empty
     */
    @Override
    public T last() {
        return get(size() - 1);
    }

    private T get(final int index) {
        if (isEmpty()) {
            error("ArraySet is empty", "size == 0");
            throw new NoSuchElementException();
        } else if (index < 0 || index >= size()) {
            error(
                    "Incorrect value of index",
                    "excepted: 0 <= index <= " + (size() - 1) + "\n\tfound: index == " + index
            );
            throw new IndexOutOfBoundsException();
        } else {
            return source.get(index);
        }
    }

    public int binarySearch(final T key) {
        requireNonNull(key);
        return Collections.binarySearch(source, key, c);
    }

    public void requireNonNull(final T argument) {
        if (argument == null) {
            error("'null' arguments are not allowed", "your argument is 'null'");
            throw new IllegalArgumentException();
        }
    }

    public void requireNonNulls(final T fromElement, final T toElement) {
        if (fromElement == null || toElement == null) {
            error(
                    "null arguments are not allowed",
                    "your arguments are: [" + fromElement + ", " + toElement + "]"
            );
            throw new IllegalArgumentException();
        }

        if (compare(fromElement, toElement) > 0) {
            error(
                    "Invalid arguments",
                    fromElement + " is greater than " + toElement + ", where comparator is " + null
            );
            throw new IllegalArgumentException();
        }
    }

    @SuppressWarnings("unchecked")
    private int compare(final T o1, final T o2) {
        if (c == null) {
            return ((Comparable<T>) o1).compareTo(o2);
        } else {
            return c.compare(o1, o2);
        }
    }

    private ArraySet<T> subArraySet(final int from, final int to) {
        return new ArraySet<>(source.subList(from, to), c);
    }

    private int binarySearchImpl(final T key) {
        final int index = binarySearch(key);
        return index < 0 ? -1 - index : index;
    }

    public static <R> void error(final String message, final R reason) {
        System.err.printf(
                """
                         Error: %s.
                            Caused by: %s.
                                     
                        """, message, reason.toString());
    }
}

