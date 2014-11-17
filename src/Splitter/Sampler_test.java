package Splitter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.xuggle.mediatool.IMediaListener;
import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import com.xuggle.xuggler.Global;

public class Sampler_test {
	public static final double SECONDS_BETWEEN_FRAMES = 0.5;

    private static final String inputFilename = "videoplayback.mp4";
    private static final String outputFilePrefix = ".\\images\\";
    
    // The video stream index, used to ensure we display frames from one and
    // only one video stream from the media container.
    private static int mVideoStreamIndex = -1;
    
    // Time of last frame write
    private static long mLastPtsWrite = Global.NO_PTS;
    
    public static final long MICRO_SECONDS_BETWEEN_FRAMES = 
        (long)(Global.DEFAULT_PTS_PER_SECOND * SECONDS_BETWEEN_FRAMES);
    
    public Sampler_test(){
    	
    	System.out.println(Splitt_Video(inputFilename).size());
    	
    }
    
    public static void main(String[] args) {

        new Sampler_test();

    }
    
    public ArrayList<BufferedImage> Splitt_Video(String inputFile){
    	
    	IMediaReader mediaReader = ToolFactory.makeReader(inputFile);
    	
        // stipulate that we want BufferedImages created in BGR 24bit color space
        mediaReader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);
        
        ImageSnapListener Worker = new ImageSnapListener();
        
        mediaReader.addListener((IMediaListener) Worker);

        // read out the contents of the media file and
        // dispatch events to the attached listener
        while (mediaReader.readPacket() == null) ;
        
        return Worker.Screencapture;
    }

    private static class ImageSnapListener extends MediaListenerAdapter {
    	
    	private ArrayList<BufferedImage> Screencapture=null;
    	
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
                System.out.printf(
                        "at elapsed time of %6.3f seconds wrote: %s\n",
                        seconds, outputFilename);

                // update last write time
                mLastPtsWrite += MICRO_SECONDS_BETWEEN_FRAMES;
            }

        }
        
        private String dumpImageToFile(BufferedImage image) {
            try {
            	if(this.Screencapture==null){
            		this.Screencapture = new ArrayList<BufferedImage>();
            	}
            	
            	Screencapture.add(image);
            	
                String outputFilename = outputFilePrefix + 
                     System.currentTimeMillis() + ".png";
                ImageIO.write(image, "png", new File(outputFilename));
                return outputFilename;
            } 
            catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        
        public ArrayList<BufferedImage> getScreencapture(){
        	return this.Screencapture;
        }

    }
}
