package fcul.ppc.parallelization;

@FunctionalInterface
public interface Parallelizable {
    public void run(int startIndex, int endIndex);
}
