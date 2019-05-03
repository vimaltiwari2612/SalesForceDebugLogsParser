import javafx.application.*;
import javafx.collections.*;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.scene.paint.*;
import javafx.stage.FileChooser.*;
import javafx.geometry.*;
import javafx.event.*;
import java.io.*;
import java.util.*;
import javafx.collections.transformation.*;


public class LogProcessingScreen
        extends Application {

	private String fileName;
	private Text actionStatus;
	private ListView listView, listView2;
	private Stage savedStage;
	private Button processButton;
	private static final String titleTxt = "Filter Salesforce Debug Logs.";
	private ProgressIndicator pb;
	public static void main(String [] args) {

		Application.launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
	
		primaryStage.setTitle(titleTxt);	
		pb = new ProgressIndicator(); 
		pb.setVisible(false);
		// Buttons
		Button btn1 = new Button("Choose a Debug Log file...");
		btn1.setOnAction(new SingleFcButtonListener());
		HBox buttonHb1 = new HBox(10);
		buttonHb1.setAlignment(Pos.CENTER);
		buttonHb1.getChildren().addAll(btn1);

		Button btn2 = new Button("Choose multiple Debug Log files...");
		btn2.setOnAction(new MultipleFcButtonListener());
		HBox buttonHb2 = new HBox(10);
		buttonHb2.setAlignment(Pos.CENTER);
		buttonHb2.getChildren().addAll(btn2);

		// Status message text
		actionStatus = new Text();
		actionStatus.setFont(Font.font("Calibri", FontWeight.NORMAL, 15));
		actionStatus.setFill(Color.FIREBRICK);
		
		listView = new ListView();
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		listView2 = new ListView();
		listView2.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		
        Button button = new Button(" > ");
        button.setOnAction(event -> {
            ObservableList selectedIndices = listView.getSelectionModel().getSelectedItems();
			ObservableList listView2Items = listView2.getItems();
			TreeSet<String> toBeAdded = new TreeSet<String>();
            for(Object o : selectedIndices){
				listView2Items.add(o.toString());
				toBeAdded.add(o.toString());
            }
			listView.getItems().removeAll(toBeAdded);
			
			listView.setItems(sort(listView));
			listView2.setItems(sort(listView2));
			
        });
		
		Button button1 = new Button(" < ");
        button1.setOnAction(event -> {
            ObservableList selectedIndices = listView2.getSelectionModel().getSelectedItems();
			ObservableList listView1Items = listView.getItems();
			TreeSet<String> toBeAdded = new TreeSet<String>();
            for(Object o : selectedIndices){
				if(!listView1Items.contains(o.toString())) listView1Items.add(o.toString());
            }
			listView2.getItems().removeAll(toBeAdded);
			listView.setItems(sort(listView));
			listView2.setItems(sort(listView2));
        });
		
		processButton = new Button("Process Logs");
		processButton.setOnAction(event -> { 
			   actionStatus.setText("");
			   try{
				   if(fileName == null || fileName.isEmpty()) throw new Exception("Choose a file!!");
				   LogProcessor lp = new LogProcessor();
				   lp.setTags(listView2.getItems());
				   String newFileName = lp.processLogs(fileName);
				   actionStatus.setText("Successfully done! location : "+newFileName);
				   fileName = null;
			    }catch(Exception e){
					actionStatus.setText("Error while parsing : "+e.getMessage());
			    }
		   });
		   
		VBox buttonBox = new VBox(10);
		buttonBox.setPadding(new Insets(25, 25, 25, 25));
		buttonBox.getChildren().addAll(button,button1);
		
		HBox hbox = new HBox(20);    
		hbox.setPadding(new Insets(25, 25, 25, 25));
		hbox.getChildren().addAll(listView,buttonBox,listView2);
		
		TilePane r = new TilePane();
		r.getChildren().add(pb);  
		
		// Vbox
		VBox vbox = new VBox();
		vbox.setPadding(new Insets(25, 25, 25, 25));
		vbox.getChildren().addAll(buttonHb1,r, actionStatus,hbox, processButton);
		
		// Scene
		Scene scene = new Scene(vbox, 750, 600); // w x h
		primaryStage.setScene(scene);
		primaryStage.show();
		primaryStage.setResizable(false);
		savedStage = primaryStage;
	}
	
	private ObservableList sort(ListView listView){
		ArrayList<String> toBeReturned  = new ArrayList<String>();
		for(Object o : listView.getItems()) toBeReturned.add(o.toString());
		Collections.sort(toBeReturned); 
		ObservableList<String> oListStavaka = FXCollections.observableArrayList(toBeReturned);
		return oListStavaka;
	}

	private class SingleFcButtonListener implements EventHandler<ActionEvent> {

		@Override
		public void handle(ActionEvent e) {

			showSingleFileChooser();
		}
	}

	private void showSingleFileChooser() {
		
		FileChooser fileChooser = new FileChooser();
		File dir = new File(System.getProperty("user.dir")+"/logs");
		if(!dir.exists())dir.mkdir();
		fileChooser.setInitialDirectory(dir);
		fileChooser.setTitle("Select Debug log file");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Debug log Files", "*.log"));
		File selectedFile = fileChooser.showOpenDialog(null);
		actionStatus.setText("");
		pb.setVisible(true);
		if (selectedFile != null) {
			LogProcessor lp = new LogProcessor();
			try{
				fileName = selectedFile.getAbsolutePath();
				String tags = lp.getTags(fileName);
				if(tags!=null  && !tags.trim().isEmpty()) 
					listView.getItems().addAll(Arrays.asList(tags.split(",")));
			}catch(Exception e){
				System.out.println(e);
				actionStatus.setText(e.getMessage());
			}
		}
		else {
			actionStatus.setText("Log File selection cancelled.");
		}
		pb.setVisible(false);
	}

	private class MultipleFcButtonListener implements EventHandler<ActionEvent> {

		@Override
		public void handle(ActionEvent e) {

			showMultipleFileChooser();
		}
	}

	private void showMultipleFileChooser() {

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select Debug log files");
		fileChooser.getExtensionFilters().addAll(
			new ExtensionFilter("Debug log Files", "*.log"));
		List<File> selectedFiles = fileChooser.showOpenMultipleDialog(savedStage);

		if (selectedFiles != null) {

			actionStatus.setText("Log Files selected [" + selectedFiles.size() + "]: " +
					selectedFiles.get(0).getName() + "..");
		}
		else {
			actionStatus.setText("Log file selection cancelled.");
		}
	}
}