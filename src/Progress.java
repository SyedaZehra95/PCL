
public class Progress {
	private int index=0;
	private int man_hours=0;
	private int end_week=0;
	private int penalty=0;
	private int avg_from_start=0;

    public Progress() {
    }

    public Progress(int index,int man_hours, int penalty,int end_week, int avg_from_start) {
       this.index=index;
       this.man_hours=man_hours;
       this.end_week=end_week;
       this.penalty=penalty;
       this.avg_from_start=avg_from_start;
    }

    public int getIndex() {
        return index;
    }
    
    public int getManHours() {
        return this.man_hours;
    }
    
    public int getEndWeek() {
        return this.end_week;
    }
    
    public int getPenalty() {
        return this.penalty;
    }
    public int getAvgFromStart() {
        return this.avg_from_start;
    }

    public void setIndex(int index) {
        this.index=index;
    }
    
    public void setManHours(int man_hours) {
        this.man_hours=man_hours;
    }

    public void setEndWeek(int end_week) {
        this.end_week=end_week;
    }

    public void setPenalty(int penalty) {
        this.penalty=penalty;
    }
    public void setAvgFromStart(int avg_from_start) {
        this.avg_from_start=avg_from_start;
    }
}
