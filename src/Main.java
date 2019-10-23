import java.io.File;
import java.util.Set;
import java.util.TreeSet;

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
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
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
	private final String FILE_NAME = "data15.xlsx";

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
		Stage stage=new Stage();
		topHBox.prefWidthProperty().bind(main_vBox.widthProperty());
		topHBox.setPadding(new Insets(10, 10, 10, 10));

		TextArea tArea = new TextArea();
		tArea.setText("");
		tArea.setMinHeight(300);
		
		
		TableView<Progress> tableView = new TableView<Progress>();
		
		tableView.setMinHeight(300.5);
	    tableView.setMinWidth(540);
	    tableView.setMaxWidth(435);
        TableColumn<Progress,String> column1 = new TableColumn<Progress,String>("Run");
        column1.setCellValueFactory(new PropertyValueFactory<>("index"));
        TableColumn<Progress,String> column2 = new TableColumn<Progress,String>("Man Hours Per Week");
        column2.setCellValueFactory(new PropertyValueFactory<>("ManHours"));
        TableColumn<Progress,String> column3 = new TableColumn<Progress,String>("Penalty");
        column3.setCellValueFactory(new PropertyValueFactory<>("Penalty"));
        TableColumn<Progress,String> column4 = new TableColumn<Progress,String>("  End Week  ");
        column4.setCellValueFactory(new PropertyValueFactory<>("EndWeek"));
        TableColumn<Progress,String> column5 = new TableColumn<Progress,String>("Average No. Of Days from TMin");
        column5.setCellValueFactory(new PropertyValueFactory<>("AvgFromStart"));
        
        
        tableView.getColumns().add(column1);
        tableView.getColumns().add(column2);
        tableView.getColumns().add(column3);
        tableView.getColumns().add(column4);
        tableView.getColumns().add(column5);
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        final KeyCodeCombination keyCodeCopy = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY);
        tableView.setOnKeyPressed(event -> {
            if (keyCodeCopy.match(event)) {
                copySelectionToClipboard(tableView);
            }
        });
		
		
        //tableView.prefHeightProperty().bind(stage.heightProperty());
        //tableView.prefWidthProperty().bind(stage.widthProperty());
        //tableView.resize(100, 100);

		TextField resourceVariationLimitTextField = new TextField("10");
		TextField workingHoursTextField = new TextField("10");
		TextField RMaxTextField = new TextField("80");
		TextField RMinTextField = new TextField("1");
		TextField workingDaysPerWeekTextField = new TextField("5");
		TextField accuracyTextField = new TextField("100");

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
		int button_position=position;
		int[] progress = {7};
		Button buttonRunOptimization = new Button("Run Optimization");
		buttonRunOptimization.setId("dark-blue");
		HBox hbButton = new HBox(buttonRunOptimization);
		hbButton.setPadding(new Insets(10, 0, 0, 0));
		grid_config.add(hbButton, 1, position);
		position++;
		buttonRunOptimization.setOnAction(new EventHandler<ActionEvent>() {
			@SuppressWarnings("unused")
			@Override
			public void handle(ActionEvent arg0) {
				HBox piBox=new HBox();
				
				ProgressIndicator pi = new ProgressIndicator();
				piBox.getChildren().add(pi);
				progress[0]=1;
				
				piBox.setMargin(pi, new Insets(5,5,5,135));
				//toggle_button(progress[0],hbButton,buttonRunOptimization);
				grid_config.add(piBox, 1, button_position);
				ActivityData.setAborted(false);
				setDataParams(Integer.parseInt(resourceVariationLimitTextField.getText()),
						Integer.parseInt(workingHoursTextField.getText()), Integer.parseInt(RMaxTextField.getText()),
						Integer.parseInt(RMinTextField.getText()),
						Integer.parseInt(workingDaysPerWeekTextField.getText()),
						Integer.parseInt(accuracyTextField.getText()));

				new Thread(() -> {
					Algorithm algorithm = new Algorithm();
					Population resultPopulation = algorithm.autoRun(tableView, main_vBox, topHBox);
					
					if(resultPopulation==null) {
						System.out.println("printing pop"+ resultPopulation);
						Platform.runLater(()->{
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
											progress[0]=0;
											//toggle_button(progress[0],hbButton,buttonRunOptimization);
											
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

		
		
		autoResizeColumns(tableView);
		VBox tableArea = new VBox(tableView);
		
		//tableArea.setPadding(new Insets(10, 0, 0, 0));
		
		grid_config.add(tableArea, 0, position, 2, 1);
		//grid_config.add(vbox, 0, position, 2, 1);
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
	protected void toggle_button(int progress, HBox hbox,Button button) {
		if(progress==0) {
			hbox.getChildren().add(button);
		}else {
			hbox.getChildren().clear();
		}
		
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
	
	@SuppressWarnings("rawtypes")
	public void copySelectionToClipboard(final TableView<?> table) {
	    final Set<Integer> rows = new TreeSet<>();
	    for (final TablePosition tablePosition : table.getSelectionModel().getSelectedCells()) {
	        rows.add(tablePosition.getRow());
	    }
	    final StringBuilder strb = new StringBuilder();
	    boolean firstRow = true;
	    for (final Integer row : rows) {
	        if (!firstRow) {
	            strb.append('\n');
	        }
	        firstRow = false;
	        boolean firstCol = true;
	        for (final TableColumn<?, ?> column : table.getColumns()) {
	            if (!firstCol) {
	                strb.append('\t');
	            }
	            firstCol = false;
	            final Object cellData = column.getCellData(row);
	            strb.append(cellData == null ? "" : cellData.toString());
	        }
	    }
	    final ClipboardContent clipboardContent = new ClipboardContent();
	    clipboardContent.putString(strb.toString());
	    Clipboard.getSystemClipboard().setContent(clipboardContent);
	}
}
