import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;

import org.controlsfx.control.PopOver;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;
import com.flexganttfx.extras.GanttChartStatusBar;
import com.flexganttfx.extras.GanttChartToolBar;
import com.flexganttfx.model.ActivityRef;
import com.flexganttfx.model.Layer;
import com.flexganttfx.model.layout.GanttLayout;
import com.flexganttfx.model.Row;
import com.flexganttfx.model.activity.MutableActivityBase;
import com.flexganttfx.view.GanttChart;
import com.flexganttfx.view.GanttChart.DisplayMode;
import com.flexganttfx.view.graphics.GraphicsBase;
import com.flexganttfx.view.graphics.ListViewGraphics;
import com.flexganttfx.view.graphics.renderer.ActivityBarRenderer;
import com.flexganttfx.view.timeline.Timeline;

public class IndividualGanttChart {
	/*
	 * Plain data object storing dummy Pack information.
	 */
	private Individual ind;
	 private PopOver popOver;
	 GanttChart<WorkPackages> gantt;
	 DecimalFormat df = new DecimalFormat("0.00");
	class PackageData {

		String packageName;
		Instant startTime = Instant.now();
		Instant endTime = Instant.now().plus(Duration.ofHours(6));
		
		public PackageData(String packageName, LocalDate startTime,LocalDate endTime) {
			this.packageName = packageName;
			this.startTime = startTime.atStartOfDay().toInstant(ZoneOffset.UTC);
			this.endTime = endTime.atStartOfDay().toInstant(ZoneOffset.UTC);
		}
	}

	/*
	 * The activity representing the Pack. This object will be rendered as a
	 * bar in the graphics view of the Gantt chart. The Pack is mutable, so
	 * the user will be able to interact with it.
	 */
	class Pack extends MutableActivityBase<PackageData> {
		public Pack(PackageData data) {
			setUserObject(data);
			setName(data.packageName);
			setStartTime(data.startTime);
			setEndTime(data.endTime);
		}
	}

	/*
	 * Each row represents an WorkPackage in this example. The activities shown on
	 * the row are of type Pack.
	 */
	
	public class WorkPackages extends Row {
		private String name=null;
		private String resource="0.0";
		
		public WorkPackages(String name,String resource) {
			this.setName(name);
			this.setResource(resource);
		}
		public void setResource(String resource) {
			this.resource=resource;
		}
		public String getResource() {
			return this.resource;
		}
	}

