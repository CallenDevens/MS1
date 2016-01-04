package model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;


public class FileFrameTree {

	public FileFrameTreeNode root = new FileFrameTreeNode("main");

	public void addToTree(FileFrameTreeNode node) {
		
		ArrayList<FileFrameTreeNode> parents = node.getParents();
		ListIterator<FileFrameTreeNode> paIter = parents.listIterator();
		
		while(paIter.hasNext()){
			FileFrameTreeNode parent = paIter.next();
			//System.out.print(" parent name :"+ parent.getName()+"....");
			if(this.contains(parent, root)){
				linkParent(parent.getName(), node);
			}
			else{
				addToTree(parent);
				//linkParent(root.getName(), node);
			}
		}
	}

	private void linkParent(String name, FileFrameTreeNode node) {
		FileFrameTreeNode parent = getNode(name, root);
		parent.getChildren().add(node);
		//node.addParent(parent);
	}

	private FileFrameTreeNode getNode(String name, FileFrameTreeNode node) {
		String keyword = name;
		if(name.equals(root.getName())){
			return root;
		}
		ArrayList<FileFrameTreeNode> children = node.getChildren();
		if(!children.isEmpty()){
			for(FileFrameTreeNode child:children){
				if(keyword.equals(child.getName())){
					return child;
				}
				else{
					return getNode(keyword, child);
				}
			}
		}
		return null;
      }

	private boolean contains(FileFrameTreeNode node, FileFrameTreeNode startNode) {
		String keyword = node.getName();
		if(keyword.equals(root.getName())){
			return true;
		}
		ArrayList<FileFrameTreeNode> children = startNode.getChildren();
		if(!children.isEmpty()){
			for(FileFrameTreeNode child:children){
				if(keyword.equals(child.getName())){
					return true;
				}
				else{
					return contains(node, child);
				}
			}
		}
		return false;
	}

	public void constructTree(ArrayList<FileFrameTreeNode> nodeList) {
		Iterator<FileFrameTreeNode> nodeIter = nodeList.iterator();
		while(nodeIter.hasNext()){
		    FileFrameTreeNode node = nodeIter.next();
		    if(node.getName().equals("main")){
		    	this.root = node;
		    }
			//this.addToTree(node);
		}
		//this.linkChildren(root);
	}

	private void linkChildren(FileFrameTreeNode startNode) {
		if(startNode == null){
			return;
		}
		Iterator<FileFrameTreeNode> childrenIter = startNode.getChildren().iterator();
		while(childrenIter.hasNext()){
			FileFrameTreeNode child = childrenIter.next();
			child.addParent(startNode);
			linkChildren(child);
		}
		
	}

	public void unlinkTree(FileFrameTreeNode node) {
		ArrayList<FileFrameTreeNode> children = node.getChildren();
		if(!children.isEmpty()){
			for(FileFrameTreeNode child : children){
				child.clearReduntancy();
				unlinkTree(child);
			}
		}
	}

	public void printTree(FileFrameTreeNode node, String spliter) {
		if(node == null){return;}
		else{
			System.out.println(spliter+node.getName());
			ArrayList<FileFrameTreeNode> children = node.getChildren();
			if(!children.isEmpty()){
				for(FileFrameTreeNode child : children){
					printTree(child, spliter+" ");
				}
			}
			else{
				return;
			}
		}
	}
}
