import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import lombok.Getter;
import lombok.Setter;


public class GeneticAlgorithm {
    private int populationSize, binCapacity;
    private List<Individual> population;
    private List<Integer> packages;
    private int optimalBins= -1;

    /**
     *
     * @param populationSize
     * @param binCapacity
     * @param packageSize
     * @param arr
     * @param optimalBins
     */

    public GeneticAlgorithm(int populationSize, int binCapacity, int packageSize, Integer[] arr ,int optimalBins) {
        this.populationSize = populationSize;
        this.binCapacity = binCapacity;
        this.optimalBins = optimalBins;
        initPackages(packageSize ,arr);
        initPop(populationSize, binCapacity, packageSize);

    }


    public void runGA(UTILS selectionType,UTILS crossOverType, UTILS mutationType, double mutationRate) {

        int generation = 0;
        while(true){
            if(generation>5000){
                System.out.println("No solution found");
                return;
            }
            evaluateFitness();
            population.sort(Comparator.comparingInt(Individual::getFitness)
                    .thenComparingInt(Individual::getRemainingCapacity));
            System.out.println("Best fitness in generation: " + generation + "\t Fitness: " + population.getFirst().getFitness());


           if(population.getFirst().getFitness()<=optimalBins){
               System.out.println(population.getFirst().getBins().toString());
               return;
           }
            initCrossover(selectionType,crossOverType);
            mutate(mutationType, mutationRate);
            generation++;
        }
    }

    /*
     * ************************************************************
     * ***********************************************************
     * ****************** INITIALIZATION ***********************
     * ***********************************************************
     * ************************************************************
     */

    /**
     * @param populationSize
     * @param binCapacity
     * @param packageSize
     */
    private void initPop(int populationSize, int binCapacity, int packageSize) {
        this.populationSize = populationSize;
        this.binCapacity = binCapacity;
        this.population = new ArrayList<>();


        for (int i = 0; i < populationSize; i++) {
            List<Integer> n = new ArrayList<>();
            for (int j = 0; j < packageSize; j++) {
                n.add(j);
            }
            Collections.shuffle(n, new Random());
            population.add(new Individual(n));
        }
    }

    /**
     * @param packageSize
     */
    private void initPackages(int packageSize, Integer[] arr) {
        packages = new ArrayList<>();

        if(arr == null || arr.length == 0){
            int last =binCapacity-1;
            for(int i=1;i<=binCapacity/2;i++) {
                packages.add(last);
                packages.add(i);
                last--;
            }
        }else{
            packageSize = arr.length;
            packages.addAll(Arrays.asList(arr));
        }

        while(packages.size()<packageSize){
            List<Integer> n = new ArrayList<>(packages);
            packages.addAll(n);
        }

        if (packages.size() > packageSize) {
            packages = packages.subList(0, packageSize);
        }

        Collections.shuffle(packages, new Random());
        System.out.print("Integer[] packages = {");
        for (int i = 0; i < packages.size(); i++) {
            System.out.print(packages.get(i));
            if (i != packages.size() - 1) System.out.print(", ");
        }
        System.out.println("};");
    }




    /*
     * ************************************************************
     * ***********************************************************
     *   ******************FITNESS METHODS***********************
     * ***********************************************************
     * ************************************************************
     */

    public void evaluateFitness() {

        evaluateBestFit();
    }

    private List<Integer> nextFit() {
        List<Integer> binList = new ArrayList<>();
        int remainingCapacity;
        for (int i = 0; i < population.size(); i++) {
            remainingCapacity = binCapacity;
            int binCount = 1;
            for (Integer j : population.get(i).chromosome) {
                int packSize = packages.get(j);
                System.out.print(j + " - " + packSize + " | ");
                if (packSize > remainingCapacity) {
                    binCount++;
                    remainingCapacity = binCapacity;
                }
                remainingCapacity -= packSize;
            }
            System.out.println();
            binList.add(binCount);
        }
        return binList;
    }




