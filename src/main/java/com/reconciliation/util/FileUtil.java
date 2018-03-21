package com.reconciliation.util;


import com.reconciliation.pojo.Config;
import com.reconciliation.pojo.StatementAccount;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.util.*;

/**
 * 读取文件 工具
 *
 * @author Administrator
 *
 */
public class FileUtil {


	private final static Logger logger = LoggerFactory.getLogger(FileUtil.class);



	/**
	 * 对账单读取文件特定方法 以行为单位读取文件，常用于读面向行的格式化文件
	 * @param config
	 * 			配置信息
	 * @param parkId
	 *            停车场id
	 * @param folderName
	 * 			文件名称
	 * @param beginDate
	 *            开始查询的日期
	 * @param endDate
	 *            结束查询的日期
	 * @param cluodParkId
	 * 			  云平台停车场编号是否为null
	 * @return
	 */
	public Map<String, Object> readFileByLines(Config config,String parkId,String folderName, String beginDate, String endDate,String cluodParkId) {

		String initFileName = config.getFtp_filePath()+folderName+"//CCB_TCC_JYDZ_";
		String fileName = config.getFtp_filePath()+folderName+"//CCB_TCC_JYDZ_";
		Date date1 = DateUtils.format(beginDate); // 转换成date格式
		Date date2 = DateUtils.format(endDate); // 转换成date格式

		int days = DateUtils.differentDaysByMillisecond(date1, date2); // 间隔天数
		logger.info("读取停车场为【"+parkId+"】的对账单文件，共读取【"+(days+1)+"】天！");

		String tempParkId = null;
		String tempDate = null;
		if(StringUtils.isNotBlank(cluodParkId)){  //云平台编号为空 说明该停车场是云平台得，那么读取得FTP文件名与非云平台得不一样
			tempParkId = cluodParkId;
			tempDate = DateUtils.format(DateUtils.addOneDay(DateUtils.format(DateUtils.format(date1)),-1));
		}else{
			tempParkId = parkId;
			tempDate = DateUtils.formatYYYYMMDD(date1); // 转换成yyyyMMdd格式
		}

		fileName = fileName + tempDate + "_" + tempParkId + ".txt"; // 组装文件名称
		FileInputStream file = null;
		BufferedReader reader = null;
		List<StatementAccount> list = new ArrayList<StatementAccount>();
		Map<String, Object> oneParkAllInfo = new HashMap<String, Object>();
		try {
			for (int i = 0; i < days + 1; i++) {
				String fileName1 = initFileName;
				System.out.println(fileName);
				file = new FileInputStream(fileName);
				reader = new BufferedReader(new InputStreamReader(file, "UTF-8"));
				String tempString = null;

				int line = 1;
				while ((tempString = reader.readLine()) != null) {
					if (line == 2) {
						String[] tempArr = tempString.split("\\|");
						HashMap<String, Object> parkInfo = new HashMap<String, Object>();
						parkInfo.put("parkId", tempArr[0]); // 停车场编号
						parkInfo.put("copeNum", tempArr[1]); // 应付笔数
						parkInfo.put("copeMoney", tempArr[2]); // 应付金额
						parkInfo.put("outNum", tempArr[3]); // 实付笔数
						parkInfo.put("outMoney", tempArr[4]); // 实付金额
						parkInfo.put("discountsNum", tempArr[5]); // 优惠笔数
						parkInfo.put("discountsMoney", tempArr[6]); // 优惠金额
						parkInfo.put("cashNum", tempArr[7]); // 现金笔数
						parkInfo.put("cashMoney", tempArr[8]); // 现金金额
						oneParkAllInfo.put("parkInfo_" + i, parkInfo);
					}
					boolean mark = false;
					if (line > 3) {
						if (!tempString.equals("*")) {
							StatementAccount statementAccount = new StatementAccount();
							String[] tempArr = tempString.split("\\|");
							if(tempArr[0]==null||"".equals(tempArr[0])||tempArr[0].equals("空")||tempArr[0].equals("null")){
								statementAccount.setpRecordId(0);
							}else{
								statementAccount.setpRecordId(Integer.parseInt(tempArr[0]));
							}
							if(tempArr[1]==null||"".equals(tempArr[1])||tempArr[1].equals("空")||tempArr[1].equals("null")){
								statementAccount.setParkId(0);
							}else{
								statementAccount.setParkId(Integer.parseInt(tempArr[1]));
							}
							statementAccount.setPlateNum(tempArr[2]);
							if(tempArr[3]==null||"".equals(tempArr[3])||tempArr[3].equals("空")||tempArr[3].equals("null")){
								statementAccount.setOriginalAmount(0.0);
								mark = true;
							}else{
								statementAccount.setOriginalAmount(Double.parseDouble(tempArr[3]));
								mark = false;
							}
							if(tempArr[4]==null||"".equals(tempArr[4])||tempArr[4].equals("空")||tempArr[4].equals("null")){
								statementAccount.setRecordAmount(0.0);
							}else{
								statementAccount.setRecordAmount(Double.parseDouble(tempArr[4]));
							}
							if(tempArr[5]==null||"".equals(tempArr[5])||tempArr[5].equals("空")||tempArr[5].equals("null")){
								statementAccount.setCouponamount(0.0);
							}else {
								statementAccount.setCouponamount(Double.parseDouble(tempArr[5]));
							}
							statementAccount.setTradeDate(tempArr[6]);
							statementAccount.setTradeTime(tempArr[7]);
							statementAccount.setStatus(tempArr[8]);
							statementAccount.setExplain(tempArr[9]);
							if(!mark){
								list.add(statementAccount);
							}
						}
					}
					line++;
				}
				if(StringUtils.isNotBlank(cluodParkId)){  //云平台编号为空 说明该停车场是云平台得，那么读取得FTP文件名与非云平台得不一样
					fileName = fileName1 + DateUtils.format(DateUtils.addOneDay(date1, i + 1)) + "_" + tempParkId + ".txt"; // 组装文件名称
				}else{
					fileName = fileName1 + DateUtils.formatYYYYMMDD(DateUtils.addOneDay(date1, i + 1)) + "_" + tempParkId + ".txt"; // 组装文件名称
				}

			}
			oneParkAllInfo.put("list", list);
			reader.close();
		} catch (IOException e) {
			logger.info(e.getMessage());
			return null;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
					logger.info(e1.getMessage());
					return null;
				}
			}
		}
		return oneParkAllInfo;
	}

	/**  场景: 2018-03-01 至 2018-03-20 得数据需要读取，而一些文件不存在从而报错，不存在得文件忽略跳过继续读取下面得文件
	 * 对账单读取文件特定方法 以行为单位读取文件，常用于读面向行的格式化文件
	 * @param config
	 * 			配置信息
	 * @param parkId
	 *            停车场id
	 * @param folderName
	 * 			文件名称
	 * @param beginDate
	 *            开始查询的日期
	 * @param endDate
	 *            结束查询的日期
	 * @param cluodParkId
	 * 			  云平台停车场编号是否为null
	 * @return
	 */
	public Map<String, Object> readFileByLines1(Config config,String parkId,String folderName, String beginDate, String endDate,String cluodParkId) {

		String initFileName = config.getFtp_filePath()+folderName+"//CCB_TCC_JYDZ_";
		String fileName = config.getFtp_filePath()+folderName+"//CCB_TCC_JYDZ_";
		Date date1 = DateUtils.format(beginDate); // 转换成date格式
		Date date2 = DateUtils.format(endDate); // 转换成date格式

		int days = DateUtils.differentDaysByMillisecond(date1, date2); // 间隔天数
		logger.info("读取停车场为【"+parkId+"】的对账单文件，共读取【"+(days+1)+"】天！");

		String tempParkId = null;
		String tempDate = null;
		if(StringUtils.isNotBlank(cluodParkId)){  //云平台编号为空 说明该停车场是云平台得，那么读取得FTP文件名与非云平台得不一样
			tempParkId = cluodParkId;
			tempDate = DateUtils.format(DateUtils.addOneDay(DateUtils.format(DateUtils.format(date1)),-1));
		}else{
			tempParkId = parkId;
			tempDate = DateUtils.formatYYYYMMDD(date1); // 转换成yyyyMMdd格式
		}

		fileName = fileName + tempDate + "_" + tempParkId + ".txt"; // 组装文件名称
		FileInputStream file = null;
		BufferedReader reader = null;
		List<StatementAccount> list = new ArrayList<StatementAccount>();
		Map<String, Object> oneParkAllInfo = new HashMap<String, Object>();
		for (int i = 0; i < days + 1; i++) {
			String fileName1 = initFileName;
		try {

				file = new FileInputStream(fileName);
				reader = new BufferedReader(new InputStreamReader(file, "UTF-8"));
				String tempString = null;

				int line = 1;
				while ((tempString = reader.readLine()) != null) {
				if (line == 2) {
					String[] tempArr = tempString.split("\\|");
					HashMap<String, Object> parkInfo = new HashMap<String, Object>();
					parkInfo.put("parkId", tempArr[0]); // 停车场编号
					parkInfo.put("copeNum", tempArr[1]); // 应付笔数
					parkInfo.put("copeMoney", tempArr[2]); // 应付金额
					parkInfo.put("outNum", tempArr[3]); // 实付笔数
					parkInfo.put("outMoney", tempArr[4]); // 实付金额
					parkInfo.put("discountsNum", tempArr[5]); // 优惠笔数
					parkInfo.put("discountsMoney", tempArr[6]); // 优惠金额
					parkInfo.put("cashNum", tempArr[7]); // 现金笔数
					parkInfo.put("cashMoney", tempArr[8]); // 现金金额
					oneParkAllInfo.put("parkInfo_" + i, parkInfo);
				}
				boolean mark = false;
				if (line > 3) {
					if (!tempString.equals("*")) {
						StatementAccount statementAccount = new StatementAccount();
						String[] tempArr = tempString.split("\\|");
						if(tempArr[0]==null||"".equals(tempArr[0])||tempArr[0].equals("空")||tempArr[0].equals("null")){
							statementAccount.setpRecordId(0);
						}else{
							statementAccount.setpRecordId(Integer.parseInt(tempArr[0]));
						}
						if(tempArr[1]==null||"".equals(tempArr[1])||tempArr[1].equals("空")||tempArr[1].equals("null")){
							statementAccount.setParkId(0);
						}else{
							statementAccount.setParkId(Integer.parseInt(tempArr[1]));
						}
						statementAccount.setPlateNum(tempArr[2]);
						if(tempArr[3]==null||"".equals(tempArr[3])||tempArr[3].equals("空")||tempArr[3].equals("null")){
							statementAccount.setOriginalAmount(0.0);
							mark = true;
						}else{
							statementAccount.setOriginalAmount(Double.parseDouble(tempArr[3]));
							mark = false;
						}
						if(tempArr[4]==null||"".equals(tempArr[4])||tempArr[4].equals("空")||tempArr[4].equals("null")){
							statementAccount.setRecordAmount(0.0);
						}else{
							statementAccount.setRecordAmount(Double.parseDouble(tempArr[4]));
						}
						if(tempArr[5]==null||"".equals(tempArr[5])||tempArr[5].equals("空")||tempArr[5].equals("null")){
							statementAccount.setCouponamount(0.0);
						}else {
							statementAccount.setCouponamount(Double.parseDouble(tempArr[5]));
						}
						statementAccount.setTradeDate(tempArr[6]);
						statementAccount.setTradeTime(tempArr[7]);
						statementAccount.setStatus(tempArr[8]);
						statementAccount.setExplain(tempArr[9]);
						if(!mark){
							list.add(statementAccount);
						}
					}
				}
				line++;
			}
				if(StringUtils.isNotBlank(cluodParkId)){  //云平台编号为空 说明该停车场是云平台得，那么读取得FTP文件名与非云平台得不一样
					fileName = fileName1 + DateUtils.format(DateUtils.addOneDay(date1, i + 1)) + "_" + tempParkId + ".txt"; // 组装文件名称
				}else{
					fileName = fileName1 + DateUtils.formatYYYYMMDD(DateUtils.addOneDay(date1, i + 1)) + "_" + tempParkId + ".txt"; // 组装文件名称
				}
			oneParkAllInfo.put("list", list);
			reader.close();
		} catch (IOException e) {
			logger.info(e.getMessage()+"第一");
			if(StringUtils.isNotBlank(cluodParkId)){  //云平台编号为空 说明该停车场是云平台得，那么读取得FTP文件名与非云平台得不一样
				fileName = fileName1 + DateUtils.format(DateUtils.addOneDay(date1, i + 1)) + "_" + tempParkId + ".txt"; // 组装文件名称
			}else{
				fileName = fileName1 + DateUtils.formatYYYYMMDD(DateUtils.addOneDay(date1, i + 1)) + "_" + tempParkId + ".txt"; // 组装文件名称
			}
			continue;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
					if(StringUtils.isNotBlank(cluodParkId)){  //云平台编号为空 说明该停车场是云平台得，那么读取得FTP文件名与非云平台得不一样
						fileName = fileName1 + DateUtils.format(DateUtils.addOneDay(date1, i + 1)) + "_" + tempParkId + ".txt"; // 组装文件名称
					}else{
						fileName = fileName1 + DateUtils.formatYYYYMMDD(DateUtils.addOneDay(date1, i + 1)) + "_" + tempParkId + ".txt"; // 组装文件名称
					}
					logger.info(e1.getMessage()+"第er");
					continue;
				}
			}
		}
		}
		return oneParkAllInfo;
	}



	/**
	 * 财务对账金额  读取
	 * @param parkId
	 * @param folderName
	 * @param beginDate
	 * @param endDate
	 * @param cluodParkId
	 * @return
	 */
	public Map<String, Object> finance(Config config,String parkId,String folderName, String beginDate, String endDate,String cluodParkId) {
		String initFileName = config.getFtp_filePath()+folderName+"//CCB_TCC_JYDZ_";
		String fileName = config.getFtp_filePath()+folderName+"//CCB_TCC_JYDZ_";
		Date date1 = DateUtils.format(beginDate); // 转换成date格式
		Date date2 = DateUtils.format(endDate); // 转换成date格式

		int days = DateUtils.differentDaysByMillisecond(date1, date2); // 间隔天数
		logger.info("读取停车场为【"+parkId+"】的对账单文件，共读取【"+(days+1)+"】天！");

		String tempParkId = null;
		String tempDate = null;
		if(StringUtils.isNotBlank(cluodParkId)){  //云平台编号为空 说明该停车场是云平台得，那么读取得FTP文件名与非云平台得不一样
			tempParkId = cluodParkId;
			tempDate = DateUtils.format(date1);
		}else{
			tempParkId = parkId;
			tempDate = DateUtils.formatYYYYMMDD(date1); // 转换成yyyyMMdd格式
		}

		fileName = fileName + tempDate + "_" + tempParkId + ".txt"; // 组装文件名称
		FileInputStream file = null;
		BufferedReader reader = null;
		List<StatementAccount> list = new ArrayList<StatementAccount>();
		Map<String, Object> oneParkAllInfo = new HashMap<String, Object>();
		try {
			for (int i = 0; i < days + 1; i++) {
				String fileName1 = initFileName;
				System.out.println(fileName);
				file = new FileInputStream(fileName);
				reader = new BufferedReader(new InputStreamReader(file, "UTF-8"));
				String tempString = null;

				int line = 1;
				while ((tempString = reader.readLine()) != null) {
					if (line > 3) {
						if (!tempString.equals("*")) {
							StatementAccount statementAccount = new StatementAccount();
							String[] tempArr = tempString.split("\\|");
							statementAccount.setPlateNum(tempArr[2]);
							statementAccount.setOriginalAmount(Double.parseDouble(tempArr[3]));
							statementAccount.setRecordAmount(Double.parseDouble(tempArr[4]));
							statementAccount.setCouponamount(Double.parseDouble(tempArr[5]));
							statementAccount.setTradeDate(tempArr[6]);
							statementAccount.setTradeTime(tempArr[7]);
							statementAccount.setStatus(tempArr[8]);
							statementAccount.setExplain(tempArr[9]);
							list.add(statementAccount);
						}
					}
					line++;
				}
				if(StringUtils.isNotBlank(cluodParkId)){  //云平台编号为空 说明该停车场是云平台得，那么读取得FTP文件名与非云平台得不一样
					fileName = fileName1 + DateUtils.format(DateUtils.addOneDay(date1, i + 1)) + "_" + tempParkId + ".txt"; // 组装文件名称
				}else{
					fileName = fileName1 + DateUtils.formatYYYYMMDD(DateUtils.addOneDay(date1, i + 1)) + "_" + tempParkId + ".txt"; // 组装文件名称
				}

			}
			oneParkAllInfo.put("list", list);
			reader.close();
		} catch (IOException e) {
			logger.info(e.getMessage());
			return null;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
					logger.info(e1.getMessage());
					return null;
				}
			}
		}
		return oneParkAllInfo;
	}


	/**
	 * 获取招商对账文件数据
	 * @return
	 */
	public List<Map<String,Object>> getCmbData(Config config){
		String zs_filePath_prefix = config.getZs_filePath_prefix();
		String zs_filePath_suffix = config.getZs_filePath_suffix();
		String zs_filePath = config.getZs_filePath();
		String strDate = DateUtils.formatYYYYMMDD(DateUtils.addOneDay(new Date(),-1));
		//String strDate = DateUtils.formatYYYYMMDD(DateUtils.format("2017-12-25"));
		String fileName = zs_filePath + zs_filePath_prefix + strDate + zs_filePath_suffix;
		FileInputStream file = null;
		BufferedReader reader = null;
		List<Map<String,Object>> cmdList = new ArrayList<Map<String,Object>>();
		try {
			file = new FileInputStream(fileName);
			reader = new BufferedReader(new InputStreamReader(file, "UTF-8"));
			String tempString = null;
			int line = 1;
			while ((tempString = reader.readLine()) != null) {
				if(line>2){
					String[] tempArr = tempString.split("\\|");
					if(tempArr[8].equals("Y")){
						Map<String,Object> tempMap = new HashMap<String,Object>();
						tempMap.put("cRecordId","");
						tempMap.put("pRecordId","");
						tempMap.put("UnionpayRightsId","");
						tempMap.put("parkId","");
						tempMap.put("createTime",new Date());
						tempMap.put("payType",6);
						String billDate = tempArr[0];
						if(billDate.length()==16){
							billDate = billDate.substring(0,billDate.length()-2);
						}else if(billDate.length()==15){
							billDate = billDate.substring(0,billDate.length()-1);
						}
						tempMap.put("billDate",DateUtils.convertDateFormat(tempArr[0]));
						tempMap.put("cmbId",tempArr[0]);
						tempMap.put("money",Double.parseDouble(tempArr[6])/100);
						cmdList.add(tempMap);
					}
				}
				line++;
			}
			reader.close();
		} catch (IOException e) {
			logger.info(e.getMessage());
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
					logger.info(e1.getMessage());
				}
			}
		}
		return cmdList;
	}



	/**
	 *
	 * @param fileName
	 */
	public static void readFileByLines(String fileName) {
		FileInputStream file = null;
		BufferedReader reader = null;
		try {
			file = new FileInputStream(fileName);
			reader = new BufferedReader(new InputStreamReader(file, "GBK"));
			String tempString = null;
			int line = 1;
			List<Map<String,String>> rtnList = new ArrayList<Map<String,String>>();
			while ((tempString = reader.readLine()) != null) {
				String[] strArr = tempString.split(" ");
				Map<String,String> temp = new HashMap<String,String>();
				System.out.println("line " + line + ": " + tempString);
				line++;
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
	}

	/**
	 * 删除文件
	 * @param path
	 * @param filename
	 */
	public static void delFile(String path,String filename){
        File file=new File(path+"\\"+filename);
        if(file.exists()&&file.isFile())
            file.delete();
    }


	/**
	 * 数据写到txt文件
	 * @param paramList  数据
	 * @param parkid     停车场编号
	 * @param parkNameAbbreviation  文件名称
	 * @throws Exception
	 */
	public void writeToFile(Config config,List<Map<String,Object>> paramList,String parkid,String parkNameAbbreviation,String date) throws  Exception{
		/*停车场编号|应付笔数|应付金额|实付笔数|实付金额|优惠笔数|优惠金额|现金笔数|现金金额|
		1104|411|5328.00|256|3259.00|174|2069.00|1|15.00|
		流水号|停车场编号|车牌号|应付金额|实付金额|优惠金额|交易日期|交易时间|记账状态|状态说明|*/
		String fileName = /*"D:/YsFile/"*/config.getYs_path() + parkNameAbbreviation + "/CCB_TCC_JYDZ_" +  /*DateUtils.formatYYYYMMDD(DateUtils.addOneDay(new Date(), -1))*/date + "_" + parkid + ".txt";
		System.out.println("-------------------------------------");
		System.out.println(fileName);
		System.out.println("-------------------------------------");
		File file = new File(fileName);
		createDir(fileName);
		file.createNewFile(); // 创建新文件
		StringBuffer sb = new StringBuffer();
		sb.append("停车场编号|应付笔数|应付金额|实付笔数|实付金额|优惠笔数|优惠金额|现金笔数|现金金额|").append("\r\n");
		sb.append("0|0|0|0|0|0|0|0|0|").append("\r\n");
		sb.append("流水号|停车场编号|车牌号|应付金额|实付金额|优惠金额|交易日期|交易时间|记账状态|状态说明|订单号|订单创建时间|商户号").append("\r\n");
		for(int j = 0;j<paramList.size();j++) {
			Map<String, Object> temp = paramList.get(j);
			sb.append(temp.get("pid")).append("|").append(temp.get("parkid")).append("|").append(temp.get("plateNo")).append("|").append(temp.get("payCharge")).append("|");
			sb.append(temp.get("realCharge")).append("|").append(temp.get("discount")).append("|").append(temp.get("chargeTime")).append("|").append("null").append("|").append(temp.get("state")).append("|");
			if("".equals(temp.get("chargeKind"))||temp.get("chargeKind")==null){
				sb.append("空").append("|");
			}else{
				sb.append(temp.get("chargeKind")).append("|");
			}
			sb.append(temp.get("orderid")).append("|").append(temp.get("paytime")).append("|").append(temp.get("mid")).append("\r\n");
		}
		sb.append("*");
		if (file.exists()) {
			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			out.write(sb.toString());
			out.flush();
			out.close();
		}
	}

	/**   银商
	 * 对账单读取文件特定方法 以行为单位读取文件，常用于读面向行的格式化文件
	 * @param config
	 * 			配置信息
	 * @param parkId
	 *            停车场id
	 * @param folderName
	 * 			文件名称
	 * @param beginDate
	 *            开始查询的日期
	 * @param endDate
	 *            结束查询的日期
	 * @param cluodParkId
	 * 			  云平台停车场编号是否为null
	 * @return
	 */
	public Map<String, Object> readFileByLines_ys(Config config,String parkId,String folderName, String beginDate, String endDate,String cluodParkId) {

		String initFileName = config.getFtp_filePath()+folderName+"//CCB_TCC_JYDZ_";
		String fileName = config.getFtp_filePath()+folderName+"//CCB_TCC_JYDZ_";
		Date date1 = DateUtils.format(beginDate); // 转换成date格式
		Date date2 = DateUtils.format(endDate); // 转换成date格式

		int days = DateUtils.differentDaysByMillisecond(date1, date2); // 间隔天数
		logger.info("读取停车场为【"+parkId+"】的对账单文件，共读取【"+(days+1)+"】天！");

		String tempParkId = null;
		String tempDate = null;
		if(StringUtils.isNotBlank(cluodParkId)){  //云平台编号为空 说明该停车场是云平台得，那么读取得FTP文件名与非云平台得不一样
			tempParkId = cluodParkId;
			tempDate = DateUtils.format(DateUtils.addOneDay(DateUtils.format(DateUtils.format(date1)),-1));
		}else{
			tempParkId = parkId;
			tempDate = DateUtils.formatYYYYMMDD(date1); // 转换成yyyyMMdd格式
		}

		fileName = fileName + tempDate + "_" + tempParkId + ".txt"; // 组装文件名称
		FileInputStream file = null;
		BufferedReader reader = null;
		List<StatementAccount> list = new ArrayList<StatementAccount>();
		Map<String, Object> oneParkAllInfo = new HashMap<String, Object>();
		try {
			for (int i = 0; i < days + 1; i++) {
				String fileName1 = initFileName;
				System.out.println(fileName);
				file = new FileInputStream(fileName);
				reader = new BufferedReader(new InputStreamReader(file, "UTF-8"));
				String tempString = null;

				int line = 1;
				while ((tempString = reader.readLine()) != null) {
					if (line == 2) {
						String[] tempArr = tempString.split("\\|");
						HashMap<String, Object> parkInfo = new HashMap<String, Object>();
						parkInfo.put("parkId", tempArr[0]); // 停车场编号
						parkInfo.put("copeNum", tempArr[1]); // 应付笔数
						parkInfo.put("copeMoney", tempArr[2]); // 应付金额
						parkInfo.put("outNum", tempArr[3]); // 实付笔数
						parkInfo.put("outMoney", tempArr[4]); // 实付金额
						parkInfo.put("discountsNum", tempArr[5]); // 优惠笔数
						parkInfo.put("discountsMoney", tempArr[6]); // 优惠金额
						parkInfo.put("cashNum", tempArr[7]); // 现金笔数
						parkInfo.put("cashMoney", tempArr[8]); // 现金金额
						oneParkAllInfo.put("parkInfo_" + i, parkInfo);
					}
					boolean mark = false;
					if (line > 3) {
						if (!tempString.equals("*")) {
							StatementAccount statementAccount = new StatementAccount();
							String[] tempArr = tempString.split("\\|");
							if(tempArr[0]==null||"".equals(tempArr[0])||tempArr[0].equals("空")||tempArr[0].equals("null")){
								statementAccount.setpRecordId(0);
							}else{
								statementAccount.setpRecordId(Integer.parseInt(tempArr[0]));
							}
							if(tempArr[1]==null||"".equals(tempArr[1])||tempArr[1].equals("空")||tempArr[1].equals("null")){
								statementAccount.setParkId(0);
							}else{
								statementAccount.setParkId(Integer.parseInt(tempArr[1]));
							}
							statementAccount.setPlateNum(tempArr[2]);
							if(tempArr[3]==null||"".equals(tempArr[3])||tempArr[3].equals("空")||tempArr[3].equals("null")){
								statementAccount.setOriginalAmount(0.0);
								mark = true;
							}else{
								statementAccount.setOriginalAmount(Double.parseDouble(tempArr[3]));
								mark = false;
							}
							if(tempArr[4]==null||"".equals(tempArr[4])||tempArr[4].equals("空")||tempArr[4].equals("null")){
								statementAccount.setRecordAmount(0.0);
							}else{
								statementAccount.setRecordAmount(Double.parseDouble(tempArr[4]));
							}
							if(tempArr[5]==null||"".equals(tempArr[5])||tempArr[5].equals("空")||tempArr[5].equals("null")){
								statementAccount.setCouponamount(0.0);
							}else {
								statementAccount.setCouponamount(Double.parseDouble(tempArr[5]));
							}
							statementAccount.setTradeDate(tempArr[6]);
							statementAccount.setTradeTime(tempArr[7]);
							statementAccount.setStatus(tempArr[8]);
							statementAccount.setExplain(tempArr[9]);
							statementAccount.setOrderid(tempArr[10]);
							statementAccount.setPaytime(tempArr[11]);
							statementAccount.setMid(tempArr[12]);
							if(!mark){
								list.add(statementAccount);
							}
						}
					}
					line++;
				}
				if(StringUtils.isNotBlank(cluodParkId)){  //云平台编号为空 说明该停车场是云平台得，那么读取得FTP文件名与非云平台得不一样
					fileName = fileName1 + DateUtils.format(DateUtils.addOneDay(date1, i + 1)) + "_" + tempParkId + ".txt"; // 组装文件名称
				}else{
					fileName = fileName1 + DateUtils.formatYYYYMMDD(DateUtils.addOneDay(date1, i + 1)) + "_" + tempParkId + ".txt"; // 组装文件名称
				}

			}
			oneParkAllInfo.put("list", list);
			reader.close();
		} catch (IOException e) {
			logger.info(e.getMessage());
			return null;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
					logger.info(e1.getMessage());
					return null;
				}
			}
		}
		return oneParkAllInfo;
	}



	/**
	 *  判断文件夹是否
	 * @param destDirName  路劲名称
	 * @return
	 */
	public static Boolean createDir(String destDirName) {
		File dir = new File(destDirName);
		if(!dir.getParentFile().exists()){				//判断有没有父路径，就是判断文件整个路径是否存在
			return dir.getParentFile().mkdirs();		//不存在就全部创建
		}
		return false;
	}

}
