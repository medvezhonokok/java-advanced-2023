package info.kgeorgiy.ja.kim.walk;


public class RecursiveWalk extends WalkService {
    public static void main(final String... args) {
        if (invalidArguments(args)) {
            return;
        }
        walk(args[0], args[1], true);
    }
}
