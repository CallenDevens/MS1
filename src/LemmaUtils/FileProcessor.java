package LemmaUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.StatisticsNode;

/**
 * class FileProcessor
 * used to analyze a file contains many lemma
 *
 */
public class FileProcessor {
	//keyword of proving
	private final String KEY_WORD_BY = "by";
	
	private String fileName;
	private BufferedReader br;
	
	//Statistics data of the file (many lemmas in it)
	private Map<String, StatisticsNode> statistics = new HashMap<String, StatisticsNode>();
	
	//Regular Expressions for compare
	private String lemmaRegex = "^lemma\\s.*";
	private String lemmaStartRegex = "lemma";
	private Pattern lemmaPattern = Pattern.compile(lemmaRegex, Pattern.CASE_INSENSITIVE);
	private Pattern lemmaStartPattern = Pattern.compile(lemmaStartRegex, Pattern.CASE_INSENSITIVE);

	//private Pattern proofPattern = Pattern.compile("^[A-Za-z ]*(using\\s+assms\\s+)?by", Pattern.CASE_INSENSITIVE);
	
	public FileProcessor(String fileName){
		this.fileName = fileName;
		init();
	}
	
	//initialize BufferedReader
	private void init(){
		try{
    		br = new BufferedReader(new FileReader(new File(fileName)));
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void process(Map<String, StatisticsNode> mainStatistics){
		String line = "";
		StatisticsNode sNode = null;
		
		try{
    		line = br.readLine();
	    	do{
		    	Matcher m = lemmaPattern.matcher(line);
			    if(m.matches()){
			    	Matcher indexMacther = lemmaStartPattern.matcher(line);
		    	    	if(indexMacther.find()){
		    	    		this.addStatisticsNode(sNode);
		    	    		String keyword = line.substring(indexMacther.end(), line.length()).trim().replaceAll(":", "");
		    	    		sNode = new StatisticsNode(keyword, StatisticsNode.TYPE_LEMMA);
		    	    		
			    	}
			    }
			    if(line.toLowerCase().contains(KEY_WORD_BY)){ 
				    proofAnalyze(line.substring(line.toLowerCase()
				    		.lastIndexOf(KEY_WORD_BY)+KEY_WORD_BY.length(), line.length())
				    		.trim(), mainStatistics, sNode);	
			    }
		    }while ((line = br.readLine()) != null);
	    	this.addStatisticsNode(sNode);
	    }
		catch(IOException e){
			e.printStackTrace();
		}
		finally{
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}		
		}
		this.aggreagateStatistics(mainStatistics);
	}
	
	private void addStatisticsNode(StatisticsNode sNode) {
		if(sNode!=null){
			this.statistics.put(sNode.getKeyWord(), sNode);
		}		
	}

	private void proofAnalyze(String line, Map<String, StatisticsNode> mainStatistics,
			StatisticsNode sNode){
		if(sNode == null){
			return;
		}
		String [] proofs = line.split("(\\s*)(\\(|\\)|,|:|\\s+)+(\\s)*");
		for(int i=0; i < proofs.length; i++){
			//System.out.print(proof[i]+",");
			String proof = proofs[i];
			
			//proof is a lemma
			if(!proof.equals("") && mainStatistics.get(proof)!=null){
			    sNode.addLemmaCount(proof);
			}
			else{
				//proof is a definition or bases
			    sNode.addRecordCount(proof);
			}
		}
	}

	//test function
	public static void main(String [] args){
		FileProcessor fp = new FileProcessor("arith_hints.thy");
		fp.print();
	}

	private void print() {
		for(Map.Entry<String, StatisticsNode> entry :this.statistics.entrySet()){
			System.out.println(entry.getValue());
		}
	}

	public void aggreagateStatistics(Map<String, StatisticsNode> mainStatistics) {
		mainStatistics.putAll(this.statistics);
	}
}
