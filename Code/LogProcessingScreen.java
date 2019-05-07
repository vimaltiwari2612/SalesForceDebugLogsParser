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
import javafx.scene.control.Alert.AlertType;
import javafx.collections.transformation.*;


public class LogProcessingScreen extends Application {
	private String fileName;
	private Text processText;
	private Label availabletags , selectedTags;
	private ListView listView, listView2;
	private Stage savedStage;
	private Button processButton,button,button1,btn1,saveFile;
	private static final String titleTxt = "Filter Salesforce Debug Logs.";
	private ProgressIndicator pb;
	private TextArea textArea;
	private Alert a;
	public static void main(String [] args) {

		Application.launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		a = new Alert(AlertType.NONE); 
		Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
		primaryStage.setTitle(titleTxt);	
		pb = new ProgressIndicator(); 
		pb.setVisible(false);
		
		processText = new Text();
		
		TilePane r = new TilePane();
		r.getChildren().add(pb);  
		r.getChildren().add(processText);  
		
		// Buttons
		btn1 = new Button("Choose a Debug Log file");
		btn1.setOnAction(new SingleFcButtonListener());
		HBox buttonHb1 = new HBox(10);
		buttonHb1.setAlignment(Pos.CENTER);
		buttonHb1.getChildren().addAll(btn1);
	
		listView = new ListView();
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		listView2 = new ListView();
		listView2.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		
        button = new Button(">");
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
		
		button1 = new Button("<");
	
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
		HBox buttonHb2 = new HBox(10);
		buttonHb2.setAlignment(Pos.CENTER);
		buttonHb2.getChildren().addAll(processButton);
		processButton.setOnAction(event -> { 
			   try{
					if(fileName == null || fileName.isEmpty()) 
						throw new Exception("Choose a file!!");
					
					if(listView2.getItems().isEmpty()) 
						throw new Exception("Mininmum one tag should be selected!");
					
					
					new Thread(new Runnable() {
						String message = null;
						@Override
						public void run() {
							try{
								LogProcessor lp = new LogProcessor();
								lp.setTags(listView2.getItems());
								String output = lp.processLogs(fileName);
								textArea.setText(output.trim());
					
							}catch(Exception e){
								message = e.getMessage();
								e.printStackTrace();
								pb.setVisible(false);
								processText.setText("");
								setAllFunc(false);
							}
							Platform.runLater(new Runnable() {
								@Override
								public void run() { 
								 if(message != null){
										a.setAlertType(AlertType.INFORMATION); 
										a.setContentText(message);  
										a.show();
									 }
									 pb.setVisible(false);
									 processText.setText("");
									 setAllFunc(false);
								}
							});  
						}
					}).start();	
					
					pb.setVisible(true);
					processText.setText("Processing...");
					setAllFunc(true);
				}catch(Exception e){
					pb.setVisible(false);
					processText.setText("");
					setAllFunc(false);
			    }
		   });
		saveFile = new Button("Save as File"); 
		saveFile.setOnAction(event -> { 
			   try{
					if(textArea.getText().trim().equals("")) 
						throw new Exception("No Data found!!");
					
					new Thread(new Runnable() {
						String message = "Saved!";
						@Override
						public void run() {
							try{
								LogProcessor lp = new LogProcessor();
								String output = lp.saveFile(fileName,textArea.getText().trim());
								message +="Location : "+output;
							}catch(Exception e){
								message = e.getMessage();
								e.printStackTrace();
								pb.setVisible(false);
								processText.setText("");
								setAllFunc(false);
							}
							Platform.runLater(new Runnable() {
								@Override
								public void run() { 
									 if(message != null){
										a.setAlertType(AlertType.INFORMATION); 
										a.setContentText(message);  
										a.show();
									 }
									 pb.setVisible(false);
									 processText.setText("");
									 setAllFunc(false);
								}
							});  
						}
					}).start();	
					
					pb.setVisible(true);
					processText.setText("Saving...");
					setAllFunc(true);
				}catch(Exception e){
					a.setAlertType(AlertType.ERROR); 
					a.setContentText(e.getMessage());  
					a.show();
					pb.setVisible(false);
					processText.setText("");
					setAllFunc(false);
			    }
		   });
		
		HBox filterPanel = new HBox(20);    
		filterPanel.setPadding(new Insets(25, 10, 25, 25));
		filterPanel.getChildren().addAll(saveFile);		
		
		
		textArea = new TextArea();
		textArea.setEditable(false);
		textArea.setPrefHeight(primaryScreenBounds.getHeight() - 200);
		textArea.setPrefWidth( primaryScreenBounds.getWidth() - 400);
		ScrollPane scrollPane = new ScrollPane(textArea);
		
		VBox buttonBox = new VBox(20);
		buttonBox.setPadding(new Insets(150, 0, 0, 0));
		buttonBox.getChildren().addAll(button,button1);
		
		availabletags = new Label("Available Tags");
		selectedTags = new Label("Selected Tags");
		
		VBox ls1 = new VBox(20);
		ls1.setPadding(new Insets(25, 25, 25, 25));
		ls1.getChildren().addAll(availabletags,listView);
		
		VBox ls2 = new VBox(20);
		ls2.setPadding(new Insets(25, 25, 25, 25));
		ls2.getChildren().addAll(selectedTags,listView2);
		
		HBox hbox = new HBox(20);    
		hbox.setPadding(new Insets(25, 10, 25, 25));
		hbox.getChildren().addAll(ls1,buttonBox,ls2);
		
		
		// Vbox
		VBox vbox = new VBox();
		vbox.setPadding(new Insets(25, 25, 25, 25));
		vbox.getChildren().addAll(buttonHb1,hbox, buttonHb2,r);
		
		// Vbox
		VBox showbox = new VBox();
		showbox.setPadding(new Insets(25, 25, 25, 25));
		showbox.getChildren().addAll(scrollPane,filterPanel);
		
		HBox finalHbox = new HBox();
		finalHbox.setPadding(new Insets(25, 25, 25, 25));
		finalHbox.getChildren().addAll(vbox,showbox);

		// Scene
		Scene scene = new Scene(finalHbox, primaryScreenBounds.getWidth() - 20, primaryScreenBounds.getHeight() - 100); // w x h
		primaryStage.setScene(scene);
		primaryStage.show();
		primaryStage.setResizable(false);
		savedStage = primaryStage;
	}
	
