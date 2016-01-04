package model;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelWriter {
	private static final String FILE_PATH = ";";
	private static final ExcelWriter INSTANCE = new ExcelWriter();
	private static Map<String, HashMap<String, StatisticsNode>> fileStatices
	    = new HashMap<String, HashMap<String, StatisticsNode>>();
	CellStyle headerStyle = null;
	CellStyle dataCellStyle = null;
	
	public static ExcelWriter getInstance(){
		return INSTANCE;
	}
	
	@SuppressWarnings("resource")
	public void writeLemmaStructure(Map<String, StatisticsNode> statistics){
		this.reconstruct(statistics);
		
		for(Map.Entry<String, HashMap<String, StatisticsNode>> fileStEntry: this.fileStatices.entrySet()){
            String fileName = fileStEntry.getKey();
			
			Workbook lemmaWorkbook = new XSSFWorkbook();
			Sheet lemmaSheet = lemmaWorkbook.createSheet();
			Sheet definitionSheet = lemmaWorkbook.createSheet();
			Sheet tacticsSheet = lemmaWorkbook.createSheet();
			Sheet usedinSheet = lemmaWorkbook.createSheet();
			
			lemmaWorkbook.setSheetName(0, "Lemma");
			lemmaWorkbook.setSheetName(1, "Definition");
			lemmaWorkbook.setSheetName(2, "Tactic");
			lemmaWorkbook.setSheetName(3, "Used-in");
			
			headerStyle = lemmaWorkbook.createCellStyle();
			dataCellStyle =  lemmaWorkbook.createCellStyle();
			this.initializeHeaderStyle(headerStyle, lemmaWorkbook);
			this.initializeDataCellStyle(dataCellStyle, lemmaWorkbook);
			
			Row lemmaHeaderRow = lemmaSheet.createRow(0);
			Row defHeaderRow = definitionSheet.createRow(0);
			Row tacticHeaderRow = tacticsSheet.createRow(0);
			Row usedinHeaderRow = usedinSheet.createRow(0);

			this.initializeStartingCell(lemmaHeaderRow);
			this.initializeStartingCell(defHeaderRow);
			this.initializeStartingCell(tacticHeaderRow);
			this.initializeStartingCell(usedinHeaderRow);
            
			int rowIndex = 1;
			for(Map.Entry<String, StatisticsNode> stEntry :fileStEntry.getValue().entrySet()){
				
				int cellIndex = 0;
				StatisticsNode sNode = stEntry.getValue();
				String keyword = sNode.getKeyWord();
				Cell headerCell = null;

				Row lemmaRow = lemmaSheet.createRow(rowIndex);
				Row defRow = definitionSheet.createRow(rowIndex);
				Row tacticRow = tacticsSheet.createRow(rowIndex);
				Row usedinRow = usedinSheet.createRow(rowIndex);
				
				writeLineToSheet(lemmaRow,  lemmaHeaderRow, sNode.getLemmas(),     keyword, "Lemma");
				writeLineToSheet(defRow,    defHeaderRow,   sNode.getDefinition(), keyword, "Definition");
				writeLineToSheet(tacticRow, tacticHeaderRow,sNode.getTactics(),    keyword, "Tactic");
				writeLineToSheet(usedinRow, usedinHeaderRow,sNode.getUsedin(),     keyword, "Used in");

				rowIndex++;
			}
			ResizeSheet(lemmaSheet);
			ResizeSheet(definitionSheet);
			ResizeSheet(tacticsSheet);
			ResizeSheet(usedinSheet);

			try{
				FileOutputStream fos = new FileOutputStream(fileName+".xlsx");
				lemmaWorkbook.write(fos);
				fos.close();
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}

	private void initializeStartingCell(Row headerRow) {
		Cell startingCell = headerRow.createCell(0);
		startingCell.setCellValue("Lemma");
		if(headerStyle!=null){
		    startingCell.setCellStyle(headerStyle);
		}
	}

	private void writeLineToSheet(Row row, Row headerRow, Map<String, Integer> map, String keyword, String type) {
		int cellIndex = 0;
		XSSFCell headercell = null;
		headercell = (XSSFCell) row.createCell(cellIndex++);
		headercell.setCellValue(keyword);
		headercell.setCellStyle(headerStyle);

		for(Map.Entry<String, Integer> entry: map.entrySet()){
			String nodeName = entry.getKey();
			for(int i = 0; i< entry.getValue(); i++){
				
				Cell cellValue = row.createCell(cellIndex++);
				cellValue.setCellValue(nodeName);
				cellValue.setCellStyle(dataCellStyle);
				
				if(cellIndex > headerRow.getLastCellNum()){
					headercell = (XSSFCell) headerRow.createCell(headerRow.getLastCellNum());
					headercell.setCellValue(type+"-"+(cellIndex-1));
					headercell.setCellStyle(headerStyle);
				}
			}
		}
	}

	private void ResizeSheet(Sheet sheet) {
		for(int i = 0; i< sheet.getLastRowNum(); i++){
			sheet.setColumnWidth(i, 8000);
		}		
	}

	private void reconstruct(Map<String, StatisticsNode> statistics) {
		for(Entry<String, StatisticsNode> stEntry:statistics.entrySet()){
			String fileName = stEntry.getValue().getFileName();
			String lemmaName = stEntry.getValue().getKeyWord();
			if(this.fileStatices.get(fileName) == null){
				this.fileStatices.put(fileName, new HashMap<String, StatisticsNode>());
			}
			this.fileStatices.get(fileName).put(lemmaName, stEntry.getValue());
		}
	}

	public static void writeFilesStructure(ArrayList<FileFrameTreeNode> nodeList) {
		Workbook fileWorkbook = new XSSFWorkbook();
		Sheet importSheet = fileWorkbook.createSheet();
		int rowIndex = 0;
		
		for(FileFrameTreeNode node:nodeList){
			int cellIndex = 0;
			Row fileRow = importSheet.createRow(rowIndex++);
			fileRow.createCell(cellIndex++).setCellValue(node.getName());
			
			for(FileFrameTreeNode parent: node.getParents()){
				fileRow.createCell(cellIndex++).setCellValue(parent.getName());
			}
			
		}
		
		try{
			FileOutputStream fos = new FileOutputStream("imports_structure.xlsx");
			fileWorkbook.write(fos);
			fos.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		
	}
	
	private void initializeHeaderStyle(CellStyle headerStyle, Workbook wb){
		headerStyle.setBorderBottom(XSSFCellStyle.BORDER_MEDIUM);
		headerStyle.setBorderTop(XSSFCellStyle.BORDER_MEDIUM);
		headerStyle.setBorderRight(XSSFCellStyle.BORDER_MEDIUM);
		headerStyle.setBorderLeft(XSSFCellStyle.BORDER_MEDIUM); 
		
		headerStyle.setFillBackgroundColor(HSSFColor.PALE_BLUE.index);
		headerStyle.setFillForegroundColor(HSSFColor.PALE_BLUE.index);
		headerStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
		
		headerStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
        
		XSSFFont xSSFFont = (XSSFFont) wb.createFont();
        xSSFFont.setFontName(HSSFFont.FONT_ARIAL);
        xSSFFont.setFontHeight(12);
//        xSSFFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
//        xSSFFont.setColor(HSSFColor.WHITE.index);
        headerStyle.setFont(xSSFFont); 
	}
	
	private void initializeDataCellStyle(CellStyle headerStyle, Workbook wb){
		/*
		headerStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
		headerStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
		headerStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
		headerStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
		*/
		
        XSSFFont xSSFFont = (XSSFFont) wb.createFont();
        xSSFFont.setFontHeight(12);
//        xSSFFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
//        xSSFFont.setColor(HSSFColor.WHITE.index);
        headerStyle.setFont(xSSFFont); 
	}

}
