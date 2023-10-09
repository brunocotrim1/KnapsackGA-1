package fcul.ppc.implementations;

import fcul.ppc.interfaces.KnapsackInterface;
import fcul.ppc.parallelization.ParallelFW;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

public class KnapsackFullyParallelSplitPop implements KnapsackInterface {
    private static final int N_GENERATIONS = 500;
    private static final int POP_SIZE = 100000;
    private static final double PROB_MUTATION = 0.5;
    private static final int TOURNAMENT_SIZE = 3;


    private Individual[] population = new Individual[POP_SIZE];

    public KnapsackFullyParallelSplitPop() {
        populateInitialPopulationRandomly();
    }

    private void populateInitialPopulationRandomly() {
        /* Creates a new population, made of random individuals in parallel */
        ParallelFW.doInParallel(((startIndex, endIndex) -> {
            for (int i = startIndex; i < endIndex; i++) {
                population[i] = Individual.createRandom(ThreadLocalRandom.current());
            }
        }), POP_SIZE);
    }

    public void run() {
        for (int generation = 0; generation < N_GENERATIONS; generation++) {

            //In this method I will split the population into smaller populations and split the whole process among threads
            Individual[] newPopulation = new Individual[POP_SIZE];
            AtomicReference<Individual> best = new AtomicReference<>(null);
            ParallelFW.doInParallel(((startIndex, endIndex) -> {
                // Step1 - Calculate Fitness
                for (int i = startIndex; i < endIndex; i++) {
                    population[i].measureFitness();
                }
                // Step2 - Print the best individual so far.
                Individual bestLocal = bestOfPopulation(startIndex,endIndex);
                synchronized (best) {
                    if (best.get() != null && best.get().fitness > bestLocal.fitness) {
                        bestLocal = best.get();
                    } else {
                        best.set(bestLocal);
                    }
                }
                // Step3 - Find parents to mate (cross-over)
                newPopulation[startIndex] = bestLocal; // The best individual remains
                ThreadLocalRandom r = ThreadLocalRandom.current();
                for (int i = startIndex; i < endIndex; i++) {
                    // We select two parents, using a tournament.
                    Individual parent1 = tournament(TOURNAMENT_SIZE, r,startIndex,endIndex);
                    Individual parent2 = tournament(TOURNAMENT_SIZE, r,startIndex,endIndex);

                    newPopulation[i] = parent1.crossoverWith(parent2, r);
                }

                // Step4 - Mutate
                for (int i = startIndex; i < endIndex; i++) {
                    if (r.nextDouble() < PROB_MUTATION) {
                        newPopulation[i].mutate(r);
                    }
                }
            }), POP_SIZE);
            System.out.println("Best at generation " + generation + " is " + best + " with "
                    + best.get().fitness);
            population = newPopulation;
        }
    }

    private Individual tournament(int tournamentSize, Random r,int startIndex,int endIndex) {
        /*
         * In each tournament, we select tournamentSize individuals at random, and we
         * keep the best of those.
         */
        Individual best = population[randomBetweenInterval(startIndex,endIndex,r)];
        for (int i = 0; i < tournamentSize; i++) {
            Individual other = population[randomBetweenInterval(startIndex,endIndex,r)];
            if (other.fitness > best.fitness) {
                best = other;
            }
        }
        return best;
    }

    private Individual bestOfPopulation(int startIndex,int endIndex) {
        /*
         * Returns the best individual of the population.
         */
        Individual best = population[startIndex];
        for(int i = 0;i<endIndex;i++){
            if(population[i].fitness>best.fitness){
                best= population[i];
            }
        }
        return best;
    }
    private int randomBetweenInterval(int min, int max,Random r) {
        return r.nextInt(min, max);
    }
}
