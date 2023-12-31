package fcul.ppc;

import fcul.ppc.implementations.*;
import fcul.ppc.interfaces.KnapsackInterface;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KnapsackMain {
    public static void main(String[] args) {
        boolean writeFiles = false;
        KnapsackGA ga = new KnapsackGA();
        KnapsackGAPartiallyParallel gaPartiallyParallel = new KnapsackGAPartiallyParallel();
        KnapsackFullyParallel knapsackFullyParallel = new KnapsackFullyParallel();
        KnapsackAlmostFullyParallel knapsackAlmostFullyParallel = new KnapsackAlmostFullyParallel();
        KnapsackFullyParallelSplitPop knapsackFullyParallelSplitPop = new KnapsackFullyParallelSplitPop();
        if(writeFiles) {
            timedKnapsackExecution(knapsackFullyParallelSplitPop);
            timedKnapsackExecution(knapsackFullyParallel);//Slower because we waster more time waiting for others in each generation then creating threads and proecess all at once separatelly
            timedKnapsackExecution(knapsackAlmostFullyParallel);
            timedKnapsackExecution(gaPartiallyParallel);
            timedKnapsackExecution(ga);
        }else {
            knapsackFullyParallelSplitPop.run();
            knapsackFullyParallel.run();
            knapsackAlmostFullyParallel.run();
            gaPartiallyParallel.run();
            ga.run();
        }

    }

    public static void timedKnapsackExecution(KnapsackInterface knapsack) {
        try {
            FileWriter fileWriter = new FileWriter(knapsack.getClass().getName().split("\\.")[3] + "_4C.csv");
            CSVPrinter csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT.withHeader("iteration", "time"));
            List<List<String>> data = new ArrayList<>();

            for (int i = 0; i < 30; i++) {
                System.out.println("Executing " + knapsack.getClass() + " run " + i);
                Instant start = Instant.now();
                knapsack.run();
                Instant finish = Instant.now();
                long timeElapsed = Duration.between(start, finish).toMillis();
                System.out.println("Time taken: " + (double)timeElapsed/1000 + " seconds");
                data.add(Arrays.asList(Integer.toString(i),Double.toString( (double) timeElapsed / 1000)));
            }
            for (List<String> rowData : data) {
                csvPrinter.printRecord(rowData);
            }
            // Close the CSVPrinter and FileWriter
            csvPrinter.close();
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