	public GanttChart start(Individual individual,VBox main_vbox) {

		// Create the Gantt chart
		 gantt = new GanttChart<WorkPackages>(new WorkPackages(
				"Packages"," "));

		Layer layer = new Layer("Packs");
		gantt.getLayers().add(layer);
		this.ind=individual;
		TreeTableView<WorkPackages> table=gantt.getTreeTable();
		TreeTableColumn<WorkPackages,String> col=new TreeTableColumn<>("Resources");
		col.setCellValueFactory(new TreeItemPropertyValueFactory<>("resource"));
		table.getColumns().add(col);
		int stepSize = (ActivityData.RMax() - ActivityData.RMin()) / 4;
		for (int i = 0; i < ActivityData.size(); i++) {
			String packageName = ActivityData.getActivity(i).getName();
			
			LocalDate baseDate= LocalDate.of(ActivityData.getBaseDate().getYear(),ActivityData.getBaseDate().getMonthOfYear(), ActivityData.getBaseDate().getDayOfMonth());
			LocalDate endDate= LocalDate.of(individual.getGene(i).getEndDate().getYear(),individual.getGene(i).getEndDate().getMonthOfYear(), individual.getGene(i).getEndDate().getDayOfMonth());
			LocalDate startDate= LocalDate.of(individual.getGene(i).getStartDate().getYear(),individual.getGene(i).getStartDate().getMonthOfYear(), individual.getGene(i).getStartDate().getDayOfMonth());
			LocalDate activeDate= LocalDate.of(individual.getGene(i).getActivationDate().getYear(),individual.getGene(i).getActivationDate().getMonthOfYear(), individual.getGene(i).getActivationDate().getDayOfMonth());
			LocalDate deactiveDate= LocalDate.of(individual.getGene(i).getDeactivationDate().getYear(),individual.getGene(i).getDeactivationDate().getMonthOfYear(), individual.getGene(i).getDeactivationDate().getDayOfMonth());
			
			
			double res= (individual.getGene(i).getNumberOfResources()*individual.getGene(i).getRatioOfLastDayUsed()*ActivityData.workingHoursPerDay());
			if(activeDate.isBefore(deactiveDate)) {
				res=individual.getGene(i).getNumberOfResources()*ActivityData.workingHoursPerDay();
			}
			WorkPackages ac = new WorkPackages(packageName,this.df.format(res));
			
			boolean show = false;
			boolean[] checkBoxTruth = ActivityData.getCheckBoxTruth();

			//"Resource="+(individual.getGene(i).getNumberOfResources()*ActivityData.workingHoursPerDay())
			ac.addActivity(layer, new Pack(new PackageData("Tmin"+startDate,startDate,startDate)));
			ac.addActivity(layer, new Pack(new PackageData(packageName+"/"+i, activeDate,deactiveDate)));
			ac.addActivity(layer, new Pack(new PackageData("Tmax"+endDate, endDate,endDate)));
			layer.setVisible(true);
		
			gantt.getRoot().getChildren().add(ac);
			//int l=gantt.getTreeTable().getRow(new TreeItem<WorkPackages>(new WorkPackages(packageName)));
			
			
			
			
		}

		

		

		Timeline timeline = gantt.getTimeline();
		timeline.showTemporalUnit(ChronoUnit.HOURS, 10);
		
		GraphicsBase<WorkPackages> graphics = gantt.getGraphics();
		ListViewGraphics<WorkPackages> graphic = gantt.getGraphics();
		graphics.setActivityRenderer(Pack.class, GanttLayout.class,
				new ActivityBarRenderer<>(graphics, "PackRenderer"));
		graphic.getListView().addEventHandler(MouseEvent.MOUSE_MOVED, evt -> mouseMoved(evt));
		
		graphics.showEarliestActivities();
		gantt.setDisplayMode(DisplayMode.STANDARD);
		gantt.setShowTreeTable(true);
		gantt.requestFocus();
		System.out.println(gantt.getTooltip());
		StackPane sp=individual.layerCharts(individual.createResourceVariationChart(),individual.createLineResourceVariationChart());
		//gantt.setShowDetail(true);
		Platform.runLater(()->{
			Screen screen = Screen.getPrimary();
			ScrollPane root = new ScrollPane();
			
			Stage stage=new Stage();
			
			Label title=new Label("Max manhours: " + individual.getHighestManhours() + " | End week: " + individual.getProjectEndWeek()
						+ " | Duration: " + (individual.getFitness()[0] - individual.getStartWeek())
						+ " weeks | Penalty: " + individual.getPenalty() + " | Avg no. of days from T-min: "
						+ this.df.format(individual.getAverageLateStartDays()) + " | Weighted Avg: "
						+ this.df.format(individual.getWeightedAverageLateStartDays()));
			title.setPadding(new Insets(10,10,10,10));
			title.setFont(  javafx.scene.text.Font.font("Arial",FontWeight.BOLD,14) );
			Scene scene = new Scene(root);
			VBox box =new VBox();
			box.getChildren().addAll(title,new GanttChartToolBar(gantt),gantt,new GanttChartStatusBar(gantt));
			box.getChildren().add(sp);
			box.setMinWidth(screen.getVisualBounds().getWidth());
			box.setMinHeight(screen.getVisualBounds().getHeight());
			root.setContent(box);
			//main_vbox.getChildren().add(gantt);
			stage.setScene(scene);
			stage.sizeToScene();
			stage.centerOnScreen();
			stage.setMaximized(true);
			stage.show();
		});
		
		
		return gantt;
	}
	
