package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;

public class FileFrameAnalyser {
	private final String IMPORTS_KW = "imports";
	private final String BASIC_FILE = "main";
	private final static String RELATIVE_FILE_PATH = "/Users/aya/Documents/workspace/MS1/thy/";
	
	private FileFrameTree fileTree = new FileFrameTree();
	private ArrayList<FileFrameTreeNode> nodeList = new ArrayList<FileFrameTreeNode>();
	
	public FileFrameAnalyser(){
		nodeList.add(new FileFrameTreeNode("main"));
	}
	
	public void analyseFilesStructure(){
		File path = new File(RELATIVE_FILE_PATH);
	    File [] files = path.listFiles();
	    for (int i = 0; i < files.length; i++){
	        if (files[i].isFile() && files[i].getName().endsWith(".thy")){ //this line weeds out other directories/folders
	        	analyseFileStructure(files[i].getName());
	        }
	    }
	    /*
	    for(FileFrameTreeNode node: nodeList){
	         node.print();
	         System.out.println();
	    }
	    */
	    
	    fileTree.constructTree(nodeList);
	    fileTree.unlinkTree(fileTree.root);
	    
	    /*
	    System.out.println("After unlink!");
	    for(FileFrameTreeNode node: nodeList){
	         node.print();
	         System.out.println();
	    }
	    */
	}

	private void analyseFileStructure(String fileName){
		BufferedReader br = null;
		if(fileName.equals("")){
			return;
		}
		String nodeName = fileName.replace(".thy", "").toLowerCase();
		FileFrameTreeNode node = getListTreeNode(nodeName);
		if(node == null){
			node= new FileFrameTreeNode(nodeName);
			nodeList.add(node);
		}
		try {
			String line = "";
			br = new BufferedReader(new FileReader(new File(RELATIVE_FILE_PATH+fileName)));
			while((line = br.readLine()) !=null ){
				if(line.toLowerCase().contains(IMPORTS_KW)){
					String [] bases = line.substring(line.lastIndexOf(IMPORTS_KW)+IMPORTS_KW.length()
					, line.length()).trim().split("\\s+");
					for(String basis : bases){
						analyseImportedFile(basis, node);
					}
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void analyseImportedFile(String basis, FileFrameTreeNode node) {
		basis = basis.toLowerCase();
		FileFrameTreeNode parent = getListTreeNode(basis);
		if(parent == null){
			parent = new FileFrameTreeNode(basis);
			nodeList.add(parent);
		}
		node.addToParents(parent);
		parent.addToChildren(node);
	}
	
	private FileFrameTreeNode getListTreeNode(String basis) {
		for(FileFrameTreeNode node: this.nodeList){
			if(node.getName().equals(basis)){
				return node;
			}
		}
		return null;
	}

	public void saveFileStructure() {
		PrintWriter writer;
		/*
		 * format:
		 * module name
		 * imported module1|imported module2|...|
		 * * as end of every record
		 * */
		try {
			writer = new PrintWriter(new FileOutputStream(new File("file_structure.anl")));
            for(FileFrameTreeNode node: nodeList){
    			writer.println(node.getName());
				for(FileFrameTreeNode parent: node.getParents()){
					writer.print(parent.getName()+"|");;
				}
				writer.println();
				writer.println("*");
			}
	    	writer.close();	
		}catch (FileNotFoundException e) {
				e.printStackTrace();	    
		}
		//write results into excel file.
		ExcelWriter.writeFilesStructure(this.nodeList);
	}
	
}
