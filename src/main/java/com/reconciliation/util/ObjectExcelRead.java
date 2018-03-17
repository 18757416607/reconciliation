package com.reconciliation.util;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * 从EXCEL导入到数据库
 * 
 * @version
 */
public class ObjectExcelRead {

	/**
	 * @param filepath
	 *            //文件路径
	 * @param filename
	 *            //文件名
	 * @param startrow
	 *            //开始行号
	 * @param startcol
	 *            //开始列号
	 * @param sheetnum
	 *            //sheet
	 * @return list
	 */
	public static List<Object> readExcel(String filepath, String filename, int startrow, int startcol, int sheetnum) {
		List<Object> varList = new ArrayList<Object>();

		try {
			File target = new File(filepath, filename);
			FileInputStream fi = new FileInputStream(target);
			Workbook wb = WorkbookFactory.create(fi);
			Sheet sheet = wb.getSheetAt(sheetnum); // sheet 从0开始
			int rowNum = sheet.getLastRowNum() + 1; // 取得最后一行的行号

			for (int i = startrow; i < rowNum; i++) { // 行循环开始

				Map<String, Object> varpd = new HashMap<String, Object>();
				Row row = sheet.getRow(i); // 行
				int cellNum = row.getLastCellNum(); // 每行的最后一个单元格位置

				for (int j = startcol; j < cellNum; j++) { // 列循环开始

					Cell cell = row.getCell(Short.parseShort(j + ""));
					String cellValue = null;
					if (null != cell) {
						switch (cell.getCellType()) { // 判断excel单元格内容的格式，并对其进行转换，以便插入数据库
						case 0:
							cellValue = String.valueOf(cell.getNumericCellValue());
							break;
						case 1:
							cellValue = cell.getStringCellValue();
							break;
						case 2:
							cellValue = cell.getNumericCellValue() + "";
							// cellValue =
							// String.valueOf(cell.getDateCellValue());
							break;
						case 3:
							cellValue = "";
							break;
						case 4:
							cellValue = String.valueOf(cell.getBooleanCellValue());
							break;
						case 5:
							cellValue = String.valueOf(cell.getErrorCellValue());
							break;
						}
					} else {
						cellValue = "";
					}

					varpd.put("var" + j, cellValue);

				}
				varList.add(varpd);
			}

		} catch (Exception e) {
			System.out.println(e);
		}

		return varList;
	}

	/**
	 * 下载停车场对账单文件
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	public static void getParkReconciliationExcel(HttpServletRequest request, HttpServletResponse response, List<Map<String,Object>> moenyList) throws  Exception{
		String serpath = PathUtil.getClasspath() + "uploadFiles/停车场对账单.xls";
		FileInputStream fs= new FileInputStream(serpath);  //获取d://test.xls
		POIFSFileSystem ps= new POIFSFileSystem(fs);  //使用POI提供的方法得到excel的信息
		HSSFWorkbook wb=new HSSFWorkbook(ps);
		HSSFSheet sheet=wb.getSheetAt(0);  //获取到工作表，因为一个excel可能有多个工作表
		sheet.setColumnWidth(0, 8766);  //设置列宽
		sheet.setColumnWidth(1, 3766);
		sheet.setColumnWidth(2, 3766);
		sheet.setColumnWidth(3, 3766);

		//给行定制样式
		HSSFCellStyle style = wb.createCellStyle();
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		HSSFFont font = wb.createFont();
		style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		font.setFontName("宋体");
		font.setFontHeightInPoints((short) 18);// 设置字体大小
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);//粗体显示
		style.setFont(font);


		//合并单元格
		CellRangeAddress cra=new CellRangeAddress(0, 0, 0, 3);
		sheet.addMergedRegion(cra);
		HSSFRow row1=sheet.getRow(0);
		row1=sheet.createRow((short)(0)); //在现有行号后追加数据
		row1.setHeightInPoints((short) 39);  //设置行高
		HSSFCell cell = row1.createCell((short) 0);
		cell.setCellValue(DateUtils.format(DateUtils.addOneDay(new Date(),-1))+"  停车场对账单");
		//设置单元格居中
		HSSFCellStyle cellStyle = wb.createCellStyle();  //新建单元格样式
		cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 居中
		cellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);//垂直
		cell.setCellStyle(cellStyle);


		//设置列头
		HSSFRow row2=sheet.getRow(1);  //获取第一行（excel中的行默认从0开始，所以这就是为什么，一个excel必须有字段列头），即，字段列头，便于赋值
		row2=sheet.createRow((short)(sheet.getLastRowNum()+1)); //在现有行号后追加数据
		row2.createCell((short) 0).setCellValue("停车场名称"); //设置第一个（从0开始）单元格的数据
		row2.createCell((short) 1).setCellValue("应付金额"); //设置第二个（从0开始）单元格的数据
		row2.createCell((short) 2).setCellValue("实付金额"); //设置第二个（从0开始）单元格的数据
		row2.createCell((short) 3).setCellValue("优惠金额"); //设置第二个（从0开始）单元格的数据
		row2.setRowStyle(style);


		for (int i= 0;i<moenyList.size();i++){
			Map<String,Object> moneyInfo = moenyList.get(i);
			//普通往单元格写内容
			HSSFRow row=sheet.getRow(2);  //获取第一行（excel中的行默认从0开始，所以这就是为什么，一个excel必须有字段列头），即，字段列头，便于赋值
			row=sheet.createRow((short)(sheet.getLastRowNum()+1)); //在现有行号后追加数据
			row.createCell((short) 0).setCellValue(moneyInfo.get("parkName").toString()); //设置第一个（从0开始）单元格的数据
			row.createCell((short) 1).setCellValue(moneyInfo.get("payCharge").toString()); //设置第二个（从0开始）单元格的数据
			row.createCell((short) 2).setCellValue(moneyInfo.get("realCharge").toString()); //设置第二个（从0开始）单元格的数据
			row.createCell((short) 3).setCellValue(moneyInfo.get("discount").toString()); //设置第二个（从0开始）单元格的数据
		}

		response.reset();
		String fileName = "attachment;filename=\"" + DateUtils.format(DateUtils.addOneDay(new Date(),-1)) + "停车场对账单.xls" + "\"";
		response.setHeader("Content-disposition", new String(fileName.getBytes("UTF-8"), "ISO-8859-1"));
		response.setContentType("application/octet-stream;charset=UTF-8");
		OutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
		wb.write(outputStream);
		outputStream.flush();
		outputStream.close();
		response.flushBuffer();

	}


}
