package Splitter;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import com.xuggle.mediatool.IMediaListener;
import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.Global;

public class VideoSplitter {
	
	//default values
	private double SECONDS_BETWEEN_FRAMES = 0.5;
	private String ResourceFolder = ".\\";
	
	
	/* 
	 * 
	 * In the Config File if the Tags: 
	 * seconds_between_frames (default value is 0.5)
	 * resource_folder (default location is the project directory or deploy location)
	 * 
	 * format example:
	 * seconds_between_frames=0.5
	 * resource_folder=.\
	 * 
	 */
	
	private final String Config_File = ".\\Config_Splitter.ini";
	
	//location of file input and directory output to test class
    private static String inputFilename = "videoplayback.mp4";
    private static String outputFilePrefix = ".\\images\\";
    
    
    private long MICRO_SECONDS_BETWEEN_FRAMES = 
        (long)(Global.DEFAULT_PTS_PER_SECOND * SECONDS_BETWEEN_FRAMES);
    
    //if flag equals 1 it will output stacktrace from java
    private int debug_flag;
    
    public VideoSplitter(int 							debug,
    					  String 						FileToSplit,
    					  String						LocationOutput,
    					  BlockingQueue<String>			ImagesPath,
    					  //flag to notice the splitter end
    					  AtomicBoolean					ThreadEnd){

    	this.debug_flag=debug;
    	
    	this.ReadConfigFile();    	
    	
    	if(ResourceFolder.endsWith("\\")==false){
    		ResourceFolder = ResourceFolder + "\\";
    	}
    	
    	SplitVideo(ResourceFolder+FileToSplit,
    					LocationOutput,
    					ImagesPath,
    					ThreadEnd);
    	
    }
    
    /**
     * Function to retrieve the splitter configurations
     * from file 
     */
    private void ReadConfigFile(){
    	
    	try{
	    	BufferedReader reader = new BufferedReader(new FileReader(this.Config_File));
	        
	    	String line;
	        String[] buffer;
	        
	        while ((line = reader.readLine()) != null)
	        {
	        	try{
	        		buffer = line.toLowerCase().split("=");
	        		
	        		if(buffer[0].equals("seconds_between_frames")){
	        			
	        			this.SECONDS_BETWEEN_FRAMES = Integer.parseInt(buffer[1]);
	        			this.MICRO_SECONDS_BETWEEN_FRAMES = 
	        					(long)(Global.DEFAULT_PTS_PER_SECOND * this.SECONDS_BETWEEN_FRAMES);
	        		
	        		}
	        		
	        		if(buffer[0].equals("resource_folder")){
	        			this.ResourceFolder=buffer[1];
	        		}
	        		
	        	} catch(Exception e){
	        		if(debug_flag==1)
	            		e.printStackTrace();
	        	}
	        }
	        
	        reader.close();
    	
    	} catch (IOException e1){
    	
    		if(debug_flag==1)
        		e1.printStackTrace();
    		System.out.println("File \"Config_Splitter.ini\" not found! ");
    	}
    }
    
    
    public int SplitVideo( String 						InputFile,
    						 String 						LocationOutput,
    						 BlockingQueue<String>		 	ImagesPath,
    						 AtomicBoolean					ThreadEnd){
    	
    	IMediaReader mediaReader = ToolFactory.makeReader(InputFile);
    	
        // stipulate that we want BufferedImages created in BGR 24bit colour space
        mediaReader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);
        
        SplitterProcessor Worker = new SplitterProcessor(debug_flag, 
        												  MICRO_SECONDS_BETWEEN_FRAMES,
        												  LocationOutput,
        												  ImagesPath);
        
        mediaReader.addListener((IMediaListener) Worker);

        // read out the contents of the media file and
        // dispatch events to the attached listener
        while (mediaReader.readPacket() == null) ;
        
        synchronized (ThreadEnd) {
            ThreadEnd.set(false);
            ThreadEnd.notifyAll();
		}
        
        
        return 0;
    }
    
    //used only to test the splitter
    //Will disapear afterwards
    public static void main(String[] args) {
    	
    	BlockingQueue<String> TestObject = new LinkedBlockingQueue<String>();
    	
        new VideoSplitter(	0,
        					inputFilename,
        					outputFilePrefix,
        					TestObject,
        					new AtomicBoolean(true));
        
        while(!TestObject.isEmpty()){
        	try{
        		System.out.println(TestObject.take());
        	} catch (InterruptedException e) {
        		
        	}
        }

    }
    
}
