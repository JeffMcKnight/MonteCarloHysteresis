/**
 * 
 */
package com.jeffmcknight.magneticmontecarlo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


//**********  Singleton - WriteToFile **********
/**
 * @author jeffmcknight
 *
 */
public class WriteToFile 
{
	private static WriteToFile instance = null;
	static File file;
	static FileWriter fw;
	static BufferedWriter bw; 
	
	   protected WriteToFile() 
	   {
		   // Exists only to defeat instantiation.
	   }

	 //**********  getInstance **********
	   public static WriteToFile getInstance() 
	   {
		   if(instance == null) 
		   {
			   instance = new WriteToFile();
		   }
		   return instance;
	   }
	   
//**********  open **********
	/**
	 * @param file
	 */
	public static void open(String stringFilePath) 
	{
		file = new File(stringFilePath);
		
		// if file doesn't exist, then create it
		if (!file.exists()) 
		{
			try 
			{
				file.createNewFile();
				fw = new FileWriter(file.getAbsoluteFile());
				bw = new BufferedWriter(fw);
			} 
			catch (IOException e) 
			{
			   System.out.println("FILE ALREADY EXISTS!!");
				// TODO Auto-generated catch block
			   // Handle with user dialog.
				e.printStackTrace();
			}
		}
		
	}
	
	//**********  close **********
		/**
		 * @param file
		 */
		public static void close() 
		{
			// if file doesn't exist, then create it
			if (file.exists()) 
			{
				try 
				{
					bw.close();
				} 
				catch (IOException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		//**********  write  **********
	public static void write(String args) 
	{
		try 
		{
			String content = args;
			bw.write(content);
//			System.out.println("write() - content: " + content);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	//**********  append **********
	public static void append(String args) 
	{
		try 
		{
			String content = args;
			bw.append(content);
//			System.out.println("append() - content: " + content);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	//**********  appendNewLine **********
	public static void appendNewLine() 
	{
		try 
		{
			String content = "\n";
			bw.append(content);
//			System.out.println("append() - content: " + content);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
}
