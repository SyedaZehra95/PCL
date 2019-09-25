import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
public class LineCharts  {
	private String title;
	private XYChart.Series<Number,Number> series = new XYChart.Series<Number,Number>();
	final NumberAxis xAxis = new NumberAxis();
    final NumberAxis yAxis = new NumberAxis();
	final LineChart<Number,Number> lineChart = 
            new LineChart<Number,Number>(xAxis,yAxis);
	
	public LineCharts(String title) {
		this.title=title;
	}
	
	public LineChart run(VBox main_vBox, HBox topHBox, VBox vbox) {
		
		Thread updateThread = new Thread(() -> {

			Platform.runLater(() -> createLineChart(this.lineChart,this.xAxis,this.yAxis,main_vBox,topHBox,vbox));

		});
		updateThread.setDaemon(true);
		updateThread.start();
		return lineChart;
	}

	 public LineChart createLineChart(LineChart<Number,Number> lineChart,NumberAxis xAxis,NumberAxis yAxis,VBox main_vBox, HBox topHBox,VBox vbox) {
	       
	       
	        
	        xAxis.setLabel("generations");
	        
	        
	          
	        lineChart.setTitle(this.title);
	        VBox vBox=new VBox(lineChart);
	        vbox.getChildren().add(vBox);
			topHBox.getChildren().add(vbox);
	        return lineChart;
	    }
	 public void addSeries(int gen, int value) {
		 this.series.getData().add(new XYChart.Data(gen,value));
		 this.lineChart.getData().add(this.series);
	 }

}
