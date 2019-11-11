import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ThreadLocalRandom;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class Algorithm {

	private int populationSize;
	private int numberOfGenerations = 1000;
	private int tournamentSize = 3;
	private int objectiveId = 100;

	private int numIterations = 1;
	private final boolean DEBUG = false;

	private int min = 0;
	private int sec = 0;

	public Population autoRun(VBox main_vBox, HBox topHBox, GridPane grid_config, int position, Label process_time) {
		try {

			TableView<Progress> tableView = createTableView(grid_config, position);

			if (topHBox.getChildren().size() > 1) {
				Platform.runLater(() -> {
					while (topHBox.getChildren().size() > 1) {
						topHBox.getChildren().remove(1);

					}
				});
			}

			VBox vBox = new VBox(5);
			LineCharts lineCharts1 = new LineCharts("Man-Hours per Week");
			lineCharts1.run(0, main_vBox, topHBox, vBox);
			LineCharts lineCharts2 = new LineCharts("End week");
			lineCharts2.run(0, main_vBox, topHBox, vBox);
			LineCharts lineCharts3 = new LineCharts("Average No. of Days from TMin");
			lineCharts3.run(0, main_vBox, topHBox, vBox);
			LineCharts lineCharts4 = new LineCharts("Days b/w System Completion and T-Max");
			lineCharts4.run(0, main_vBox, topHBox, vBox);

			tableView.getItems().clear();
			populationSize = ActivityData.size();
			int gen = 1;
			if (objectiveId == 100) {
				ArrayList<Individual> solutions = new ArrayList<>();
				for (int itr = 0; itr < numIterations; itr++) {
					LocalDateTime start_time = LocalDateTime.now();
					if (ActivityData.isAborted()) {
						return null;
					}

					Population population = new Population(populationSize, true);
					Population unionPopulation = new Population(populationSize, false);

					Individual firstInd = population.getIndividual(0);

					Individual[] _fitness = new Individual[] { firstInd, firstInd, firstInd, firstInd };
					int sameFitnessCount = 0;
					while (true) {
						if (ActivityData.isAborted()) {
							return null;
						}
						if (sameFitnessCount > 1) {
							tableView.setPlaceholder(new Label("Done"));
							break;
						}

						Population offspringPopulation = evolveForMultiObjective(population);
						unionPopulation = new Population(population.size() + offspringPopulation.size(), false);
						unionPopulation.setIndividualsArray(Utils.concat(population.getIndividualsArray(),
								offspringPopulation.getIndividualsArray()));
						population = newPopAfterRanking(unionPopulation);

						if (gen % ActivityData.getAccuracyThreshold() == 0) {
							Individual[] currentFittest = { population.getFittest(0), population.getFittest(1),
									population.getFittest(2), population.getFittest(3) };

							final int manHrs = currentFittest[1].getHighestManhours();
							final int penalty = currentFittest[1].getPenalty();
							final double avgFromStart = currentFittest[2].getWeightedAverageLateStartDays();
							final int i = gen;

							tableView.getItems()
									.add(new Progress(gen, manHrs, penalty, currentFittest[0].getProjectEndWeek(),
											avgFromStart, currentFittest[3].getFitness()[3]));

							LocalDateTime end_time = LocalDateTime.now();
							long diff = ChronoUnit.SECONDS.between(start_time, end_time);
							if (diff > 60) {
								this.min = (int) diff / 60;
								this.sec = (int) diff % 60;
							} else {
								this.sec = (int) diff;
							}
							Text sub = new Text("min");

							Platform.runLater(() -> {
								process_time.setText(this.min + " : " + this.sec + " sec");
								tableView.scrollTo(tableView.getItems().size() - 2);
								lineCharts1.addSeries(i, manHrs);
								lineCharts2.addSeries(i, currentFittest[0].getFitness()[0]);
								lineCharts3.addSeries(i, avgFromStart);
								lineCharts4.addSeries(i, currentFittest[3].getFitness()[3]);
							});

							if (_fitness[0].getFitness()[0] == currentFittest[0].getFitness()[0]
									&& _fitness[1].getFitness()[1] == currentFittest[1].getFitness()[1]
									&& _fitness[2].getFitness()[2] == currentFittest[2].getFitness()[2]
									&& _fitness[3].getFitness()[3] == currentFittest[3].getFitness()[3]) {
								sameFitnessCount++;
							} else {
								_fitness = currentFittest;
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

				ActivityData.setNumSolutions(gen * populationSize);
				return paretoPopulation;
			} else {
				Population fittestPopulation = new Population(numIterations, false);
				for (int itr = 0; itr < numIterations; itr++) {
					Population population = new Population(populationSize, true);
					for (gen = 0; gen < numberOfGenerations; gen++) {
						population = evolve(population);
					}
					fittestPopulation.saveIndividual(itr, population.getFittest(objectiveId));
				}
				return fittestPopulation;
			}

		} catch (Exception e) {
			Platform.runLater(() -> {
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));

				Alert alert = new Alert(AlertType.ERROR);
				alert.setHeaderText("An exception occurred!");
				alert.getDialogPane().setExpandableContent(new ScrollPane(new TextArea(sw.toString())));
				alert.showAndWait();

			});

		}
		return null;

	}

	private TableView<Progress> createTableView(GridPane grid_config, int position) {
		TableView<Progress> tableView = new TableView<Progress>();

		tableView.setMinHeight(300.5);
		tableView.setMinWidth(540);
		tableView.setMaxWidth(435);
		TableColumn<Progress, String> column1 = new TableColumn<Progress, String>("Run");
		column1.setCellValueFactory(new PropertyValueFactory<>("index"));
		TableColumn<Progress, String> column2 = new TableColumn<Progress, String>("Max man-hours");
		column2.setCellValueFactory(new PropertyValueFactory<>("ManHours"));
		TableColumn<Progress, String> column3 = new TableColumn<Progress, String>("Penalty");
		column3.setCellValueFactory(new PropertyValueFactory<>("Penalty"));
		TableColumn<Progress, String> column4 = new TableColumn<Progress, String>("  End week  ");
		column4.setCellValueFactory(new PropertyValueFactory<>("EndWeek"));
		TableColumn<Progress, String> column5 = new TableColumn<Progress, String>("Avg days from T-Min");
		column5.setCellValueFactory(new PropertyValueFactory<>("AvgFromStart"));
		TableColumn<Progress, String> column6 = new TableColumn<Progress, String>("System completion");
		column6.setCellValueFactory(new PropertyValueFactory<>("AvgSystemCompletion"));

		tableView.getColumns().add(column1);
		tableView.getColumns().add(column2);
		tableView.getColumns().add(column3);
		tableView.getColumns().add(column4);
		tableView.getColumns().add(column5);
		tableView.getColumns().add(column6);
		tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		final KeyCodeCombination keyCodeCopy = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY);
		tableView.setOnKeyPressed(event -> {
			if (keyCodeCopy.match(event)) {
				copySelectionToClipboard(tableView);
			}
		});

		autoResizeColumns(tableView);

		VBox tableArea = new VBox(tableView);
		tableArea.setPadding(new Insets(10, 0, 0, 0));

		Platform.runLater(() -> {
			grid_config.add(tableArea, 0, position, 2, 1);
		});

		return tableView;
	}

	private Population newPopAfterRanking(Population unionPopulation) {
		Population new_population = new Population(populationSize, false);

		int NUM_OBJECTIVES = 4;

		for (int i = 0; i < NUM_OBJECTIVES; i++) {
			new_population.saveIndividual(i, unionPopulation.getFittest(i));
		}

		Ranking ranking = new Ranking(unionPopulation);
		int remain = populationSize - NUM_OBJECTIVES;
		int index = 0;
		ArrayList<Individual> front = ranking.getSubfront(index);

		int counter = 4;
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

	public static void autoResizeColumns(TableView<?> table) {
		// Set the right policy
		table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
		table.getColumns().stream().forEach((column) -> {
			// Minimal width = column header
			Text t = new Text(column.getText());
			double max = t.getLayoutBounds().getWidth();
			for (int i = 0; i < table.getItems().size(); i++) {
				// cell must not be empty
				if (column.getCellData(i) != null) {
					t = new Text(column.getCellData(i).toString());
					double calcwidth = t.getLayoutBounds().getWidth();
					// remember new max-width
					if (calcwidth > max) {
						max = calcwidth;
					}
				}
			}
			// set the new max-width with some extra space
			column.setPrefWidth(max + 10.0d);
		});
	}

	@SuppressWarnings("rawtypes")
	public void copySelectionToClipboard(final TableView<?> table) {
		final Set<Integer> rows = new TreeSet<>();
		for (final TablePosition tablePosition : table.getSelectionModel().getSelectedCells()) {
			rows.add(tablePosition.getRow());
		}
		final StringBuilder strb = new StringBuilder();
		boolean firstRow = true;
		for (final Integer row : rows) {
			if (!firstRow) {
				strb.append('\n');
			}
			firstRow = false;
			boolean firstCol = true;
			for (final TableColumn<?, ?> column : table.getColumns()) {
				if (!firstCol) {
					strb.append('\t');
				}
				firstCol = false;
				final Object cellData = column.getCellData(row);
				strb.append(cellData == null ? "" : cellData.toString());
			}
		}
		final ClipboardContent clipboardContent = new ClipboardContent();
		clipboardContent.putString(strb.toString());
		Clipboard.getSystemClipboard().setContent(clipboardContent);
	}

}
