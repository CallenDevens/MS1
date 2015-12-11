package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StatisticsNode {
	public static final String TYPE_LEMMA = "lemma";
	public static final String TYPE_SODIUM = "lemma";
	
	private ArrayList<String> FILTER_WORDS;
	private final Pattern LEMMA_KEY_PATTERN = Pattern.compile("^l\\d+_(.)+", Pattern.CASE_INSENSITIVE);
	private final Pattern DEF_KEY_PATTERN = Pattern.compile("^(.)+_def", Pattern.CASE_INSENSITIVE);

	
	private String type;
	private String keyword;
	
    private Map<String, Integer> lemmas;
    private Map<String, Integer> definitions;
    private Map<String, Integer> rules;
	
	public StatisticsNode(String kw, String type){
		this.keyword = kw;
		this.type = type;
		init();
	}
	
	public StatisticsNode(){
		this.keyword = "";
		this.type = "";
		init();
	}
	
	public Map<String, Integer> getRules(){
		return this.rules;
	}
	private void init(){
		lemmas = new HashMap<String, Integer>();
		definitions = new HashMap<String, Integer>();
		rules = new HashMap<String, Integer>();
		
		FILTER_WORDS = new ArrayList<String>();
		FILTER_WORDS.add("add");
		FILTER_WORDS.add("rule");
		FILTER_WORDS.add("only");

	}
	public void mapClear(){
		lemmas.clear();;
		definitions.clear();;
		rules.clear();;
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
		return (lemmas.get(kw)!=null?true:false)||(definitions.get(kw)!=null?true:false)||(rules.get(kw)!=null?true:false);
	}

	public boolean containsLemma(String kw){
		return (lemmas.get(kw)!=null?true:false);
	}
	
	public boolean containsDefinition(String kw){
		return (definitions.get(kw)!=null?true:false);
	}
	
	public boolean containsRule(String kw){
		return (rules.get(kw)!=null?true:false);
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
	
	public void addRuleCount(String kw){
		if(this.containsRule(kw)){
		    rules.put(kw, rules.get(kw)+1);
		}
		else{
			rules.put(kw, 1);
		}
	}
	public void addRecordCount(String kw) {
		if(kw == null || kw.equals("")||this.isCooperator(kw))
			return;
		/*
		Matcher lemmaMatcher = LEMMA_KEY_PATTERN.matcher(kw);
		if(lemmaMatcher.matches()){
			this.addLemmaCount(kw);
		}
		else{
		*/
		
	    Matcher defMatcher = DEF_KEY_PATTERN.matcher(kw);
		if(defMatcher.matches()){
			this.addDefintionCount(kw);
		}
		else{
			this.addRuleCount(kw);
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
		
		result += "\nRules:";
		for (Map.Entry<String, Integer> entry: this.rules.entrySet()){
			result += "("+entry.getKey() + ":" + entry.getValue() +"),";
		}
		result += "\n";
		return result;}
		else{
			return "";
		}
	}

	public void rearrangeLemma(Entry<String, Integer> node) {
		this.lemmas.put(node.getKey(), node.getValue());
	}
}
