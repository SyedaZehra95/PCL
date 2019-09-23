import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.joda.time.Days;

import javafx.beans.NamedArg;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.Axis;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class IndividualGanttChart<X, Y> extends XYChart<X, Y> {

	static IndividualGanttChart<Number, String> chart;
	private static DecimalFormat df = new DecimalFormat("0.00");

	public static Node startGanttChart(Individual individual) {

		final NumberAxis xAxis = new NumberAxis();
		final CategoryAxis yAxis = new CategoryAxis();
		chart = new IndividualGanttChart<Number, String>(xAxis, yAxis);

		return chart.run(individual);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Node run(Individual individual) {

		final NumberAxis xAxis = new NumberAxis();
		final CategoryAxis yAxis = new CategoryAxis();

		final IndividualGanttChart<Number, String> chart = new IndividualGanttChart<Number, String>(xAxis, yAxis);
		xAxis.setLabel("Days");
		xAxis.setTickLabelFill(Color.BLACK);
		xAxis.setTickLabelGap(1);
		xAxis.setMinorTickCount(10);
		xAxis.setSide(Side.TOP);
		xAxis.setAutoRanging(true);
		xAxis.setForceZeroInRange(false);

		yAxis.setTickLabelFill(Color.BLACK);
		yAxis.setLabel("Activity ID");

		chart.setTitle("Max manhours per week: " + individual.getHighestManhours() + " | End week: "
				+ individual.getProjectEndWeek() + " | Duration: "
				+ (individual.getFitness()[0] - individual.getStartWeek()) + " weeks | Resource variation penalty: "
				+ individual.getPenalty() + " | Avg no. of days from T-min: "
				+ df.format(individual.getAverageLateStartDays()) + " | Weighted Avg: "
				+ df.format(individual.getWeightedAverageLateStartDays()));

		chart.setLegendVisible(false);
		chart.setBlockHeight(10);

		String conns[] = new String[ActivityData.size()];
		int k = 0;

		int stepSize = (ActivityData.RMax() - ActivityData.RMin()) / 4;

		for (int i = 0; i < ActivityData.size(); i++) {
			boolean show = false;
			boolean[] checkBoxTruth = ActivityData.getCheckBoxTruth();
			XYChart.Series series = new XYChart.Series();
			chart.getData().add(series);
			conns[k] = ActivityData.getActivity(i).getName();
			String color = "status-8";
			int res = individual.getGene(i).getNumberOfResources();
			if (res == ActivityData.RMin()) {
				color = "status-4";
				if (checkBoxTruth[0]) {
					show = true;
				}
			} else if (res < ActivityData.RMin() + stepSize) {
				color = "status-5";
				if (checkBoxTruth[1]) {
					show = true;
				}
			} else if (res < ActivityData.RMin() + (2 * stepSize)) {
				color = "status-6";
				if (checkBoxTruth[2]) {
					show = true;
				}
			} else if (res < ActivityData.RMin() + (3 * stepSize)) {
				color = "status-7";
				if (checkBoxTruth[3]) {
					show = true;
				}
			} else {
				color = "status-8";
				if (checkBoxTruth[4]) {
					show = true;
				}
			}

			series.getData()
					.add(new XYChart.Data(
							Days.daysBetween(ActivityData.getBaseDate(), individual.getGene(i).getEndDate()).getDays(),
							conns[k], new ExtraData(1, "status-black")));

			int start = Days.daysBetween(ActivityData.getBaseDate(), individual.getGene(i).getStartDate()).getDays();

			series.getData().add(new XYChart.Data(start, conns[k], new ExtraData(1, "status-black")));

			if (show) {
				int activateDate = Days
						.daysBetween(ActivityData.getBaseDate(), individual.getGene(i).getActivationDate()).getDays();
				XYChart.Data<Number, String> data = new XYChart.Data(activateDate, conns[k],
						new ExtraData((int) individual.getGene(i).getDurationInDays() + 1, color));
				series.getData().add(data);
				data.getNode().setOnMouseClicked((e) -> {
					showDetailsPage(data.getXValue(), data.getYValue());
				});
			}
			k++;
		}

		yAxis.setCategories(FXCollections.<String>observableArrayList(Arrays.asList(conns)));
		chart.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		chart.setMinHeight(chart.getData().size() * 40);
		return chart;

	}

	private void showDetailsPage(Number xValue, String yValue) {
		final Stage dialog = new Stage();
		dialog.initModality(Modality.APPLICATION_MODAL);
		dialog.setTitle("Test package: " + yValue);

		VBox vbox = new VBox();
		vbox.setSpacing(5);
		vbox.setPadding(new Insets(10, 10, 10, 10));
		Label label = new Label("Test package: " + yValue + "\n");
		label.setFont(new Font("Arial", 20));

		Label labelTMax = new Label("Tmax: " + ActivityData.getActivity(yValue).getEndDate().toString() + "\n");
		labelTMax.setFont(new Font("Arial", 20));

		ArrayList<WorkPackage> wPackages = ActivityData.getActivity(yValue).getAssociatedWorkPackages();
		WorkPackage[] wpArray = wPackages.toArray(new WorkPackage[wPackages.size()]);
		Arrays.sort(wpArray);
		GridPane grid = new GridPane();
		grid.setGridLinesVisible(true);
		HashSet<String> set = new HashSet<>();
		int rowIndex = 0;
		for (int i = 0; i < wpArray.length; i++) {
			String packageName = wpArray[i].getName();
			if (set.add(packageName)) {
				Label nameLabel = new Label(packageName);
				nameLabel.setPadding(new Insets(2, 2, 2, 2));
				grid.add(nameLabel, 0, rowIndex);
				Label dateLabel = new Label(wpArray[i].getFinishDate().toString());
				dateLabel.setPadding(new Insets(2, 2, 2, 2));
				grid.add(dateLabel, 1, rowIndex);
				rowIndex++;
			}
		}
		vbox.getChildren().addAll(label, labelTMax, grid);

		ScrollPane scrollPane = new ScrollPane(vbox);
		scrollPane.setFitToHeight(true);
		Scene dialogScene = new Scene(scrollPane);
		dialog.setScene(dialogScene);
		dialog.show();
	}

	public static class Wpackage {
		private final SimpleStringProperty name;
		private final SimpleStringProperty date;

		private Wpackage(String sname, String sdate) {
			this.name = new SimpleStringProperty(sname);
			this.date = new SimpleStringProperty(sdate);
		}

		public String getName() {
			return name.get();
		}

		public void setName(String sname) {
			name.set(sname);
		}

		public String getDate() {
			return date.get();
		}

		public void setDate(String sdate) {
			date.set(sdate);
		}
	}

	public static class ExtraData {

		public long length;
		public String styleClass;

		public ExtraData(long lengthMs, String styleClass) {
			super();
			this.length = lengthMs;
			this.styleClass = styleClass;
		}

		public long getLength() {
			return length;
		}

		public void setLength(long length) {
			this.length = length;
		}

		public String getStyleClass() {
			return styleClass;
		}

		public void setStyleClass(String styleClass) {
			this.styleClass = styleClass;
		}

	}

	private double blockHeight = 17;

	public IndividualGanttChart(@NamedArg("xAxis") Axis<X> xAxis, @NamedArg("yAxis") Axis<Y> yAxis) {
		this(xAxis, yAxis, FXCollections.<Series<X, Y>>observableArrayList());
	}

	public IndividualGanttChart(@NamedArg("xAxis") Axis<X> xAxis, @NamedArg("yAxis") Axis<Y> yAxis,
			@NamedArg("data") ObservableList<Series<X, Y>> data) {
		super(xAxis, yAxis);
		if (!(xAxis instanceof ValueAxis && yAxis instanceof CategoryAxis)) {
			throw new IllegalArgumentException("Axis type incorrect, X and Y should both be NumberAxis");
		}
		setData(data);
	}

	private static String getStyleClass(Object obj) {
		return ((ExtraData) obj).getStyleClass();
	}

	private static double getLength(Object obj) {
		return ((ExtraData) obj).getLength();
	}

	@Override
	protected void layoutPlotChildren() {

		for (int seriesIndex = 0; seriesIndex < getData().size(); seriesIndex++) {

			Series<X, Y> series = getData().get(seriesIndex);

			Iterator<Data<X, Y>> iter = getDisplayedDataIterator(series);
			while (iter.hasNext()) {
				Data<X, Y> item = iter.next();
				double x = getXAxis().getDisplayPosition(item.getXValue());
				double y = getYAxis().getDisplayPosition(item.getYValue());
				if (Double.isNaN(x) || Double.isNaN(y)) {
					continue;
				}
				Node block = item.getNode();
				Rectangle ellipse;
				if (block != null) {
					if (block instanceof StackPane) {
						StackPane region = (StackPane) item.getNode();
						if (region.getShape() == null) {
							ellipse = new Rectangle(getLength(item.getExtraValue()), getBlockHeight());
						} else if (region.getShape() instanceof Rectangle) {
							ellipse = (Rectangle) region.getShape();
						} else {
							return;
						}
						ellipse.setWidth(getLength(item.getExtraValue())
								* ((getXAxis() instanceof NumberAxis) ? Math.abs(((NumberAxis) getXAxis()).getScale())
										: 1));
						ellipse.setHeight(getBlockHeight()
								* ((getYAxis() instanceof NumberAxis) ? Math.abs(((NumberAxis) getYAxis()).getScale())
										: 1));
						y -= getBlockHeight() / 2.0;

						region.setShape(null);
						region.setShape(ellipse);

						region.setScaleShape(false);
						region.setCenterShape(false);
						region.setCacheShape(false);

						block.setLayoutX(x);
						block.setLayoutY(y);
					}
				}
			}
		}
	}

	public double getBlockHeight() {
		return blockHeight;
	}

	public void setBlockHeight(double blockHeight) {
		this.blockHeight = blockHeight;
	}

	@Override
	protected void dataItemAdded(Series<X, Y> series, int itemIndex, Data<X, Y> item) {
		Node block = createContainer(series, getData().indexOf(series), item, itemIndex);
		getPlotChildren().add(block);
	}

	@Override
	protected void dataItemRemoved(final Data<X, Y> item, final Series<X, Y> series) {
		final Node block = item.getNode();
		getPlotChildren().remove(block);
		removeDataItemFromDisplay(series, item);
	}

	@Override
	protected void dataItemChanged(Data<X, Y> item) {
	}

	@Override
	protected void seriesAdded(Series<X, Y> series, int seriesIndex) {
		for (int j = 0; j < series.getData().size(); j++) {
			Data<X, Y> item = series.getData().get(j);
			Node container = createContainer(series, seriesIndex, item, j);
			getPlotChildren().add(container);
		}
	}

	@Override
	protected void seriesRemoved(final Series<X, Y> series) {
		for (XYChart.Data<X, Y> d : series.getData()) {
			final Node container = d.getNode();
			getPlotChildren().remove(container);
		}
		removeSeriesFromDisplay(series);

	}

	private Node createContainer(Series<X, Y> series, int seriesIndex, final Data<X, Y> item, int itemIndex) {

		Node container = item.getNode();

		if (container == null) {
			container = new StackPane();
			item.setNode(container);
		}

		container.getStyleClass().add(getStyleClass(item.getExtraValue()));

		return container;
	}

	@Override
	protected void updateAxisRange() {

		final Axis<X> xa = getXAxis();
		final Axis<Y> ya = getYAxis();
		List<X> xData = null;
		List<Y> yData = null;
		if (xa.isAutoRanging())
			xData = new ArrayList<X>();
		if (ya.isAutoRanging())
			yData = new ArrayList<Y>();
		if (xData != null || yData != null) {
			for (Series<X, Y> series : getData()) {
				for (Data<X, Y> data : series.getData()) {
					if (xData != null) {
						xData.add(data.getXValue());
						xData.add(
								xa.toRealValue(xa.toNumericValue(data.getXValue()) + getLength(data.getExtraValue())));
					}
					if (yData != null) {
						yData.add(data.getYValue());
					}
				}
			}
			if (xData != null)
				xa.invalidateRange(xData);
			if (yData != null)
				ya.invalidateRange(yData);
		}
	}
}