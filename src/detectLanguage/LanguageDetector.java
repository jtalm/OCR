package detectLanguage;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

import debug.Debug;

/**
 * The Class LanguageDetector.
 * 
 * 
 */
public final class LanguageDetector {
	
	
	/**
	 * Detect language.
	 *
	 * @param lyric the music lyric
	 * @return Language detected from the profiles directory. If no language if found return a empty String
	 */
	public static String DetectLanguage(String lyric){
		String lang="en";
		Detector detector;
		try {
			DetectorFactory.loadProfile("./profiles");
			detector = DetectorFactory.create();

			detector.append(lyric);
			
			lang=detector.detect();
		} catch (LangDetectException e) {
			Debug.printDebug("language not detected!");
		}
		
		return lang;
	}
}
