package com.sunshine.recog.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

public abstract class OutputExcelUtils{
	
	private static HSSFWorkbook wb;
	/**
	 * 标题行样式
	 */
	private static HSSFCellStyle titleStyle;
	/**
	 * 标题行字体
	 */
	private static HSSFFont titleFont;
	/**
	 * 日期行样式
	 */
	private static HSSFCellStyle dateStyle; 
	/**
	 * 日期行字体
	 */
	private static HSSFFont dateFont;
	/**
	 * 表头行样式
	 */
	private static HSSFCellStyle headStyle;
	/**
	 * 表头行字体
	 */
	private static HSSFFont headFont;
	/**
	 * 内容行样式
	 */
	private static HSSFCellStyle contentStyle;
	/**
	 * 内容行字体
	 */
	private static HSSFFont contentFont;
	
	/**
	 * 样式加载标志
	 */
	private static boolean isloadStyle = false;
	
	
	private static Logger log = LoggerFactory.getLogger(OutputExcelUtils.class);
	
	/**
	 * 导出模板文件
	 */
	public static String modelFile = "/excel/modle.xls";
	
	/**
	 * 初始化
	 * @param input
	 */
	private static void initWorkbook(InputStream input) {
		if(input == null){
			wb = new HSSFWorkbook();
		} else {
			try {
				POIFSFileSystem fs = new POIFSFileSystem(input);
				wb = new HSSFWorkbook(fs);
			} catch (Exception e) {
				log.info("输入流获取失败!");
				return;
			}
		}
		if(!isloadStyle){
			titleFont = wb.createFont();
			titleStyle = wb.createCellStyle();
			dateStyle = wb.createCellStyle();
			dateFont = wb.createFont();
			headStyle = wb.createCellStyle();
			headFont = wb.createFont();
			contentStyle = wb.createCellStyle();
			contentFont = wb.createFont();
	
			initTitleCellStyle();
			initTitleFont();
			initDateCellStyle();
			initDateFont();
			initHeadCellStyle();
			initHeadFont();
			initContentCellStyle();
			initContentFont();
		}
	}
	
