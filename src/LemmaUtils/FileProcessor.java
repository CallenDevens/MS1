package LemmaUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
	private final String KEY_WORD_PROOF = "proof";
	
	private String fileName;
	private BufferedReader br;
	
	//Statistics data of the file (many lemmas in it)
	private Map<String, StatisticsNode> statistics = new LinkedHashMap<String, StatisticsNode>();
	
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
			//System.out.println(fileName);
    		br = new BufferedReader(new FileReader(new File(fileName+".thy")));
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
		    	// ^lemma\\s.*
			    if(m.matches()){
			    	Matcher indexMacther = lemmaStartPattern.matcher(line);
		    	    	if(indexMacther.find()){
		    	    		this.addStatisticsNode(sNode);
		    	    		String keyword = line.substring(indexMacther.end(), line.indexOf(":")).trim().replaceAll(":", "");
		    	    		sNode = new StatisticsNode(keyword, StatisticsNode.TYPE_LEMMA, this.fileName);
			    	}
			    }
			    if(!line.startsWith("--")){
			        if(line.toLowerCase().contains(KEY_WORD_BY)){ 
				        proofAnalyze(line.substring(line.toLowerCase()
				    	    	.lastIndexOf(KEY_WORD_BY)+KEY_WORD_BY.length(), line.length())
				    		    .trim(), mainStatistics, sNode);	
			        }
			        else if(line.toLowerCase().trim().startsWith(KEY_WORD_PROOF) && line.contains("(")){
			    	    //System.out.println(line);
			    	    proofAnalyze2(line.toLowerCase().trim(), mainStatistics, sNode);
			        }
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
	
	private void proofAnalyze2(String line, Map<String, StatisticsNode> mainStatistics, StatisticsNode sNode) {
		if(sNode == null){
			return;
		}
		line = line.replaceAll("\".*?\"", "");
		String [] proofs = line.split("\\(|\\)|\\s+");
		for(int i=0; i < proofs.length; i++){
			//System.out.print(proof[i]+",");
			String proof = proofs[i];
			proof = proof.trim();
			//proof is not empty or variable
			if(!proof.equals("") && proof.length() >2 && !proof.contains("\"") ){
				sNode.addRecordCount(proof);
			}
		}		
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
			proof = proof.trim();
			//proof is not empty or variable
			if(!proof.equals("") && proof.length() >1 ){
				if( mainStatistics.get(proof)!=null){
					sNode.addLemmaCount(proof);
				}
				else{
					//proof is a definition or bases
					sNode.addRecordCount(proof);
				}
			}
		}
	}

	//test function
	public static void main(String [] args){
		//FileProcessor fp = new FileProcessor("arith_hints.thy");
		//fp.print();
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
