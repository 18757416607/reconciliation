package com.reconciliation.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/3/5.
 */
public class FileUtils {

    public static void main(String[] args) throws IOException {
        List<Map<String,Object>> list = readeGaoDeMapParkInfo("D:/json.txt");
        for(int i = 0;i<list.size();i++){

            System.out.println((i+1)+"="+list.get(i));
        }
    }

    /**
     * 读取高德地图下载的停车场txt文件
     * @param filePath 读取文件的路劲
     * @return
     */
    public static List<Map<String,Object>> readeGaoDeMapParkInfo(String filePath){
        List<Map<String,Object>> mapList = new ArrayList<Map<String,Object>>();
        FileInputStream file = null;
        BufferedReader reader = null;
        try {
            file = new FileInputStream(filePath);
            reader = new BufferedReader(new InputStreamReader(file, "GBK"));
            String tempString = null;
            List<Map<String,String>> rtnList = new ArrayList<Map<String,String>>();
            while ((tempString = reader.readLine()) != null) {
                tempString = tempString.replace("'","\"");
                Map<String,Object> temp = JsonUtil.jsonToMap(tempString);
                if(temp.get("address").toString().equals("[]")){
                    temp.put("address","");
                }
                String location = temp.get("location").toString().trim();
                StringBuffer newlocation = new StringBuffer();
                newlocation.append("POINT(").append(location.substring(location.indexOf(",")+2,location.length()-1).trim());
                newlocation.append(",").append(location.substring(1,location.indexOf(","))).append(")");
                temp.put("address_loc", newlocation.toString().replace(","," "));
                temp.put("longitude",location.substring(location.indexOf(",")+2,location.length()-1).trim());
                temp.put("latitude",location.substring(1,location.indexOf(",")));
                mapList.add(temp);
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
        return mapList;
    }




}
