package OCRModule;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import detectLanguage.LanguageDetector;
import Splitter.VideoSplitter;
import ocr.OCREngine;

public class MainModule {
	public static void main(String[] args) {
		String fileSeparator = System.getProperty("file.separator");
		
		BlockingQueue<String> FilePath = new LinkedBlockingQueue<String>();
		BlockingQueue<String> FilePathBW = new LinkedBlockingQueue<String>();
		String outputLocation = "."+fileSeparator+"images";
		AtomicBoolean SplitterThreadAlive = new AtomicBoolean();
		SplitterThreadAlive.set(true);
		String Lyric = "";
		
		String fileName = "All About That Bass - Karaoke Version in the style of Meghan Trainor(720p_H.264-AAC).mp4";
		
		String FileToSplit = "../videos/"+fileName;
		
		System.out.println(LanguageDetector.DetectLanguage("hello this is a sentence in english"));
		
		VideoSplitter threadSplit = new VideoSplitter(	FileToSplit, 
														outputLocation, 
														FilePath,
														FilePathBW,
														SplitterThreadAlive);
		//threadSplit.run();
		Thread split = new Thread(threadSplit);
		split.start();
		
		OCREngine threadOCR = new OCREngine(outputLocation, 
											FilePath, 
											SplitterThreadAlive, 
											Lyric,
											fileName+".txt");
		
		OCREngine threadOCRBW = new OCREngine(	outputLocation, 
												FilePathBW, 
												SplitterThreadAlive, 
												Lyric,
												fileName+".BW.txt");

		
		Thread OCR = new Thread(threadOCR);
		
		Thread OCRBW = new Thread(threadOCRBW);
		
		OCR.start();
		OCRBW.start();
		
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
