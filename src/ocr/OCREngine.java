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

public class OCREngine implements Runnable{
	
	private float OCRSimillarityThreshold = (float) 0.1;
	
	private AtomicBoolean SplitterThreadAlive;
	private BlockingQueue<String> ImagesPath;
	
	private String outputLocation;
	
	private OCRLyricsCompiler analyser;
	
	private String lyrics;
	private String buffer="";
	
	private String fileSeparator;
	private String outputFilename;
	
	public OCREngine(		String					outputLocation,
							BlockingQueue<String> 	ImagesPath,
							AtomicBoolean 			SplitterThreadAlive,
							String 					lyric,
							String					outputFilename) {
		
		this.outputFilename = outputFilename;
		this.lyrics = lyric;
		
		this.analyser = new OCRLyricsCompiler();
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
						
						lyricsFile.write(ocrResult[0]+":"+ocrResult[1]);
						lyricsFile.flush();
						buffer = ocrResult[1].substring(0);
					}else {

						Debug.printDebug("deleting file");
						image.delete();
						
					}
					
					break;
				} catch (java.lang.Error e) {
					System.out.println("ERROR");
				}
			}
			
		}
		if(SplitterThreadAlive.get()==true){
			Debug.printDebug("splitter is dead");
		}
	}
	
}
