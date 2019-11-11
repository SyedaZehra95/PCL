
public class Progress {
	private int index = 0;
	private int man_hours = 0;
	private String end_week = "";
	private int penalty = 0;
	private double avg_from_start = 0;
	private double avgSystemCompletion = 0;

	public Progress() {
	}

	public Progress(int index, int man_hours, int penalty, String end_week, double avg_from_start, double avgSysComp) {
		this.index = index;
		this.man_hours = man_hours;
		this.end_week = end_week;
		this.penalty = penalty;
		this.avg_from_start = avg_from_start;
		this.avgSystemCompletion = avgSysComp;
	}

	public int getIndex() {
		return index;
	}

	public int getManHours() {
		return this.man_hours;
	}

	public String getEndWeek() {
		return this.end_week;
	}

	public int getPenalty() {
		return this.penalty;
	}

	public double getAvgFromStart() {
		return this.avg_from_start;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public void setManHours(int man_hours) {
		this.man_hours = man_hours;
	}

	public void setEndWeek(String end_week) {
		this.end_week = end_week;
	}

	public void setPenalty(int penalty) {
		this.penalty = penalty;
	}

	public void setAvgFromStart(int avg_from_start) {
		this.avg_from_start = avg_from_start;
	}

	public double getAvgSystemCompletion() {
		return avgSystemCompletion;
	}

	public void setAvgSystemCompletion(double avgSystemCompletion) {
		this.avgSystemCompletion = avgSystemCompletion;
	}
}
