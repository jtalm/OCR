package detectLanguage;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

public final class LanguageDetector {
	
	
	public static String DetectLanguage(String lyric){
		String lang="";
		Detector detector;
		try {
			DetectorFactory.loadProfile(".\\profiles");
			detector = DetectorFactory.create();

			detector.append(lyric);
			
			lang=detector.detect();
		} catch (LangDetectException e) {
			e.printStackTrace();
		}
		
		return lang;
	}
}
