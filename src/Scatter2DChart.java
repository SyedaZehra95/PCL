import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class Scatter2DChart {
	private NumberAxis xAxis;
	private NumberAxis yAxis;
	private ScatterChart<Number, Number> sc;

	public ScatterChart<Number, Number> getChart() {
		return sc;
	}

	Scatter2DChart() {
		xAxis = new NumberAxis();
		xAxis.setLabel("End week (weeks)");
		xAxis.setForceZeroInRange(false);
		yAxis = new NumberAxis();
		yAxis.setLabel("Man-hours per week");
		yAxis.setForceZeroInRange(false);
		sc = new ScatterChart<Number, Number>(xAxis, yAxis);
	}

	public void run(Population population, VBox main_vBox, HBox topPane, int[] durationsDisplayed) {
		Thread updateThread = new Thread(() -> {

			Platform.runLater(() -> run1(population, main_vBox, topPane, durationsDisplayed));

		});
		updateThread.setDaemon(true);
		updateThread.start();
	}

	public void run(Population population, VBox main_vBox, HBox topPane) {
		int highestResourseFitness = 0;
		int highestEndDateFitness = 0;
		for (int i = 0; i < population.size(); i++) {
			if (population.getIndividual(i).getHighestResourceLevel() > highestResourseFitness) {
				highestResourseFitness = population.getIndividual(i).getHighestResourceLevel();
			}

			if (population.getIndividual(i).getFitness()[0] > highestEndDateFitness) {
				highestEndDateFitness = (int)population.getIndividual(i).getFitness()[0];
			}
		}
		yAxis.setAutoRanging(false);
		yAxis.setLowerBound((population.getFittest(1).getHighestResourceLevel() - 1));
		yAxis.setUpperBound(highestResourseFitness + 1);
		yAxis.setTickUnit(1);

		xAxis.setAutoRanging(false);
		xAxis.setLowerBound(population.getFittest(0).getFitness()[0] - 1);
		xAxis.setUpperBound(highestEndDateFitness + 1);
		xAxis.setTickUnit(1);

		Thread updateThread = new Thread(() -> {

			Platform.runLater(() -> run1(population, main_vBox, topPane));

		});
		updateThread.setDaemon(true);
		updateThread.start();
	}

	private void run1(Population population, VBox main_vBox, HBox topPane) {
		sc.setLegendVisible(false);
		if (sc.getData() == null) {
			sc.setData(FXCollections.<XYChart.Series<Number, Number>>observableArrayList());
		}
		sc.getData().clear();
		ScatterChart.Series<Number, Number> series = new ScatterChart.Series<Number, Number>();
		sc.getData().add(series);

		for (int i = 0; i < population.size(); i++) {

			int fitnessZero = (int)population.getIndividual(i).getFitness()[0];
			int fitnessOne = population.getIndividual(i).getHighestResourceLevel();

			ScatterChart.Data<Number, Number> data = new ScatterChart.Data<Number, Number>(fitnessZero, fitnessOne);
			series.getData().add(data);
			data.setExtraValue(population.getIndividual(i));
			data.getNode().setStyle("-fx-cursor: hand; -fx-background-color: #e8702a;");
			data.getNode().setOnMouseClicked(e -> {

				ObservableList<Data<Number, Number>> list = series.getData();
				for (int k = 0; k < list.size(); k++) {
					list.get(k).getNode().setStyle("-fx-cursor: hand; -fx-background-color: #e8702a;");
				}
				data.getNode().setStyle("-fx-background-color: #006B6B;");
				Individual individual = (Individual) data.getExtraValue();
				individual.visualize(main_vBox);
			});
		}
	}

	private void run1(Population population, VBox main_vBox, HBox topPane, int[] durationsDisplayed) {
		sc.setLegendVisible(false);
		if (sc.getData() == null) {
			sc.setData(FXCollections.<XYChart.Series<Number, Number>>observableArrayList());
		}
		sc.getData().clear();
		ScatterChart.Series<Number, Number> series = new ScatterChart.Series<Number, Number>();
		int fittestDuration = (int)population.getFittest(0).getFitness()[0];
//		String name = "";
//		for (int i = 0; i < durationsDisplayed.length; i++) {
//			if (i == durationsDisplayed.length - 1) {
//				name += ActivityData.getBaseDate().plusDays(fittestDuration + durationsDisplayed[i] - 1)
//						.toString("MM/dd/yyyy");
//			} else {
//				name += ActivityData.getBaseDate().plusDays(fittestDuration + durationsDisplayed[i] - 1)
//						.toString("MM/dd/yyyy") + ", ";
//			}
//
//		}
//		 series.setName(name);
		sc.getData().add(series);

		for (int i = 0; i < population.size(); i++) {
			for (int j = 0; j < durationsDisplayed.length; j++) {
				if (population.getIndividual(i).getFitness()[0] == fittestDuration + durationsDisplayed[j]) {
					ScatterChart.Data<Number, Number> data = new ScatterChart.Data<Number, Number>(
							population.getIndividual(i).getFitness()[0], population.getIndividual(i).getFitness()[1]);
					series.getData().add(data);
					data.setExtraValue(population.getIndividual(i));
					data.getNode().setStyle("-fx-cursor: hand; -fx-background-color: #e8702a;");
//					switch (j) {
//					case 0:
//						data.getNode().setStyle("-fx-cursor: hand; -fx-background-color: #e8702a;");
//						break;
//					case 1:
//						data.getNode().setStyle("-fx-cursor: hand; -fx-background-color: #00b159;");
//						break;
//					case 2:
//						data.getNode().setStyle("-fx-cursor: hand; -fx-background-color: #00aedb;");
//						break;
//					case 3:
//						data.getNode().setStyle("-fx-cursor: hand; -fx-background-color: #f37735;");
//						break;
//					case 4:
//						data.getNode().setStyle("-fx-cursor: hand; -fx-background-color: #ffc425;");
//						break;
//					case 5:
//						data.getNode().setStyle("-fx-cursor: hand; -fx-background-color: #00aba9;");
//						break;
//					case 6:
//						data.getNode().setStyle("-fx-cursor: hand; -fx-background-color: #ff0097;");
//						break;
//					case 7:
//						data.getNode().setStyle("-fx-cursor: hand; -fx-background-color: #a200ff;");
//						break;
//					case 8:
//						data.getNode().setStyle("-fx-cursor: hand; -fx-background-color: #0c457d;");
//						break;
//					case 9:
//						data.getNode().setStyle("-fx-cursor: hand; -fx-background-color: #adff00;");
//						break;
//					case 10:
//						data.getNode().setStyle("-fx-cursor: hand; -fx-background-color: #ff084a;");
//						break;
//
//					default:
//						data.getNode().setStyle("-fx-cursor: hand;");
//						break;
//					}
					data.getNode().setOnMouseClicked(e -> {

						ObservableList<Data<Number, Number>> list = series.getData();
						for (int k = 0; k < list.size(); k++) {
							list.get(k).getNode().setStyle("-fx-cursor: hand; -fx-background-color: #e8702a;");
						}
						data.getNode().setStyle("-fx-background-color: #006B6B;");
						Individual individual = (Individual) data.getExtraValue();
						individual.visualize(main_vBox);
					});
					break;
				}
			}
		}
	}
}
