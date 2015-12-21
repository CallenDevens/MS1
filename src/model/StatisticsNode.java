package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StatisticsNode {
	public static final String TYPE_LEMMA = "lemma";
	public static final String TYPE_SODIUM = "lemma";
	
	private static ArrayList<String> FILTER_WORDS;
	private static ArrayList<String> TACTIC_LIBRARY;;

	private final Pattern LEMMA_KEY_PATTERN = Pattern.compile("^l\\d+_(.)+", Pattern.CASE_INSENSITIVE);
	private final Pattern DEF_KEY_PATTERN = Pattern.compile("^(.)+_def", Pattern.CASE_INSENSITIVE);

	
	private String type;
	private String keyword;
	private String fileName;
	
    private Map<String, Integer> lemmas;
    private Map<String, Integer> definitions;
    private Map<String, Integer> tactics;
    
    private Map<String, Integer> usedin;
	
	public StatisticsNode(String kw, String type, String filen){
		this.keyword = kw;
		this.type = type;
		this.fileName = filen;
		init();
	}
	
	public StatisticsNode(){
		this.keyword = "";
		this.type = "";
		init();
	}
	
	public Map<String, Integer> getTactics(){
		return this.tactics;
	}
	private void init(){
		lemmas = new HashMap<String, Integer>();
		definitions = new HashMap<String, Integer>();
		tactics = new HashMap<String, Integer>();
		usedin = new HashMap<String, Integer>();
		
		if(FILTER_WORDS == null){
		    FILTER_WORDS = new ArrayList<String>();
		    initFilterWords();
		}
		
		if(TACTIC_LIBRARY==null){
    		TACTIC_LIBRARY = new ArrayList<String>();
	    	initTactics();
		}
	}
	private void initTactics() {
		BufferedReader br = null;
		try {
			String line = "";
			br = new BufferedReader(new FileReader(new File("tactic_library")));
			while((line = br.readLine()) !=null ){
				TACTIC_LIBRARY.add(line);
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void initFilterWords() {
		BufferedReader br = null;
		try {
			String line = "";
			br = new BufferedReader(new FileReader(new File("filter_words")));
			while((line = br.readLine()) !=null ){
				FILTER_WORDS.add(line);
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void mapClear(){
		lemmas.clear();;
		definitions.clear();;
		tactics.clear();;
	}
	
	public String getKeyWord(){
		return this.keyword;
	}
	
	public void clear(){
		this.keyword = "";
		this.mapClear();
	}

	public void reset(String keyword, String type) {
		this.keyword = keyword;
		this.type = type;
	}
	
	public boolean containsRecord(String kw){
		return (lemmas.get(kw)!=null?true:false)||(definitions.get(kw)!=null?true:false)||(tactics.get(kw)!=null?true:false);
	}

	public boolean containsLemma(String kw){
		return (lemmas.get(kw)!=null?true:false);
	}
	
	public boolean containsUsedIn(String kw){
		return (usedin.get(kw)!=null?true:false);
	}
	
	public boolean containsDefinition(String kw){
		return (definitions.get(kw)!=null?true:false);
	}
	
	public boolean containsTactic(String kw){
		return (tactics.get(kw)!=null?true:false);
	}
	
	public void addLemmaCount(String kw){
		if(this.containsLemma(kw)){
		    lemmas.put(kw, lemmas.get(kw)+1);
		}
		else{
		    lemmas.put(kw, 1);
		}
	}
	
	public void addDefintionCount(String kw){
		if(this.containsDefinition(kw)){
		    definitions.put(kw, definitions.get(kw)+1);
		}
		else{
			definitions.put(kw, 1);
		}
	}
	
	public void addTacticCount(String kw){
		if(this.containsTactic(kw)){
		    tactics.put(kw, tactics.get(kw)+1);
		}
		else{
			tactics.put(kw, 1);
		}
	}
	public void addRecordCount(String kw) {
		if(kw == null || kw.equals("")||this.isCooperator(kw))
			return;
		if(TACTIC_LIBRARY.contains(kw)){
			this.addTacticCount(kw);	
		}
		else{
	        Matcher defMatcher = DEF_KEY_PATTERN.matcher(kw);
		    if(defMatcher.matches()){
			   this.addDefintionCount(kw);
		    }
		    else{
		    	this.addLemmaCount(kw);
		    }
		}
	}

	private boolean isCooperator(String kw) {
		return FILTER_WORDS.contains(kw.toLowerCase());
	}

	public String toString(){
		if(!keyword.equals("")){
		String result = keyword + ":\n" ;
		result+="lemmas:";
		for (Map.Entry<String, Integer> entry: this.lemmas.entrySet()){
			result += "("+entry.getKey() + ":" + entry.getValue() +"),";
		}
		result += "\nDefs:";
		for (Map.Entry<String, Integer> entry: this.definitions.entrySet()){
			result += "("+entry.getKey() + ":" + entry.getValue() +"),";
		}
		
		result += "\nTactics:";
		for (Map.Entry<String, Integer> entry: this.tactics.entrySet()){
			result += "("+entry.getKey() + ":" + entry.getValue() +"),";
		}
		result += "\nUsedin";
		for (Map.Entry<String, Integer> entry: this.usedin.entrySet()){
			result += "("+entry.getKey() + ":" + entry.getValue() +"),";
		}
		result += "\n";
		return result;
		}
		else{
			return "";
		}
	}

	public void rearrangeLemma(Entry<String, Integer> node) {
		this.lemmas.put(node.getKey(), node.getValue());
	}
	
	public String getFileName(){
		return this.fileName;
	}

	public Map<String, Integer> getLemmas() {
		return this.lemmas;
	}

	public void addUsedIn(String kw) {
		if(this.containsUsedIn(kw)){
		    usedin.put(kw, usedin.get(kw)+1);
		}
		else{
			usedin.put(kw, 1);
		}		
	}
}
