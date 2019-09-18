
public class Progress {
	private int index=0;
	private int max_work_penalty=0;
	private int last_work_week=0;
	private int late_start_penalty=0;

    public Progress() {
    }

    public Progress(int index,int max_work_penalty, int last_work_week,int late_start_penalty) {
       this.index=index;
       this.max_work_penalty=max_work_penalty;
       this.last_work_week=last_work_week;
       this.late_start_penalty=late_start_penalty;
    }

    public int getIndex() {
        return index;
    }
    
    public int getMaxWorkPenalty() {
        return max_work_penalty;
    }
    
    public int getLastWorkWeek() {
        return last_work_week;
    }
    
    public int getLateStartPenalty() {
        return late_start_penalty;
    }

    public void setIndex(int index) {
        this.index=index;
    }
    
    public void setMaxWorkPenalty(int max_work_penalty) {
        this.max_work_penalty=max_work_penalty;
    }

    public void setLastWorkWeek(int last_work_week) {
        this.last_work_week=last_work_week;
    }

    public void setLateStartPenalty(int late_start_penalty) {
        this.late_start_penalty=late_start_penalty;
    }
}
