package ocr;

import java.io.File;

import net.sourceforge.tess4j.*;
import debug.Debug;

/**
 * The Class OCRLyricsCompiler.
 * Responsible for retrieving text from a given image
 * <p>
 * This class uses Tess4j to retrieve text from image
 */
public class OCRLyricsCompiler {
	
	private Tesseract1 instance;
	
	
	/**
	 * Instantiates a new OCR lyrics compiler
	 */
	public OCRLyricsCompiler(String lang) {
		String fileSeparator = System.getProperty("file.separator");
		this.instance = new Tesseract1();
		this.instance.setDatapath("."+fileSeparator);
		this.instance.setLanguage(lang);
	}

	/**
	 * Do OCR.
	 *
	 * @param FilePath the file path
	 * @return a string list containing only 2 positions with the filename processed and the text retrieved from it
	 */
	public String[] DoOCR(String FilePath){
		File imageFile = new File(FilePath);
		String filename = imageFile.getName().split("[.]")[0];
		String result = "";
		
		Debug.printDebug("Processing file: "+imageFile.getName());
		
		try{
			
			if(imageFile.exists()){
			
				result = instance.doOCR(imageFile);
			}
			
		} catch (TesseractException e){
			Debug.printDebug("ERROR Tesseract");
		} catch (Exception e) {
			Debug.printDebug("ERROR");
		}
		
		String[] results = new String[2];
		results[0] = filename;
		results[1] = result;
		
		return results;
	}
	
}