    private void evaluateBestFit() {
        for (Individual individual : population) {
            List<List<Integer>> bins = new ArrayList<>();
            List<Integer> cap = new ArrayList<>();

            for (Integer j : individual.chromosome) {
                int pack = packages.get(j);

                int bestIdx = -1;
                int minRemaining = Integer.MAX_VALUE;
                for (int k = 0; k < cap.size(); k++) {
                    int remaining = cap.get(k) - pack;
                    if (remaining >= 0 && remaining < minRemaining) {
                        bestIdx = k;
                        minRemaining = remaining;
                    }
                }

                if (bestIdx >= 0) {
                    bins.get(bestIdx).add(pack);
                    cap.set(bestIdx, (cap.get(bestIdx) - pack));
                } else {
                    List<Integer> newBin = new ArrayList<>();
                    newBin.add(pack);
                    bins.add(newBin);
                    cap.add(binCapacity - pack);
                }
            }

            // Calculate total remaining capacity
            int totalRemainingCapacity = cap.stream().mapToInt(Integer::intValue).sum();

            individual.setRemainingCapacity(totalRemainingCapacity);
            individual.setBins(bins);
            individual.setFitness(bins.size());
        }
    }



    /*
     * ************************************************************
     * ***********************************************************
     *  ******************SELECTION METHODS**********************
     * ***********************************************************
     * ************************************************************
     */

    private Individual tournamentSelection(double k) {
        int tournamentSize = (int) (population.size()* k);
        List<Individual> tournament = new ArrayList<>();
        for (int i = 0; i < tournamentSize; i++) {
            int rand  = ThreadLocalRandom.current().nextInt(0, population.size());
            tournament.add(population.get(rand));
        }

        Individual best = tournament.getFirst();
        for (int i =1; i < tournamentSize; i++) {
            Individual ind = tournament.get(i);
            if (ind.getRemainingCapacity() < best.getRemainingCapacity()) {
                best = ind;
            }
            else if (ind.getRemainingCapacity() == best.getRemainingCapacity() && ind.getFitness() < best.getFitness()) {
                best = ind;
            }
        }

        return best;
    }


    private Individual rouletteSelection() {
        List<Double> adjustedFitness = new ArrayList<>();
        List<Double> cumulativeFitness = new ArrayList<>();


        double totalFitness = population.stream().mapToDouble(Individual::getRemainingCapacity).sum();
        double totalAdjustedFitness = 0;
        for (Individual individual : population) {

            double fit = totalFitness / individual.getRemainingCapacity();

            adjustedFitness.add(fit);
            totalAdjustedFitness += fit;
        }

        double totalCumulativeFitness = 0;
        for (Double fitness : adjustedFitness) {
            double prob = fitness / totalAdjustedFitness;
            totalCumulativeFitness += prob;
            cumulativeFitness.add(totalCumulativeFitness);
        }


        double r = Math.random();
        Individual best = null;
        for(int i=0; i<cumulativeFitness.size(); i++){
            if(r <= cumulativeFitness.get(i)){
                best  = population.get(i);
                break;
            }
        }
        if(best == null) return population.getLast();
        return best;
    }

    /*
     * ************************************************************
     * ***********************************************************
     *  ******************CROSSOVER METHODS**********************
     * ***********************************************************
     * ************************************************************
     */

    /**
     * @param crossoverType
     */

    private void initCrossover(UTILS selectionType, UTILS crossoverType) {
        List<Individual> newPopulation = new ArrayList<>();
        int elitismSize = (int) (population.size() * .01);

        for (int i = 0; i < elitismSize; i++) {
            newPopulation.add(population.get(i));
        }
        newPopulation.add(population.getFirst());

        while (newPopulation.size()< populationSize-1){
            Individual parent1 = null;
            Individual parent2 = null;

            if(selectionType == UTILS.ROULETTE_SELECTION) {
                parent1 = rouletteSelection();
                parent2 = rouletteSelection();
            }else if(selectionType == UTILS.TOURNAMENT_SELECTION){
                parent1 = tournamentSelection(.75);
                parent2 = tournamentSelection(.75);
            }else throw new RuntimeException("Unsupported selection type");


            if(crossoverType == UTILS.ORDER1_CROSSOVER)
                Order1(parent1.chromosome, parent2.chromosome,newPopulation);
            else if(crossoverType == UTILS.CUT_N_CROSS_FILL_CROSSOVER)
                cutAndCrossFill(parent1.chromosome, parent2.chromosome,newPopulation);
            else throw new IllegalArgumentException("Unsupported Crossover type");
        }

        if (newPopulation.size() < population.size()) {
            newPopulation.add(population.getLast());
        }
        population = newPopulation;
    }

    /**
     * @param parent1
     * @param parent2
     * @param newPopulation
     */
    
