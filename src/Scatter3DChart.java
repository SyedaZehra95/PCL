import com.orsoncharts.Chart3D;
import com.orsoncharts.axis.NumberAxis3D;
import com.orsoncharts.data.xyz.XYZDataItem;
import com.orsoncharts.data.xyz.XYZSeries;
import com.orsoncharts.data.xyz.XYZSeriesCollection;
import com.orsoncharts.plot.XYZPlot;
import com.orsoncharts.renderer.xyz.ScatterXYZRenderer;

import javafx.application.Platform;

public class Scatter3DChart {
	private NumberAxis3D xAxis;
	private NumberAxis3D yAxis;
	private NumberAxis3D zAxis;
	private Chart3D sc;
	private XYZSeriesCollection dataset;

	public Chart3D getChart() {
		return sc;
	}

	Scatter3DChart() {
		xAxis = new NumberAxis3D("Man-hours per week");
		yAxis = new NumberAxis3D("Avg no. of days from T-start");
		zAxis = new NumberAxis3D("End week (weeks)");

		dataset = new XYZSeriesCollection();
		ScatterXYZRenderer renderer = new ScatterXYZRenderer();
//		renderer.setColors(Colors.createIntenseColors());
		renderer.setSize(0.3);
		XYZPlot plot = new XYZPlot(dataset, renderer, xAxis, yAxis, zAxis);
		sc = new Chart3D("", null, plot);
	}

	public void run(Population population) {
		Thread updateThread = new Thread(() -> {

			Platform.runLater(() -> run1(population));

		});
		updateThread.setDaemon(true);
		updateThread.start();
	}

	private void run1(Population population) {
		XYZSeries series = new XYZSeries("Optimum solutions");
		int min0 = ActivityData.getErrorFitness();
		int max0 = 0;
		int min1 = ActivityData.getErrorFitness();
		int max1 = 0;
		double min2 = ActivityData.getErrorFitness();
		double max2 = 0;
		ActivityData.resetChart3DData();
		for (Individual ind : population.getIndividualsArray()) {
			int[] fitness = ind.getFitness();
			if (ActivityData.getMaxPenaltyShow() >= ind.getPenalty()) {
				XYZDataItem item = new XYZDataItem(ind.getHighestManhours(), ind.getWeightedAverageLateStartDays(),
						fitness[0]);
				series.add(item);
				ActivityData.addChart3DData(ind);
			}
			if (fitness[0] < min0) {
				min0 = fitness[0];
			}
			if (fitness[0] > max0) {
				max0 = fitness[0];
			}
			if (ind.getHighestManhours() < min1) {
				min1 = ind.getHighestManhours();
			}
			if (ind.getHighestManhours() > max1) {
				max1 = ind.getHighestManhours();
			}
			if (ind.getWeightedAverageLateStartDays() < min2) {
				min2 = ind.getWeightedAverageLateStartDays();
			}
			if (ind.getWeightedAverageLateStartDays() > max2) {
				max2 = ind.getWeightedAverageLateStartDays();
			}
		}
		dataset.add(series);
		if (min0 == max0) {
			min0 = min0 - 1;
			max0 = min0 + 2;
		}
		if (min1 == max1) {
			min1 = min1 - 1;
			max1 = min1 + 2;
		}
		if (min2 == max2) {
			min2 = min2 - 1;
			max2 = min2 + 2;
		}
		xAxis.setRange(min1, max1);
		yAxis.setRange(min2, max2);
		zAxis.setRange(min0, max0);
	}
}
