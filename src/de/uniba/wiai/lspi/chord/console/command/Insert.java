/***************************************************************************
 *                                                                         *
 *                               Insert.java                               *
 *                            -------------------                          *
 *   date                 : 15.09.2004, 10:14                              *
 *   copyright            : (C) 2004-2008 Distributed and                  *
 *                              Mobile Systems Group                       *
 *                              Lehrstuhl fuer Praktische Informatik       *
 *                              Universitaet Bamberg                       *
 *                              http://www.uni-bamberg.de/pi/              *
 *   email                : sven.kaffille@uni-bamberg.de                   *
 *                          karsten.loesing@uni-bamberg.de                 *
 *                                                                         *
 *                                                                         *
 ***************************************************************************/

/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   A copy of the license can be found in the license.txt file supplied   *
 *   with this software or at: http://www.gnu.org/copyleft/gpl.html        *
 *                                                                         *
 ***************************************************************************/
 
package de.uniba.wiai.lspi.chord.console.command;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.*;

import de.uniba.wiai.lspi.chord.com.Node;
import de.uniba.wiai.lspi.chord.com.local.ChordImplAccess;
import de.uniba.wiai.lspi.chord.com.local.Registry;
import de.uniba.wiai.lspi.chord.com.local.ThreadEndpoint;
import de.uniba.wiai.lspi.chord.console.command.entry.Key;
import de.uniba.wiai.lspi.chord.console.command.entry.Value;
import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.impl.HashFunction;
import de.uniba.wiai.lspi.util.console.ConsoleException;
import de.uniba.wiai.lspi.util.node.DataTxtLoader;

/**
 * Command to insert a value from a specified node into the local chord network.
 * 
 * To get a description of this command type <code>insert -help</code> into
 * the {@link de.uniba.wiai.lspi.chord.console.Main console}.
 * 
 * @author sven
 * @version 1.0.5
 */
public class Insert extends de.uniba.wiai.lspi.util.console.Command {

	/**
	 * Name of this command.
	 */
	public static final String COMMAND_NAME = "insert";

	/**
	 * The name of the parameter, that defines the name of the node, from that
	 * the value should be inserted.
	 */
	protected static final String NODE_PARAM = "node";

	/**
	 * The name of the parameter, that defines the key for the value to insert.
	 */
	protected static final String KEY_PARAM = "key";

	/**
	 * The name of the parameter, that defines the value, which is inserted into
	 * the distributed hash table.
	 */
	protected static final String VALUE_PARAM = "value";

	/**
	 * Creates a new instance of Insert
	 * 
	 * @param toCommand1
	 * @param out1
	 */
	public Insert(Object[] toCommand1, java.io.PrintStream out1) {
		super(toCommand1, out1);
	}

