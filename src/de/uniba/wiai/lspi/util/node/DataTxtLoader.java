package de.uniba.wiai.lspi.util.node;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataTxtLoader {

    private static String fileName = "data.txt";
    private static Map<String,String> dataMap=  null;


    public static Map<String,String> getDataMap() throws FileNotFoundException {

        if (dataMap == null) {
            dataMap = new HashMap<String, String>();
            InputStream systemResourceAsStream = ClassLoader.getSystemResourceAsStream("data/" + fileName);
            if (systemResourceAsStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(systemResourceAsStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                try {
                    String text = null;
                    while((text = bufferedReader.readLine()) != null &&!"".equals(text)){
                        String idStr = text.trim().substring(0, text.trim().indexOf(","));
                        String logcStr = text.trim().substring(text.trim().indexOf(",")+1,text.trim().length());
                        String[] split0 = idStr.trim().split("=");
                        String[] split1 = logcStr.trim().split("=");
                        if (split0 != null && split0.length ==2&&split1 != null && split1.length ==2) {
                            dataMap.put(split0[1],split1[1]);
                        }
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }else {
                throw new FileNotFoundException();
            }
        }
        return dataMap;
    }
    public static void main(String[] args) {
        try {
            Map<String, String> dataMap = getDataMap();
            System.out.println("dataMap = " + dataMap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

}

