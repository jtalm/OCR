package Splitter;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;


import com.xuggle.mediatool.IMediaListener;
import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.Global;

import debug.Debug;

// TODO: Auto-generated Javadoc
/**
 * The Class VideoSplitter.
 */
public class VideoSplitter implements Runnable {
	
	//default values
	private double SECONDS_BETWEEN_FRAMES = 0.5;
	
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
	
	private final String Config_File = "./Config_Splitter.ini";
	
	//location of file input and directory output to test class
    private static String inputFilename = "./images/9275933.png";
    private static String outputFilePrefix = "./images/";
    
    
    private long MICRO_SECONDS_BETWEEN_FRAMES = 
        (long)(Global.DEFAULT_PTS_PER_SECOND * SECONDS_BETWEEN_FRAMES);
    
    private String fileToSplit;
    private String locationOutput;
    private BlockingQueue<String> imagesPath;
    private BlockingQueue<String> imagesPathBW;
    private AtomicBoolean threadEnd;
    
    private String[] supportedFormats={"avi","flv","3gp","mpg","mp4","webm","ogg","mkv","wmv","rmvb","m4v"};
    
    /**
     * Instantiates a new video splitter.
     *
     * @param FileToSplit the file to split
     * @param LocationOutput the location output
     * @param ImagesPath the images path
     * @param ImagesPathBW the images path bw
     * @param ThreadEnd the thread end
     * @throws FileNotFoundException 
     */
    public VideoSplitter( String 						FileToSplit,
    					  String						LocationOutput,
    					  BlockingQueue<String>			ImagesPath,
    					  BlockingQueue<String> 		ImagesPathBW,
    					  //flag to notice the splitter end
    					  AtomicBoolean					ThreadEnd) throws FileNotFoundException,RuntimeException{
    	
    	File tmp = new File(FileToSplit);
    	String fileFormat = null;
    	String fileExtentions = FileToSplit.substring(FileToSplit.length()-3, FileToSplit.length()); 

    	if(!tmp.exists()){
    		throw new FileNotFoundException();
    	}
    	
    	for(String i:supportedFormats){
    		if(i.equalsIgnoreCase(fileExtentions)){
    			fileFormat = i;
    		}
    	}
    	if(fileFormat==null){
    		throw new RuntimeException("Unsupported Format");
    	}
    	tmp = new File(LocationOutput);
    	if(!tmp.exists()){
    		throw new FileNotFoundException();
    	}
    	
    	this.fileToSplit = FileToSplit;
    	this.locationOutput = LocationOutput;
    	this.imagesPath = ImagesPath;
    	this.imagesPathBW = ImagesPathBW;
    	this.threadEnd = ThreadEnd;
    	
    	this.ReadConfigFile();    	
    	
    	
    	
    }
    
    @Override
    public void run() {
    	SplitVideo(fileToSplit,
				locationOutput,
				imagesPath,
				imagesPathBW,
				threadEnd);
    	
    	Debug.printDebug("Splitter finished!");
    	
    	threadEnd.set(false);
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
	        	} catch(Exception e){
	        		e.printStackTrace();
	        	}
	        }
	        
	        reader.close();
    	
    	} catch (IOException e1){
    	
    		Debug.printDebug(e1.getMessage());
    		System.out.println("File \"Config_Splitter.ini\" not found! ");
    	}
    }
    
    
    /**
     * Split video.
     *
     * @param InputFile the input file
     * @param LocationOutput the location output
     * @param ImagesPath the images path
     * @param ImagesPathBW the images path bw
     * @param ThreadEnd the thread end
     * @return the int
     */
    public int SplitVideo( String 						InputFile,
    						 String 						LocationOutput,
    						 BlockingQueue<String>		 	ImagesPath,
    						 BlockingQueue<String> 			ImagesPathBW,
    						 AtomicBoolean					ThreadEnd){
    	
    	IMediaReader mediaReader = ToolFactory.makeReader(InputFile);
    	
        // stipulate that we want BufferedImages created in BGR 24bit colour space
        mediaReader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);
        
        Debug.printDebug(LocationOutput);
        
        SplitterProcessor Worker = new SplitterProcessor( MICRO_SECONDS_BETWEEN_FRAMES,
        												  LocationOutput,
        												  ImagesPath,
        												  ImagesPathBW);
        
        mediaReader.addListener((IMediaListener) Worker);

        // read out the contents of the media file and
        // dispatch events to the attached listener
        while (mediaReader.readPacket() == null) ;
                
        return 0;
    }
    
    //used only to test the splitter
    //Will disapear afterwards
    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String[] args) throws FileNotFoundException,RuntimeException{
    	
    	BlockingQueue<String> TestObject = new LinkedBlockingQueue<String>();
    	
        new VideoSplitter(	inputFilename,
        					outputFilePrefix,
        					TestObject,
        					null,
        					new AtomicBoolean(true));
        
        while(!TestObject.isEmpty()){
        	try{
        		System.out.println(TestObject.take());
        	} catch (InterruptedException e) {
        		e.printStackTrace();
        	}
        }

    }
    
}
