import java.util.ArrayList;

import org.joda.time.LocalDate;

public class SystemObj {
	private String id;
	private double manhours;
	private LocalDate activationDate;
	private LocalDate deactivationDate;
	private LocalDate TMax;
	private ArrayList<Activity> activities = new ArrayList<>();

	public LocalDate getActivationDate() {
		return activationDate;
	}

	public LocalDate getDeactivationDate() {
		return deactivationDate;
	}

	public SystemObj(String id, Activity act) {
		this.id = id;
		this.manhours = act.getManHours();
		this.activationDate = DateHelper.MAX_VALUE;
		this.deactivationDate = DateHelper.MIN_VALUE;
		this.TMax = act.getEndDate();
		this.activities.add(act);
	}

	public SystemObj(String id, double res, LocalDate act, LocalDate deact, LocalDate TMax) {
		this.id = id;
		this.manhours = res;
		this.activationDate = act;
		this.deactivationDate = deact;
		this.TMax = TMax;
	}

//	public SystemObj(String id2, double resources2) {
//		this.id = id2;
//		this.resources = resources2;
//	}

	public void plusResources(double res) {
		this.manhours += res;
	}

	public void plusDeactivationDate(LocalDate date) {
		if (date != null && date.isAfter(this.deactivationDate)) {
			deactivationDate = date;
		}
	}

	public void plusActivationDate(LocalDate date) {
		if (date != null && date.isBefore(this.activationDate)) {
			activationDate = date;
		}
	}

	public void plusActivity(Activity activity) {
		plusResources(activity.getManHours());
		if (activity.getActivationDate() != null) {
			plusDeactivationDate(activity.getDeactivationDate());
			plusActivationDate(activity.getActivationDate());
		}
		this.activities.add(activity);
	}

	public double getResources() {
		return manhours;
	}

	public SystemObj clone() {
		return new SystemObj(this.id, this.manhours, DateHelper.MAX_VALUE, DateHelper.MIN_VALUE, this.TMax);

	}

	public LocalDate getTMax() {
		return TMax;
	}

	public void setTMax(LocalDate tMax) {
		TMax = tMax;
	}
}
