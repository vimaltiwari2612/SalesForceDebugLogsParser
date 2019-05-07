import java.io.*;
import java.util.*;
import javafx.collections.*;

public class LogProcessor{
	private static ArrayList<String> toBeIngnored = new ArrayList<String>(Arrays.asList("HEAP_ALLOCATE","STATEMENT_EXECUTE","USER_INFO","EXECUTION_STARTED","VARIABLE_SCOPE_BEGIN","VARIABLE_ASSIGNMENT"));
	private static ArrayList<String> tags = new ArrayList<String>();
	private TreeSet<String> tagsAvailable = new TreeSet<String>();
	private static LinkedList<String> localStack = new LinkedList<String>();
	
	public String getTags(String fileName) throws Exception{
		this.tagsAvailable = this.getTagsFromFile(fileName);
		if(this.tagsAvailable == null || this.tagsAvailable.isEmpty()) return "";
		String toBeReturned = "";
		for(String str : tagsAvailable)
			toBeReturned +=","+str;
		return toBeReturned.substring(1);
	}	
	
	public void setTags(ObservableList tagList){
		if(tags == null) tags = new ArrayList<String>();
		tags.clear();
		for(Object o: tagList) tags.add(o.toString());
	}
	
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
	
	private static String getTabs(int count){
		String tabs = "";
		while(count > 0){
			tabs+="\t";
			count--;
		}
		return tabs;
	}
	
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
	
	private String getTagFromLine(String line){
		String retString = null;
		line = line.substring(line.indexOf("|") + 1);
		int typeIndex = line.indexOf("|");
		if(typeIndex != -1)
			retString = line.substring(0,typeIndex);
		return retString;
	}
	
	private static String processLine(String line){
		String retString = "";
		line = line.substring(line.indexOf("|") + 1);
		int typeIndex = line.indexOf("|");
		if(typeIndex != -1){
			String tag = line.substring(0,typeIndex);
			if(tag.contains("METHOD_ENTRY")){	
				retString +=getTabs(localStack.size())+tag;
				localStack.add(tag);
			}
			else if(tag.contains("METHOD_EXIT") && localStack.peekLast().contains("METHOD_ENTRY")){
				localStack.removeLast();
				retString +=getTabs(localStack.size())+tag;
			}
			else{
				retString +=getTabs(localStack.size())+tag;
			}
			retString +="     :      ";
			typeIndex = line.lastIndexOf("|");
			if(typeIndex != -1)
				retString+= line.substring(typeIndex+1);
		}
		else{
			return line;
		}
		return retString;
	}
	
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