	public void exec() throws ConsoleException {
		String node = this.parameters.get(NODE_PARAM);
		String key = this.parameters.get(KEY_PARAM);
		String value = this.parameters.get(VALUE_PARAM);
		if ((node == null) || (node.length() == 0)) {

			insertKeyValueFromTxt();
			return;

		}



		if ((node == null) || (node.length() == 0)) {
			throw new ConsoleException("Not enough parameters! " + NODE_PARAM
					+ " is missing.");
		}
		if ((key == null) || (key.length() == 0)) {
			throw new ConsoleException("Not enough parameters! " + KEY_PARAM
					+ " is missing.");
		}
		if ((value == null) || (value.length() == 0)) {
			throw new ConsoleException("Not enough parameters! " + VALUE_PARAM
					+ " is missing.");
		}
		URL url = null; 
		try {
			url = new URL(URL.KNOWN_PROTOCOLS.get(URL.LOCAL_PROTOCOL) + "://" + node + "/");
		} catch (MalformedURLException e1) {
			throw new ConsoleException(e1.getMessage());
		} 

		Key keyObject = new Key(key);
		Value valueObject = new Value(value);

		ThreadEndpoint ep = Registry.getRegistryInstance().lookup(url);
		if (ep == null) {
			this.out.println("Node '" + node + "' does not exist!");
			return;
		}

		//新增逻辑开始    start
		Node nodeI = null;//要插入的节点

		ID nodeID = ep.getNodeID();
		HashFunction hashFunction = HashFunction.getHashFunction();
		ID valueId = hashFunction.createID(value.getBytes());
		this.out.println("value="+value+",转换成16进制id："+valueId);
		int compareResult = nodeID.compareTo(valueId);


		//处理valueIdHashCode  start
		//找id值最小和最大,
		Map eps = Registry.getRegistryInstance().lookupAll();
		ID maxID = null;
		Node maxNode = null;
		if (eps.size() != 0) {
			Iterator valueIterator = eps.values().iterator();
			while (valueIterator.hasNext()) {
				ThreadEndpoint threadEndpoint = (ThreadEndpoint) valueIterator.next();
				if (maxID ==null){
					maxID = threadEndpoint.getNodeID();
					maxNode = threadEndpoint.getNode();
				}else {
					int result = threadEndpoint.getNodeID().compareTo(maxID);
					if (result > 0){
						maxID = threadEndpoint.getNodeID();
						maxNode = threadEndpoint.getNode();
					}
				}
			}
			this.out.println("最大id node："+maxNode.getNodeURL().getHost()+",id:"+maxID);
			this.out.println("最小id node："+Registry.getRegistryInstance().lookup(maxNode.getSuccessor().getNodeURL()).getNode().getNodeURL().getHost()+",id:"+maxNode.getSuccessor().getNodeID());
		}
		if (maxID.compareTo(valueId)<0||maxNode.getSuccessor().getNodeID().compareTo(valueId)>0){
			nodeI =  Registry.getRegistryInstance().lookup(maxNode.getSuccessor().getNodeURL()).getNode();
		}else {
			//处理valueIdHashCode  end

//		int i = nodeID.compareTo(valueId);
			//比较node id
			if (compareResult > 0){
				this.out.println("和 node:"+ep.getNode().getNodeURL().getHost()+" 比较结果："+compareResult+"，找前继");
				ThreadEndpoint tempep = Registry.getRegistryInstance().lookup(ep.getNode().getPredecessor().getNodeURL());
				nodeI = preNode(tempep.getNode(),valueId);
				Node sucNode = nodeI.getSuccessor();
				nodeI = Registry.getRegistryInstance().lookup(sucNode.getNodeURL()).getNode();

			}else if(compareResult < 0){
				this.out.println("和 node:"+ep.getNode().getNodeURL().getHost()+" 比较结果："+compareResult+"，找后继");
				ThreadEndpoint tempep = Registry.getRegistryInstance().lookup(ep.getNode().getPredecessor().getNodeURL());
				nodeI = sucNode(tempep.getNode(),valueId);
			}else if (compareResult ==  0){
				this.out.println("比较结果："+compareResult);
				nodeI = ep.getNode();
			}
		}

		this.out.println("找到节点node："+nodeI.getNodeURL().getHost());


		//新增逻辑开始    end

		try {
			ChordImplAccess.fetchChordImplOfNode(nodeI).insert(
					keyObject, valueObject);
		} catch (Throwable t) {
			ConsoleException e = new ConsoleException(
					"Exception during execution of command. " + t.getMessage());
			e.setStackTrace(t.getStackTrace());
			throw e;
		}
		this.out.println("Value '" + value + "' with key '" + key
				+ "' inserted " + "successfully from node '" + nodeI + "'.");
	}

