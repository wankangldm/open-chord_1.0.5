package de.uniba.wiai.lspi.util.node;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class NodeTxtLoader {
    private static String name = "node.txt";
    private static List<String>  nodeList=  null;


    public static List<String> getNodes() throws FileNotFoundException {

        if (nodeList == null) {
            nodeList = new ArrayList<String>();
            InputStream systemResourceAsStream = ClassLoader.getSystemResourceAsStream("data/" + name);
            if (systemResourceAsStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(systemResourceAsStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuffer sb = new StringBuffer();
                try {
                    String text = null;
                    while((text = bufferedReader.readLine()) != null){
                        nodeList.add(text);
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }else {
                throw new FileNotFoundException();
            }
        }
        return nodeList;
    }

    public static void main(String[] args) {
        try {
            List<String> nodes = getNodes();
            System.out.println("nodes = " + nodes);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}
