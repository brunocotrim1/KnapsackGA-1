## Knapsack GA

This repository is organized in three classes:

* knapsack.Main: This is the entrypoint to run the program. Your several versions of the program should be located here.
* knapsack.Individual: This represents each solution of the Knapsack problem, which can be mutated and crossed over with other solutions. *You are not supposed to change this file*.
* knapsack.KnapsackGA: This class is the sequential algorithm: it generates an initial population of 100000 random solutions/individuals. Then, through 500 generations, it selects the best ones, and crosses them to fill the population of the next generation. Then, there is a change the new individuals will be mutated. And the process repeats until all intended generations are completed.


### More information:

* [Knapsack Problem](https://en.wikipedia.org/wiki/Knapsack_problem)
* [How Genetic Algorithms work](https://saturncloud.io/blog/what-are-genetic-algorithms-and-how-do-they-work/)
* [ThreadLocalRandom](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ThreadLocalRandom.html)

Notes:
https://www.ques10.com/p/69237/what-is-parallelizing-genetic-algorithms-explain-1/

Implement the above approach and see how it impacts performance