	public GanttChart startGanttChart(Individual individual,VBox main_vBox) {

		/*final NumberAxis xAxis = new NumberAxis();
		final CategoryAxis yAxis = new CategoryAxis();
		chart = new IndividualGanttChart<Number, String>(xAxis, yAxis);
		return chart.run(individual);*/
		GanttChart gantt = start(individual,main_vBox);
		
		
		return gantt;

		
	}
	
	
	 
	    private void mouseMoved(MouseEvent evt) {
	        ActivityRef<?> ref = gantt.getGraphics().getActivityRefAt(evt.getX(), evt.getY());
	        if (ref != null) {
	            if (popOver == null || popOver.isDetached()) {
	                popOver = new PopOver();
	                popOver.setArrowLocation(PopOver.ArrowLocation.TOP_CENTER);
	                popOver.getContentNode().setMouseTransparent(true);
	            }
	 
	            double x = evt.getScreenX();
	            double y = evt.getScreenY();
	 
	            if (!popOver.isShowing()) {
	            	String[] yValue=ref.getActivity().getName().split("/");
	            	//String yValue=ref.getActivity().getName();
	                popOver.setTitle("Test Package"+yValue[0]);
	                LocalDate activeDate= LocalDate.of(this.ind.getGene(Integer.parseInt(yValue[1])).getActivationDate().getYear(),this.ind.getGene(Integer.parseInt(yValue[1])).getActivationDate().getMonthOfYear(), this.ind.getGene(Integer.parseInt(yValue[1])).getActivationDate().getDayOfMonth());
	    			LocalDate deactiveDate= LocalDate.of(this.ind.getGene(Integer.parseInt(yValue[1])).getDeactivationDate().getYear(),this.ind.getGene(Integer.parseInt(yValue[1])).getDeactivationDate().getMonthOfYear(), this.ind.getGene(Integer.parseInt(yValue[1])).getDeactivationDate().getDayOfMonth());
	    			
	    			double res= (this.ind.getGene(Integer.parseInt(yValue[1])).getNumberOfResources()*this.ind.getGene(Integer.parseInt(yValue[1])).getRatioOfLastDayUsed()*ActivityData.workingHoursPerDay());
	    			if(activeDate.isBefore(deactiveDate)) {
	    				res= (this.ind.getGene(Integer.parseInt(yValue[1])).getNumberOfResources()*ActivityData.workingHoursPerDay());
	    			}
	                VBox vbox = new VBox();
	    			vbox.setSpacing(5);
	    			vbox.setPadding(new Insets(10, 10, 10, 10));
	                Label label = new Label("Test package: " + yValue[0] + "\n");
	    			label.setFont(new Font("Arial", 20));
	    			Label labelTMax = new Label("Tmax: " + ActivityData.getActivity(yValue[0]).getEndDate().toString() + "\n");
	    			labelTMax.setFont(new Font("Arial", 20));
	    			
	    			
	    			Label labelResource = new Label("Resources: " + this.df.format(res) + "\n");
	    			labelResource.setFont(new Font("Arial", 20));
	    			ArrayList<WorkPackage> wPackages = ActivityData.getActivity(yValue[0]).getAssociatedWorkPackages();
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
	    			vbox.getChildren().addAll(label, labelTMax,labelResource, grid);
	    			
	    			popOver.setContentNode(vbox);
	                popOver.show(gantt.getGraphics(),x,y, javafx.util.Duration.ONE);
	            }
	        } else {
	            if (popOver != null && !popOver.isDetached()) {
	                popOver.hide();
	            }
	        }

	    }

	/*static IndividualGanttChart<Number, String> chart;
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

		chart.setTitle(
				"Max manhours: " + individual.getHighestManhours() + " | End week: " + individual.getProjectEndWeek()
						+ " | Duration: " + (individual.getFitness()[0] - individual.getStartWeek())
						+ " weeks | Penalty: " + individual.getPenalty() + " | Avg no. of days from T-min: "
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
			System.out.println(series.getData());
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
	}*/
}