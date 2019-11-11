import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.HashMap;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.Days;
import org.joda.time.LocalDate;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class Individual {
	private Activity[] genes;
	private HashMap<String, SystemObj> systemGenes;
	private double[] fitness = new double[] { -1, -1, -1, -1 };
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
	private double averageLateStartDays = -1;

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

	public double[] getFitness() {
		if (fitness[0] == -1) {
			lastDate = genes[0].getDeactivationDate();

			for (Activity gene : genes) {
				if (gene.getDeactivationDate().isAfter(lastDate)) {
					lastDate = gene.getDeactivationDate();
				}
			}

			double[] tempResourceLevels = new double[Days.daysBetween(ActivityData.getBaseDate(), lastDate).getDays()
					+ 1];

			for (Activity activity : this.genes) {
				int start = Days.daysBetween(ActivityData.getBaseDate(), activity.getActivationDate()).getDays();
				int end = Days.daysBetween(ActivityData.getBaseDate(), activity.getDeactivationDate()).getDays();
				int k = start;
				for (k = start; k < end; k++) {
					double res = activity.getNumberOfResources();
					tempResourceLevels[k] += res;
				}

				if (k == end) {
					tempResourceLevels[k] += (activity.getNumberOfResources() * activity.getRatioOfLastDayUsed());
				}
			}

			if (ActivityData.getNumberOfDaysPerWeek() < 7) {
				for (int i = 0; i < tempResourceLevels.length; i++) {
					if (ActivityData.getBaseDate().plusDays(i).getDayOfWeek() > ActivityData.getNumberOfDaysPerWeek()) {
						tempResourceLevels[i] = -1;
					}
				}
			}

			this.resourceLevels = new int[tempResourceLevels.length];
			for (int i = 0; i < this.resourceLevels.length; ++i) {
				this.resourceLevels[i] = (int) tempResourceLevels[i];
			}

			int currentWeek = 0;
			int weeklyResourceTotal = 0;
			boolean isWeekendStarted = false;
			int i;

			weeklyResourceLevels = new HashMap<>();
			weekDates = new HashMap<>();

			if (ActivityData.getNumberOfDaysPerWeek() > 6) {
				for (i = 0; i < resourceLevels.length; i++) {
					if (ActivityData.getBaseDate().plusDays(i).getDayOfWeek() == ActivityData
							.getNumberOfDaysPerWeek()) {
						weeklyResourceLevels.put(currentWeek, weeklyResourceTotal + resourceLevels[i]);
						LocalDate weekDate = ActivityData.getBaseDate().plusDays(i);
						weekDates.put(currentWeek, weekDate.toString());
						weeklyResourceTotal = 0;
						currentWeek++;
					} else {
						weeklyResourceTotal += resourceLevels[i];
					}
				}
			} else {
				for (i = 0; i < resourceLevels.length; i++) {
					if (resourceLevels[i] == -1) {
						if (!isWeekendStarted) {
							weeklyResourceLevels.put(currentWeek, weeklyResourceTotal);
							if (currentWeek != 0) {
								LocalDate weekDate = ActivityData.getBaseDate().plusDays(i);
								weekDate = weekDate.plusDays(7 - weekDate.getDayOfWeek());
								weekDates.put(currentWeek, weekDate.toString());
							} else {
								LocalDate weekDate = ActivityData.getBaseDate();
								weekDate = weekDate.plusDays(7 - weekDate.getDayOfWeek());
								weekDates.put(currentWeek, weekDate.toString());
							}
							isWeekendStarted = true;
							weeklyResourceTotal = 0;
							currentWeek++;
						}
					} else {
						isWeekendStarted = false;
						weeklyResourceTotal += resourceLevels[i];
					}
				}
			}

			if (weeklyResourceTotal != 0) {
				weeklyResourceLevels.put(currentWeek, weeklyResourceTotal);
				LocalDate weekDate = ActivityData.getBaseDate().plusDays(i);
				weekDate = weekDate.plusDays(7 - weekDate.getDayOfWeek());
				weekDates.put(currentWeek, weekDate.toString());
			}

			fitness[0] = weeklyResourceLevels.size();
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

			int globalPeak = highestResourceLevel;
			int lowestResourceLevel = highestResourceLevel;
			for (int i = halfIndex; i < weeklyResourceLevels.size(); i++) {
				if (weeklyResourceLevels.get(i) > globalPeak) {
					globalPeak = weeklyResourceLevels.get(i);
				}
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

			this.setHighestResourceLevel(globalPeak);
			this.setPenalty(changeInResources);

			fitness[1] = globalPeak + changeInResources;

		}

		if (fitness[2] == -1) {
			double sum = 0;
			double weightedSum = 0;
			double total = 0;
			double distancePenalty = 0;
			for (Activity gene : genes) {
				int days = Days.daysBetween(gene.getStartDate(), gene.getActivationDate()).getDays();
				sum += days;
				distancePenalty += days * Math.pow(gene.getManHours(), 2);
				weightedSum += days * gene.getManHours();
				total += gene.getManHours();
			}

			setAverageLateStartDays(sum / genes.length);
			setWeightedAverageLateStartDays(weightedSum / total);
			fitness[2] = distancePenalty;
		}

		if (fitness[3] == -1) {
			systemGenes = new HashMap<>();

			for (Activity gene : genes) {
				String systemId = gene.getSystemId();
				SystemObj systemObj = ActivityData.getSystems().get(systemId);
				LocalDate deactDate = gene.getDeactivationDate();
				if (!systemGenes.containsKey(systemId)) {
					SystemObj obj = systemObj.clone();
					obj.plusDeactivationDate(deactDate);
					systemGenes.put(systemId, obj);
				} else {
					systemGenes.get(systemId).plusDeactivationDate(deactDate);
					systemGenes.get(systemId).plusResources(gene.getManHours());
				}
			}

			double totalSystemDuration = 0;

			for (String key : systemGenes.keySet()) {
				SystemObj sys = systemGenes.get(key);
				int duration = Days.daysBetween(sys.getDeactivationDate(), sys.getTMax()).getDays();
				totalSystemDuration += duration;
			}

			fitness[3] = -1 * totalSystemDuration;
		}
		return fitness;
	}

	public void resetFitness() {
		this.fitness = new double[] { -1, -1, -1, -1 };
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
		IndividualGanttChart indg = new IndividualGanttChart();

		VBox vBox = new VBox();
		vBox.getChildren().addAll(indg.startGanttChart(this, main_vBox), hBox);
		main_vBox.getChildren().add(3, vBox);
		main_vBox.getChildren().add(4,

				createResourceVariationChart());
		HBox hb = new HBox();
		hb.setBackground(new Background(new BackgroundFill(Color.web("#324851"), CornerRadii.EMPTY, Insets.EMPTY)));
		hb.setMinHeight(10);
		main_vBox.getChildren().add(5, hb);
	}

	public void printToExcel() {
		getFitness();
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("Schedule");

		CellStyle grayStyle = workbook.createCellStyle();
		grayStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		grayStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		grayStyle.setBorderBottom(BorderStyle.THIN);
		grayStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
		grayStyle.setBorderLeft(BorderStyle.THIN);
		grayStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
		grayStyle.setBorderRight(BorderStyle.THIN);
		grayStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
		grayStyle.setBorderTop(BorderStyle.THIN);
		grayStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());

		CellStyle darkGrayStyle = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex());
		darkGrayStyle.setFont(font);
		darkGrayStyle.setFillForegroundColor(IndexedColors.GREY_80_PERCENT.getIndex());
		darkGrayStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		darkGrayStyle.setBorderBottom(BorderStyle.THIN);
		darkGrayStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
		darkGrayStyle.setBorderLeft(BorderStyle.THIN);
		darkGrayStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
		darkGrayStyle.setBorderRight(BorderStyle.THIN);
		darkGrayStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
		darkGrayStyle.setBorderTop(BorderStyle.THIN);
		darkGrayStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());

		int rownum = 0;
		int colNum = 0;

		Row headerRow = sheet.createRow(rownum++);

		Cell hCell = headerRow.createCell(colNum);
		hCell.setCellValue("Test Pacakge");
		hCell.setCellStyle(darkGrayStyle);
		colNum++;

		hCell = headerRow.createCell(colNum);
		hCell.setCellValue("System");
		hCell.setCellStyle(darkGrayStyle);
		colNum++;

		hCell = headerRow.createCell(colNum);
		hCell.setCellValue("First Avail (T-min)");
		hCell.setCellStyle(darkGrayStyle);
		colNum++;

		hCell = headerRow.createCell(colNum);
		hCell.setCellValue("Test Start");
		hCell.setCellStyle(darkGrayStyle);
		colNum++;

		hCell = headerRow.createCell(colNum);
		hCell.setCellValue("Test Finish");
		hCell.setCellStyle(darkGrayStyle);
		colNum++;

		hCell = headerRow.createCell(colNum);
		hCell.setCellValue("T-Max");
		hCell.setCellStyle(darkGrayStyle);
		colNum++;

		hCell = headerRow.createCell(colNum);
		hCell.setCellValue("Resource Size (manhours)");
		hCell.setCellStyle(darkGrayStyle);
		colNum++;

		LocalDate baseDate = ActivityData.getBaseDate();
		int cell = colNum;

		while (baseDate.isBefore(ActivityData.getTmax())) {
			hCell = headerRow.createCell(cell);
			hCell.setCellValue(baseDate.toString());
			hCell.setCellStyle(darkGrayStyle);
			baseDate = baseDate.plusDays(1);
			cell++;
		}

		for (int i = 0; i < this.genes.length; i++) {
			Row row = sheet.createRow(rownum++);
			writeTestPackage(i, row, colNum, grayStyle, darkGrayStyle);
		}

		for (int i = 0; i < 6; i++) {
			sheet.autoSizeColumn(i);
		}

		try {
			FileOutputStream out = new FileOutputStream(new File("HTSchedule.xlsx"));
			workbook.write(out);
			out.close();
			workbook.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void writeTestPackage(int id, Row row, int dateStartIndex, CellStyle grayStyle, CellStyle darkGrayStyle) {
		Activity gene = this.genes[id];

		int colNum = 0;
		Cell cell = row.createCell(colNum);
		cell.setCellValue(gene.getName());
		cell.setCellStyle(darkGrayStyle);
		colNum++;

		cell = row.createCell(colNum);
		cell.setCellValue(gene.getSystemId());
		cell.setCellStyle(darkGrayStyle);
		colNum++;

		cell = row.createCell(colNum);
		cell.setCellValue(gene.getStartDate().toString());
		cell.setCellStyle(darkGrayStyle);
		colNum++;

		cell = row.createCell(colNum);
		cell.setCellValue(gene.getActivationDate().toString());
		cell.setCellStyle(darkGrayStyle);
		colNum++;

		cell = row.createCell(colNum);
		cell.setCellValue(gene.getDeactivationDate().toString());
		cell.setCellStyle(darkGrayStyle);
		colNum++;

		cell = row.createCell(colNum);
		cell.setCellValue(gene.getEndDate().toString());
		cell.setCellStyle(darkGrayStyle);
		colNum++;

		cell = row.createCell(colNum);
		cell.setCellValue(gene.getManHours());
		cell.setCellStyle(darkGrayStyle);
		colNum++;

		LocalDate Tmin = gene.getStartDate();
		cell = row.createCell(Days.daysBetween(ActivityData.getBaseDate(), Tmin).getDays() + dateStartIndex);
//		cell.setCellValue("Tmin");
		cell.setCellStyle(darkGrayStyle);

		LocalDate Tmax = gene.getEndDate();
		cell = row.createCell(Days.daysBetween(ActivityData.getBaseDate(), Tmax).getDays() + dateStartIndex);
//		cell.setCellValue("Tmax");
		cell.setCellStyle(darkGrayStyle);

		LocalDate activationDate = gene.getActivationDate();
		LocalDate deactivationDate = gene.getDeactivationDate();

		while (activationDate.isBefore(deactivationDate)) {
			if (Utils.isWorkingDay(activationDate)) {
				cell = row.createCell(
						Days.daysBetween(ActivityData.getBaseDate(), activationDate).getDays() + dateStartIndex);
				cell.setCellValue(gene.getNumberOfResources() * ActivityData.workingHoursPerDay());
				if (activationDate.isEqual(Tmin)) {
					cell.setCellStyle(darkGrayStyle);
				} else {
					cell.setCellStyle(grayStyle);
				}
			}
			activationDate = activationDate.plusDays(1);
		}

		while (!Utils.isWorkingDay(activationDate)) {
			activationDate = activationDate.plusDays(1);
		}
		cell = row.createCell(Days.daysBetween(ActivityData.getBaseDate(), activationDate).getDays() + dateStartIndex);
		DecimalFormat df = new DecimalFormat("#.###");
		String val = df.format(
				(gene.getNumberOfResources() * gene.getRatioOfLastDayUsed() * ActivityData.workingHoursPerDay()));
		cell.setCellValue(Double.parseDouble(val));
		if (activationDate.isEqual(Tmin)) {
			cell.setCellStyle(darkGrayStyle);
		} else {
			cell.setCellStyle(grayStyle);
		}
	}

	private NumberAxis createYaxis(String title) {
		final NumberAxis axis = new NumberAxis();
		axis.setMinorTickCount(10);
		axis.setLabel(title);
		return axis;
	}

	private CategoryAxis createXaxis() {
		final CategoryAxis axis = new CategoryAxis();
		axis.setLabel("Weeks");
//		axis.setTickLabelGap(10);

		return axis;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public LineChart<String, Number> createLineResourceVariationChart() {
		final LineChart<String, Number> chart = new LineChart<>(createXaxis(), createYaxis("Cumulative Man-hours"));
		chart.setMinHeight(400);
		chart.setMaxWidth(800);
		chart.setPadding(new Insets(0, -75, 95, 45));

		@SuppressWarnings("rawtypes")
		XYChart.Series series1 = new XYChart.Series();
		int i = 0;
		int rLevel = 0;
		while (i < this.weeklyResourceLevels.size()) {

			series1.getData().add(new XYChart.Data(weekDates.get(i), rLevel));
			rLevel += this.weeklyResourceLevels.get(i) * ActivityData.workingHoursPerDay();
			i++;
		}
		int totalDays = i * 7;
		totalDays = ((totalDays / 25) + 1) * 25;
		int totalWeeks = totalDays / 7;
		LocalDate curDate = new LocalDate(weekDates.get(i - 1));
		while (i < totalWeeks) {
			curDate = curDate.plusWeeks(1);
			series1.getData().add(new XYChart.Data(curDate.toString(), rLevel));
			i++;
		}
		chart.getYAxis().setSide(Side.RIGHT);
		chart.getXAxis().setTickLabelsVisible(false);
		chart.getXAxis().setTickMarkVisible(false);
		chart.getXAxis().setLabel("");
		series1.setName("Resource level");
		chart.getData().addAll(series1);
		chart.setLegendVisible(false);
		return chart;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public BarChart<String, Number> createResourceVariationChart() {

		final BarChart<String, Number> chart = new BarChart<>(createXaxis(), createYaxis("Man-hours"));

		chart.setMinHeight(400);
		chart.setMaxWidth(800);
//		chart.setPadding(new Insets(0, 0, 0, 175));

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
		LocalDate curDate = new LocalDate(weekDates.get(i - 1));
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

	public StackPane layerCharts(final XYChart<String, Number>... charts) {
		for (int i = 1; i < charts.length; i++) {
			configureOverlayChart(charts[i]);
		}
		StackPane stackpane = new StackPane();
		stackpane.getChildren().addAll(charts);
		return stackpane;
	}

	private void configureOverlayChart(final XYChart<String, Number> chart) {
		chart.setAlternativeRowFillVisible(false);
		chart.setAlternativeColumnFillVisible(false);
		chart.setHorizontalGridLinesVisible(false);
		chart.setVerticalGridLinesVisible(false);
		chart.getXAxis().setVisible(false);
		chart.getYAxis().setVisible(false);

		chart.getStylesheets().add("overlay.css");
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

	public double getAverageLateStartDays() {
		if (this.averageLateStartDays == -1) {
			this.getFitness();
		}
		return averageLateStartDays;
	}

	public void setAverageLateStartDays(double averageLateStartDays) {
		this.averageLateStartDays = averageLateStartDays;
	}

	public String getProjectEndWeek() {
		return weekDates.get(((int) getFitness()[0]) - 1);
	}
}
