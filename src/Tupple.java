import java.util.Date;

public class Tupple {
	private Date startDate;
	private int manhours;

	public Tupple(Date date, int manhours) {
		this.startDate = date;
		this.manhours = manhours;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public int getManhours() {
		return manhours;
	}

	public void setManhours(int manhours) {
		this.manhours = manhours;
	}
}
