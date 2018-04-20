package com.mypack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;

import com.ibm.mq.MQC;
import com.ibm.mq.MQEnvironment;
import com.ibm.mq.MQException;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;

public class MQReporting {

	protected Shell shell;
	private static Text textMessage;
	protected Properties prop;
	
	protected int l;
	protected String[] qn;
	protected int[] qd;
		
	public static void main(String[] args) {
		try {
			MQReporting window = new MQReporting();
			window.open();
		} catch (Exception e) {
			textMessage.setText(""+e);
		}
	}

	public void open() {
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	protected void createContents() {
		shell = new Shell();
		shell.setModified(true);
		shell.setSize(485, 450);
		shell.setText("MQ Reporting");
		
		loadProperties();
		shell.setLayout(null);
		
		textMessage = new Text(shell, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.CENTER | SWT.MULTI);
		textMessage.setBounds(134, 10, 304, 100);
		textMessage.setText("This tool can be used to generate the MQ Report\r\n\r\nSelect the Server Node, and then click on Generate Report button to generate the report.");
		
		final Combo comboServer = new Combo(shell, SWT.NONE);
		comboServer.setBounds(10, 10, 91, 23);
		String[] s = prop.getProperty("servers").split(";");		
		for(int i=0; i<s.length; i++) 
			comboServer.add(s[i]);			
		comboServer.setText("-- Server --");
		
		Button btnReport = new Button(shell, SWT.NONE);
		btnReport.setBounds(10, 85, 91, 25);
		btnReport.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				
				textMessage.setText("Queue Depth of the Queues are being fetched...");
				
				String hostName = prop.getProperty("hostName_"+comboServer.getText());
				String channel = prop.getProperty("channel_"+comboServer.getText());
				Integer port = new Integer(prop.getProperty("port_"+comboServer.getText()));
				String qm = prop.getProperty("qm_"+comboServer.getText());
				final String[] queueNames = prop.getProperty("allQueues").split(";");
				
				l = queueNames.length;
				qn = new String[l];
				qd = new int [l];
				
				Table table = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
	    		table.setBounds(10, 171, 443, 231);
	    		table.setHeaderVisible(true);
	    		table.setLinesVisible(true);
	    		
	    		String[] titles = { "#", "QUEUE_NAME", "QUEUE_DEPTH" };
	    	    for (int colIndex = 0; colIndex < titles.length; colIndex++) {
	    	        TableColumn column = new TableColumn(table, SWT.NULL);
	    	        column.setText(titles[colIndex]);
	    	    }	    	    
	    	    	
				MQQueue queue;
				MQQueueManager qManager;
				
				MQEnvironment.hostname = hostName;
		        MQEnvironment.channel = channel;
		        MQEnvironment.port = port;
		        
		        try {
		        	qManager = new MQQueueManager(qm);		        			        	
		           	for(int n=0; n<l; n++) {
		           		queue = qManager.accessQueue(queueNames[n], MQC.MQOO_INQUIRE, qm, null, null);
			        	qn[n] = queueNames[n];
			        	qd[n] = queue.getCurrentDepth();
			        	
			        	TableItem item = new TableItem(table, SWT.NULL);
			        	item.setText(0, ""+(n+1));
			        	item.setText(1, qn[n]);			        	
			            item.setText(2, ""+qd[n]);			            
			            for (int colIndex = 0; colIndex < titles.length; colIndex++) {
			    	        table.getColumn(colIndex).pack();
			    	    }
		           	}		           	
		        }
			    catch (MQException e1) {
			    	textMessage.setText(""+e1);
			    }
		        
		        textMessage.setText("Queue Depth of the Queues successfully fetched !");
		        
		        Button btnExport = new Button(shell, SWT.NONE);
				btnExport.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						String filename = "location/of/output/file/"+comboServer.getText()+".csv";
				   		try {				   			
				   			File file = new File(filename);
				   			file.createNewFile();
				   			
							FileWriter fw = new FileWriter(file);
							fw.append("QUEUE_NAME");
				        	fw.append('\t');					            
				            fw.append("QUEUE_DEPTH");
				            fw.append('\n');
							for(int n=0; n<l; n++) {			        
					        	fw.append(qn[n]);
					        	fw.append('\t');					            
					            fw.append(""+qd[n]);
					            fw.append('\n');				            			            
				        	}
							fw.flush();
				            fw.close();
				            
							textMessage.setText("Report exported successfully !");
						} 
						catch (IOException e1) {
							textMessage.setText(""+e1);
						}
					}
				});
				btnExport.setBounds(182, 131, 75, 25);
				btnExport.setText("Export");				
		    }			
		});
		btnReport.setToolTipText("Click to generate Report");
		btnReport.setText("Backout Report");
		
			
		Label separator = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setBounds(10, 126, 449, 2); 			
		
	}
	
	protected void loadProperties(){
		try {
			prop = new Properties();
			prop.load(new FileInputStream("MQ.properties"));
		}
		catch(Exception e){
			textMessage.setText("" + e);
		}		
	}
}