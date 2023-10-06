package fcul.ppc.implementations;

import fcul.ppc.interfaces.KnapsackInterface;
import fcul.ppc.parallelization.ParallelFW;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * In this implementation only the most computational expensive methods of the algorithm will be parallelized
 * this aims to test if the cost of using threads on the most simple iterative algorithms reduces the performance
 * of the execution
 */
public class KnapsackGAPartiallyParallel implements KnapsackInterface {
    private static final int N_GENERATIONS = 500;
    private static final int POP_SIZE = 100000;
    private static final double PROB_MUTATION = 0.5;
    private static final int TOURNAMENT_SIZE = 3;
    private Individual[] population = new Individual[POP_SIZE];

    public KnapsackGAPartiallyParallel() {
        populateInitialPopulationRandomly();
    }

    private void populateInitialPopulationRandomly() {
        /* Creates a new population, made of random individuals */
        ParallelFW.doInParallel(((startIndex, endIndex) -> {
            for (int i = startIndex; i < endIndex; i++) {
                population[i] = Individual.createRandom(ThreadLocalRandom.current());
            }
        }), POP_SIZE);
    }
    public void run() {
        for (int generation = 0; generation < N_GENERATIONS; generation++) {

            // Step1 - Calculate Fitness
            ParallelMeasureFitness();

            // Step2 - Print the best individual so far.
            Individual best = bestOfPopulation();
           // Individual best = ParallelBestOfPopulation();
            System.out.println("Best at generation " + generation + " is " + best + " with "
                    + best.fitness);
            // Step3 - Find parents to mate (cross-over)
            Individual[] newPopulation = new Individual[POP_SIZE];
            newPopulation[0] = best; // The best individual remains
            ParallelMate(newPopulation);
            // Step4 - Mutate
            ParallelMutate(newPopulation);
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

    private void ParallelMate(Individual[] newPopulation) {
        //Even-tough multiple parents might be selected at the same time, since they will not be modified, and only
        //their genes will be selected("read") in the crossover, no inconsistencies in the reads might occur since
        //there are no writes, and different indexes(across threads) in the new population will
        //have new unique individuals
        ThreadLocalRandom r = ThreadLocalRandom.current();
        ParallelFW.doInParallel(((startIndex, endIndex) -> {
            for (int i = startIndex; i < endIndex; i++) {
                Individual parent1 = tournament(TOURNAMENT_SIZE, r);
                Individual parent2 = tournament(TOURNAMENT_SIZE, r);

                newPopulation[i] = parent1.crossoverWith(parent2, r);
            }
        }), POP_SIZE);
    }

    private void ParallelMutate(Individual[] newPopulation) {
        //Since Different Individuals are being accessed in Parallel, there will be no accesses to the same individual
        //so there is no need to waste time locking the individuals
        ParallelFW.doInParallel(((startIndex, endIndex) -> {
            for (int i = startIndex; i < endIndex; i++) {
                if (ThreadLocalRandom.current().nextDouble() < PROB_MUTATION) {
                    newPopulation[i].mutate(ThreadLocalRandom.current());
                }
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
