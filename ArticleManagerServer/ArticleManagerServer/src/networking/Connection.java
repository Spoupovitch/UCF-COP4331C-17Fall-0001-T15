package networking;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import am_utils.CUtils;

import am_utils.ArticleInfo;

public class Connection extends Thread {
	private int port; // Port to listen for clients on.
	private Socket clientSocket; // Client socket object.
	private int failTolerance; // Number of consecutive communication failures to tolerate before the connection is considered terminated.
	private boolean waiting; // indicates whether thread is waiting on a client or not. If false, connection manager will place in connection list and spin up a new listener thread.
	private int consecFail;
	private int permissionLevel;
	private Object waitingLock = new Object();
	String username;
	
	protected static final int OBJECTPORT = 1907;
	protected static final int FILEPORT = 1908;
	
	Connection(int serverPort, int failTolerance)
	{
		port = serverPort;
		this.failTolerance = failTolerance;
		waiting = true;
		consecFail = 0;
		permissionLevel = -1;
	}

	public boolean isWaiting()
	{
		synchronized(waitingLock)
		{
			return waiting;
		}
	}
	
	private void sendString(String outString)
	{
		try {
			PrintWriter netOut = new PrintWriter(clientSocket.getOutputStream(), true);
			netOut.println(outString);
		} catch (IOException e) {
			System.err.println("Failed to send string to client.");
			e.printStackTrace();
		}
	}
	
	private String receiveString()
	{
		String messageString;
		
		while(consecFail <= failTolerance)
		try {
			BufferedReader inReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			messageString = inReader.readLine();
			consecFail = 0;
			return messageString;
		} catch (IOException e) {
			consecFail++;
		}
		return null;
	}
	
	private void sendObject(Object outObj)
	{
		try
		{
			ServerSocket objectServ = new ServerSocket(1907);
			Socket objectSocket = objectServ.accept();
			objectServ.close();
			while(consecFail <= failTolerance)
			{
				ObjectOutputStream sendStream = new ObjectOutputStream(objectSocket.getOutputStream());
				sendStream.writeObject(outObj);
			}
			consecFail = 0;
			objectSocket.close();
		} catch (IOException e1)
		{
			consecFail++;
		}
	}
	
	private Object receiveObject()
	{
		Object receiveObject = null;
		try {
			ServerSocket objectServ = new ServerSocket(1907);
			Socket objectSocket = objectServ.accept();
                        objectServ.close();
			ObjectInputStream receiveStream = new ObjectInputStream(objectSocket.getInputStream());
			try {
                                receiveObject = receiveStream.readObject();
			} catch (ClassNotFoundException e) {
				System.err.println("Error reconstituting serialized object from stream.");
				e.printStackTrace();
				return null;
			}
			objectSocket.close();
			return receiveObject;
		} catch (IOException e) {
			consecFail++;
                        System.err.println("FAILED TO DO SOME SOCKET THING.");
			e.printStackTrace();
		}
		return null;
	}
	
