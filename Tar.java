import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.io.*;
//import java.nio.file.*;





class TreeNode 
{
	private String fileName;
	private int nodeSize;
	TreeNode[] tn;
	long pos;
	public TreeNode(String fileName,long pos)
	{
		this.fileName=fileName;
		this.pos=pos;
	}
	
	public TreeNode(String fileName, int size)
	{
		this.fileName = fileName;
		this.nodeSize =size;
		tn= new TreeNode[size];
				
	}
	
	public String getFileName() 
	{
		return fileName;
	}
   
	public int getNodeSize() 
	{
		return nodeSize;
	}
	
	public void display()
	{
		System.out.println(fileName);
	}
}





class TreeStorage1  {
	//FileWriter fw;
	int totalLength=0;
	int fileCount;
	String path;
	int size;
	int kk=0;
	boolean ck;
	TreeNode root1;
    long pos=0;
    Object lock = new Object();
	public TreeStorage1(String path,int size)
	{
		this.path = path;
		this.size =size;
	}
    public TreeNode getRoot()
    {
    	root1 = new TreeNode(path,size);
    	return root1;
    }
    public void checkFile(File file[],TreeNode current) throws IOException
    {
    	
    	for(int sk=0;sk<file.length;sk++)
    	{  
    		File flist[] = null;
    		if(file[sk].isDirectory())
    		{
    			flist =file[sk].listFiles();
    			TreeNode tn1 = new TreeNode(file[sk].getPath(),flist.length);
    			checkFile(flist,tn1);
    			current.tn[sk]=tn1;
    		}
    		else
    		{
    			TreeNode tn1 = new TreeNode(file[sk].getPath(),pos);
    			pos=pos+file[sk].length()+2+file[sk].getPath().length();
    			current.tn[sk]=tn1;
    		}		
    	}
    }
    
	public void displayStructure(TreeNode root,ExecutorService executor) throws IOException, InterruptedException
	{
		for(int i=0;i<root.getNodeSize();i++)
    	{	
			TreeNode next = root.tn[i];
			File file = new File(next.getFileName());
			String readFile = next.getFileName();
		   
		    
			if(!file.isDirectory())
			{
				long seekPos =next.pos;
				System.out.println("writing "+ readFile+" at "+seekPos );
				BlockingQueue<Byte> qu = new LinkedBlockingQueue<Byte>();
				executor.execute(new Runnable(){
				@Override
				public void run() 
				{
					// TODO Auto-generated method stub
					
					System.out.println("reading.." +next.getFileName());
					try 
					{
					//ts1.finalWrite(next.getFileName(),(int)file.length());
							InputStream in = new FileInputStream(readFile);
							int i;
							int fileNameLength =readFile.length();
							long fileLength = file.length();
							long  total = (2+fileNameLength+fileLength);
							//System.out.println("toal length" +total);
							qu.put((byte)total);
							qu.put((byte)readFile.length());  // file name length
							qu.put((byte)file.length());   //file content length
							
							for(int j=0;j<readFile.length();j++)
							{
								qu.put((byte)readFile.charAt(j)); //writing file Name
							}
							while((i=in.read())!=-1)
							{
								qu.put((byte)i);	
							}
							in.close();
					} 
					catch (IOException e) 
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
					catch (InterruptedException e) 
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				
			});
			
			
			executor.execute(new Runnable(){

				@Override
				public void run() 
				{
					try 
					{
						RandomAccessFile out = new RandomAccessFile(path+".par","rw");
						
						
						//OutputStream out= new FileOutputStream(path+".par",true);
						//System.out.println("yy");
						//System.out.println("wriote   "+rd.qu.take());
						/*int fileNameLength = rd.qu.take();
						out.write((byte)fileNameLength);
						
						out.write( rd.qu.take());
						for(int j=0;j<fileNameLength;j++)
						{	
							out.write(rd.qu.take());
						}*/
						long total = qu.take();
						out.seek(seekPos);
						//System.out.println(total);
						int s=0;
						while(s<total)
						{
							//System.out.println(s);
							//System.out.println("waitng for" +readFile);
							out.write((byte)qu.take());
							//System.out.println("done");
							s++;
						} 
						out.close();
					}
					catch (IOException | InterruptedException e) 
					{
							// TODO Auto-generated catch block
							e.printStackTrace();
					}
				}
		     
				});
			}
			else
			{	
				displayStructure(root.tn[i],executor);
			}
    	}
	}
	
	public void processTree(TreeNode root) throws IOException, InterruptedException
	{
		ExecutorService executor = Executors.newFixedThreadPool(4);
		displayStructure(root,executor);
		executor.shutdown();
		while (!executor.isTerminated()) {   }  
        System.out.println("Finished all threads"); 
	}

	/*
	public void finalWrite(String readFile,int length) throws IOException
	{
		
		OutputStream out = new FileOutputStream(path+".par",true);
		InputStream in = new FileInputStream(readFile);
		int i;
		String data="";
		//System.out.println(readFile.length());
		//fw.write(2);
		System.out.println(length);
	    byte fileNameLength = (byte)readFile.length();
		out.write(fileNameLength);  // file name length
		out.write((byte)length);    //file content length
		out.write(readFile.getBytes()); //writing file Name
	     
	    while((i=in.read())!=-1)
	    {
	        data = data+(char)i;	
	    }
		out.write(data.getBytes());
		
		out.close();
		in.close();
		
	}*/
}









public class Tar
{
	public static void main(String args[]) throws IOException, InterruptedException
	{
		
		long start = System.currentTimeMillis();
		TreeStorage1 ts1 = new TreeStorage1(args[0],args.length-1);
		//ts1.finalFile(args[0]);
		TreeNode root = ts1.getRoot(); 
		File files[] = new File[args.length-1];
		for(int i=1;i<args.length;i++)
		{
			
		 files[i-1] = new File(args[i]);
		 System.out.println(files[i-1]);
		}
		ts1.checkFile(files,root);
		ts1.processTree(root);
		long end = System.currentTimeMillis();
        System.out.println("time taken" +(end-start)); 
	}
}

