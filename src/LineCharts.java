import javafx.application.Platform;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class LineCharts {
	private String title;
	private XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();
	private XYChart.Series<Number, String> sseries = new XYChart.Series<Number, String>();
	final NumberAxis xAxis = new NumberAxis();
	final NumberAxis yAxis = new NumberAxis();
	final CategoryAxis yaxis = new CategoryAxis();
	final LineChart<Number, Number> lineChart = new LineChart<Number, Number>(xAxis, yAxis);
	final LineChart<Number, String> slineChart = new LineChart<Number, String>(xAxis, yaxis);

	public LineCharts(String title) {
		this.title = title;
		xAxis.setForceZeroInRange(false);
		yAxis.setForceZeroInRange(false);
	}

	public LineChart run(int type, VBox main_vBox, HBox topHBox, VBox vbox) {
		Thread updateThread = new Thread(() -> {

			Platform.runLater(() -> {
				if (type == 0) {
					createLineChart(this.lineChart, this.xAxis, this.yAxis, main_vBox, topHBox, vbox);
				} else {
					createLineChart(this.slineChart, this.xAxis, this.yAxis, main_vBox, topHBox, vbox);
				}
			});

		});
		updateThread.setDaemon(true);
		updateThread.start();
		return lineChart;
	}

	public LineChart createLineChart(LineChart lineChart, NumberAxis xAxis, NumberAxis yAxis, VBox main_vBox,
			HBox topHBox, VBox vbox) {

		xAxis.setLabel("generations");

		this.lineChart.setAnimated(false);
		this.lineChart.setTitle(this.title);
		VBox vBox = new VBox(this.lineChart);
		vbox.getChildren().addAll(vBox);
		while (topHBox.getChildren().size() > 1) {
			topHBox.getChildren().remove(1);
		}
		topHBox.getChildren().addAll(vbox);
		return this.lineChart;
	}

	public void addSeries(int gen, int value) {

		this.series.getData().add(new XYChart.Data<Number, Number>(gen, value));
		this.lineChart.getData().clear();
		this.lineChart.getData().add(this.series);
	}

	public void addSeries(int gen, double value) {

		this.series.getData().add(new XYChart.Data<Number, Number>(gen, value));
		this.lineChart.getData().clear();
		this.lineChart.getData().add(this.series);
	}

	public void addSseries(int gen, String value) {
		System.out.println(value);
		this.sseries.getData().add(new Data<Number, String>(gen, value));
		this.slineChart.getData().clear();
		this.slineChart.getData().add(this.sseries);
	}

}