	private void sendFile(File fp)
	{
		byte[] fileBytes = new byte[(int)fp.length()];
		OutputStream byteOutput;
		FileInputStream inputStream;
		
		try 
                {
                    inputStream = new FileInputStream(fp);
		} 
                catch(FileNotFoundException e)
		{
			e.printStackTrace();
			return;
		}
		
		try 
                {	
                    ServerSocket fileServ = new ServerSocket(1908);
                    Socket fileSocket = fileServ.accept();
                    fileServ.close();
				
                    int count = 0;
                    while ((count = inputStream.read(fileBytes)) > 0)
                    {
                        fileSocket.getOutputStream().write(fileBytes, 0, count);
                    }
                                
                    fileSocket.getOutputStream().close();
                    inputStream.close();
                                
                    fileSocket.close();
                    return;
		} 
                catch (IOException e) 
                {
			consecFail++;
		}
		try {
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
	
	private File receiveFile(int byteCount) // writes file to disk and returns file location as File object.
	{
		byte[] receivedBytes = new byte[byteCount];
		File newFile = new File("articles/" + new SimpleDateFormat("yyyyMMdd_SSS").format(new Date()) + ".pdf");
		
		try 
		{
			ServerSocket fileServ = new ServerSocket(1908);
			Socket fileSocket = fileServ.accept();
			fileServ.close();
				
			FileOutputStream fos = new FileOutputStream(newFile.getPath());
			BufferedOutputStream bos = new BufferedOutputStream(fos);
                                
                        int count = 0;
                        while ((count = fileSocket.getInputStream().read(receivedBytes)) > 0)
                        {
                        	fos.write(receivedBytes, 0, count);
                        }

                        //outStream.write(articleByteArray, 0, fileSize);
                        CUtils.debugMsg("Wrote file to buffer, time to close stream...");
                        fileSocket.getInputStream().close();
                        fos.close();
			fileSocket.close();
                                
			return newFile;
		} 
		catch (IOException e) 
		{
			consecFail++;
                        CUtils.warning("Failed to do some file write thing.");
                        CUtils.warning(e.getMessage());
                        e.printStackTrace();
		}

		
		return null;
	}
	
	private void parse(String input)
	{
		if(input == null)
		{
			return;
		}
		String[] substrings = input.split(" ");
		String opString;
		String arg1 = "";
		String arg2 = "";
		int opcode;
		opString = substrings[0];
		opcode = Integer.parseInt(opString);
		ArrayList<ArticleInfo> returnedArticles;
		ArticleInfo tempArticleInfo;
		File tempFile;
		
		if(substrings.length > 1)
		{
			arg1 = substrings[1];
		}
		if(substrings.length > 2)
		{
			arg2 = substrings[2];
		}
		
		//Try to keep networking sends here and remember to update consecFail as necessary.
		switch(opcode) { 
			case 0:
				consecFail = 0;
				break;
			case 1:
				permissionLevel = users.Public.login(arg1, arg2);
				if(permissionLevel == -1)
				{
					sendString("-1");
				} else
				{
					username = arg1;
					sendString("0");
				}
				break;
			case 2:
				permissionLevel = -1;
				sendString("0"); //Confirm that logout was successful to client.
				break;
			case 3:
				if(users.Public.register(arg1, arg2))
				{
					sendString("0");
				} else
				{
					sendString("-1");
				}
				break;
			case 4:
				returnedArticles = database.Public.getArticlesFromCategory(Integer.valueOf(arg1), Integer.valueOf(arg2));
				sendObject(returnedArticles);
				break;
			case 5:
				tempArticleInfo = (ArticleInfo)receiveObject();
				System.out.println("Received article info object from client.");
				tempFile = receiveFile(Integer.parseInt(arg1));
				System.out.println("Received file from client.");
				database.Public.insertArticle(tempFile, tempArticleInfo, permissionLevel);
				System.out.println("Article info data inserted into database.");
				break;
			case 6:
				/*
				tempArticleInfo = database.Public.getArticleInfo(Integer.valueOf(arg1));
				sendObject(tempArticleInfo);
				break;
				*/
				
				if ( arg1.compareTo("25") == 0 )
                {
                    tempArticleInfo = new ArticleInfo(3, 95, 55);

                    tempArticleInfo.printName = "TestArticleTitle";
                    tempArticleInfo.abstractText = "TestArticleAbstract";
                    tempArticleInfo.author = "TestArticleAuthors";
                    tempArticleInfo.doiNumber = "TestArticleDOI";
                    tempArticleInfo.owner = "Nobody";

                    sendObject(tempArticleInfo);
                }
                else
                {
                    tempArticleInfo = new ArticleInfo(4, 20, 50);

                    tempArticleInfo.printName = "TestArticleTitle2";
                    tempArticleInfo.abstractText = "TestArticleAbstract2";
                    tempArticleInfo.author = "TestArticleAuthors2";
                    tempArticleInfo.doiNumber = "TestArticleDOI2";
                    tempArticleInfo.owner = "Noah2";

                    sendObject(tempArticleInfo);
                }
				break;
				
			case 7:
				tempFile = database.Public.downloadArticle(Integer.valueOf(arg1));
				sendString(database.Public.getArticleInfo(Integer.valueOf(arg1)).printName + " " + tempFile.length());
				sendFile(tempFile);
				break;
			case 8:
				sendString(Integer.toString(permissionLevel));
				break;
			case 9:
				returnedArticles = database.Public.getArticlesFromUser(username);
				sendObject(returnedArticles);
				break;
		}
		
	}
	
	public void run()
	{
		BufferedReader clientStream;
		String clientMessage;
		try {
			ServerSocket listener = new ServerSocket(port);
			clientSocket = listener.accept();
			clientSocket.setSoTimeout(2000);
			listener.close();
			synchronized(waitingLock)
			{
				this.waiting = false;
			}
		} catch (IOException e) {
			System.err.println("Error listening for clients.");
			e.printStackTrace();
			return;
		}
		
		try {
			clientStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Failed to create buffered reader for client input stream.");
			return;
		}
		
		while(!clientSocket.isClosed() && consecFail <= failTolerance)
		{
			try {
				clientMessage = clientStream.readLine();
				parse(clientMessage);
				consecFail = 0;
			} catch (IOException e) {
				System.out.println("Listen timeout, consecutive timeouts: " + (consecFail+1));
				consecFail++;
			}
		}
		
		try {
			clientSocket.close();
		} catch (IOException e) {
			System.err.println("Error closing client socket.");
			e.printStackTrace();
		}
	}
}
