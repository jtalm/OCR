package ocr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import debug.Debug;
import ocr.OCRLyricsCompiler;

// TODO: Auto-generated Javadoc
/**
 * The Class OCREngine.
 * Module \
 */
public class OCREngine implements Runnable{
	
	private float OCRSimillarityThreshold = (float) 0.2;
	
	private AtomicBoolean SplitterThreadAlive;
	private BlockingQueue<String> ImagesPath;
	
	private String outputLocation;
	
	private OCRLyricsCompiler analyser;
	
	private String lyrics;
	private String buffer="";
	private String lasttime="";
	
	private String lyricretrieved="";
	
	public String getLyricretrieved() {
		return lyricretrieved;
	}

	private String fileSeparator;
	private String outputFilename;
	
	private float similarity=1;
	
	/**
	 * Instantiates a new OCR engine.
	 *
	 * @param outputLocation the output location
	 * @param ImagesPath the images path
	 * @param SplitterThreadAlive the splitter thread alive
	 * @param lyric the lyric
	 * @param outputFilename the output filename
	 */
	public OCREngine(		String					outputLocation,
							BlockingQueue<String> 	ImagesPath,
							AtomicBoolean 			SplitterThreadAlive,
							String 					lyric,
							String					outputFilename,
							String 					lang) {
		
		this.outputFilename = outputFilename;
		this.lyrics = lyric;
		
		this.analyser = new OCRLyricsCompiler(lang);
		this.outputLocation = outputLocation;
		this.ImagesPath = ImagesPath;
		this.SplitterThreadAlive = SplitterThreadAlive;
		
		fileSeparator = System.getProperty("file.separator");
	}
	
	@Override
	public void run() {
		
		String ocrResult[];
		float differenceQuoficient;
		
		String path;
		
		PrintWriter lyricsFile=null;
		try {
			lyricsFile = new PrintWriter(outputLocation+fileSeparator+outputFilename, "UTF-8");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		
		while(	this.SplitterThreadAlive.get() || 
				!this.ImagesPath.isEmpty()){
			path="";
			try {
				path = this.ImagesPath.poll(5, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				Debug.printDebug(e.getMessage());
				e.printStackTrace();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			} 
			if(path==null){
				continue;
			}
			while(true){
				try{
					File image;
					ocrResult = this.analyser.DoOCR(path);
					
					image = new File(path);
					
					if(ocrResult[1].length() == 0) {
						Debug.printDebug("deleting file");
						image.delete();
						break;
					}
					
					int divisor = Math.max(ocrResult[1].length(), buffer.length());
					
					differenceQuoficient = (float)(Levenshtein.LevenshteinDistance(ocrResult[1],buffer)/(divisor*1.0));
					
					
					Debug.printDebug(differenceQuoficient+"");
					if(differenceQuoficient >= OCRSimillarityThreshold){
						
						Debug.printDebug(ocrResult[1]);
						Debug.printDebug(buffer);
						
						lyricsFile.write(ocrResult[0]+":"+buffer);
						lyricsFile.flush();
						
						this.lyricretrieved += ocrResult[1];
						
						buffer = ocrResult[1].substring(0);
						
					}else {

						Debug.printDebug("deleting file");
						image.delete();
						
					}
					lasttime = ocrResult[0];
					
					break;
				} catch (java.lang.Error e) {
					Debug.printDebug("ERROR tessaract library");
				}
			}
			
		}
		if(SplitterThreadAlive.get()==true){
			Debug.printDebug("splitter is dead");
		}
		
		lyricsFile.write(lasttime+":"+buffer);
		lyricsFile.flush();
		lyricsFile.close();
		
		int divisor = Math.max(lyricretrieved.length(), lyrics.length());
		
		differenceQuoficient = (float)(Levenshtein.LevenshteinDistance(lyricretrieved,lyrics)/(divisor*1.0));
		Debug.printDebug(outputFilename+"FINISH: "+differenceQuoficient);
		this.similarity = differenceQuoficient;
	}
	
	public float getSimilarity() {
		return similarity;
	}
	
}
