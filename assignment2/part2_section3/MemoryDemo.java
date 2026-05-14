import java.lang.ref.WeakReference;

public class MemoryDemo {

    private static void printHeap(String tag) {
        Runtime rt = Runtime.getRuntime();
        long usedMB  = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
        long totalMB =  rt.totalMemory() / (1024 * 1024);
        System.out.printf("[%-15s] used = %4d MB, jvm total = %4d MB%n",
                          tag, usedMB, totalMB);
    }

    public static void main(String[] args) throws Exception {
        printHeap("start");

        long[] numbers = new long[1_000_000];
        for (int i = 0; i < numbers.length; i++) numbers[i] = i + 1;
        printHeap("after alloc");

        long sum = 0;
        for (long n : numbers) sum += n;
        System.out.println("Sum = " + sum);

        WeakReference<long[]> weak = new WeakReference<>(numbers);
        System.out.println("Before clearing strong ref: weak.get() != null -> "
                           + (weak.get() != null));

        numbers = null;
        System.gc();
        Thread.sleep(200);
        printHeap("after gc");

        System.out.println("After clearing strong ref:  weak.get() != null -> "
                           + (weak.get() != null));
    }
}
