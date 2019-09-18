import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.Days;
import org.joda.time.LocalDate;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class Individual {
	private Activity[] genes;
	private int[] fitness = new int[] { -1, -1, -1 };
	private int[] resourceLevels;
	private HashMap<Integer, Integer> weeklyResourceLevels;
	private HashMap<Integer, String> weekDates;
	private boolean selected = false;
	private int rank = 0;
	private LocalDate lastDate;
	private int startIndex = -1;
	private int startWeek = -1;
	private int highestResourceLevel = -1;
	private int penalty = -1;
	private double weightedAverageLateStartDays = -1;

	public Individual() {
		this.genes = new Activity[ActivityData.size()];
	}

	public Individual(Individual individual) {
		this.genes = new Activity[ActivityData.size()];
		for (int i = 0; i < ActivityData.size(); i++) {
			if (ActivityData.isAborted()) {
				return;
			}
			Activity tempGene = new Activity(individual.getGene(i));
			this.setGene(i, tempGene);
		}
	}

	public void setGene(int id, Activity gene) {
		this.genes[id] = gene;
	}

	public Activity getGene(int id) {
		return this.genes[id];
	}

	public int getStartDay() {
		if (startIndex == -1) {
			startIndex = 0;
			while (resourceLevels[startIndex] == 0) {
				startIndex++;
			}
		}
		return startIndex;
	}

	public int getStartWeek() {
		if (startWeek == -1) {
			startWeek = 0;
			while (weeklyResourceLevels.get(startWeek) == 0) {
				startWeek++;
			}
		}
		return startWeek;
	}

	public Individual generateSequence() {
		for (int i = 0; i < ActivityData.size(); i++) {
			genes[i] = new Activity(i);
		}
		return this;
	}

	public int[] getFitness() {
		if (fitness[0] == -1) {
			lastDate = genes[0].getDeactivationDate();

			for (Activity gene : genes) {
				if (gene.getDeactivationDate().isAfter(lastDate)) {
					lastDate = gene.getDeactivationDate();
				}
			}

			int[] tempResourceLevels = new int[Days.daysBetween(ActivityData.getBaseDate(), lastDate).getDays() + 1];

			for (Activity activity : this.genes) {
				int start = Days.daysBetween(ActivityData.getBaseDate(), activity.getActivationDate()).getDays();
				int end = Days.daysBetween(ActivityData.getBaseDate(), activity.getDeactivationDate()).getDays();
				int k = start;
				for (k = start; k < end; k++) {
					if (ActivityData.getBaseDate().plusDays(k).getDayOfWeek() <= ActivityData
							.getNumberOfDaysPerWeek()) {
						tempResourceLevels[k] += activity.getNumberOfResources();
					} else {
						tempResourceLevels[k] = -1;
					}
				}

				if (k == end) {
					if (ActivityData.getBaseDate().plusDays(k).getDayOfWeek() <= ActivityData
							.getNumberOfDaysPerWeek()) {
						tempResourceLevels[k] += (activity.getNumberOfResources() * activity.getRatioOfLastDayUsed())
								+ 1;
					} else {
						tempResourceLevels[k] = -1;
					}
				}
			}

			ArrayList<Integer> list = new ArrayList<>();

			for (int i = 0; i < tempResourceLevels.length; i++) {
				if (tempResourceLevels[i] != -1) {
					list.add(tempResourceLevels[i]);
				}
			}

			resourceLevels = new int[list.size()];

//			int debug_sum = 0;
			for (int i = 0; i < resourceLevels.length; i++) {
				resourceLevels[i] = list.get(i);
//				debug_sum += resourceLevels[i];
			}

//			System.out.println(debug_sum * 10);

			int currentWeek = 0;
			int i = 0;
			int weeklyResourceTotal = 0;
			int currentDay = 0;

			weeklyResourceLevels = new HashMap<>();
			weekDates = new HashMap<>();

			while (i < this.resourceLevels.length) {
				if (ActivityData.getBaseDate().plusDays(currentDay).getDayOfWeek() <= ActivityData
						.getNumberOfDaysPerWeek()) {
					if (weeklyResourceTotal == -1) {
						weeklyResourceTotal++;
					}
					weeklyResourceTotal += resourceLevels[i];
					i++;
				} else {
					if (weeklyResourceTotal != -1) {
						weeklyResourceLevels.put(currentWeek, weeklyResourceTotal);
						if (currentWeek == 0) {
							weekDates.put(currentWeek, ActivityData.getBaseDate().toString());
						} else {
							weekDates.put(currentWeek, ActivityData.getBaseDate()
									.plusDays(currentDay - ActivityData.getNumberOfDaysPerWeek()).toString());
						}
						currentWeek++;
					}
					weeklyResourceTotal = -1;
//					if (resourceLevels[i] == 0) {
//						i++;
//					}
				}
				currentDay++;
			}
			weeklyResourceLevels.put(currentWeek, weeklyResourceTotal);
			LocalDate date = ActivityData.getBaseDate().plusDays(currentDay);
			weekDates.put(currentWeek, date.minusDays(date.getDayOfWeek() - 1).toString());

			fitness[0] = weeklyResourceLevels.size() - 1;

		}

		if (fitness[1] == -1) {
			int highestResourceLevel = 0;
			int changeInResources = 0;
			startIndex = 0;
			startWeek = 0;
			while (weeklyResourceLevels.get(startWeek) == 0) {
				startWeek++;
			}

			for (int i = startWeek + 1; i < weeklyResourceLevels.size(); i++) {
				if (weeklyResourceLevels.get(i) == 0) {
					changeInResources++;
				}
			}

			int halfIndex = ((startWeek + weeklyResourceLevels.size()) / 2) + 1;

			for (int i = 0; i < halfIndex; i++) {
				if (weeklyResourceLevels.get(i) > highestResourceLevel) {
					highestResourceLevel = weeklyResourceLevels.get(i);
				} else {
					int threshold = 0;
					if (ActivityData.getResourceVariationLimit() != 0) {
						threshold = (int) Math
								.ceil((highestResourceLevel * ActivityData.getResourceVariationLimit()) / 100);
					}
					if ((highestResourceLevel - weeklyResourceLevels.get(i)) > threshold) {
						changeInResources += highestResourceLevel - weeklyResourceLevels.get(i);
					}

				}
			}

			int lowestResourceLevel = highestResourceLevel;
			for (int i = halfIndex; i < weeklyResourceLevels.size(); i++) {
				if (weeklyResourceLevels.get(i) < lowestResourceLevel) {
					lowestResourceLevel = weeklyResourceLevels.get(i);
				} else {
					int threshold = 0;
					if (ActivityData.getResourceVariationLimit() != 0) {
						threshold = (int) Math
								.ceil((lowestResourceLevel * ActivityData.getResourceVariationLimit()) / 100);
					}
					if ((weeklyResourceLevels.get(i) - lowestResourceLevel) > threshold) {
						changeInResources += weeklyResourceLevels.get(i) - lowestResourceLevel;
					}

				}
			}

			this.setHighestResourceLevel(highestResourceLevel);
			this.setPenalty(changeInResources);

			fitness[1] = highestResourceLevel + changeInResources;

		}

		if (fitness[2] == -1) {
			double sum = 0;
			double total = 0;
			int distancePenalty = 0;
			for (Activity gene : genes) {
				int days = Days.daysBetween(gene.getStartDate(), gene.getActivationDate()).getDays();
				distancePenalty += days * Math.pow(gene.getManHours(), 2);
				sum += days * gene.getManHours();
				total += gene.getManHours();
			}

			setWeightedAverageLateStartDays(sum / total);
			fitness[2] = distancePenalty;
		}
		return fitness;
	}

	public void resetFitness() {
		this.fitness = new int[] { -1, -1, -1 };
	}

	public boolean isSelected() {
		return selected;
	}

	public void markSelected() {
		this.selected = true;
	}

	public void sysout() {
		System.out.println("Duration: " + this.getFitness()[0] + ", Leveling: " + this.getFitness()[1]);
//		for (int i = 0; i < ActivityData.size(); i++) {
//			System.out.println(this.gene(i).R() + ", " + this.gene(i).T() + ", "
//					+ (this.gene(i).T() + this.gene(i).durationHours()));
//		}
	}

	public void visualize(VBox main_vBox) {
		if (main_vBox.getChildren().size() > 10) {
			for (int i = 25; i < main_vBox.getChildren().size(); i++) {
				main_vBox.getChildren().remove(i);
			}
		}

		if (main_vBox.getChildren().size() == 2) {
			File imgFile = new File("legend.png");
			Image image = new Image(imgFile.toURI().toString());
			ImageView imageView = new ImageView();
			imageView.setImage(image);

			CheckBox checkBox1 = new CheckBox("Show R min");
			checkBox1.setSelected(ActivityData.getCheckBoxTruth()[0]);
			CheckBox checkBox2 = new CheckBox("Show R1");
			checkBox2.setSelected(ActivityData.getCheckBoxTruth()[1]);
			CheckBox checkBox3 = new CheckBox("Show R2");
			checkBox3.setSelected(ActivityData.getCheckBoxTruth()[2]);
			CheckBox checkBox4 = new CheckBox("Show R3");
			checkBox4.setSelected(ActivityData.getCheckBoxTruth()[3]);
			CheckBox checkBox5 = new CheckBox("Show R max");
			checkBox5.setSelected(ActivityData.getCheckBoxTruth()[4]);

			Button applyButton = new Button("Appy changes");
			applyButton.setId("dark-blue");
			applyButton.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent arg0) {
					ActivityData.setCheckBoxTruth(new boolean[] { checkBox1.isSelected(), checkBox2.isSelected(),
							checkBox3.isSelected(), checkBox4.isSelected(), checkBox5.isSelected() });
					visualize(main_vBox);
				}
			});

			VBox vBoxButton = new VBox();
			vBoxButton.getChildren().add(applyButton);
			vBoxButton.setPadding(new Insets(8, 0, 0, 0));
			VBox vBox = new VBox();
			vBox.getChildren().addAll(checkBox1, checkBox2, checkBox3, checkBox4, checkBox5, vBoxButton);
			HBox hBox = new HBox();
			hBox.getChildren().addAll(imageView, vBox);
			hBox.setPadding(new Insets(0, 0, 10, 20));
			main_vBox.getChildren().add(hBox);
		}

		Button printToExcelButton = new Button("Save as excel file");
		printToExcelButton.setId("dark-blue");
		printToExcelButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				printToExcelButton.setText("Saved as HTSchedule.xlsx ✓");
				printToExcel();
			}

		});

		HBox hBox = new HBox();
		hBox.getChildren().add(printToExcelButton);
		hBox.setAlignment(Pos.BASELINE_RIGHT);
		hBox.setPadding(new Insets(10, 60, 10, 10));

		VBox vBox = new VBox();
		vBox.getChildren().addAll(IndividualGanttChart.startGanttChart(this), hBox);
		main_vBox.getChildren().add(3, vBox);
		main_vBox.getChildren().add(4, createResourceVariationChart());
		HBox hb = new HBox();
		hb.setBackground(new Background(new BackgroundFill(Color.web("#324851"), CornerRadii.EMPTY, Insets.EMPTY)));
		hb.setMinHeight(10);
		main_vBox.getChildren().add(5, hb);
	}

	public void printToExcel() {
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("Schedule");

		int rownum = 0;

		Row headerRow = sheet.createRow(rownum++);

		Cell hCell = headerRow.createCell(0);
		hCell.setCellValue("Test Pacakge");

		hCell = headerRow.createCell(1);
		hCell.setCellValue("First Avail (T-min)");

		hCell = headerRow.createCell(2);
		hCell.setCellValue("Test Start");

		hCell = headerRow.createCell(3);
		hCell.setCellValue("Test Finish");

		hCell = headerRow.createCell(4);
		hCell.setCellValue("T-Max");

		hCell = headerRow.createCell(5);
		hCell.setCellValue("Resource Size (manhours)");

		LocalDate baseDate = ActivityData.getBaseDate();
		int cell = 6;

		while (baseDate.isBefore(ActivityData.getTmax())) {
			hCell = headerRow.createCell(cell);
			hCell.setCellValue(baseDate.toString());
			baseDate = baseDate.plusDays(1);
			cell++;
		}

		for (int i = 0; i < this.genes.length; i++) {
			Row row = sheet.createRow(rownum++);
			writeTestPackage(i, row, 6);
		}

		try {
			FileOutputStream out = new FileOutputStream(new File("HTSchedule.xlsx"));
			workbook.write(out);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void writeTestPackage(int id, Row row, int dateStartIndex) {
		Activity gene = this.genes[id];

		Cell cell = row.createCell(0);
		cell.setCellValue(gene.getName());

		cell = row.createCell(1);
		cell.setCellValue(gene.getStartDate().toString());

		cell = row.createCell(2);
		cell.setCellValue(gene.getActivationDate().toString());

		cell = row.createCell(3);
		cell.setCellValue(gene.getDeactivationDate().toString());

		cell = row.createCell(4);
		cell.setCellValue(gene.getEndDate().toString());

		cell = row.createCell(5);
		cell.setCellValue(gene.getManHours());

		LocalDate Tmin = gene.getStartDate();
		cell = row.createCell(Days.daysBetween(ActivityData.getBaseDate(), Tmin).getDays() + dateStartIndex);
		cell.setCellValue("Tmin");

		LocalDate Tmax = gene.getEndDate();
		cell = row.createCell(Days.daysBetween(ActivityData.getBaseDate(), Tmax).getDays() + dateStartIndex);
		cell.setCellValue("Tmax");

		LocalDate activationDate = gene.getActivationDate();
		LocalDate deactivationDate = gene.getDeactivationDate();

		while (activationDate.isBefore(deactivationDate)) {
			if (Utils.isWorkingDay(activationDate)) {
				cell = row.createCell(
						Days.daysBetween(ActivityData.getBaseDate(), activationDate).getDays() + dateStartIndex);
				cell.setCellValue(gene.getNumberOfResources());
			}
			activationDate = activationDate.plusDays(1);
		}

		while (!Utils.isWorkingDay(activationDate)) {
			activationDate = activationDate.plusDays(1);
		}
		cell = row.createCell(Days.daysBetween(ActivityData.getBaseDate(), activationDate).getDays() + dateStartIndex);
		cell.setCellValue(gene.getNumberOfResources() * gene.getRatioOfLastDayUsed());

	}

	private NumberAxis createYaxis() {
		final NumberAxis axis = new NumberAxis();
		axis.setMinorTickCount(10);
		axis.setLabel("Man-hours");
		return axis;
	}

	private CategoryAxis createXaxis() {
		final CategoryAxis axis = new CategoryAxis();
		axis.setLabel("Weeks");
		axis.setTickLabelGap(10);

		return axis;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private BarChart<String, Number> createResourceVariationChart() {

		final BarChart<String, Number> chart = new BarChart<>(createXaxis(), createYaxis());

		chart.setMinHeight(400);
		chart.setPadding(new Insets(0, 0, 0, 175));

		@SuppressWarnings("rawtypes")
		XYChart.Series series1 = new XYChart.Series();

		int i = 0;

		while (i < this.weeklyResourceLevels.size()) {
			series1.getData().add(new XYChart.Data(weekDates.get(i),
					this.weeklyResourceLevels.get(i) * ActivityData.workingHoursPerDay()));
			i++;
		}

		int totalDays = i * 7;
		totalDays = ((totalDays / 25) + 1) * 25;

		int totalWeeks = totalDays / 7;
		LocalDate curDate = new LocalDate(weekDates.get(i));
		while (i < totalWeeks) {
			curDate = curDate.plusWeeks(1);
			series1.getData().add(new XYChart.Data(curDate.toString(), 0));
			i++;
		}

		series1.setName("Resource level");
		chart.getData().addAll(series1);
		chart.setLegendVisible(false);
		return chart;
	}

	public void setRank(int r) {
		this.rank = r;
	}

	public int getHighestResourceLevel() {
		return highestResourceLevel;
	}

	public int getHighestManhours() {
		return highestResourceLevel * ActivityData.workingHoursPerDay();
	}

	public void setHighestResourceLevel(int highestResourceLevel) {
		this.highestResourceLevel = highestResourceLevel;
	}

	public int getPenalty() {
		return penalty;
	}

	public void setPenalty(int penalty) {
		this.penalty = penalty;
	}

	public double getWeightedAverageLateStartDays() {
		if (this.weightedAverageLateStartDays == -1) {
			this.getFitness();
		}
		return weightedAverageLateStartDays;
	}

	public void setWeightedAverageLateStartDays(double weightedAverageLateStartDays) {
		this.weightedAverageLateStartDays = weightedAverageLateStartDays;
	}
}
