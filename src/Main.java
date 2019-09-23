import java.io.File;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Main extends Application {

	private boolean autoSelectFile = true;
	private final String FILE_NAME = "data10.xlsx";

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		AnchorPane root = new AnchorPane();
		Scene scene = new Scene(root, 100, 100);
		scene.getStylesheets().add("style.css");
		primaryStage.setScene(scene);
		primaryStage.setMaximized(true);
		primaryStage.show();

		VBox main_vBox = new VBox();
		main_vBox.prefWidthProperty().bind(root.widthProperty());
		main_vBox.prefHeightProperty().bind(root.heightProperty());

		Button buttonSelectDataset = new Button("Select Dataset");
		buttonSelectDataset.setId("btn-white");
		HBox hbSelectButton = new HBox(buttonSelectDataset);
		hbSelectButton.setBackground(
				new Background(new BackgroundFill(Color.web("#324851"), CornerRadii.EMPTY, Insets.EMPTY)));
		hbSelectButton.setPadding(new Insets(10, 10, 10, 10));
		buttonSelectDataset.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				reset(main_vBox);
				File file;
				if (autoSelectFile) {
					file = new File(FILE_NAME);
				} else {
					FileChooser fileChooser = new FileChooser();
					file = fileChooser.showOpenDialog(primaryStage);
				}
				if (file != null) {
					ActivityData.setDatasetFile(file);
					while (hbSelectButton.getChildren().size() > 1) {
						hbSelectButton.getChildren().remove(1);
					}
					buttonSelectDataset.setText("Processing Data");
					new Thread(() -> {
						ActivityData.loadExcel(file);
						Platform.runLater(new Runnable() {

							@Override
							public void run() {
								buttonSelectDataset.setText("Change Dataset");
								fileSelected(file, main_vBox, hbSelectButton);
							}
						});
					}).start();

				}
			}
		});

		main_vBox.getChildren().addAll(hbSelectButton);
		ScrollPane scroll = new ScrollPane();
		scroll.setContent(main_vBox);
		root.getChildren().addAll(scroll);
	}

	protected void fileSelected(File file, VBox main_vBox, HBox hbSelectButton) {
		HBox topHBox = new HBox();
		topHBox.prefWidthProperty().bind(main_vBox.widthProperty());
		topHBox.setPadding(new Insets(10, 10, 10, 10));

		TextArea tArea = new TextArea();
		tArea.setText("");
		tArea.setMinHeight(300);
		TableView tableView = new TableView();
		TableColumn<String, Progress> column1 = new TableColumn<>("Generation");
		column1.setCellValueFactory(new PropertyValueFactory<>("index"));
		TableColumn<String, Progress> column2 = new TableColumn<>("Max Work Penalty");
		column2.setCellValueFactory(new PropertyValueFactory<>("MaxWorkPenalty"));
		TableColumn<String, Progress> column3 = new TableColumn<>("Last Work Week");
		column3.setCellValueFactory(new PropertyValueFactory<>("LastWorkWeek"));
		TableColumn<String, Progress> column4 = new TableColumn<>("Late Start Penalty");
		column4.setCellValueFactory(new PropertyValueFactory<>("LateStartPenalty"));
		tableView.resize(100, 100);
		tableView.getColumns().add(column1);
		tableView.getColumns().add(column2);
		tableView.getColumns().add(column3);
		tableView.getColumns().add(column4);

		TextField resourceVariationLimitTextField = new TextField("10");
		TextField workingHoursTextField = new TextField("10");
		TextField RMaxTextField = new TextField("80");
		TextField RMinTextField = new TextField("1");
		TextField workingDaysPerWeekTextField = new TextField("5");
		TextField accuracyTextField = new TextField("1");

		Label labelFileName = new Label(file.getName() + " ✓");
		labelFileName.setTextFill(Color.web("#ffffff"));
		labelFileName.setFont(new Font("Arial", 22));
		labelFileName.setPadding(new Insets(0, 10, 0, 10));

		hbSelectButton.getChildren().add(labelFileName);
		GridPane grid_config = new GridPane();

		int position = 0;
		grid_config.add(new Label("No. of working hours per day"), 0, position);
		grid_config.add(workingHoursTextField, 1, position);
		position++;
		grid_config.add(new Label("No. of working days per week"), 0, position);
		grid_config.add(workingDaysPerWeekTextField, 1, position);
		position++;
		grid_config.add(new Label("Allowed variation in resource (%)"), 0, position);
		grid_config.add(resourceVariationLimitTextField, 1, position);
		position++;
		grid_config.add(new Label("Max people per activity per day (R max)"), 0, position);
		grid_config.add(RMaxTextField, 1, position);
		position++;
		grid_config.add(new Label("Min people per activity per day (R min)"), 0, position);
		grid_config.add(RMinTextField, 1, position);
		position++;
		grid_config.add(new Label("Accuracy"), 0, position);
		grid_config.add(accuracyTextField, 1, position);
		position++;

		Button buttonRunOptimization = new Button("Run Optimization");
		buttonRunOptimization.setId("dark-blue");
		buttonRunOptimization.setOnAction(new EventHandler<ActionEvent>() {
			@SuppressWarnings("unused")
			@Override
			public void handle(ActionEvent arg0) {
				ActivityData.setAborted(false);
				setDataParams(Integer.parseInt(resourceVariationLimitTextField.getText()),
						Integer.parseInt(workingHoursTextField.getText()), Integer.parseInt(RMaxTextField.getText()),
						Integer.parseInt(RMinTextField.getText()),
						Integer.parseInt(workingDaysPerWeekTextField.getText()),
						Integer.parseInt(accuracyTextField.getText()));

				new Thread(() -> {
					Algorithm algorithm = new Algorithm();
					Population resultPopulation = algorithm.autoRun(tableView, main_vBox, topHBox);
					if (!ActivityData.isAborted()) {
						Platform.runLater(new Runnable() {

							@Override
							public void run() {
								if (resultPopulation.getIndividual(0).getFitness()[0] == ActivityData
										.getErrorFitness()) {
									Alert alert = new Alert(AlertType.ERROR);
									alert.setTitle("Error");
									alert.setHeaderText("Resource limit too low!");
									alert.showAndWait();
								} else {
									if (algorithm.getObjectiveId() == 100) {
										if (!ActivityData.isAborted()) {
											resultPopulation.visualize(topHBox, main_vBox);
										}
									} else {
										if (topHBox.getChildren().size() > 1) {
											topHBox.getChildren().remove(1);
										}
										resultPopulation.getFittest(algorithm.getObjectiveId()).visualize(main_vBox);
									}
								}
							}
						});
					}
				}).start();
			}

			private void setDataParams(int limit, int workingHours, int RMax, int RMin, int workingDays,
					int accuracyThreshold) {
				ActivityData.setRMax(RMax);
				ActivityData.setRMin(RMin);
				ActivityData.setResourceVariationLimit(limit);
				ActivityData.setWorkingHoursPerDay(workingHours);
				ActivityData.setNumberOfDaysPerWeek(workingDays);
				ActivityData.setAccuracyThreshold(accuracyThreshold);
			}
		});

		HBox hbButton = new HBox(buttonRunOptimization);
		hbButton.setPadding(new Insets(10, 0, 0, 0));
		grid_config.add(hbButton, 1, position);
		position++;
		autoResizeColumns(tableView);
		VBox tableArea = new VBox(tableView);
		tableArea.setPadding(new Insets(10, 0, 0, 0));
		grid_config.add(tableArea, 0, position, 2, 1);
		position++;
		topHBox.getChildren().add(grid_config);
		main_vBox.getChildren().add(topHBox);
	}

	protected void reset(VBox main_vBox) {
		while (main_vBox.getChildren().size() > 1) {
			main_vBox.getChildren().remove(1);
		}
		ActivityData.reset();
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
}