	public static InputStream loadStream(String path){
		InputStream input = null;
		try {
			input = OutputExcelUtils.class.getClassLoader().getResourceAsStream(path);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return input;
	}
	
	/**
	 * 
	 * @param data 导出数据
	 * @param heads 字段名
	 * @param fields 数据字段
	 * @param sheets EXCEL Sheet定义
	 * @param titles 标题
	 * @param input 读入模板
	 * @param output 输出
	 * @param isloadModle 是否加默认模板
	 */
	private static void outPutExcel(List<String[]> heads, List<String[]> fields, 
			LinkedHashMap<String, List> sheets, String[] titles,
			InputStream input, OutputStream output, boolean isloadModle){
		InputStream tinput = input;
		try {
			if(tinput==null && isloadModle){
				tinput  = loadStream(modelFile);
			}
			initWorkbook(tinput);
			ExportSetInfo esinfo = new ExportSetInfo();
			esinfo.setTitles(titles);
			esinfo.setHeadNames(heads);
			esinfo.setFieldNames(fields);
			esinfo.setObjsMap(sheets);
			esinfo.setOut(output);
			createExcel(esinfo);
			output.flush();
			//tinput.close();
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			try {
				if(output != null)
					output.close(); 
				if(tinput != null)
					tinput.close();
				wb.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 根据默认模板创建WorkBook
	 * @param data 导出数据
	 * @param heads 字段名
	 * @param fields 数据字段
	 * @param sheets EXCEL Sheet定义
	 * @param titles 标题
	 * @param output 输出
	 */
	public static void outPutExcel(List<String[]> heads, List<String[]> fields, 
			LinkedHashMap<String, List> sheets, String[] titles, OutputStream output){
		
		outPutExcel(heads, fields, sheets, titles, null, output, true);
	}
	
	/**
	 * @param data 导出数据
	 * @param heads 字段名
	 * @param fields 数据字段
	 * @param sheets EXCEL Sheet定义
	 * @param titles 标题
	 * @param input 根据输入创建WorkBook
	 * @param output 输出
	 */
	public static void outPutExcel(List<String[]> heads, List<String[]> fields, 
			LinkedHashMap<String, List> sheets, String[] titles,InputStream input, 
			OutputStream output){
		
		outPutExcel(heads, fields, sheets, titles, input, output, false);
	}
	
	/**
	 * 将Map里的集合对象数据输出Excel数据流
	 * @param setInfo
	 * @param input
	 * @throws IOException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings({"unchecked"})
	private static void createExcel(ExportSetInfo setInfo) throws IOException,
			IllegalArgumentException, IllegalAccessException {
		// Sheet numbers
		Set<Entry<String, List>> set = setInfo.getObjsMap().entrySet(); 
		String[] sheetNames = new String[setInfo.getObjsMap().size()];
		int sheetNameNum = 0;
		for (Entry<String, List> entry : set) {
			sheetNames[sheetNameNum] = entry.getKey();
			sheetNameNum++;
		}
		// Create or Get Sheet object
		// Maybe a lot of sheets,  general one sheet by default
		HSSFSheet[] sheets = getSheets(setInfo.getObjsMap().size(), sheetNames);
		int sheetNum = 0;
		for (Entry<String, List> entry : set) {
			// Sheet
			List objs = entry.getValue();
			// 标题行
			createTableTitleRow(setInfo, sheets, sheetNum);
			// 日期行
			createTableDateRow(setInfo, sheets, sheetNum);
			// 表头
			creatTableHeadRow(setInfo, sheets, sheetNum);
			// 表体
			String[] fieldNames = setInfo.getFieldNames().get(sheetNum);
			int rowNum = 3;
			for (Object obj : objs) {
				HSSFRow contentRow = sheets[sheetNum].createRow(rowNum);
				contentRow.setHeight((short) 300);
				HSSFCell[] cells = getCells(contentRow, setInfo.getFieldNames()
						.get(sheetNum).length);
				/**去掉一列序号，因此从1开始*/
				int cellNum = 1;
				if (fieldNames != null) {
					for (int num = 0; num < fieldNames.length; num++) {
						Field field = ReflectionUtils.findField(obj.getClass(), fieldNames[num]);
						field.setAccessible(true);
						Object value = ReflectionUtils.getField(field, obj);
						cells[cellNum].setCellValue(value == null ? "" : value
								.toString());
						cellNum++;
					}
				}
				rowNum++;
			}
			/**自动调整列宽*/
			adjustColumnSize(sheets, sheetNum, fieldNames); 
			sheetNum++;
		}
		wb.write(setInfo.getOut());
		// 添加关闭
		wb.close();
	}
	
	/**
	 * 自动调整列宽
	 * @param sheets
	 * @param sheetNum
	 * @param fieldNames
	 */
	@SuppressWarnings("unused")
	private static void adjustColumnSize(HSSFSheet[] sheets, int sheetNum,
			String[] fieldNames) {
		for (int i = 0; i < fieldNames.length + 1; i++) {
			sheets[sheetNum].autoSizeColumn(i, true);
		}
	}

	/**
	 * 创建标题行(需合并单元格)
	 * @param setInfo
	 * @param sheets
	 * @param sheetNum
	 */
	private static void createTableTitleRow(ExportSetInfo setInfo,
			HSSFSheet[] sheets, int sheetNum) {
		CellRangeAddress titleRange = new CellRangeAddress(0, 0, 0, setInfo
				.getFieldNames().get(sheetNum).length);
		sheets[sheetNum].addMergedRegion(titleRange);
		HSSFRow titleRow = sheets[sheetNum].createRow(0);
		titleRow.setHeight((short) 800);
		HSSFCell titleCell = titleRow.createCell(0);
		titleCell.setCellStyle(titleStyle);
		titleCell.setCellValue(setInfo.getTitles()[sheetNum]);
	}

	/**
	 * 创建日期行(需合并单元格)
	 * @param setInfo
	 * @param sheets
	 * @param sheetNum
	 */
	private static void createTableDateRow(ExportSetInfo setInfo,
			HSSFSheet[] sheets, int sheetNum) {
		CellRangeAddress dateRange = new CellRangeAddress(1, 1, 0, setInfo
				.getFieldNames().get(sheetNum).length);
		sheets[sheetNum].addMergedRegion(dateRange);
		HSSFRow dateRow = sheets[sheetNum].createRow(1);
		dateRow.setHeight((short) 350);
		HSSFCell dateCell = dateRow.createCell(0);
		dateCell.setCellStyle(dateStyle);
		dateCell.setCellValue(new SimpleDateFormat("yyyy-MM-dd")
				.format(new Date()));
	}

	/**
	 * 创建表头行(需合并单元格)
	 * @param setInfo
	 * @param sheets
	 * @param sheetNum
	 */
	private static void creatTableHeadRow(ExportSetInfo setInfo,
			HSSFSheet[] sheets, int sheetNum) {
		// 表头
		HSSFRow headRow = sheets[sheetNum].createRow(2);
		headRow.setHeight((short) 350);
		// 序号列
		HSSFCell snCell = headRow.createCell(0);
		snCell.setCellStyle(headStyle);
		snCell.setCellValue("序号");
		// 列头名称
		for (int num = 1, len = setInfo.getHeadNames().get(sheetNum).length; num <= len; num++) {
			HSSFCell headCell = headRow.createCell(num);
			headCell.setCellStyle(headStyle);
			headCell.setCellValue(setInfo.getHeadNames().get(sheetNum)[num - 1]);
		}
	}
	
	/**
	 * 创建所有的Sheet
	 * @param num
	 * @param names
	 * @return
	 */
	private static HSSFSheet[] getSheets(int num, String[] names) {
		HSSFSheet[] sheets = new HSSFSheet[num];
		HSSFSheet _sheet = null;
		boolean flag = false;
		for (int i = 0; i < num; i++) {
			try{
				_sheet = wb.getSheetAt(i);
			}catch(Exception e){
				sheets[i] = wb.createSheet(names[i]); //创建sheet
				flag = true;
			}
			if(!flag){			
				wb.setSheetName(i, names[i]);
				sheets[i] = _sheet;
			}
			flag = false;
		}
		
		return sheets;
	}

	/**
	 * 创建内容行的每一列(附加一列序号)
	 * @param contentRow
	 * @param num
	 * @return
	 */
	private static HSSFCell[] getCells(HSSFRow contentRow, int num) {
		HSSFCell[] cells = new HSSFCell[num + 1];

		for (int i = 0, len = cells.length; i < len; i++) {
			cells[i] = contentRow.createCell(i);
			cells[i].setCellStyle(contentStyle);
		}
		// 设置序号列值，因为出去标题行和日期行，所有-2
		cells[0].setCellValue(contentRow.getRowNum() - 2);

		return cells;
	}

	/**
	 * 初始化标题行样式
	 */
	private static void initTitleCellStyle() {
		titleStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		titleStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		titleStyle.setFont(titleFont);
		titleStyle.setFillBackgroundColor(HSSFColor.SKY_BLUE.index);
	}

	/**
	 * 初始化日期行样式
	 */
	private static void initDateCellStyle() {
		dateStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER_SELECTION);
		dateStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		dateStyle.setFont(dateFont);
		dateStyle.setFillBackgroundColor(HSSFColor.SKY_BLUE.index);
	}

