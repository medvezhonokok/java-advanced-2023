package info.kgeorgiy.ja.kim.walk;

public class Walk extends RecursiveWalk {
    public static void main(String[] args) {
        if (invalidArguments(args)) {
            return;
        }
        walk(args[0], args[1], false);
    }
}