	private void setAllFunc(Boolean val){
		button.setDisable(val);
		button1.setDisable(val);
		processButton.setDisable(val);
		btn1.setDisable(val);
		saveFile.setDisable(val);
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
		pb.setVisible(true);
		if (selectedFile != null) {
			LogProcessor lp = new LogProcessor();
			try{
				new Thread(new Runnable() {
					String message = null;
						@Override
						public void run() {
							try{				
								fileName = selectedFile.getAbsolutePath();
								String tags = lp.getTags(fileName);
								if(tags!=null  && !tags.trim().isEmpty()) 
									listView.getItems().addAll(Arrays.asList(tags.split(",")));
							}catch(Exception e){
								message = e.getMessage();
								e.printStackTrace();
							}
							Platform.runLater(new Runnable() {

								@Override
								public void run() { 
									 if(message != null){
										a.setAlertType(AlertType.INFORMATION); 
										a.setContentText(message);  
										a.show();
									 }
									 pb.setVisible(false);
									 setAllFunc(false);
									 processText.setText("");
								}
							});  
						}
					}).start();	
					
					pb.setVisible(true);
					processText.setText("Reading tags...");
					setAllFunc(true);
				
			}catch(Exception e){
				System.out.println(e);
				pb.setVisible(false);
				processText.setText("");
				setAllFunc(false);
			}
		}
		else {
			System.out.println("Log File selection cancelled.");
		}
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

	}
}