    private void cutAndCrossFill(List<Integer> parent1, List<Integer> parent2, List<Individual> newPopulation) {
        int size = parent1.size();

        int crossoverPoint = ThreadLocalRandom.current().nextInt(1,size-1);
        List<Integer> child1 = new ArrayList<>(parent1.subList(0, crossoverPoint));
        List<Integer> child2 = new ArrayList<>(parent2.subList(0, crossoverPoint));


        for (int allele : parent2) if (!child1.contains(allele)) child1.add(allele);
        for (int allele : parent1) if (!child2.contains(allele)) child2.add(allele);

        newPopulation.add(new Individual(child1));
        newPopulation.add(new Individual(child2));
    }


    /**
     * @param parent1
     * @param parent2
     * @param newPopulation
     */

    private void Order1(List<Integer> parent1, List<Integer> parent2, List<Individual> newPopulation) {
        int size = parent1.size();
        int left = ThreadLocalRandom.current().nextInt(0, (size -1));
        int right = ThreadLocalRandom.current().nextInt(left + 1, size );

        List<Integer> child1 = new ArrayList<>(Collections.nCopies(size, -1));
        List<Integer> child2 = new ArrayList<>(Collections.nCopies(size, -1));

        for (int i = left; i < right; i++) {
            child1.set(i, parent2.get(i));
            child2.set(i, parent1.get(i));
        }
        fillRemainingOrder1(child1, parent1, right);
        fillRemainingOrder1(child2, parent2, left);
        newPopulation.add(new Individual(child1));
        newPopulation.add(new Individual(child2));

    }

    /**
     * @param child
     * @param parent
     * @param start
     */
    private void fillRemainingOrder1(List<Integer> child, List<Integer> parent, int start) {
        int size = parent.size();
        int idx = start % size;

        for (int i = 0; i < size; i++) {
            int allele = parent.get((start + i) % size);
            if (!child.contains(allele)) {
                while (child.get(idx) != -1) {
                    idx = (idx + 1) % size;
                }
                child.set(idx, allele);
            }
        }
    }

    /*
     * ************************************************************
     * **********************************************************
     *  *****************MUTATION METHODS***********************
     * ***********************************************************
     * ************************************************************
     */

    /**
     * @param MutationType
     * @param mutationRate
     */
    private void mutate(UTILS MutationType, double mutationRate) {
        for (Individual individual : population) {
            if (Math.random() < mutationRate) {
                if (MutationType == UTILS.PAIRWISE_MUTATION)
                    mutatePairWise(individual.chromosome);
                else if (MutationType == UTILS.SINGLE_MOVE_MUTATION)
                    mutateSingleMove(individual.chromosome);
                else throw new IllegalArgumentException("Unsupported MutationType");
            }
        }
    }

    /**
     * @param individual
     */
    private void mutatePairWise(List<Integer> individual) {
        int left = ThreadLocalRandom.current().nextInt(0, individual.size()-1);
        int right = ThreadLocalRandom.current().nextInt(left + 1, individual.size());

        Integer tmp = individual.get(left);
        individual.set(left, individual.get(right));
        individual.set(right, tmp);
    }

    /**
     * @param individual
     */
    private void mutateSingleMove(List<Integer> individual) {
        int removePoint = ThreadLocalRandom.current().nextInt(0, individual.size());
        int insertPoint;

        do {
            insertPoint = ThreadLocalRandom.current().nextInt(0, individual.size());
        } while (insertPoint == removePoint);

        int tmp = individual.remove((int) removePoint);
        if (removePoint < insertPoint) insertPoint--;
        individual.add(insertPoint, tmp);
    }

    /*
     * ************************************************************
     * ***********************************************************
     * ******************PRINTING METHODS***********************
     * ***********************************************************
     * ************************************************************
     */

    public void printPackages() {
        for (Integer i : packages) {
            System.out.println(i);
        }
    }

    public void printPopulation() {
        System.out.println("Chromosome\t\tFitness");
        for (Individual individual : population) {
            System.out.print(individual.getChromosome() + "\t");
            System.out.println(individual.getFitness());
        }
    }


    /*
     * ************************************************************
     * ***********************************************************
     *      ****************** CLASSES ***********************
     * ***********************************************************
     * ************************************************************
     */


    @Getter
    @Setter
    private static class Individual {
        private List<Integer> chromosome;
        private List<List<Integer>> bins;
        //private List<Integer> binsCapacity;
        private int remainingCapacity;
        private int fitness;

        public Individual(List<Integer> chromosome) {
            this.chromosome = chromosome;
        }

        public Individual(List<Integer> chromosome, int fitness) {
            this.chromosome = chromosome;
            this.fitness = fitness;
        }
    }


}
