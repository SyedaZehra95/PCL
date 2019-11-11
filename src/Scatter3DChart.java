import com.orsoncharts.Chart3D;
import com.orsoncharts.Chart3DFactory;
import com.orsoncharts.axis.ValueAxis3D;
import com.orsoncharts.data.function.Function3D;
import com.orsoncharts.data.xyz.XYZDataItem;
import com.orsoncharts.data.xyz.XYZSeries;
import com.orsoncharts.data.xyz.XYZSeriesCollection;
import com.orsoncharts.plot.XYZPlot;
import com.orsoncharts.renderer.xyz.ScatterXYZRenderer;

import javafx.application.Platform;

public class Scatter3DChart {
	private Chart3D sc;
	private XYZSeriesCollection dataset;
	private XYZPlot plot;
	private int num_sol = 0;

	Function3D function = new Function3D() {
		@Override
		public double getValue(double x, double z) {
			return Math.sin(x * x + z * z);
		}
	};

	public Chart3D getChart() {

		return sc;
	}

	Scatter3DChart(int numSolutions) {
		this.num_sol = numSolutions;
		dataset = new XYZSeriesCollection();
		sc = Chart3DFactory.createScatterChart("Optimal Solution", this.num_sol + " solutions", dataset,
				"Man-hours per week", "Avg no. of days from T-start", "End week (weeks)");

//		renderer.setColors(Colors.createIntenseColors());

		plot = (XYZPlot) sc.getPlot();
		ScatterXYZRenderer renderer = (ScatterXYZRenderer) plot.getRenderer();
		renderer.setSize(0.3);

	}

	public void run(Population population) {
		Thread updateThread = new Thread(() -> {

			Platform.runLater(() -> {
				try {
					run1(population);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});

		});
		updateThread.setDaemon(true);
		updateThread.start();
	}

	private void run1(Population population) throws Exception {
		XYZSeries series = new XYZSeries("Optimum solutions");
		int min0 = ActivityData.getErrorFitness();
		int max0 = 0;
		int min1 = ActivityData.getErrorFitness();
		int max1 = 0;
		double min2 = ActivityData.getErrorFitness();
		double max2 = 0;
		ActivityData.resetChart3DData();
		for (Individual ind : population.getIndividualsArray()) {
			// Date date1=new SimpleDateFormat("dd/MM/yyyy").parse(ind.getProjectEndWeek());

			int week = Integer.parseInt(ind.getProjectEndWeek().replaceAll("[\\s\\-()]", ""));
			// Date week=date1;

			double[] fitness = ind.getFitness();
			if (ActivityData.getMaxPenaltyShow() >= ind.getPenalty()) {

				XYZDataItem item = new XYZDataItem(ind.getHighestManhours(), ind.getWeightedAverageLateStartDays(),
						fitness[0]);
				series.add(item);
				ActivityData.addChart3DData(ind);
			}
			if (fitness[0] < min0) {
				min0 = (int) fitness[0];
			}
			if (fitness[0] > max0) {
				max0 = (int) fitness[0];
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
		ValueAxis3D xAxis = plot.getXAxis();
		ValueAxis3D yAxis = plot.getYAxis();
		ValueAxis3D zAxis = plot.getZAxis();
		System.out.println(min0 + ":" + max0);
		xAxis.setRange(min1, max1);
		yAxis.setRange(min2, max2);
		zAxis.setRange(min0, max0);
	}
}
