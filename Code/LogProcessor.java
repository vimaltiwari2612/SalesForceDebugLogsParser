import java.io.*;
import java.util.*;
import javafx.collections.*;

public class LogProcessor{
	private static ArrayList<String> tags = new ArrayList<String>();
	private TreeSet<String> tagsAvailable = new TreeSet<String>();
	
	//get tags present in file
	//@return comma seperated tags
	public String getTags(String fileName) throws Exception{
		this.tagsAvailable = this.getTagsFromFile(fileName);
		if(this.tagsAvailable == null || this.tagsAvailable.isEmpty()) return "";
		String toBeReturned = "";
		for(String str : tagsAvailable)
			toBeReturned +=","+str;
		return toBeReturned.substring(1);
	}	
	
	//set tags for processing logs
	public void setTags(ObservableList tagList){
		if(tags == null) tags = new ArrayList<String>();
		tags.clear();
		for(Object o: tagList) tags.add(o.toString());
	}
	
	//main method for parsing selected log file
	public String processLogs(String fileName) throws Exception {
		String codeToBeReturned = "";
		RandomAccessFile raf = new RandomAccessFile(fileName,"rw");
		raf.seek(0);
		raf.readLine();
		while(raf.getFilePointer() < raf.length()){
			String line = raf.readLine();
			if(line.contains("EXECUTION_FINISHED")) break;
			if(isValid(line)){
				line = line.substring(line.indexOf("|") + 1);
					codeToBeReturned+=line+"\n";
			}	
		}
		raf.close();	
		return codeToBeReturned;
	}
	
	//check if the line which is going to be processed has one of the selected tag
	private static Boolean isValid(String line){
		Boolean found = false;
		for(String str : tags){
		if(line.contains(str)) {
			found = true; 
			break;
			}
		}
		return found;
	}
	
	//fetch tags from file 
	private TreeSet<String> getTagsFromFile(String fileName) throws Exception{
		if(tagsAvailable == null) tagsAvailable = new TreeSet<String>();
		tagsAvailable.clear();
		File file = new File(fileName);
		RandomAccessFile raf = new RandomAccessFile(fileName,"rw");
		raf.seek(0);
		while(raf.getFilePointer() < raf.length()){
			String line = raf.readLine();
			String tag = this.getTagFromLine(line);
			if(tag != null)
			tagsAvailable.add(tag);
		}
		raf.close();
		return tagsAvailable;
	}
	
	//fetch tag from line
	private String getTagFromLine(String line){
		String retString = null;
		line = line.substring(line.indexOf("|") + 1);
		int typeIndex = line.indexOf("|");
		if(typeIndex != -1)
			retString = line.substring(0,typeIndex);
		return retString;
	}
	
	//saving the processed log from UI to file
	public String saveFile(String oldFileName,String logs) throws Exception{
		String newFileName = "";
		newFileName = oldFileName.substring(0,oldFileName.lastIndexOf(".")) +"_"+String.valueOf(Calendar.getInstance().getTime()).replaceAll(" ","_").replaceAll(":","_")+".log";
		File file = new File(newFileName);
		if(file.exists())
			throw new Exception("File already saved!");
		file.createNewFile();
		RandomAccessFile raf = new RandomAccessFile(file,"rw");
		raf.seek(0);
		for(String log : logs.split("\n")){
			raf.writeBytes(log);
			raf.writeBytes(System.getProperty("line.separator"));	
		}
		raf.close();
		return newFileName;
	}
}