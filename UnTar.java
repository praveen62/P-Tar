import java.io.*;
import java.util.*;
public class UnTar {
public static void main(String args[]) throws FileNotFoundException, IOException, ClassNotFoundException
{
	long start = System.currentTimeMillis();
	//ObjectInputStream in = new ObjectInputStream(new FileInputStream(args[0]));
	//TreeStorage1 ts1 = (TreeStorage1)in.readObject();
	InputStream fr = new FileInputStream(args[0]);
	//int headerLength = fr.read(); // no of fields
	int fileNameLength;
	while((fileNameLength=fr.read())!=-1)
	{
		
	
	int  fileLength = fr.read();
	System.out.println(fileLength);
	System.out.println(fileNameLength);
	String fileName="";            
	for(int i=0;i<fileNameLength;i++)  // getting file name
	{
	fileName= fileName +(char)fr.read();	
	}
	System.out.println(fileName);
	File file = new File(fileName);
	if(file.getParentFile()!=null)
	{
		file.getParentFile().mkdir();
		file.createNewFile();
	}
	else
	{
	    file.createNewFile();
	}
	OutputStream fw = new FileOutputStream(fileName);
	int k;
	for(int i=0;i<fileLength;i++ )
	{
		if((k=(char)fr.read())!=-1)
		{
			fw.write(k);
		}
	}
	fw.close();
	}
	
	fr.close();
	long end = System.currentTimeMillis();
    System.out.println("time taken" +(end-start));
}
}
