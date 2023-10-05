package fcul.ppc.parallelization;

public class ParallelFW {
    public static void doInParallel(Parallelizable pw, int iterations) {
        int nThreads = Math.min(iterations, Runtime.getRuntime().availableProcessors());
        doInParallel(pw, iterations, nThreads);
    }

    public static void doInParallel(Parallelizable pw, int iterations, int nThreads) {
        Thread[] threads = new Thread[(int) nThreads];
        for (int i = 0; i < nThreads; i++) {
            int finalI = i;
            threads[i] = new Thread(() -> {
                int start = finalI * iterations / nThreads;
                int end = (finalI + 1) * iterations / nThreads;
                //   System.out.println("Thread " + finalI + " Interval " + start + " " + end);
                if (finalI == nThreads - 1) {
                    end = iterations;
                }
                pw.run(start, end);
            });

            threads[i].start();
        }
        for (int i = 0; i < nThreads; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