	private void insertKeyValueFromTxt() throws ConsoleException {
		try {
			Map<String, String> dataMap = DataTxtLoader.getDataMap();
			for (String dataKey : dataMap.keySet()) {
				String dataValue = dataMap.get(dataKey);
				Key keyObject = new Key(dataKey);
				Value valueObject = new Value(dataValue);

				Node node = null;

				//查找node
				node  = findNode(dataKey);

				try {
					ChordImplAccess.fetchChordImplOfNode(node).insert(
							keyObject, valueObject);
				} catch (Throwable t) {
					ConsoleException e = new ConsoleException(
							"Exception during execution of command. " + t.getMessage());
					e.setStackTrace(t.getStackTrace());
					throw e;
				}
				this.out.println("Value '" + dataValue + "' with key '" + dataKey
						+ "' inserted " + "successfully from node '" + node.getNodeURL().getHost() + "'.id :"+node.getNodeID());
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	//根据id查找要插入的节点
	private Node findNode(String value) {
		HashFunction hashFunction = HashFunction.getHashFunction();
		ID valueId = hashFunction.createID(value.getBytes());
		this.out.println("ID="+value+",转换成16进制id："+valueId);

		List<Node> sortList = getSortNodeList();
		//判断最大值和最小值
		if (sortList != null && sortList.size()>0){

			//和最小值比较
			int compare = valueId.compareTo(sortList.get(0).getNodeID());
			//如果比最小值还小,返回最小节点
			if (compare<0){
				return sortList.get(0);
			}
			//和最大值比较
			compare = valueId.compareTo(sortList.get(sortList.size()-1).getNodeID());
			//如果比最大值还大,返回最小节点
			if (compare>0){
				return sortList.get(0);
			}

		}
		for (Node node : sortList) {
			int compareResult = valueId.compareTo(node.getNodeID());
			if (compareResult<0){
				return node;
			}
		}





		return null;
	}

	/**
	 * 获得已排序的list集合
	 */
	private List<Node> getSortNodeList(){
		Map eps = Registry.getRegistryInstance().lookupAll();

		Map<ID, ThreadEndpoint> temp = new HashMap<ID, ThreadEndpoint>();
		List<Node> list  =new ArrayList<Node>();

		if (eps.size() != 0) {
			Iterator valueIterator = eps.values().iterator();
			ID[] ids = new ID[eps.size()];
			int index = 0;
			while (valueIterator.hasNext()) {
				ThreadEndpoint ep = (ThreadEndpoint) valueIterator.next();
				ids[index] = ep.getNodeID();
				temp.put(ids[index], ep);
				index++;
			}
			Arrays.sort(ids);
			for (int i = 0; i < ids.length; i++) {
				ThreadEndpoint ep = temp.get(ids[i]);
				list.add(ep.getNode());
			}
		}
		return list;
	}

	/**
	 * 方法递归找到前置节点
	 * @param node 当前节点
	 * @param
	 * @return
	 */
	private synchronized Node preNode(Node node, ID otherID){
		//新增逻辑开始
		ID nodeID = node.getNodeID();
		//hash值
		int compare = nodeID.compareTo(otherID);
		this.out.println("和 node: "+node.getNodeURL().getHost()+" 比较结果为："+compare);

		if (compare < 0 ) {
			return node;
		}else if (compare > 0){
			Node preNode = node.getPredecessor();
			preNode = Registry.getRegistryInstance().lookup(preNode.getNodeURL()).getNode();
			return preNode(preNode, otherID);
		}else if (compare == 0){
			return node;
		}
		return null;
	}
	/**
	 * 方法递归找到后继节点
	 * @param node 当前节点
	 * @param
	 * @return
	 */
	private synchronized Node sucNode(Node node, ID otherID){
		//新增逻辑开始
		ID nodeID = node.getNodeID();
		//hash值
		int compare = nodeID.compareTo(otherID);
		this.out.println("和 node: "+node.getNodeURL().getHost()+" 比较结果为："+compare);

		if (compare > 0 ) {
			return node;
		}else if (compare < 0){
			Node sucNode = node.getSuccessor();
			sucNode = Registry.getRegistryInstance().lookup(sucNode.getNodeURL()).getNode();
			return sucNode(sucNode, otherID);
		}else if (compare == 0){
			return node;
		}
		return null;
	}




	public String getCommandName() {
		return COMMAND_NAME;
	}

	public void printOutHelp() {
		this.out
				.println("This command inserts a value with a provided key into the chord network.");
		this.out
				.println("The key is inserted starting from the node provided as parameter.");
		this.out.println("Required parameters: ");
		this.out.println("\t" + NODE_PARAM
				+ ": The name of the node, from where the key is inserted.");
		this.out.println("\t" + KEY_PARAM + ": The key for the value.");
		this.out.println("\t" + VALUE_PARAM + ": The value to insert.");
		this.out.println();
	}




}
