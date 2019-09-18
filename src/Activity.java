import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import org.joda.time.LocalDate;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class Activity {
	private int id;
	private String name;
	private LocalDate startDate;
	private LocalDate endDate;
	private LocalDate deactivationDate;
	private double manHours = -1;
	private LocalDate activationDate;
	private int numberOfResources;
	private double durationInDays = -1;
	private double ratioOfLastDayUsed = -1;
	private ArrayList<WorkPackage> workPackages = new ArrayList<>();

	public void addWorkPackage(WorkPackage wp) {
		this.workPackages.add(wp);
	}

	public ArrayList<WorkPackage> getAssociatedWorkPackages() {
		return this.workPackages;
	}

	public Activity(String name) {
		this.name = name;
	}

	public Activity(Activity gene) {
		this.id = gene.id();
		this.name = gene.getName();
		this.startDate = gene.getStartDate();
		this.manHours = gene.getManHours();
		this.activationDate = gene.getActivationDate();
		this.numberOfResources = gene.getNumberOfResources();
		this.deactivationDate = gene.getDeactivationDate();
		this.endDate = gene.getEndDate();
	}

	public Activity(int id) {
		if (ActivityData.isAborted()) {
			return;
		}
		this.id = id;
		int maxDays = 0;

		LocalDate currentDate = ActivityData.getActivity(id).getStartDate();
		LocalDate endDate = ActivityData.getActivity(id).getEndDate();
		if (currentDate.isBefore(endDate)) {
			while (currentDate.isBefore(endDate)) {
				int dayOfWeek = currentDate.getDayOfWeek();
				if (dayOfWeek <= ActivityData.getNumberOfDaysPerWeek()) {
					maxDays++;
				}
				currentDate = currentDate.plusDays(1);
			}
		}
		if (maxDays < 1) {
			System.out.println("Error in activity " + ActivityData.getActivity(id).getName()
					+ ": Activity start date is after T max: " + ActivityData.getActivity(id).getEndDate());
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Error");
					alert.setHeaderText("Error 1 in activity id: " + id);
					alert.setContentText("Error in activity " + ActivityData.getActivity(id).getName()
							+ ": Activity start date: " + ActivityData.getActivity(id).getStartDate()
							+ " is after T max: " + ActivityData.getActivity(id).getEndDate());
					alert.showAndWait();
				}
			});
			ActivityData.setAborted(true);
		}
		int maxHours = ActivityData.workingHoursPerDay() * maxDays;

		if ((maxHours * ActivityData.RMax()) < ActivityData.getActivity(id).getManHours()) {
			System.out.println("Error in activity " + ActivityData.getActivity(id).getName()
					+ ": Activity start date is after T max: " + ActivityData.getActivity(id).getEndDate());
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("No solution possible");
					alert.setHeaderText("Error 1 in package id: " + id);
					alert.setContentText("Error in package " + ActivityData.getActivity(id).getName()
							+ ": No solution possible with given Rmax value.");
					alert.showAndWait();
				}
			});
			ActivityData.setAborted(true);
		}
		int Rmin = (int) Math.ceil(ActivityData.getActivity(id).getManHours() / (double) (maxHours));
		if (Rmin > ActivityData.RMax()) {
			this.numberOfResources = ActivityData.RMax();
			System.out.println(
					"Error in activity " + ActivityData.getActivity(id).getName() + ": Value of Tmax is too low.");
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("No solution possible");
					alert.setHeaderText("Error 1 in package id: " + id);
					alert.setContentText("Error in package " + ActivityData.getActivity(id).getName()
							+ ": No solution possible with given Rmax value.");
					alert.showAndWait();
				}
			});
			ActivityData.setAborted(true);
		} else {
			if (ActivityData.RMin() > Rmin) {
				Rmin = ActivityData.RMin();
			}
			this.numberOfResources = ThreadLocalRandom.current().nextInt(Rmin, ActivityData.RMax() + 1);
		}

		this.activationDate = generateRandomT(maxHours);
	}

	private LocalDate generateRandomT(int maxHours) {
		int activityDurationHours = (int) Math.ceil((ActivityData.getActivity(id).getManHours()) / numberOfResources);
		int limit = (int) ((double) (maxHours - activityDurationHours) / (double) (ActivityData.workingHoursPerDay()))
				+ 1;
		if (limit < 1) {
			return getStartDate();
		}
		int randomDays = ThreadLocalRandom.current().nextInt(0, limit);
		LocalDate date = getStartDate();
		int days = randomDays;
		int nonWorkingDays = 0;
		if (days > 0) {
			while (days > 0) {
				if (date.getDayOfWeek() > ActivityData.getNumberOfDaysPerWeek()) {
					nonWorkingDays++;
				}
				date.plusDays(1);
				days--;
			}
		}
		return getStartDate().plusDays(randomDays + nonWorkingDays);
	}

	public int getNumberOfResources() {
		return numberOfResources;
	}

	public String getName() {
		if (name == null) {
			this.name = ActivityData.getActivity(this.id).getName();
		}
		return this.name;
	}

	public int id() {
		return this.id;
	}

	public LocalDate getStartDate() {
		if (startDate == null) {
			startDate = ActivityData.getActivity(this.id).getStartDate();
		}
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public double getManHours() {
		if (this.manHours == -1) {
			this.manHours = ActivityData.getActivity(this.id).getManHours();
		}
		return manHours;
	}

	public void setManHours(double manHours) {
		this.manHours = manHours;
	}

	public LocalDate getActivationDate() {
		return activationDate;
	}

	public double getDurationInDays() {
		if (durationInDays == -1) {
			durationInDays = this.getManHours() / (double) (ActivityData.workingHoursPerDay() * this.numberOfResources);
			ratioOfLastDayUsed = durationInDays - (int) durationInDays;
			durationInDays += Utils.getNumberOfNonWorkingDays(activationDate, (int) Math.ceil(durationInDays));
		}
		return durationInDays;
	}

	public double getRatioOfLastDayUsed() {
		return ratioOfLastDayUsed;
	}

	public void setRatioOfLastDayUsed(double ratioOfLastDayUsed) {
		this.ratioOfLastDayUsed = ratioOfLastDayUsed;
	}

	public LocalDate getDeactivationDate() {
		if (deactivationDate == null || durationInDays == -1.0) {
			deactivationDate = activationDate.plusDays((int) Math.ceil(this.getDurationInDays()) - 1);
		}
		return deactivationDate;
	}

	public boolean mutateTo(Activity gene) {
		if (gene.getDeactivationDate().isBefore(this.getStartDate())) {
			return false;
		}
		int maxDays = 0;

		LocalDate currentDate = gene.getDeactivationDate();
		LocalDate endDate = this.getEndDate();
		if (currentDate.isBefore(endDate)) {
			while (currentDate.isBefore(endDate)) {
				int dayOfWeek = currentDate.getDayOfWeek();
				if (dayOfWeek <= ActivityData.getNumberOfDaysPerWeek()) {
					maxDays++;
				}
				currentDate = currentDate.plusDays(1);
			}
		}
		if (maxDays < 1) {
			return false;
		}
		int maxHours = ActivityData.workingHoursPerDay() * maxDays;
		int Rmin = (int) Math.ceil(this.getManHours() / (double) (maxHours));
		if (Rmin > ActivityData.RMax()) {
			return false;
		}
		if (ActivityData.RMin() > Rmin) {
			Rmin = ActivityData.RMin();
		}
		this.numberOfResources = ThreadLocalRandom.current().nextInt(Rmin, ActivityData.RMax() + 1);
		this.activationDate = gene.getDeactivationDate();
		return true;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	public LocalDate getEndDate() {
		if (this.endDate == null) {
			this.endDate = ActivityData.getActivity(this.id()).getEndDate();
		}
		return this.endDate;
	}
}