	/**
	 * 初始化表头行样式
	 */
	private static void initHeadCellStyle() {
		headStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		headStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		headStyle.setFont(headFont);
		headStyle.setFillBackgroundColor(HSSFColor.YELLOW.index);
		headStyle.setBorderTop(HSSFCellStyle.BORDER_MEDIUM);
		headStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		headStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		headStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
		headStyle.setTopBorderColor(HSSFColor.BLUE.index);
		headStyle.setBottomBorderColor(HSSFColor.BLUE.index);
		headStyle.setLeftBorderColor(HSSFColor.BLUE.index);
		headStyle.setRightBorderColor(HSSFColor.BLUE.index);
	}

	/**
	 * 初始化内容行样式
	 */
	private static void initContentCellStyle() {
		contentStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		contentStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		contentStyle.setFont(contentFont);
		contentStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
		contentStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		contentStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		contentStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
		contentStyle.setTopBorderColor(HSSFColor.BLUE.index);
		contentStyle.setBottomBorderColor(HSSFColor.BLUE.index);
		contentStyle.setLeftBorderColor(HSSFColor.BLUE.index);
		contentStyle.setRightBorderColor(HSSFColor.BLUE.index);
		contentStyle.setWrapText(true); // 字段换行
	}

