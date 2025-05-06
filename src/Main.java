
public class Main {


    public static void main(String[] args) {
        SimulatedAnnealing sa = new SimulatedAnnealing(100, 100, 10, .98, 100000,UTILS.SINGLE_MOVE_MUTATION);

        System.out.println(sa.run().getFitness());

        GeneticAlgorithm sga = new GeneticAlgorithm(50, 50,50,null,25);

        sga.runGA(UTILS.TOURNAMENT_SELECTION,UTILS.ORDER1_CROSSOVER, UTILS.SINGLE_MOVE_MUTATION,0.05);
    }
}