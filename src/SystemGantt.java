
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.controlsfx.control.PopOver;

import com.flexganttfx.extras.GanttChartStatusBar;
import com.flexganttfx.extras.GanttChartToolBar;
import com.flexganttfx.model.ActivityRef;
import com.flexganttfx.model.Layer;
import com.flexganttfx.model.Row;
import com.flexganttfx.model.activity.MutableActivityBase;
import com.flexganttfx.model.layout.GanttLayout;
import com.flexganttfx.view.GanttChart;
import com.flexganttfx.view.GanttChart.DisplayMode;
import com.flexganttfx.view.graphics.GraphicsBase;
import com.flexganttfx.view.graphics.ListViewGraphics;
import com.flexganttfx.view.graphics.renderer.ActivityBarRenderer;
import com.flexganttfx.view.timeline.Timeline;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class SystemGantt {
	/*
	 * Plain data object storing dummy Pack information.
	 */

	private Individual ind;
	private PopOver popOver;
	GanttChart<WorkPackages> gantt;
	DecimalFormat df = new DecimalFormat("0.00");

	class PackageData {

		String packageName;
		Instant startTime = Instant.now();
		Instant endTime = Instant.now().plus(Duration.ofHours(6));

		public PackageData(String packageName, LocalDate startTime, LocalDate endTime) {
			this.packageName = packageName;
			this.startTime = startTime.atStartOfDay().toInstant(ZoneOffset.UTC);
			this.endTime = endTime.atStartOfDay().toInstant(ZoneOffset.UTC);
		}
	}

	/*
	 * The activity representing the Pack. This object will be rendered as a bar in
	 * the graphics view of the Gantt chart. The Pack is mutable, so the user will
	 * be able to interact with it.
	 */
	class Pack extends MutableActivityBase<PackageData> {
		public Pack(PackageData data) {
			setUserObject(data);
			setName(data.packageName);
			setStartTime(data.startTime);
			setEndTime(data.endTime);
		}
	}

	/*
	 * Each row represents an WorkPackage in this example. The activities shown on
	 * the row are of type Pack.
	 */
	public class WorkPackages extends Row {
		private String name = null;

		public WorkPackages(String name, String resource) {
			this.setName(name);
		}
	}

	public VBox start(Individual individual, VBox main_vbox) {

		// Create the Gantt chart
		gantt = new GanttChart<WorkPackages>();
		WorkPackages wp=new WorkPackages("Systems", " ");
		wp.setExpanded(true);
		gantt.setRoot(wp);
		Layer layers = new Layer("System");
		gantt.getLayers().add(layers);
		Layer layer = new Layer("Package");
		gantt.getLayers().add(layer);
		this.ind = individual;
		
		HashMap<String, SystemObj> systems=getSystems(individual);
		 Iterator iterator = systems.entrySet().iterator();
		 while (iterator.hasNext()) { 
	            Map.Entry mapElement = (Map.Entry)iterator.next(); 
	            SystemObj sys=(SystemObj)mapElement.getValue();
	            String sysName=(String)mapElement.getKey();
	            WorkPackages sysPackage=new WorkPackages(sysName,"");
	            LocalDate active = LocalDate.of(sys.getActivationDate().getYear(),
    					sys.getActivationDate().getMonthOfYear(),
    					sys.getActivationDate().getDayOfMonth());
	           
    			LocalDate deactive = LocalDate.of(sys.getDeactivationDate().getYear(),
    					sys.getDeactivationDate().getMonthOfYear(),
    					sys.getDeactivationDate().getDayOfMonth());
    			
    			
	            sysPackage.addActivity(layers, new Pack(new PackageData("start",active.plusDays(1) , deactive.plusDays(1))));
	            ArrayList activities=sys.getActivities();
	            for(int i=0; i<activities.size();i++) {
	            	
	            	Activity act=(Activity)activities.get(i);
	            	int id=act.id();
	            	//System.out.println(act+": "+act.id());
	            	LocalDate endDate = LocalDate.of(individual.getGene(id).getEndDate().getYear(),
	            			individual.getGene(id).getEndDate().getMonthOfYear(),
	            			individual.getGene(id).getEndDate().getDayOfMonth());
	    			LocalDate startDate = LocalDate.of(individual.getGene(id).getStartDate().getYear(),
	    					individual.getGene(id).getStartDate().getMonthOfYear(),
	    					individual.getGene(id).getStartDate().getDayOfMonth());
	    			LocalDate activeDate = LocalDate.of(individual.getGene(id).getActivationDate().getYear(),
	    					individual.getGene(id).getActivationDate().getMonthOfYear(),
	    					individual.getGene(id).getActivationDate().getDayOfMonth());
	    			LocalDate deactiveDate = LocalDate.of(individual.getGene(id).getDeactivationDate().getYear(),
	    					individual.getGene(id).getDeactivationDate().getMonthOfYear(),
	    					individual.getGene(id).getDeactivationDate().getDayOfMonth());
	            	WorkPackages testPackage=new WorkPackages(act.getName(),"");
	            	
	    			
	            	testPackage.addActivity(layer, new Pack(new PackageData("Tmin" + startDate, startDate.plusDays(1), startDate.plusDays(1))));
	            	testPackage.addActivity(layer, new Pack(new PackageData(act.getName() + "/" + i, activeDate.plusDays(1), deactiveDate.plusDays(1))));
	            	testPackage.addActivity(layer, new Pack(new PackageData("Tmax" + endDate, endDate.plusDays(1), endDate.plusDays(1))));
	            	ArrayList<WorkPackage> wPackages = act.getAssociatedWorkPackages();
					WorkPackage[] wpArray = wPackages.toArray(new WorkPackage[wPackages.size()]);
	            	for(int j=0;j<wpArray.length;j++) {
	            		WorkPackages workPackage=new WorkPackages((String)wpArray[j].getName(),"");
	            		testPackage.getChildren().add(workPackage);
	            	}
	            	sysPackage.getChildren().add(testPackage);
	            }
	            
	            
	            
	            gantt.getRoot().getChildren().add(sysPackage);
			 
		 }
		 
		
		Timeline timeline = gantt.getTimeline();
		timeline.showTemporalUnit(ChronoUnit.HOURS, 10);

		GraphicsBase<WorkPackages> graphics = gantt.getGraphics();
		ListViewGraphics<WorkPackages> graphic = gantt.getGraphics();
		graphics.setActivityRenderer(Pack.class, GanttLayout.class,
				new ActivityBarRenderer<>(graphics, "PackRenderer"));
		

		graphics.showEarliestActivities();
		gantt.setDisplayMode(DisplayMode.STANDARD);
		gantt.setShowTreeTable(true);
		gantt.requestFocus();
		System.out.println(gantt.getTooltip());
		
		VBox box = new VBox();
		box.getChildren().addAll(new GanttChartToolBar(gantt), gantt, new GanttChartStatusBar(gantt));
		
		return box;
	}
	public HashMap<String,SystemObj> getSystems(Individual ind) {
		 HashMap<String,SystemObj>systemGenes = new HashMap<>();
		 Activity[] genes=ind.getGenes();
		for (Activity gene :genes ) {
			String systemId = gene.getSystemId();
			SystemObj systemObj = ActivityData.getSystems().get(systemId);
			org.joda.time.LocalDate actDate = gene.getActivationDate();
			org.joda.time.LocalDate deactDate = gene.getDeactivationDate();
			if (!systemGenes.containsKey(systemId)) {
				SystemObj obj = systemObj.clone();
				obj.plusActivationDate(actDate);
				obj.plusDeactivationDate(deactDate);
				systemGenes.put(systemId, obj);
			} else {
				systemGenes.get(systemId).plusActivationDate(actDate);
				systemGenes.get(systemId).plusDeactivationDate(deactDate);
				systemGenes.get(systemId).plusResources(gene.getManHours());
			}
		}
		return systemGenes;
	}
	public VBox startGanttChart(Individual individual, VBox main_vBox) {
		VBox gantt = start(individual, main_vBox);
		return gantt;
	}



}