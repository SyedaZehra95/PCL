import java.util.HashSet;

import com.orsoncharts.data.xyz.XYZItemKey;
import com.orsoncharts.fx.Chart3DViewer;
import com.orsoncharts.interaction.fx.FXChart3DMouseEvent;

import javafx.geometry.Insets;
import javafx.scene.chart.ScatterChart;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class Population {
	private Individual[] individuals;
	private HashSet<String> fitnessSet;
	private int highestPenalty = -1;

	public Population(int populationSize, boolean initialise) {
		individuals = new Individual[populationSize];

		if (initialise) {
			for (int i = 0; i < populationSize; i++) {
				if (ActivityData.isAborted()) {
					return;
				}
				Individual newIndiv = new Individual();
				saveIndividual(i, newIndiv.generateSequence());
			}
		}
	}

	public void saveIndividual(int index, Individual indiv) {
		individuals[index] = indiv;
	}

	public int size() {
		return individuals.length;
	}

	public Individual getIndividual(int id) {
		
		return individuals[id];
	}

	public Individual getFittest(int objective) {
		if (objective == 100) {
			objective = 1;
		}
		individuals[0].getFitness();
		Individual fittest = individuals[0];
		for (int i = 0; i < individuals.length; i++) {
			if (fittest.getFitness()[objective] >= getIndividual(i).getFitness()[objective]) {
				fittest = getIndividual(i);
			}
		}
		return fittest;
	}

	public void setIndividualsArray(Individual[] individuals) {
		this.individuals = individuals;
	}

	public Individual[] getIndividualsArray() {
		return individuals;
	}

	public void visualize(HBox topHBox, VBox main_vBox) {
		while (main_vBox.getChildren().size() > 2) {
			main_vBox.getChildren().remove(2);
		}
		/*
		while (topHBox.getChildren().size() > 1) {
			topHBox.getChildren().remove(1);
		}

		while (main_vBox.getChildren().size() > 2) {
			main_vBox.getChildren().remove(2);
		}*/

//		int fittestDuration = this.getFittest(0).getFitness()[0];
//		ArrayList<Integer> durations = new ArrayList<>();
//		for (Individual individual : this.individuals) {
//			if (!durations.contains(individual.getFitness()[0] - fittestDuration)) {
//				durations.add(individual.getFitness()[0] - fittestDuration);
//			}
//		}
//		Collections.sort(durations);
//
//		int[] chartDurations = new int[durations.size()];
//		for (int i = 0; i < durations.size(); i++) {
//			chartDurations[i] = durations.get(i);
//		}
		
//		topTilePane.getChildren().add(createScatterPlot(main_vBox, topTilePane));
		VBox vBox = new VBox();

		Label label1 = new Label("Max resource variation penalty:");
		label1.setPadding(new Insets(10, 10, 10, 10));

		Slider slider1 = new Slider(0, this.getHighestPenalty(), this.getHighestPenalty());
		slider1.setPadding(new Insets(10, 10, 10, 10));
		slider1.setShowTickMarks(true);
		slider1.setShowTickLabels(true);
		double unit = this.getHighestPenalty() / 10;
		if (unit < 1) {
			unit = 1;
		}
		slider1.setMajorTickUnit(unit);
		slider1.setBlockIncrement(unit);

		slider1.setOnMouseReleased(event -> {
			ActivityData.setMaxPenaltyShow((int) slider1.getValue());
			vBox.getChildren().remove(0);
			vBox.getChildren().add(0, createScatterPlot3D(main_vBox, topHBox));
		});
		vBox.setMinHeight(400.0);
		vBox.getChildren().add(0, createScatterPlot3D(main_vBox, topHBox));
		vBox.getChildren().add(1, label1);
		vBox.getChildren().add(2, slider1);
		main_vBox.getChildren().add(vBox);

//		topTilePane.getChildren().add(createScatterPlot(main_vBox, topTilePane, new int[] { durations.get(0) }));
//
//		if (durations.size() > 1) {
//			topTilePane.getChildren().add(createScatterPlot(main_vBox, topTilePane,
//					new int[] { durations.get(1), durations.get(2), durations.get(3) }));
//		}

//		topTilePane.getChildren().add(createScatterPlot3D(main_vBox, topTilePane));
	}

	@SuppressWarnings("unchecked")
	private Chart3DViewer createScatterPlot3D(VBox main_vBox, HBox topTilePane) {
		
		Scatter3DChart sChart = new Scatter3DChart(this);
		sChart.run(this);
		Chart3DViewer v = new Chart3DViewer(sChart.getChart());
		v.addEventHandler(FXChart3DMouseEvent.MOUSE_CLICKED, (FXChart3DMouseEvent event) -> {
			if (event.getElement() != null) {
				XYZItemKey element = (XYZItemKey) event.getElement().getProperty("key");
				if (element != null) {
					Individual clickedIndividual = ActivityData.getChart3DData(element.getItemIndex());
					clickedIndividual.visualize(main_vBox);
				}
			}
		});
		return v;
	}

	private ScatterChart<Number, Number> createScatterPlot(VBox main_vBox, HBox topTilePane, int[] durationsDisplayed) {
		Scatter2DChart sChart = new Scatter2DChart();
		sChart.run(this, main_vBox, topTilePane, durationsDisplayed);
		return sChart.getChart();
	}

	private ScatterChart<Number, Number> createScatterPlot(VBox main_vBox, HBox topTilePane) {
		Scatter2DChart sChart = new Scatter2DChart();
		sChart.run(this, main_vBox, topTilePane);
		return sChart.getChart();
	}

	public void sysout() {
		double durationFitness = getFittest(0).getFitness()[0];
		double levelingFitness = getFittest(1).getFitness()[1];
		System.out.println("Duration: " + durationFitness / 30 + ", Leveling: " + levelingFitness);
	}

	public HashSet<String> getFitnessSet() {
		if (fitnessSet == null) {
			fitnessSet = new HashSet<>();
			for (Individual individual : individuals) {
				fitnessSet.add(Utils.convertFitnessToString(individual.getFitness()));
			}
		}
		return fitnessSet;
	}

	public int getHighestPenalty() {
		if (highestPenalty == -1) {
			for (int i = 0; i < individuals.length; i++) {
				int tempPenalty = individuals[i].getPenalty();
				if (tempPenalty > highestPenalty) {
					highestPenalty = tempPenalty;
				}
			}
			ActivityData.setMaxPenaltyShow(highestPenalty);
		}
		return highestPenalty;
	}

	public void setHighestPenalty(int highestPenalty) {
		this.highestPenalty = highestPenalty;
	}
}
