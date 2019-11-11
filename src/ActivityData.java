import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.LocalDate;

public class ActivityData {
	private static ArrayList<Activity> activities = new ArrayList<>();
	private static HashMap<String, SystemObj> systems = new HashMap<>();

	private static int RMin = 4;
	private static int workingHoursPerDay = 10;
	private static int resourceVariationLimit = 10;
	private static LocalDate baseDate = new LocalDate(2099, 1, 1);
	private static int numberOfDaysPerWeek = 5;
	private static int errorFitness = Integer.MAX_VALUE;
	private static LocalDate tmax = new LocalDate(2000, 1, 1);
	private static boolean[] checkBoxTruth = new boolean[] { true, true, true, true, true };
	private static File datasetFile;
	private static int accuracyThreshold = 100;
	private static boolean isAborted = false;
	private static int maxPenaltyShow = -1;
	private static double maxManHours = -1;
	private static int[] percentCompletionSlabs;
	private static double[] percentCompletionAllowed;
	private static int numSolutions = 0;

	private static ArrayList<Individual> chart3DData = new ArrayList<>();

	public static void reset() {
		setAborted(false);
		activities = new ArrayList<>();
		baseDate = new LocalDate(2099, 1, 1);
		tmax = new LocalDate(1900, 1, 1);
	}

	public static void addChart3DData(Individual individual) {
		chart3DData.add(individual);
	}

	public static Individual getChart3DData(int index) {
		return chart3DData.get(index);
	}

	public static void resetChart3DData() {
		chart3DData = new ArrayList<>();
	}

	public static boolean[] getCheckBoxTruth() {
		return checkBoxTruth;
	}

	public static void setCheckBoxTruth(boolean[] checkBoxTruth) {
		ActivityData.checkBoxTruth = checkBoxTruth;
	}

	public static LocalDate getBaseDate() {
		return baseDate;
	}

	public static void addActivity(Activity activity) {
		activities.add(activity);
	}

	public static Activity getActivity(int id) {
		return activities.get(id);
	}

	public static int size() {
		return activities.size();
	}

	public static int RMin() {
		return RMin;
	}

	public static void setRMin(int rMin) {
		RMin = rMin;
	}

	public static int workingHoursPerDay() {
		return workingHoursPerDay;
	}

	public static void setWorkingHoursPerDay(int workingHours) {
		ActivityData.workingHoursPerDay = workingHours;
	}

	public static int getResourceVariationLimit() {
		return resourceVariationLimit;
	}

	public static void setResourceVariationLimit(int variationLimit) {
		ActivityData.resourceVariationLimit = variationLimit;
	}

	public static LocalDate getTmax() {
		return tmax;
	}

	public static void setTmax(LocalDate tmax) {
		ActivityData.tmax = tmax;
	}

	public static int getNumberOfDaysPerWeek() {
		return numberOfDaysPerWeek;
	}

	public static void setNumberOfDaysPerWeek(int numberOfDaysPerWeek) {
		ActivityData.numberOfDaysPerWeek = numberOfDaysPerWeek;
	}

	public static int getErrorFitness() {
		return errorFitness;
	}

