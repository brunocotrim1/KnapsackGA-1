package fcul.ppc.implementations;

import fcul.ppc.interfaces.KnapsackInterface;
import fcul.ppc.parallelization.ParallelFW;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class KnapsackFullyParallel implements KnapsackInterface {
    private static final int N_GENERATIONS = 500;
    private static final int POP_SIZE = 100000;
    private static final double PROB_MUTATION = 0.5;
    private static final int TOURNAMENT_SIZE = 3;

    private Individual[] population = new Individual[POP_SIZE];

    public KnapsackFullyParallel() {
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

            // Step1 - Calculate Fitness
            //This will be the only one made in parallel since we need calculate fitness before advancing the algorithm
            //for this we shall wait for all threads to end their execution(done with the primitives learned at the moment)
            // in order to find the best across the whole
            //population,
            ParallelMeasureFitness();

            //Now I will make the following section running in a single Parallel Task in order to not waste time creating
            //and destroying threads and see how much that impacts performance
            AtomicReference<Individual> best = new AtomicReference<>(null);
            int finalGeneration = generation;
            Individual[] newPopulation = new Individual[POP_SIZE];
            AtomicInteger waiter = new AtomicInteger(0);
            ParallelFW.doInParallel(((startIndex, endIndex) -> {
                for (int i = startIndex; i < endIndex; i++) {
                    population[i].measureFitness();
                }
                synchronized (waiter) {
                    try {
                        //This block aims to make every thread wait for the last one so there are no concurrent accesses
                        //to the population when finding the best and running the other steps of the algorithm
                        if (waiter.addAndGet(1) == Runtime.getRuntime().availableProcessors()) {
                            waiter.notifyAll();
                        } else {
                            waiter.wait();
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                synchronized (best) {
                    //This way we set a lock at a Individual Object so that we only run this algorithm once and isolated
                    //this prevents unnecessary runs from other threads
                    if (best.get() == null) {
                        best.set(bestOfPopulation());
                        // Step2 - Print the best individual so far.
                        System.out.println("Best at generation " + finalGeneration + " is " + best + " with " + best.get().fitness);
                    }
                }
                newPopulation[0] = best.get(); // The best individual remains
                ThreadLocalRandom r = ThreadLocalRandom.current();
                for (int i = startIndex; i < endIndex; i++) {
                    // We select two parents, using a tournament.
                    Individual parent1 = tournament(TOURNAMENT_SIZE, r);
                    Individual parent2 = tournament(TOURNAMENT_SIZE, r);
                    newPopulation[i] = parent1.crossoverWith(parent2, r);
                }
                // Step4 - Mutate
                for (int i = startIndex; i < endIndex; i++) {
                    if (r.nextDouble() < PROB_MUTATION) {
                        newPopulation[i].mutate(r);
                    }
                }


            }), POP_SIZE);
            population = newPopulation;
        }
    }

    private void ParallelMeasureFitness() {
        ParallelFW.doInParallel(((startIndex, endIndex) -> {
            for (int i = startIndex; i < endIndex; i++) {
                population[i].measureFitness();
            }
        }), POP_SIZE);
    }

    private Individual tournament(int tournamentSize, Random r) {
        /*
         * In each tournament, we select tournamentSize individuals at random, and we
         * keep the best of those.
         */
        Individual best = population[r.nextInt(POP_SIZE)];
        for (int i = 0; i < tournamentSize; i++) {
            Individual other = population[r.nextInt(POP_SIZE)];
            if (other.fitness > best.fitness) {
                best = other;
            }
        }
        return best;
    }

    private Individual bestOfPopulation() {
        /*
         * Returns the best individual of the population.
         */
        Individual best = population[0];
        for (Individual other : population) {
            if (other.fitness > best.fitness) {
                best = other;
            }
        }
        return best;
    }
}
