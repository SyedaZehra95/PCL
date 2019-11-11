import java.util.Arrays;

import org.joda.time.LocalDate;

public class Utils {
	public static <T> T[] concat(T[] first, T[] second) {
		T[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	public static String convertFitnessToString(double[] fitness) {
		return fitness[0] + "," + fitness[1] + "," + fitness[2] + "," + fitness[3];
	}

	public static boolean isWorkingDay(LocalDate date) {
		if (date.getDayOfWeek() <= ActivityData.getNumberOfDaysPerWeek()) {
			return true;
		} else {
			return false;
		}
	}

	public static int getNumberOfNonWorkingDays(LocalDate activationDate, int duration) {
		int nonWorkingDays = 0;
		if (duration > 0) {
			while (duration > 0) {
				if (activationDate.getDayOfWeek() > ActivityData.getNumberOfDaysPerWeek()) {
					nonWorkingDays++;
				} else {
					duration--;
				}
				activationDate = activationDate.plusDays(1);
			}
		}
		return nonWorkingDays;
	}

	public static int getNumberOfNonWorkingDays(LocalDate baseDate, LocalDate lastDate) {
		int nonWorkingDays = 0;
		if (baseDate.isBefore(lastDate)) {
			while (baseDate.isBefore(lastDate)) {
				if (baseDate.getDayOfWeek() > ActivityData.getNumberOfDaysPerWeek()) {
					nonWorkingDays++;
				}
				baseDate = baseDate.plusDays(1);
			}
		}
		return nonWorkingDays;
	}
}