	public static void loadExcel(File file) {
		try {
			XSSFWorkbook workbook = new XSSFWorkbook(file);
			XSSFSheet sheet0 = workbook.getSheetAt(0);
			HashMap<String, ArrayList<String>> IsometricsInTestPackageMap = new HashMap<>();
			HashMap<String, Double> testPackageToResourcesMap = new HashMap<>();
			int rows = sheet0.getPhysicalNumberOfRows();
			for (int i = 1; i < rows; i++) {
				Row row = sheet0.getRow(i);
				if (row != null && row.getCell(0) != null) {
					String keyName = row.getCell(0).getStringCellValue();
					if (!IsometricsInTestPackageMap.containsKey(keyName)) {
						IsometricsInTestPackageMap.put(keyName, new ArrayList<String>());
					}
					testPackageToResourcesMap.put(keyName, row.getCell(1).getNumericCellValue());
				}
			}

			XSSFSheet sheet1 = workbook.getSheetAt(1);
			rows = sheet1.getPhysicalNumberOfRows();
			for (int i = 1; i < rows; i++) {
				Row row = sheet1.getRow(i);
				if (row != null && row.getCell(0) != null) {
					String keyName = row.getCell(0).getStringCellValue();
					if (IsometricsInTestPackageMap.containsKey(keyName)) {
						IsometricsInTestPackageMap.get(keyName).add(row.getCell(1).getStringCellValue());
					}
				}
			}

			HashMap<String, String> isometricToFwipMap = new HashMap<>();
			HashMap<String, Date> hMap21 = new HashMap<>();
			XSSFSheet sheet2 = workbook.getSheetAt(2);
			rows = sheet2.getPhysicalNumberOfRows();
			for (int i = 1; i < rows; i++) {
				Row row = sheet2.getRow(i);

				if (row != null && row.getCell(0) != null) {
					isometricToFwipMap.put(row.getCell(1).getStringCellValue(),
							(row.getCell(0).getStringCellValue()).toUpperCase());
				}

				if (row != null && row.getCell(2) != null) {
					row.getCell(2).setCellType(CellType.NUMERIC);
					hMap21.put(row.getCell(1).getStringCellValue().toUpperCase(), row.getCell(2).getDateCellValue());
				}
			}

			HashMap<String, Date> hMap3 = new HashMap<>();

			XSSFSheet sheet3 = workbook.getSheetAt(3);
			rows = sheet3.getPhysicalNumberOfRows();
			for (int i = 1; i < rows; i++) {
				Row row = sheet3.getRow(i);

				if (row != null && row.getCell(0) != null) {

					hMap3.put((row.getCell(0).getStringCellValue()).toUpperCase(), row.getCell(1).getDateCellValue());
				}
			}

			HashMap<String, Date> hMap4 = new HashMap<>();
			XSSFSheet sheet4 = workbook.getSheetAt(4);
			rows = sheet4.getPhysicalNumberOfRows();
			for (int i = 1; i < rows; i++) {
				Row row = sheet4.getRow(i);
				if (row != null && row.getCell(0) != null) {
					hMap4.put(row.getCell(0).getStringCellValue(), row.getCell(1).getDateCellValue());

				}
			}

			HashMap<String, String> testPackageToSystemMap = new HashMap<>();
			XSSFSheet sheet5 = workbook.getSheetAt(5);
			rows = sheet5.getPhysicalNumberOfRows();
			for (int i = 1; i < rows; i++) {
				Row row = sheet5.getRow(i);
				if (row != null && row.getCell(0) != null) {
					testPackageToSystemMap.put(row.getCell(0).getStringCellValue(),
							row.getCell(1).getStringCellValue());
				}
			}
			workbook.close();

			for (String keyName : testPackageToResourcesMap.keySet()) {
				Activity newActivity = new Activity(keyName);
				double manHours = testPackageToResourcesMap.get(keyName);
				if (ActivityData.getMaxManHours() < manHours) {
					ActivityData.setMaxManHours(manHours);
				}
				newActivity.setManHours(manHours);
				LocalDate lastDate = new LocalDate(1900, 1, 1);

				String sysId = testPackageToSystemMap.get(keyName);
				newActivity.setSystemId(sysId);
				LocalDate endDate = new LocalDate(hMap4.get(sysId));
				newActivity.setEndDate(endDate);

				if (endDate.isAfter(ActivityData.getTmax())) {
					ActivityData.setTmax(endDate);

				}

				for (String drawing : IsometricsInTestPackageMap.get(keyName)) {
					String wpName = isometricToFwipMap.get(drawing);
					if (isometricToFwipMap.containsKey(drawing) && hMap21.get(drawing) == null) {
						LocalDate localDate = new LocalDate(hMap3.get(wpName));
						System.out.println(hMap3.get(wpName));
						WorkPackage wp = new WorkPackage(wpName, localDate);
						newActivity.addWorkPackage(wp);
						if (localDate.isAfter(lastDate)) {

							lastDate = localDate;
						}
					}
					if (isometricToFwipMap.containsKey(drawing) && hMap21.get(drawing) != null) {
						LocalDate localDate;
						Calendar calendar = new GregorianCalendar();
						calendar.setTime(hMap21.get(drawing));
						int year = calendar.get(Calendar.YEAR);

						if (hMap21.containsKey(drawing) && year != 1905) {

							localDate = new LocalDate(hMap21.get(drawing));
						} else {
							localDate = new LocalDate(hMap3.get(wpName));

						}
						LocalDate idate = new LocalDate(hMap21.get(drawing));
						LocalDate fdate = new LocalDate(hMap3.get(wpName));
						System.out.println(drawing + " : " + hMap21.get(drawing) + " : " + hMap3.get(wpName) + " : "
								+ localDate + " : " + idate.compareTo(localDate) + " : " + idate.isBefore(fdate));

						WorkPackage wp = new WorkPackage(wpName, localDate);
						newActivity.addWorkPackage(wp);
						if (localDate.isAfter(lastDate)) {

							lastDate = localDate;
						}
					}
				}
				newActivity.setStartDate(lastDate);
				if (!systems.containsKey(newActivity.getSystemId())) {
					SystemObj systemObj = new SystemObj(newActivity.getSystemId(), newActivity);
					systems.put(newActivity.getSystemId(), systemObj);
				} else {
					systems.get(newActivity.getSystemId()).plusActivity(newActivity);
				}
				if (!lastDate.isEqual(new LocalDate(1900, 1, 1))) {
					if (lastDate.isBefore(baseDate)) {
						baseDate = lastDate;
					}
					activities.add(newActivity);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static File getDatasetFile() {
		return datasetFile;
	}

	public static void setDatasetFile(File datasetFile) {
		ActivityData.datasetFile = datasetFile;
	}

	public static int getAccuracyThreshold() {
		return accuracyThreshold;
	}

	public static void setAccuracyThreshold(int accuracyThreshold) {
		ActivityData.accuracyThreshold = accuracyThreshold;
	}

	public static boolean isAborted() {
		return isAborted;
	}

	public static void setAborted(boolean isAborted) {
		ActivityData.isAborted = isAborted;
	}

	public static int getMaxPenaltyShow() {
		return maxPenaltyShow;
	}

	public static void setMaxPenaltyShow(int maxPenaltyShow) {
		ActivityData.maxPenaltyShow = maxPenaltyShow;
	}

	public static double getMaxManHours() {
		return maxManHours;
	}

	public static void setMaxManHours(double maxManHours) {
		ActivityData.maxManHours = maxManHours;
	}

	public static int[] getPercentCompletionSlabs() {
		return percentCompletionSlabs;
	}

	public static void setPercentCompletionSlabs(int[] percentCompletionSlabs) {
		ActivityData.percentCompletionSlabs = percentCompletionSlabs;
	}

	public static double[] getPercentCompletionAllowed() {
		return percentCompletionAllowed;
	}

	public static void setPercentCompletionAllowed(double[] percentCompletionAllowed) {
		ActivityData.percentCompletionAllowed = percentCompletionAllowed;
	}

	public static HashMap<String, SystemObj> getSystems() {
		return systems;
	}

	public static Activity getActivity(String name) {
		for (Activity activity : activities) {
			if (activity.getName().equals(name)) {
				return activity;
			}
		}
		return null;
	}

	public static int getNumSolutions() {
		return numSolutions;
	}

	public static void setNumSolutions(int numSolutions) {
		ActivityData.numSolutions = numSolutions;
	}
}
