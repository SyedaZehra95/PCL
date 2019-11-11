

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;

/*import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;


public class GanttChart extends JFrame {

   private static final long serialVersionUID = 1L;

   public GanttChart(String title,Individual individual) {
      super(title);
      // Create dataset
      IntervalCategoryDataset dataset = getCategoryDataset(individual);
    
      // Create chart
      JFreeChart chart = ChartFactory.createGanttChart(
            "chart", // Chart title
            "Test packages", // X-Axis Label
            "Timeline", // Y-Axis Label
            dataset);
      
      ChartPanel panel = new ChartPanel(chart);
      
      setContentPane(panel);
   }

   private IntervalCategoryDataset getCategoryDataset(Individual individual) {

      TaskSeries series1 = new TaskSeries("solutions");
		int stepSize = (ActivityData.RMax() - ActivityData.RMin()) / 4;
		for (int i = 0; i < ActivityData.size(); i++) {
			
			String packageName = ActivityData.getActivity(i).getName();

			LocalDate baseDate= LocalDate.of(ActivityData.getBaseDate().getYear(),ActivityData.getBaseDate().getMonthOfYear(), ActivityData.getBaseDate().getDayOfMonth());
			LocalDate endDate= LocalDate.of(individual.getGene(i).getEndDate().getYear(),individual.getGene(i).getEndDate().getMonthOfYear(), individual.getGene(i).getEndDate().getDayOfMonth());
			LocalDate startDate= LocalDate.of(individual.getGene(i).getStartDate().getYear(),individual.getGene(i).getStartDate().getMonthOfYear(), individual.getGene(i).getStartDate().getDayOfMonth());
			LocalDate activeDate= LocalDate.of(individual.getGene(i).getActivationDate().getYear(),individual.getGene(i).getActivationDate().getMonthOfYear(), individual.getGene(i).getActivationDate().getDayOfMonth());
			LocalDate deactiveDate= LocalDate.of(individual.getGene(i).getDeactivationDate().getYear(),individual.getGene(i).getDeactivationDate().getMonthOfYear(), individual.getGene(i).getDeactivationDate().getDayOfMonth());
			double resources=0;
			if(activeDate.isBefore(deactiveDate)) {
				resources=individual.getGene(i).getNumberOfResources()*ActivityData.workingHoursPerDay();
			}
			System.out.println(baseDate+" : "+startDate+" : "+endDate+" : "+activeDate);
			 series1.add(new Task(packageName,
			            Date.from(activeDate.atStartOfDay().toInstant(ZoneOffset.UTC)),
			            Date.from(deactiveDate.atStartOfDay().toInstant(ZoneOffset.UTC))
			         ));
		/*	ac.addActivity(layer, new Pack(new PackageData("Tmin",startDate,startDate.plusDays(1))));
			ac.addActivity(layer, new Pack(new PackageData("Resource="+(individual.getGene(i).getNumberOfResources()*ActivityData.workingHoursPerDay()), activeDate,deactiveDate)));
			ac.addActivity(layer, new Pack(new PackageData("Tmax", endDate.minusDays(1),endDate)));
			layer.setVisible(true);
			
			gantt.getRoot().getChildren().add(ac);
			
		}
     
      
      TaskSeriesCollection dataset = new TaskSeriesCollection();
      dataset.add(series1);
      return dataset;
   }

  
}*/