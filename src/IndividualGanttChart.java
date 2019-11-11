import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.controlsfx.control.PopOver;

import com.flexganttfx.extras.GanttChartStatusBar;
import com.flexganttfx.extras.GanttChartToolBar;
import com.flexganttfx.model.ActivityRef;
import com.flexganttfx.model.Layer;
import com.flexganttfx.model.Row;
import com.flexganttfx.model.activity.MutableActivityBase;
import com.flexganttfx.model.layout.GanttLayout;
import com.flexganttfx.view.GanttChart;
import com.flexganttfx.view.GanttChart.DisplayMode;
import com.flexganttfx.view.graphics.GraphicsBase;
import com.flexganttfx.view.graphics.ListViewGraphics;
import com.flexganttfx.view.graphics.renderer.ActivityBarRenderer;
import com.flexganttfx.view.timeline.Timeline;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;

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

		public PackageData(String packageName, LocalDate startTime, LocalDate endTime) {
			this.packageName = packageName;
			this.startTime = startTime.atStartOfDay().toInstant(ZoneOffset.UTC);
			this.endTime = endTime.atStartOfDay().toInstant(ZoneOffset.UTC);
		}
	}

	/*
	 * The activity representing the Pack. This object will be rendered as a bar in
	 * the graphics view of the Gantt chart. The Pack is mutable, so the user will
	 * be able to interact with it.
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
		private String name = null;
		private String resource = "0.0";

		public WorkPackages(String name, String resource) {
			this.setName(name);
			this.setResource(resource);
		}

		public void setResource(String resource) {
			this.resource = resource;
		}

		public String getResource() {
			return this.resource;
		}
	}

	public GanttChart start(Individual individual, VBox main_vbox) {

		// Create the Gantt chart
		gantt = new GanttChart<WorkPackages>(new WorkPackages("Packages", " "));

		Layer layer = new Layer("Packs");
		gantt.getLayers().add(layer);
		this.ind = individual;
		TreeTableView<WorkPackages> table = gantt.getTreeTable();
		TreeTableColumn<WorkPackages, String> col = new TreeTableColumn<>("Resources");
		col.setCellValueFactory(new TreeItemPropertyValueFactory<>("resource"));
		table.getColumns().add(col);
		for (int i = 0; i < ActivityData.size(); i++) {
			String packageName = ActivityData.getActivity(i).getName();
			LocalDate endDate = LocalDate.of(individual.getGene(i).getEndDate().getYear(),
					individual.getGene(i).getEndDate().getMonthOfYear(),
					individual.getGene(i).getEndDate().getDayOfMonth());
			LocalDate startDate = LocalDate.of(individual.getGene(i).getStartDate().getYear(),
					individual.getGene(i).getStartDate().getMonthOfYear(),
					individual.getGene(i).getStartDate().getDayOfMonth());
			LocalDate activeDate = LocalDate.of(individual.getGene(i).getActivationDate().getYear(),
					individual.getGene(i).getActivationDate().getMonthOfYear(),
					individual.getGene(i).getActivationDate().getDayOfMonth());
			LocalDate deactiveDate = LocalDate.of(individual.getGene(i).getDeactivationDate().getYear(),
					individual.getGene(i).getDeactivationDate().getMonthOfYear(),
					individual.getGene(i).getDeactivationDate().getDayOfMonth());

			double res = (individual.getGene(i).getNumberOfResources() * individual.getGene(i).getRatioOfLastDayUsed()
					* ActivityData.workingHoursPerDay());
			if (activeDate.isBefore(deactiveDate)) {
				res = individual.getGene(i).getNumberOfResources() * ActivityData.workingHoursPerDay();
			}
			WorkPackages ac = new WorkPackages(packageName, this.df.format(res));

			// "Resource="+(individual.getGene(i).getNumberOfResources()*ActivityData.workingHoursPerDay())
			ac.addActivity(layer, new Pack(new PackageData("Tmin" + startDate, startDate, startDate)));
			ac.addActivity(layer, new Pack(new PackageData(packageName + "/" + i, activeDate, deactiveDate)));
			ac.addActivity(layer, new Pack(new PackageData("Tmax" + endDate, endDate, endDate)));
			layer.setVisible(true);

			gantt.getRoot().getChildren().add(ac);
			// int l=gantt.getTreeTable().getRow(new TreeItem<WorkPackages>(new
			// WorkPackages(packageName)));

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
		StackPane sp = individual.layerCharts(individual.createResourceVariationChart(),
				individual.createLineResourceVariationChart());
		// gantt.setShowDetail(true);
		Platform.runLater(() -> {
			Screen screen = Screen.getPrimary();
			ScrollPane root = new ScrollPane();

			Stage stage = new Stage();

			Label title = new Label("Max manhours: " + individual.getHighestManhours() + " | End week: "
					+ individual.getProjectEndWeek() + " | Duration: "
					+ (individual.getFitness()[0] - individual.getStartWeek()) + " weeks | Penalty: "
					+ individual.getPenalty() + " | Avg no. of days from T-min: "
					+ this.df.format(individual.getAverageLateStartDays()) + " | Weighted Avg: "
					+ this.df.format(individual.getWeightedAverageLateStartDays()));
			title.setPadding(new Insets(10, 10, 10, 10));
			title.setFont(javafx.scene.text.Font.font("Arial", FontWeight.BOLD, 14));
			Scene scene = new Scene(root);
			VBox box = new VBox();
			box.getChildren().addAll(title, new GanttChartToolBar(gantt), gantt, new GanttChartStatusBar(gantt));
			box.getChildren().add(sp);
			box.setMinWidth(screen.getVisualBounds().getWidth());
			box.setMinHeight(screen.getVisualBounds().getHeight());
			root.setContent(box);
			// main_vbox.getChildren().add(gantt);
			stage.setScene(scene);
			stage.sizeToScene();
			stage.centerOnScreen();
			stage.setMaximized(true);
			stage.show();
		});

		return gantt;
	}

	public GanttChart startGanttChart(Individual individual, VBox main_vBox) {
		GanttChart gantt = start(individual, main_vBox);
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
				String[] yValue = ref.getActivity().getName().split("/");
				// String yValue=ref.getActivity().getName();
				popOver.setTitle("Test Package" + yValue[0]);
				LocalDate activeDate = LocalDate.of(
						this.ind.getGene(Integer.parseInt(yValue[1])).getActivationDate().getYear(),
						this.ind.getGene(Integer.parseInt(yValue[1])).getActivationDate().getMonthOfYear(),
						this.ind.getGene(Integer.parseInt(yValue[1])).getActivationDate().getDayOfMonth());
				LocalDate deactiveDate = LocalDate.of(
						this.ind.getGene(Integer.parseInt(yValue[1])).getDeactivationDate().getYear(),
						this.ind.getGene(Integer.parseInt(yValue[1])).getDeactivationDate().getMonthOfYear(),
						this.ind.getGene(Integer.parseInt(yValue[1])).getDeactivationDate().getDayOfMonth());

				double res = (this.ind.getGene(Integer.parseInt(yValue[1])).getNumberOfResources()
						* this.ind.getGene(Integer.parseInt(yValue[1])).getRatioOfLastDayUsed()
						* ActivityData.workingHoursPerDay());
				if (activeDate.isBefore(deactiveDate)) {
					res = (this.ind.getGene(Integer.parseInt(yValue[1])).getNumberOfResources()
							* ActivityData.workingHoursPerDay());
				}
				VBox vbox = new VBox();
				vbox.setSpacing(5);
				vbox.setPadding(new Insets(10, 10, 10, 10));
				Label label = new Label("Test package: " + yValue[0] + "\n");
				label.setFont(new Font("Arial", 20));
				Label labelTMax = new Label(
						"Tmax: " + ActivityData.getActivity(yValue[0]).getEndDate().toString() + "\n");
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
				vbox.getChildren().addAll(label, labelTMax, labelResource, grid);

				popOver.setContentNode(vbox);
				popOver.show(gantt.getGraphics(), x, y, javafx.util.Duration.ONE);
			}
		} else {
			if (popOver != null && !popOver.isDetached()) {
				popOver.hide();
			}
		}

	}

}