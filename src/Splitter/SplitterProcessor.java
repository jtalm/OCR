package Splitter;

import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

import javax.imageio.ImageIO;

import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import com.xuggle.xuggler.Global;

import debug.Debug;

public class SplitterProcessor extends MediaListenerAdapter {
	
	private BlockingQueue<String> ImagesPath=null;
	private BlockingQueue<String> ImagesPathBW=null;
	
	// The video stream index, used to ensure we display frames from one and
    // only one video stream from the media container.
	private static int mVideoStreamIndex = -1;
	
	// Time of last frame write
    private static long mLastPtsWrite = Global.NO_PTS;
	
	private long MICRO_SECONDS_BETWEEN_FRAMES;
	
	//location where splitter will save images
	private String outputFilePrefix;
	
	private String folderSeparator;
	
	public SplitterProcessor( long 							MICRO_SECONDS_BETWEEN_FRAMES,
							  String 						outputFilePrefix,
							  BlockingQueue<String>		 	ImagesPath,
							  BlockingQueue<String>			ImagesPathBW) {
		
		this.MICRO_SECONDS_BETWEEN_FRAMES = MICRO_SECONDS_BETWEEN_FRAMES;
		this.outputFilePrefix = outputFilePrefix;
		this.ImagesPathBW = ImagesPathBW;
		this.ImagesPath = ImagesPath;
		
		folderSeparator = System.getProperty("file.separator");
		
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
                            
            String outputFilename = dumpImageToFile(event.getImage(), 
            										event.getTimeStamp());

            // indicate file written
            double seconds = ((double) event.getTimeStamp()) / 
                Global.DEFAULT_PTS_PER_SECOND;
            
            Debug.printDebug("at elapsed time of "+seconds+" seconds wrote: "+ outputFilename+"\n");

            // update last write time
            mLastPtsWrite += MICRO_SECONDS_BETWEEN_FRAMES;
        }

    }
    
    //timestamp in microseconds
    private String dumpImageToFile(BufferedImage image, Long timeStamp) {
    	BufferedImage gray = new BufferedImage(image.getWidth(),image.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY);

               // convert the original colored image to grayscale
               ColorConvertOp op = new ColorConvertOp(
                image.getColorModel().getColorSpace(),
                gray.getColorModel().getColorSpace(),null);
               op.filter(image,gray);
    	
        try {
        		String outputFilenameBW = outputFilePrefix + folderSeparator + 
                        timeStamp + ".BW" + ".png";
        		String outputFilename = outputFilePrefix + folderSeparator + 
                        timeStamp + ".png";
        		
        		if(this.ImagesPath!=null){
        			ImageIO.write(image, "png", new File(outputFilename));
        			this.ImagesPath.put(outputFilename);
        		}
                if(this.ImagesPathBW!=null){
                	ImageIO.write(gray, "png", new File(outputFilenameBW));
                	this.ImagesPathBW.put(outputFilenameBW);
                	
                }
                return outputFilename;
        } 
        catch (IOException e) {
        	
        	Debug.printDebug(e.getMessage());
        	
        	System.out.println("Error writting file to disk");
            
        } catch (InterruptedException e) {
        	
        	Debug.printDebug(e.getMessage());
			
        	System.out.println("Error inserting image to queue");
		}
        
        return null;
    }
}
