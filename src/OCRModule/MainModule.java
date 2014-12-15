package OCRModule;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import debug.Debug;
import detectLanguage.LanguageDetector;
import edu.dei.gp.jpa.Song;
import edu.dei.gp.status.OCRStatus;
import Splitter.VideoSplitter;
import ocr.OCREngine;

// TODO: Auto-generated Javadoc
/**
 * The Class MainModule.
 */
public class MainModule implements Runnable {

	private String Lyric;

	private String outputLocation;
	private String fileName;
	private String FileToSplit; 

	private Song music;


	//JPA Class
	//

	/**
	 * Instantiates a new main module.
	 */

	public MainModule(Song music){
		this.FileToSplit = music.getFilePath();
		this.outputLocation = (new File(FileToSplit)).getParent();
		this.Lyric = music.getLyric();
		this.fileName = (new File(this.FileToSplit)).getName();

	}

	public MainModule(	String outputLocation,
						String fileToSplit) {
		//this.bean = bean; 

		this.outputLocation = outputLocation;
		this.FileToSplit 	= fileToSplit;
		this.fileName = (new File(this.FileToSplit)).getName();

		this.Lyric = "For every man there is a cause which he would gladly die for \n"
				+ "Defend the right to have a place for which he can belong to \n"
				+ "And every man will fight with his bare hands in desperation \n"
				+ "and shed his blood to stem the flood to barricade invasion\n";

		Lyric += Lyric+Lyric+Lyric+Lyric;
	}


	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		if(args.length<=0 || args.length>2){
			System.out.println("Invalid Arguments");
			System.out.println("to run just run command example:");
			System.out.println("java MainModule music.avi c:\\output");
		} else {
			new MainModule(	args[1],
							args[0]).run();
		}
	}

	@Override
	public void run() {


		BlockingQueue<String> FilePath = new LinkedBlockingQueue<String>();
		BlockingQueue<String> FilePathBW = new LinkedBlockingQueue<String>();

		AtomicBoolean SplitterThreadAlive = new AtomicBoolean();
		SplitterThreadAlive.set(true);

		String lang = LanguageDetector.DetectLanguage(Lyric);
		System.out.println(lang);
		VideoSplitter threadSplit = null;
		try{
			threadSplit = new VideoSplitter(	FileToSplit, 
												outputLocation, 
												FilePath,
												FilePathBW,
												SplitterThreadAlive);
		} catch (FileNotFoundException e){
			Debug.printDebug("File not found or outputlocation not valid");
			music.setOcrStatus(OCRStatus.ERROR);
			return ;
		} catch (RuntimeException e) {
			Debug.printDebug("Invalid format");
			music.setOcrStatus(OCRStatus.ERROR);
			return ;
		}
		
		Thread split = new Thread(threadSplit);
		split.start();

		OCREngine threadOCR = new OCREngine(outputLocation, 
				FilePath, 
				SplitterThreadAlive, 
				Lyric,
				fileName+".txt",
				lang);

		OCREngine threadOCRBW = new OCREngine(	outputLocation, 
				FilePathBW, 
				SplitterThreadAlive, 
				Lyric,
				fileName+".BW.txt",
				lang);


		Thread OCR = new Thread(threadOCR);

		Thread OCRBW = new Thread(threadOCRBW);

		OCR.start();
		OCRBW.start();
		if(music!=null){
			try {
				music.setOcrStatus(OCRStatus.SEGMENTING);
				split.join();
				music.setOcrStatus(OCRStatus.OCR_WORKING);
				OCR.join();
				OCRBW.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO: handle exception
				System.out.println("ERROR");
			}
	
	
			//shoving in database
		
			if(threadOCRBW.getSimilarity()<threadOCR.getSimilarity()){
				music.setOcrTrack(threadOCRBW.getLyricretrieved());
				music.setOcrError(1-threadOCRBW.getSimilarity());
			} else {
				music.setOcrTrack(threadOCR.getLyricretrieved());
				music.setOcrError(1-threadOCR.getSimilarity());
			}
			music.setOcrStatus(OCRStatus.DONE);
		} else {
			try {
				split.join();
				OCR.join();
				OCRBW.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO: handle exception
				System.out.println("ERROR");
			}
		}
	}
}
