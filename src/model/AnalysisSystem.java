package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import LemmaUtils.FileProcessor;

public class AnalysisSystem {

	private final String IMPORTS_KW = "imports";
	private final String BASIC_FILE = "main";
	private ArrayList<String> filelist = new ArrayList<String>();
	
	private Map<String, StatisticsNode> statistics = new HashMap<String, StatisticsNode>();

	public void constructGraph(){
		
	}
	
	private void analyseFile(String fileName){
		BufferedReader br = null;
		if(fileName.toLowerCase().equals(BASIC_FILE)||fileName.equals("")||isProcessed(fileName)){
			return;
		}
		try {
			String line = "";
			fileName += ".thy";
			br = new BufferedReader(new FileReader(new File(fileName)));
			while((line = br.readLine()) !=null ){
				if(line.toLowerCase().contains(IMPORTS_KW)){
					String [] bases = line.substring(line.lastIndexOf(IMPORTS_KW)+IMPORTS_KW.length()
					, line.length()).trim().split("\\s+");
					for(String basis : bases){
						System.out.println(basis);
						analyseFile(basis);
					}
				}
			}
			processFile(fileName);
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
		as.analyseFile("SteamBoiler_proof");
		as.reCheck();
		as.print();
	}
	
	private void reCheck(){
		for(Map.Entry<String, StatisticsNode> entry: this.statistics.entrySet()){
			StatisticsNode sNode = entry.getValue();
			
			Iterator it = sNode.getRules().entrySet().iterator();
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
	
	private boolean isLemma(String kw){
		return this.statistics.get(kw)!=null;
	}
	
	private void print() {
		for(Map.Entry<String, StatisticsNode> entry :this.statistics.entrySet()){
			System.out.println(entry.getValue());
		}
	}

}
