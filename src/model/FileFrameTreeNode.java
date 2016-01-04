package model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

public class FileFrameTreeNode {
	private ArrayList<FileFrameTreeNode> parents = new ArrayList<FileFrameTreeNode>();
	private String name;
	private ArrayList<FileFrameTreeNode> children =  new ArrayList<FileFrameTreeNode>();
	
	public FileFrameTreeNode(String name){
		this.name = name;
	}
	
	public void addToParents(FileFrameTreeNode node){
		parents.add(node);
	}

	public void addToChildren(FileFrameTreeNode node){
		children.add(node);
	}

	public ArrayList<FileFrameTreeNode> getParents() {
		return this.parents;
	}

	public String getName() {
		return this.name;
	}

	public ArrayList<FileFrameTreeNode> getChildren() {
		return this.children;
	}

	public void clearReduntancy() {
		Iterator<FileFrameTreeNode> paIter = this.parents.iterator();
		ArrayList<FileFrameTreeNode> parentsIterList = new ArrayList<FileFrameTreeNode>(this.parents);
	    while (paIter.hasNext()) {  
	    	FileFrameTreeNode parent = paIter.next();
	    	for(FileFrameTreeNode parforcheck:parentsIterList){
	    		if (this.ParentsContain(parent.getName(), parforcheck)) {
	    			paIter.remove();
	    			//System.out.println("remove "+parent.getName());
	    		}
	    	}
	    }  
	}

	private boolean ParentsContain(String name,FileFrameTreeNode parent) {
		boolean contained = false;
		if(parent.parents.isEmpty()){
			return false;
		}
		for(FileFrameTreeNode parparent:parent.parents){
			if(parparent.getName().equals(name)){
				return true;
			}
			else{
				contained = ParentsContain(name,parparent)||contained;
			}
		}
		return contained;
	}

	public void print() {
		System.out.print("Name :" +this.getName()+"\nparents:");
		for(FileFrameTreeNode parent:parents){
			System.out.print(parent.getName() +", ");			
		}
		System.out.print("\nchildren:");
		for(FileFrameTreeNode child:children){
			System.out.print(child.getName() +", ");			
		}
		System.out.println();
	}

	public void addParent(FileFrameTreeNode node) {
		ListIterator<FileFrameTreeNode> paIter =  this.parents.listIterator();
		paIter.add(node);
	}


}
