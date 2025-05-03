import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class SimulatedAnnealing {
    private List<Integer> packages;
    private int binCapacity;
    private UTILS mutationStrategy;
    private double initialTemp;
    private double coolingRate;
    private int maxIterations;





    public SimulatedAnnealing(int packageSize, int binCapacity, double initialTemp,
                              double coolingRate, int maxIterations, UTILS mutationStrategy) {
        this.binCapacity = binCapacity;
        this.initialTemp = initialTemp;
        this.coolingRate = coolingRate;
        this.maxIterations = maxIterations;
        this.mutationStrategy = mutationStrategy;
        initPackages(packageSize, null);
    }



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

        //Collections.shuffle(packages, new Random());
//        System.out.print("Integer[] packages = {");
//        for (int i = 0; i < packages.size(); i++) {
//            System.out.print(packages.get(i));
//            if (i != packages.size() - 1) System.out.print(", ");
//        }
//        System.out.println("};");
    }

    public Individual run() {
        List<Integer> currentSolution = new ArrayList<>();
        for (int i = 0; i < packages.size(); i++) currentSolution.add(i);
        Collections.shuffle(currentSolution);

        Individual current = new Individual(new ArrayList<>(currentSolution));
        evaluateBestFit(current);
        Individual best = new Individual(new ArrayList<>(currentSolution));
        best.setFitness(current.getFitness());
        best.setRemainingCapacity(current.getRemainingCapacity());
        best.setBins(current.getBins());

        double temp = initialTemp;
        for (int i = 0; i < maxIterations; i++) {
            List<Integer> neighbor = new ArrayList<>(current.getChromosome());
            mutate(neighbor);
            Individual neighborInd = new Individual(neighbor);
            evaluateBestFit(neighborInd);

            int delta = neighborInd.getFitness() - current.getFitness();
            if (delta < 0 || Math.exp(-delta / temp) > Math.random()) {
                current = neighborInd;
            }
            if (current.getFitness() < best.getFitness()) {
                best = new Individual(new ArrayList<>(current.getChromosome()));
                best.setFitness(current.getFitness());
                best.setRemainingCapacity(current.getRemainingCapacity());
                best.setBins(current.getBins());
            }
            temp *= coolingRate;
        }
        return best;
    }

    private void mutate(List<Integer> individual) {
        if (mutationStrategy == UTILS.PAIRWISE_MUTATION) {
            int left = ThreadLocalRandom.current().nextInt(0, individual.size() - 1);
            int right = ThreadLocalRandom.current().nextInt(left + 1, individual.size());
            Collections.swap(individual, left, right);
        } else if (mutationStrategy == UTILS.SINGLE_MOVE_MUTATION) {
            int removePoint = ThreadLocalRandom.current().nextInt(0, individual.size());
            int insertPoint;
            do {
                insertPoint = ThreadLocalRandom.current().nextInt(0, individual.size());
            } while (insertPoint == removePoint);
            int value = individual.remove(removePoint);
            if (removePoint < insertPoint) insertPoint--;
            individual.add(insertPoint, value);
        } else {
            throw new IllegalArgumentException("Unsupported mutation strategy");
        }
    }

    private void evaluateBestFit(Individual individual) {
        List<List<Integer>> bins = new ArrayList<>();
        List<Integer> cap = new ArrayList<>();

        for (Integer idx : individual.getChromosome()) {
            int pack = packages.get(idx);
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
                cap.set(bestIdx, cap.get(bestIdx) - pack);
            } else {
                List<Integer> newBin = new ArrayList<>();
                newBin.add(pack);
                bins.add(newBin);
                cap.add(binCapacity - pack);
            }
        }

        int numBins = bins.size();
        int totalRemaining = cap.stream().mapToInt(Integer::intValue).sum();
        double penalty = 1.01 * ((double) totalRemaining / (binCapacity * numBins));
        double fitness = numBins + penalty;

        individual.setRemainingCapacity(cap.stream().mapToInt(Integer::intValue).sum());
        individual.setBins(bins);
        individual.setFitness((int)fitness);
    }
}
@Getter
@Setter
class Individual {
    private List<Integer> chromosome;
    private List<List<Integer>> bins;
    private int remainingCapacity;
    private int fitness;

    public Individual(List<Integer> chromosome) {
        this.chromosome = chromosome;
    }

}