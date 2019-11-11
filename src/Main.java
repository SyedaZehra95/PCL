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
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Main extends Application {

	private boolean autoSelectFile = false;
	private final String FILE_NAME = "data11.xlsx";

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

		VBox main_vBox = new VBox(5);
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

		TextField resourceVariationLimitTextField = new TextField("10");
		TextField workingHoursTextField = new TextField("10");
		TextField RMinTextField = new TextField("2");
		TextField workingDaysPerWeekTextField = new TextField("5");
		TextField accuracyTextField = new TextField("10");

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
		grid_config.add(new Label("Min people per activity per day (R min)"), 0, position);
		grid_config.add(RMinTextField, 1, position);
		position++;
		grid_config.add(new Label("Accuracy"), 0, position);
		grid_config.add(accuracyTextField, 1, position);
		position++;
		grid_config.add(new Label(""), 0, position);
		position++;
		grid_config.add(new Label("If manhours below:"), 0, position);
		grid_config.add(new Label("Max % completion allowed per day:"), 1, position);
		position++;
		TextField slab1TextField = new TextField("100");
		grid_config.add(slab1TextField, 0, position);
		TextField percent1TextField = new TextField("100");
		grid_config.add(percent1TextField, 1, position);
		position++;
		TextField slab2TextField = new TextField("500");
		grid_config.add(slab2TextField, 0, position);
		TextField percent2TextField = new TextField("25");
		grid_config.add(percent2TextField, 1, position);
		position++;
		TextField slab3TextField = new TextField("1000");
		grid_config.add(slab3TextField, 0, position);
		TextField percent3TextField = new TextField("23");
		grid_config.add(percent3TextField, 1, position);
		position++;
		TextField slab4TextField = new TextField("2000");
		grid_config.add(slab4TextField, 0, position);
		TextField percent4TextField = new TextField("12.5");
		grid_config.add(percent4TextField, 1, position);
		position++;
		TextField slab5TextField = new TextField("4000");
		grid_config.add(slab5TextField, 0, position);
		TextField percent5TextField = new TextField("5.7143");
		grid_config.add(percent5TextField, 1, position);
		position++;
		grid_config.add(new Label("Any other manhours"), 0, position);
		TextField percent6TextField = new TextField("5.7143");
		grid_config.add(percent6TextField, 1, position);
		position++;

		int button_position = position;
		int[] progress = { 7 };
		Button buttonRunOptimization = new Button("Run Optimization");
		buttonRunOptimization.setId("dark-blue");
		HBox hbButton = new HBox(buttonRunOptimization);
		hbButton.setPadding(new Insets(10, 0, 0, 0));
		grid_config.add(hbButton, 1, position);
		position++;

		Label solution_count = new Label();
		grid_config.add(solution_count, 0, position);
		position++;

		final int grid_pos = position;
		buttonRunOptimization.setOnAction(new EventHandler<ActionEvent>() {
			@SuppressWarnings("unused")
			@Override
			public void handle(ActionEvent arg0) {
				HBox piBox = new HBox();

				ProgressIndicator pi = new ProgressIndicator();
				piBox.getChildren().add(pi);
				Label process_time = new Label();
				piBox.getChildren().add(process_time);
				HBox.setMargin(process_time, new Insets(15, 5, 5, 5));
				progress[0] = 1;

				HBox.setMargin(pi, new Insets(5, 5, 5, 135));
				// toggle_button(progress[0],hbButton,buttonRunOptimization);
				grid_config.add(piBox, 1, button_position);
				ActivityData.setAborted(false);
				setDataParams(Integer.parseInt(resourceVariationLimitTextField.getText()),
						Integer.parseInt(workingHoursTextField.getText()), Integer.parseInt(RMinTextField.getText()),
						Integer.parseInt(workingDaysPerWeekTextField.getText()),
						Integer.parseInt(accuracyTextField.getText()), Integer.parseInt(slab1TextField.getText()),
						Double.parseDouble(percent1TextField.getText()), Integer.parseInt(slab2TextField.getText()),
						Double.parseDouble(percent2TextField.getText()), Integer.parseInt(slab3TextField.getText()),
						Double.parseDouble(percent3TextField.getText()), Integer.parseInt(slab4TextField.getText()),
						Double.parseDouble(percent4TextField.getText()), Integer.parseInt(slab5TextField.getText()),
						Double.parseDouble(percent5TextField.getText()),
						Double.parseDouble(percent6TextField.getText()));

				new Thread(() -> {
					Algorithm algorithm = new Algorithm();
					Population resultPopulation = algorithm.autoRun(main_vBox, topHBox, grid_config, grid_pos,
							process_time);

					Platform.runLater(() -> {
						solution_count.setText("Number of solutions: " + ActivityData.getNumSolutions());

					});

					if (resultPopulation == null) {
						System.out.println("printing pop" + resultPopulation);
						Platform.runLater(() -> {
							piBox.getChildren().clear();
						});
					}
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
											System.out.println("I am here");
											resultPopulation.visualize(topHBox, main_vBox);
											pi.setProgress(100);
											progress[0] = 0;
											// toggle_button(progress[0],hbButton,buttonRunOptimization);

										}
									} else {
										if (topHBox.getChildren().size() > 1) {
											topHBox.getChildren().remove(1);
										}
										System.out.println("I am here as well");
										resultPopulation.getFittest(algorithm.getObjectiveId()).visualize(main_vBox);
									}
								}
							}
						});
					}
				}).start();
			}

			private void setDataParams(int limit, int workingHours, int RMin, int workingDays, int accuracyThreshold,
					int slab1, double percent1, int slab2, double percent2, int slab3, double percent3, int slab4,
					double percent4, int slab5, double percent5, double percent6) {
				ActivityData.setRMin(RMin);
				ActivityData.setResourceVariationLimit(limit);
				ActivityData.setWorkingHoursPerDay(workingHours);
				ActivityData.setNumberOfDaysPerWeek(workingDays);
				ActivityData.setAccuracyThreshold(accuracyThreshold);

				int[] percentCompletionSlabs = new int[] { slab1, slab2, slab3, slab4, slab5 };
				double[] percentCompletionAllowed = new double[] { percent1, percent2, percent3, percent4, percent5,
						percent6 };

				ActivityData.setPercentCompletionSlabs(percentCompletionSlabs);
				ActivityData.setPercentCompletionAllowed(percentCompletionAllowed);
			}
		});
		topHBox.getChildren().add(grid_config);
		main_vBox.getChildren().add(topHBox);
	}

	protected void reset(VBox main_vBox) {
		while (main_vBox.getChildren().size() > 1) {
			main_vBox.getChildren().remove(1);
		}
		ActivityData.reset();
	}

	protected void toggle_button(int progress, HBox hbox, Button button) {
		if (progress == 0) {
			hbox.getChildren().add(button);
		} else {
			hbox.getChildren().clear();
		}

	}

	@Override
	public void stop() throws Exception {
		ActivityData.setAborted(false);
	}
}
