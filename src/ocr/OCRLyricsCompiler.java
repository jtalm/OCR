package ocr;

import java.io.File;

import net.sourceforge.tess4j.*;
import debug.Debug;

public class OCRLyricsCompiler {
	
	private Tesseract1 instance;
	
	
	public OCRLyricsCompiler() {
		String fileSeparator = System.getProperty("file.separator");
		this.instance = new Tesseract1();
		this.instance.setDatapath("."+fileSeparator);
		this.instance.setLanguage("eng");
	}

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
			System.out.println("ERROR");
			System.out.println(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("ERROR");
		}
		
		String[] results = new String[2];
		results[0] = filename;
		results[1] = result;
		
		return results;
	}
	
	public static void main(String[] args) {
		//DoOCR(".\\images\\2500000.png");

	}

}
