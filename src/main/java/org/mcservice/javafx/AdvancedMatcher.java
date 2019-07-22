package org.mcservice.javafx;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdvancedMatcher {

	Pattern pattern; 
	Matcher matcher;
	String actInput; /**< The actual input string */
	static final int MAX_UNICODE=0xFF; //It is too slow to test the full UTF-8 range //0x10FFFF;

	public AdvancedMatcher(String regexp){
		this.pattern = Pattern.compile(regexp);
		this.matcher = pattern.matcher("");
	} 
	
	public AdvancedMatcher(Pattern pattern){
		this.pattern = pattern;
		this.matcher = pattern.matcher("");
	} 
	
	public void reset(String input) {
		this.matcher.reset(input);
		this.actInput=input;
	}
	
	public boolean matches() {
		return matcher.matches();
	}
	
	public boolean hitEnd() {
		return matcher.hitEnd();
	}
	
	/**
	 * This method should check if the actual input is a positive match to the regular expression 
	 * and no character could be appended to the actual input without turning the positive match 
	 * into a negative match.
	 *    
	 * If the actual input is not a match to the regular expression of this instance this 
	 * method should return false.
	 * 
	 * Otherwise false is returned if a character is found that could be appended to the regular 
	 * expression without turning it into a negative match.
	 * 
	 * @return false if actual input is a negative match or if an additional {@link Character}
	 * 		   exists that would not turn it into a negative match.  
	 */
	public boolean requireEnd() {
		//To be clear, "return m.requireEnd();" does not solve this problem
		if(!matcher.matches())
			return false;
		for(int i=0;i<MAX_UNICODE;++i) {
			Matcher tmpM=pattern.matcher(actInput+String.valueOf(Character.toChars(i)));
			if(tmpM.matches() || tmpM.hitEnd())
				return false;
		}
		
		return true;
	}
	
	/**
	 * This method should add as many unique characters to the actual input as possible to 
	 * fit to the regular expression of this instance. So characters should only be added to
	 * the result if there exist no string starting with a different character completing 
	 * the actual input to a positive match.
	 * 
	 * @return the completed string or {@code null} if the actual input could not be completed
	 *         to a positive match.   
	 */
	public String completeSquence() {
		if(matcher.matches())
			return actInput;
		if(!matcher.hitEnd())
			return null;
		
		String result = actInput;
		char[] next;
		do {
			next=null;
			for(int i=0;i<MAX_UNICODE;++i) {
				Matcher tmpM=pattern.matcher(result+String.valueOf(Character.toChars(i)));
				if(tmpM.matches() || tmpM.hitEnd()) {
					if (next!=null) {
						next=null;
						break;
					} else {
						next=Character.toChars(i);
					}
				}
			}
			if(next!=null)
				result=result+String.valueOf(next);
		} while(next!=null);
		
		return result;
	}	
}


