import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class Algorithm {

	private int populationSize;
	private int numberOfGenerations = 1000;
	private int tournamentSize = 3;
	private int objectiveId = 100;

	private int numIterations = 1;
	private final boolean DEBUG = false;

	public Population autoRun(TableView tableView, VBox main_vBox, HBox topHBox) {
		//debugTextArea.appendText("Algorithm progress: \n\n");
		populationSize = ActivityData.size() * 1;
		int sampleSize = 10;
		if (objectiveId == 100) {
			ArrayList<Individual> solutions = new ArrayList<>();
			for (int itr = 0; itr < numIterations; itr++) {
				if (ActivityData.isAborted()) {
					return null;
				}
				ArrayList<Individual> fittestIndividuals = new ArrayList<>();
				while (fittestIndividuals.size() < this.populationSize) {
					Population samplePop = new Population(sampleSize, true);
					Ranking ranking = new Ranking(samplePop);
					ArrayList<Individual> fitIndividuals = ranking.getSubfront(0);
					if (ActivityData.isAborted()) {
						tableView.setPlaceholder(new Label("No Solution"));
						//debugTextArea.appendText("\n\nNo solution.\n\n");
						return null;
					}
					fittestIndividuals.addAll(fitIndividuals);
				}

				Population population = new Population(populationSize, false);
				for (int i = 0; i < populationSize; i++) {
					population.saveIndividual(i, fittestIndividuals.get(i));
				}

				Population unionPopulation = new Population(populationSize, false);

				int[] _fitness = population.getIndividual(0).getFitness();
				tableView.getItems().add(new Progress(0,_fitness[1],_fitness[0],_fitness[2]));
				//debugTextArea.appendText(0 + ": Max workers + Penalty: " + _fitness[1] + ", Last work week: "
						//+ _fitness[0] + ", Late start penalty: " + _fitness[2] + ".\n");
				int sameFitnessCount = 0;
				int gen = 1;
				while (true) {
					if (ActivityData.isAborted()) {
						return null;
					}
					if (sameFitnessCount > 1) {
						tableView.setPlaceholder(new Label("Done"));
						//debugTextArea.appendText("\nDone!\n");
						break;
					}

					Population offspringPopulation = evolveForMultiObjective(population);
					unionPopulation = new Population(population.size() + offspringPopulation.size(), false);
					unionPopulation.setIndividualsArray(
							Utils.concat(population.getIndividualsArray(), offspringPopulation.getIndividualsArray()));
					population = newPopAfterRanking(unionPopulation);

					if (gen % ActivityData.getAccuracyThreshold() == 0) {
//						final Population pop = population;
//						Platform.runLater(new Runnable() {
//
//							@Override
//							public void run() {
//								pop.visualize(topHBox, main_vBox);
//							}
//						});
						int[] currentFitness = { population.getFittest(0).getFitness()[0],
								population.getFittest(1).getFitness()[1], population.getFittest(2).getFitness()[2] };
						tableView.getItems().add(new Progress(gen,currentFitness[1],currentFitness[0],currentFitness[2]));
						//debugTextArea
								//.appendText(gen + ": Max workers + Penalty: " + currentFitness[1] + ", Last work week: "
										//+ currentFitness[0] + ", Late start penalty: " + currentFitness[2] + ".\n");

						if (_fitness[0] == currentFitness[0] && _fitness[1] == currentFitness[1]
								&& _fitness[2] == currentFitness[2]) {
							sameFitnessCount++;
						} else {
							_fitness = currentFitness;
						}
					}

					if (DEBUG) {
						System.out.println(population.getFittest(0).getFitness()[0] + ", "
								+ population.getFittest(1).getFitness()[1]);
					}
					gen++;
				}
				Ranking ranking = new Ranking(population);
				ArrayList<Individual> fitIndividuals = ranking.getSubfront(0);
				solutions.addAll(fitIndividuals);
			}

			Population fittestPopulation = new Population(solutions.size(), false);
			for (int z = 0; z < solutions.size(); z++) {
				fittestPopulation.saveIndividual(z, solutions.get(z));
			}
			Ranking ranking = new Ranking(fittestPopulation);
			ArrayList<Individual> pareto = ranking.getSubfront(0);
			Population paretoPopulation = new Population(pareto.size(), false);
			for (int z = 0; z < paretoPopulation.size(); z++) {
				paretoPopulation.saveIndividual(z, pareto.get(z));
			}
			System.out.println("Done.");
			return paretoPopulation;
		} else {
			Population fittestPopulation = new Population(numIterations, false);
			for (int itr = 0; itr < numIterations; itr++) {
				Population population = new Population(populationSize, true);
				for (int gen = 0; gen < numberOfGenerations; gen++) {
					population = evolve(population);
				}
				fittestPopulation.saveIndividual(itr, population.getFittest(objectiveId));
			}
			return fittestPopulation;
		}
	}

	public Population run() {
		if (objectiveId == 100) {
			ArrayList<Individual> fittestIndividuals = new ArrayList<>();
			for (int itr = 0; itr < numIterations; itr++) {
				Population population = new Population(populationSize, true);
				Population unionPopulation = new Population(populationSize, false);
				for (int i = 0; i < numberOfGenerations; i++) {
					Population offspringPopulation = evolveForMultiObjective(population);
					unionPopulation = new Population(population.size() + offspringPopulation.size(), false);
					unionPopulation.setIndividualsArray(
							Utils.concat(population.getIndividualsArray(), offspringPopulation.getIndividualsArray()));
					population = newPopAfterRanking(unionPopulation);
					if (DEBUG) {
						System.out.println(population.getFittest(0).getFitness()[0] + ", "
								+ population.getFittest(1).getFitness()[1]);
					}
				}
				Ranking ranking = new Ranking(population);
				ArrayList<Individual> fitIndividuals = ranking.getSubfront(0);
				fittestIndividuals.addAll(fitIndividuals);
			}
			Population fittestPopulation = new Population(fittestIndividuals.size(), false);
			for (int z = 0; z < fittestIndividuals.size(); z++) {
				fittestPopulation.saveIndividual(z, fittestIndividuals.get(z));
			}
			Ranking ranking = new Ranking(fittestPopulation);
			ArrayList<Individual> pareto = ranking.getSubfront(0);
			Population paretoPopulation = new Population(pareto.size(), false);
			for (int z = 0; z < paretoPopulation.size(); z++) {
				paretoPopulation.saveIndividual(z, pareto.get(z));
			}
			return paretoPopulation;
		} else {
			Population fittestPopulation = new Population(numIterations, false);
			for (int itr = 0; itr < numIterations; itr++) {
				Population population = new Population(populationSize, true);
				for (int gen = 0; gen < numberOfGenerations; gen++) {
					population = evolve(population);
				}
				fittestPopulation.saveIndividual(itr, population.getFittest(objectiveId));
			}
			return fittestPopulation;
		}
	}

	private Population newPopAfterRanking(Population unionPopulation) {
		Population new_population = new Population(populationSize, false);
		Ranking ranking = new Ranking(unionPopulation);
		int remain = populationSize;
		int index = 0;
		ArrayList<Individual> front = ranking.getSubfront(index);

		int counter = 0;
		while ((remain > 0) && (remain >= front.size())) {
			for (int k = 0; k < front.size(); k++) {
				if (counter < new_population.size()) {

					new_population.saveIndividual(counter, front.get(k));
				}
				counter++;
			}
			remain = remain - front.size();
			index++;
			if (remain > 0) {
				front = ranking.getSubfront(index);
			}
		}
		if (remain > 0) {
			for (int k = 0; k < remain; k++) {
				new_population.saveIndividual(counter, front.get(k));
				counter++;
			}
			remain = 0;
		}
		return new_population;
	}

	private Population evolve(Population population) {
		Population newPopulation = new Population(populationSize, false);
		Individual fit = population.getFittest(objectiveId);
		fit.markSelected();
		newPopulation.saveIndividual(0, new Individual(fit));

		for (int i = 1; i < population.size(); i++) {
			newPopulation.saveIndividual(i, tournamentSelection(population));
		}
		return newPopulation;
	}

	private Population evolveForMultiObjective(Population population) {
		Population newPopulation = new Population(populationSize, false);

		for (int i = 0; i < population.size(); i++) {
			newPopulation.saveIndividual(i, smartMutate(population.getIndividual(i), population.getFitnessSet()));
		}
		return newPopulation;
	}

	private Individual tournamentSelection(Population population) {
		Population tournament = new Population(tournamentSize, false);
		for (int i = 0; i < tournamentSize; i++) {
			int randomId = ThreadLocalRandom.current().nextInt(0, population.size());
			tournament.saveIndividual(i, population.getIndividual(randomId));
		}
		Individual fittest = tournament.getFittest(objectiveId);
		if (fittest.isSelected()) {
			return mutate(fittest);
		} else {
			fittest.markSelected();
		}
		return fittest;
	}

	private Individual mutate(Individual individual) {
		Individual newIndiv = new Individual(individual);
		do {
			int randomId = ThreadLocalRandom.current().nextInt(0, ActivityData.size());
			Activity newGene = new Activity(randomId);
			if (Math.random() < 0.5) {
				int i = 0;
				while (i < 3) {
					if (newGene.mutateTo(
							individual.getGene(ThreadLocalRandom.current().nextInt(0, ActivityData.size())))) {
						break;
					}
					i++;
				}
			}
			newIndiv.setGene(randomId, newGene);
			newIndiv.resetFitness();
		} while (individual.getFitness()[0] == newIndiv.getFitness()[0]
				&& individual.getFitness()[1] == newIndiv.getFitness()[1]);
		return newIndiv;
	}

	private Individual smartMutate(Individual individual, HashSet<String> fitnessSet) {
		Individual newIndiv = new Individual(individual);
		do {
			int randomId = ThreadLocalRandom.current().nextInt(0, ActivityData.size());
			Activity newGene = new Activity(randomId);
			if (Math.random() < 0.5) {
				int i = 0;
				while (i < 3) {
					if (newGene.mutateTo(
							individual.getGene(ThreadLocalRandom.current().nextInt(0, ActivityData.size())))) {
						break;
					}
					i++;
				}
			}
			newIndiv.setGene(randomId, newGene);
			newIndiv.resetFitness();
		} while (!fitnessSet.add(Utils.convertFitnessToString(newIndiv.getFitness())));
		return newIndiv;
	}

	public int getObjectiveId() {
		return objectiveId;
	}
	
	
}
