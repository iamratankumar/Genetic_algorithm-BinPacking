import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) {
        GeneticAlgorithm ga = new GeneticAlgorithm(1000,50, 50, 25);
        ga.runGA(UTILS.ROULETTE_SELECTION,UTILS.ORDER1_CROSSOVER, UTILS.SINGLE_MOVE_MUTATION,0.05);

    }
}