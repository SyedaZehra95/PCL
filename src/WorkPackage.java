import org.joda.time.LocalDate;

import javafx.beans.property.SimpleStringProperty;

public class WorkPackage implements Comparable<WorkPackage> {
	private String name;
	private LocalDate finishDate;

	private SimpleStringProperty wpname;
	private SimpleStringProperty wpdate;

	public WorkPackage(String name, LocalDate date) {
		this.name = name;
		this.finishDate = date;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public LocalDate getFinishDate() {
		return finishDate;
	}

	public void setFinishDate(LocalDate finishDate) {
		this.finishDate = finishDate;
	}

	public SimpleStringProperty getWpdate() {
		wpdate.set(this.finishDate.toString());
		return wpdate;
	}

	public void setWpdate(SimpleStringProperty wpdate) {
		this.wpdate = wpdate;
	}

	public SimpleStringProperty getWpname() {
		wpname.set(name);
		return wpname;
	}

	public void setWpname(SimpleStringProperty wpname) {
		this.wpname = wpname;
	}

	@Override
	public int compareTo(WorkPackage wp) {
		if (this.finishDate.isBefore(wp.getFinishDate())) {
			return -1;
		} else if (this.finishDate.isAfter(wp.getFinishDate())) {
			return 1;
		}
		return 0;
	}
}
