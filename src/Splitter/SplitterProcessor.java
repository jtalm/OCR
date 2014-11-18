package Splitter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

import javax.imageio.ImageIO;

import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import com.xuggle.xuggler.Global;

public class SplitterProcessor extends MediaListenerAdapter {
	
	private BlockingQueue<String> ImagesPath=null;
	
	// The video stream index, used to ensure we display frames from one and
    // only one video stream from the media container.
	private static int mVideoStreamIndex = -1;
	
	// Time of last frame write
    private static long mLastPtsWrite = Global.NO_PTS;
	
  //if flag equals 1 it will output stacktrace from java
	private int debug_flag;
	
	private long MICRO_SECONDS_BETWEEN_FRAMES;
	
	//location where splitter will save images
	private String outputFilePrefix;
	
	public SplitterProcessor(int 							debug_flag,
							  long 							MICRO_SECONDS_BETWEEN_FRAMES,
							  String 						outputFilePrefix,
							  BlockingQueue<String>		 	ImagesPath) {
		this.debug_flag = debug_flag;
		this.MICRO_SECONDS_BETWEEN_FRAMES = MICRO_SECONDS_BETWEEN_FRAMES;
		this.outputFilePrefix = outputFilePrefix;
		this.ImagesPath = ImagesPath;
		
	}
	
    public void onVideoPicture(IVideoPictureEvent event) {

        if (event.getStreamIndex() != mVideoStreamIndex) {
            
        	// if the selected video stream id is not yet set, go ahead an
            // select this lucky video stream
            if (mVideoStreamIndex == -1)
                mVideoStreamIndex = event.getStreamIndex();
            
            
            // no need to show frames from this video stream
            else
                return;
        }

        // if uninitialized, back date mLastPtsWrite to get the very first frame
        if (mLastPtsWrite == Global.NO_PTS)
            mLastPtsWrite = event.getTimeStamp() - MICRO_SECONDS_BETWEEN_FRAMES;

        // if it's time to write the next frame
        if (event.getTimeStamp() - mLastPtsWrite >= 
                MICRO_SECONDS_BETWEEN_FRAMES) {
                            
            String outputFilename = dumpImageToFile(event.getImage());

            // indicate file written
            double seconds = ((double) event.getTimeStamp()) / 
                Global.DEFAULT_PTS_PER_SECOND;
            
            if(debug_flag==1)
            	System.out.printf(
	                    "at elapsed time of %6.3f seconds wrote: %s\n",
	                    seconds, outputFilename);

            // update last write time
            mLastPtsWrite += MICRO_SECONDS_BETWEEN_FRAMES;
        }

    }
    
    private String dumpImageToFile(BufferedImage image) {
        try {
        		String outputFilename = outputFilePrefix + 
                        System.currentTimeMillis() + ".png";
        		
        		this.ImagesPath.put(outputFilename);
                ImageIO.write(image, "png", new File(outputFilename));
                
                return outputFilename;
        } 
        catch (IOException e) {
        	
        	if(debug_flag==1)
        		e.printStackTrace();
            
        	System.out.println("Error writting file to disk");
            
        } catch (InterruptedException e) {
        	
        	if(debug_flag==1)
        		e.printStackTrace();
			
        	System.out.println("Error inserting image to queue");
		}
        
        return null;
    }
}
