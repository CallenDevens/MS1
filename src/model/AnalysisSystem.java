package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import LemmaUtils.FileProcessor;

public class AnalysisSystem {

	private final String IMPORTS_KW = "imports";
	private final String BASIC_FILE = "main";
	private final static String RELATIVE_FILE_PATH = "/Users/aya/Documents/workspace/MS1/thy/";
	
	private ArrayList<String> filelist = new ArrayList<String>();
	
	private Map<String, StatisticsNode> statistics = new LinkedHashMap<String, StatisticsNode>();

	public void constructGraph(){
		
	}
	
	/**
	 * Recursive method to analyze all the requirements for a file's lemmas
	 * @param fileName name of the analyzed file
	 */
	private void analyseFile(String fileName){
		BufferedReader br = null;
		
		//if the file is "Main" 
		//or file name is empty
		//or the file has been processed before. then return
		if(fileName.toLowerCase().equals(BASIC_FILE)||fileName.equals("")||isProcessed(fileName)){
			return;
		}
		try {
			String line = "";
			String fileNameWithThy = fileName + ".thy";
			br = new BufferedReader(new FileReader(new File(RELATIVE_FILE_PATH+fileNameWithThy)));
			while((line = br.readLine()) !=null ){
				if(line.toLowerCase().contains(IMPORTS_KW)){
					String [] bases = line.substring(line.lastIndexOf(IMPORTS_KW)+IMPORTS_KW.length()
					, line.length()).trim().split("\\s+");
					for(String basis : bases){
					//	System.out.println(basis);
						analyseFile(basis);
					}
				}
			}
			processFile(RELATIVE_FILE_PATH+fileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void processFile(String basis) {
		FileProcessor fp = new FileProcessor(basis);
		fp.process(this.statistics);
		this.filelist.add(basis);
	}

	private boolean isProcessed(String fileName) {
		// TODO Auto-generated method stub
		return this.filelist.contains(fileName);
	}
	
	public static void main(String [] args){
		AnalysisSystem as = new AnalysisSystem();
		File path = new File(RELATIVE_FILE_PATH);
	    File [] files = path.listFiles();
	    for (int i = 0; i < files.length; i++){
	        if (files[i].isFile() && files[i].getName().endsWith(".thy")){ //this line weeds out other directories/folders
//	    		System.out.println(files[i].getName());
	        	as.analyseFile(files[i].getName().replace(".thy", "").trim());
	        }
	    }
		as.reCheck();
		as.analyzeUsedIn();
		//as.print();
		as.saveStatistics();
	}
	
    private void saveStatistics() {
    	System.out.println("Write statistics to files.....");
    	PrintWriter writer;
    	for(Map.Entry<String, StatisticsNode> entry :this.statistics.entrySet()){
			try {
				String outputFileName = entry.getValue().getFileName()+ ".anl";
		    	System.out.print("Write to "+outputFileName+".....");
				writer = new PrintWriter(new FileOutputStream(new File(outputFileName),true));
				writer.println(entry.getValue());
		    	writer.close();
		    	System.out.println("finish!");

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void reCheck(){
		for(Map.Entry<String, StatisticsNode> entry: this.statistics.entrySet()){
			StatisticsNode sNode = entry.getValue();			
			Iterator it = sNode.getTactics().entrySet().iterator();
			while (it.hasNext())
			{
			    Map.Entry<String, Integer> node = (Entry<String, Integer>) it.next();
			    if(isLemma(node.getKey()))
			    {
			    	sNode.rearrangeLemma(node);
			    	it.remove();
				}
			}
		}
	}
	
	private void analyzeUsedIn(){
		for(Map.Entry<String, StatisticsNode> entry: this.statistics.entrySet()){
			
			String kw = entry.getKey();
			StatisticsNode sNode = entry.getValue();
			
			Iterator it = sNode.getLemmas().entrySet().iterator();
			while (it.hasNext())
			{
			    Map.Entry<String, Integer> node = (Entry<String, Integer>) it.next();
			    if(this.statistics.get(node.getKey())!=null){
			    	this.statistics.get(node.getKey()).addUsedIn(kw);
			    }
			}
		}
	}
	private boolean isLemma(String kw){
		return this.statistics.get(kw)!=null;
	}
	
	private void print() {
		for(Map.Entry<String, StatisticsNode> entry :this.statistics.entrySet()){
			System.out.println(entry.getValue());
		}
	}

}
