import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) {
        GeneticAlgorithm ga = new GeneticAlgorithm(500,50, 50);
        GeneticAlgorithm ga2 = new GeneticAlgorithm(50,10, new Integer[]{5, 6, 3, 7, 5, 4 });
        ga2.printPopulation();
        //System.out.println("=============================");
        //ga.printPackages();

    }
}