	/**
	 * 初始化标题行字体
	 */
	private static void initTitleFont() {
		titleFont.setFontName("华文楷体");
		titleFont.setFontHeightInPoints((short) 20);
		titleFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		titleFont.setCharSet(HSSFFont.DEFAULT_CHARSET);
		titleFont.setColor(HSSFColor.BLUE_GREY.index);
	}

	/**
	 * 初始化日期行字体
	 */
	private static void initDateFont() {
		dateFont.setFontName("隶书");
		dateFont.setFontHeightInPoints((short) 10);
		dateFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		dateFont.setCharSet(HSSFFont.DEFAULT_CHARSET);
		dateFont.setColor(HSSFColor.BLUE_GREY.index);
	}

	/**
	 * 初始化表头行字体
	 */
	private static void initHeadFont() {
		headFont.setFontName("宋体");
		headFont.setFontHeightInPoints((short) 10);
		headFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		headFont.setCharSet(HSSFFont.DEFAULT_CHARSET);
		headFont.setColor(HSSFColor.BLUE_GREY.index);
	}

	/**
	 * 初始化内容行字体
	 */
	private static void initContentFont() {
		contentFont.setFontName("宋体");
		contentFont.setFontHeightInPoints((short) 10);
		contentFont.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);
		contentFont.setCharSet(HSSFFont.DEFAULT_CHARSET);
		contentFont.setColor(HSSFColor.BLUE_GREY.index);
	}


	static class ExportSetInfo {
		@SuppressWarnings("unchecked")
		private LinkedHashMap<String, List> objsMap;

		private String[] titles;

		private List<String[]> headNames;

		private List<String[]> fieldNames;

		private OutputStream out;

		@SuppressWarnings("unchecked")
		public LinkedHashMap<String, List> getObjsMap() {
			return objsMap;
		}

		/**
		 * @param objMap
		 *            导出数据
		 * 
		 *            泛型 String : 代表sheet名称 List : 代表单个sheet里的所有行数据
		 */
		@SuppressWarnings("unchecked")
		public void setObjsMap(LinkedHashMap<String, List> objsMap) {
			this.objsMap = objsMap;
		}

		public List<String[]> getFieldNames() {
			return fieldNames;
		}

		/**
		 * @param clazz
		 *            对应每个sheet里的每行数据的对象的属性名称
		 */
		public void setFieldNames(List<String[]> fieldNames) {
			this.fieldNames = fieldNames;
		}

		public String[] getTitles() {
			return titles;
		}

		/**
		 * @param titles
		 *            对应每个sheet里的标题，即顶部大字
		 */
		public void setTitles(String[] titles) {
			this.titles = titles;
		}

		public List<String[]> getHeadNames() {
			return headNames;
		}

		/**
		 * @param headNames
		 *            对应每个页签的表头的每一列的名称
		 */
		public void setHeadNames(List<String[]> headNames) {
			this.headNames = headNames;
		}

		public OutputStream getOut() {
			return out;
		}

		/**
		 * @param out
		 *            Excel数据将输出到该输出流
		 */
		public void setOut(OutputStream out) {
			this.out = out;
		}
	}
}
