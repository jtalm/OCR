package OCRModule;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import detectLanguage.LanguageDetector;
import Splitter.VideoSplitter;
import ocr.OCREngine;

public class MainModule {
	public static void main(String[] args) {
		BlockingQueue<String> FilePath = new LinkedBlockingQueue<String>();
		String outputLocation = "C:\\Users\\Cysilver\\Documents\\Faculdade\\Mestrado\\GP\\OCR\\images";
		AtomicBoolean SplitterThreadAlive = new AtomicBoolean();
		SplitterThreadAlive.set(true);
		String Lyric = "";
		String FileToSplit = ".\\a.avi";
		
		
		System.out.println(LanguageDetector.DetectLanguage("hello this is a sentence in english"));
		
		VideoSplitter threadSplit = new VideoSplitter(FileToSplit, outputLocation, FilePath, SplitterThreadAlive);
		//threadSplit.run();
		Thread split = new Thread(threadSplit);
		split.start();
		
		OCREngine threadOCR = new OCREngine(outputLocation, FilePath, SplitterThreadAlive, Lyric);
		Thread OCR = new Thread(threadOCR);
		OCR.start();
		
		try {
			split.join();
			OCR.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("ERROR");
		}
	}
}
