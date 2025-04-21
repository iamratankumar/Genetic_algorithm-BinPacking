import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class GeneticAlgorithm {
    int populationSize, binCapacity, mutationRate;
    List<List<Integer>> population;
    List<Integer> packages;

    public GeneticAlgorithm(int populationSize, int binCapacity, Integer[] packages) {
        this.populationSize = populationSize;
        this.binCapacity = binCapacity;
        initPackages(packages);
    }

    public GeneticAlgorithm(int populationSize, int binCapacity, int packageSize) {
        this.populationSize = populationSize;
        this.binCapacity = binCapacity;
        initPackages(packageSize);
        initPop(populationSize, binCapacity, packageSize);

    }

    public void initPop(int populationSize, int binCapacity, int packageSize) {
        this.populationSize = populationSize;
        this.binCapacity = binCapacity;
        this.population = new ArrayList<>();


        for (int i = 0; i < populationSize; i++) {
            List<Integer> n = new ArrayList<>();
            for (int j = 0; j < packageSize; j++) {
                n.add(j);
            }
            Collections.shuffle(n, new Random());
            population.add(n);
        }
    }

    private void initPackages(int packageSize) {
        packages = new ArrayList<>();
        for (int i = 0; i < packageSize; i++) {

            packages.add(ThreadLocalRandom.current().nextInt(1, binCapacity));
        }
//        Collections.shuffle(packages);
    }

    private void initPackages(Integer[] packages) {
        this.packages = Arrays.asList(packages);
        initPop(populationSize, binCapacity, this.packages.size());
    }

    public void printPopulation() {
        List<Map<String, List<?>>> ls = bestFit();
        System.out.println("Chromosome\tFitness");
        for (int i = 0; i < population.size(); i++) {
            for (int j = 0; j < population.get(i).size(); j++) {
                System.out.print(population.get(i).get(j) + " ");
            }
            int optBins = ls.get(i).get("bins").size();
            System.out.print("\t" + optBins);
            System.out.println();
        }
    }

//    public List<Integer> calcFitness(Fitness fitness) {
//        return switch (fitness) {
//            case NEXT_FIT -> nextFit();
//            case BEST_FIT -> bestFit();
//            default -> null;
//        };
//
//    }

    public List<Integer> nextFit() {
        List<Integer> binList = new ArrayList<>();
        int remainingCapacity;
        for (int i = 0; i < population.size(); i++) {
            remainingCapacity = binCapacity;
            int binCount = 1;
            for (Integer j : population.get(i)) {
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

    public  List<Map<String, List<?>>> bestFit() {
        List<Map<String, List<?>>> result = new ArrayList<>();

        for (List<Integer> individual : population) {
            List<Integer> cap = new ArrayList<>();
            List<ArrayList<Integer>> bins = new ArrayList<>();

            for (Integer j : individual) {
                int pack = packages.get(j);

                int minRemaining = Integer.MAX_VALUE;
                int bestIdx = -1;

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
                    ArrayList<Integer> newBin = new ArrayList<>();
                    newBin.add(pack);
                    bins.add(newBin);
                    cap.add(binCapacity - pack);
                }
            }

            Map<String, List<?>> individualResult = new HashMap<>();
            individualResult.put("bins", bins);
            individualResult.put("capacities", cap);
            result.add(individualResult);
        }


        return result;
    }


    private void Crossover(List<List<Integer>> population) {
        int crossoverPoint = ThreadLocalRandom.current().nextInt(0,packages.size());
        List<List<Integer>> newPopulation = new ArrayList<>();

        for (int i = 0; i < population.size(); i++) {

        }
    }

    private List<Integer> cutAndCrossFill(List<Integer> chromosome, int crossPoint) {
        List<Integer> newPopulation = new ArrayList<>();
        for (int i = 0; i < crossPoint; i++) {

        }
    }


    public void printPackages() {
        for(Integer i : packages) {
            System.out.println(i);
        }
    }


}
