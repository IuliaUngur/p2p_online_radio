package utils;

public class StringUtils {

	public static String removeSpecialCharacters(String s) {
		   StringBuilder sb = new StringBuilder();
		   for(char c : s.toCharArray()) {
		      if (
		    		  (c >= '0' && c <= '9') || 
		    		  (c >= 'A' && c <= 'Z') || 
		    		  (c >= 'a' && c <= 'z')
		      ) {
		         sb.append(c);
		      }
		   }
		   return sb.toString();
		}
	
}
