package org.mcservice.javafx;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdvancedMatcher {

	Pattern pattern; 
	Matcher matcher;
	String actInput; /**< The actual input string */
	static final int MAX_UNICODE=0x10FFFF;
	List<String> completions = new ArrayList<String>(1024);
	Boolean requireEnd = null;
	boolean completionSet=false;
	String completion = null ;

	public AdvancedMatcher(String regexp){
		this(Pattern.compile(regexp));
	} 
	
	public AdvancedMatcher(Pattern pattern){
		this.pattern = pattern;
		this.matcher = pattern.matcher("");
		for(char i=0;i<256;++i) {
			completions.add(String.valueOf(i));
		}
	} 
	
	public void reset(String input) {
		requireEnd = null;
		completion = null;
		completionSet=false;
		this.matcher.reset(input);
		this.actInput=input;
	}
	
	public boolean matches() {
		return matcher.matches();
	}
	
	public int groupCount() {
		return matcher.groupCount();
	}
	
	public String group(int i) {
		return matcher.group(i);
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
		if(requireEnd!=null)
			return requireEnd;
		
		for(String actCompl : completions) {
			Matcher tmpM=pattern.matcher(actInput.concat(actCompl));
			if(tmpM.matches() || tmpM.hitEnd()) {
				requireEnd=false;
				return false;
			}
		}
		
		requireEnd=true;
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
			return null;
		if(!matcher.hitEnd())
			return null;
		if(completions.size()==0)
			return null;
		if(completionSet)
			return completion;
		
		String result = actInput;
		String next;
		do {
			next=null;
			for(String actCompl : completions) {
				Matcher tmpM=pattern.matcher(result.concat(actCompl));
				if(tmpM.matches() || tmpM.hitEnd()) {
					if (next!=null) {
						next=null;
						break;
					} else {
						next=actCompl;
					}
				}
			}
			if(next!=null)
				result=result.concat(next);
		} while(next!=null);
		result=result.substring(actInput.length());
		completionSet=true;
		if(result.length()==0)
			return null;
		completion=result;
		return result;
	}

	public void setCompletions(List<String> completions) {
		if(null==completions) {
			for(int i=0;i<MAX_UNICODE;++i) {
				this.completions.add(String.valueOf(Character.toChars(i)));
			}
		} else {
			this.completions.clear();
			HashSet<String> inserted=new HashSet<String>();
			for(String cmpl: completions)
				if(!inserted.contains(cmpl)) {
					this.completions.add(cmpl);
					inserted.add(cmpl);
				}
		}
	}
}


