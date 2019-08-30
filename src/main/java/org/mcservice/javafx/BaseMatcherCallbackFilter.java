/*******************************************************************************
 * Copyright (C) 2019 Sebastian Müller <sebastian.mueller@mcservice.de>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.mcservice.javafx;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import javafx.scene.control.TextFormatter.Change;

public class BaseMatcherCallbackFilter implements UnaryOperator<Change> {
	private AdvancedMatcher matcher = null;
	private Consumer<String> endEditCallback = null;
	private Consumer<Boolean> matchCallback = null;

	public BaseMatcherCallbackFilter(Pattern p) {
		if (p != null) {
			setMatcher(new AdvancedMatcher(p));
		}
	}

	@Override
	public Change apply(Change change) {
		if (null == this.matcher) {
			return change;
		}

		checkEndEdit(change);
		
		if(change.isDeleted()) {
			if (matchCallback != null) {
				matchCallback.accept(matcher.matches());
			}
			return change;
		}

		if (matcher.hitEnd() || matcher.matches()) {	
			String completedSqence=matcher.completeSquence();
			if(completedSqence!=null) {
				change.setText(change.getText().concat(completedSqence));
				change.setAnchor(change.getControlNewText().length());
				change.setCaretPosition(change.getControlNewText().length());
				checkEndEdit(change);
			}		
			
			if (matchCallback != null) {
				matchCallback.accept(matcher.matches());
			}
			return change;
		}
		
		if (matchCallback != null) {
			matcher.reset(change.getControlText());
			matchCallback.accept(matcher.matches());
		}

		return null;
	}

	private void checkEndEdit(Change change) {
		String actText=change.getControlNewText();
		matcher.reset(actText);

		if (matcher.matches() && change.isAdded()) {
			if (matcher.requireEnd() && endEditCallback != null) {
				endEditCallback.accept(actText);
			}
		}
	}
	
	public List<String> getMatchedGroups() {
		List<String> result=null;
		if(matcher.matches()) {
			result=new ArrayList<String>(matcher.groupCount());
			for (int i = 0; i < matcher.groupCount(); i++) {
				result.add(matcher.group(i+1));
			}
		}
		return result;
	}

	/**
	 * @param matcher the matcher to set
	 */
	public final void setMatcher(AdvancedMatcher matcher) {
		this.matcher = matcher;
	}
	
	/**
	 * @param completions the completions to set
	 */
	public void setCompletions(List<String> completions) {
		this.matcher.setCompletions(completions);
	}

	/**
	 * @param endEditCallback the endEditCallback to set
	 */
	public final void setEndEditCallback(Consumer<String> endEditCallback) {
		this.endEditCallback = endEditCallback;
	}

	/**
	 * @param matchCallback the matchCallback to set
	 */
	public final void setMatchCallback(Consumer<Boolean> matchCallback) {
		this.matchCallback = matchCallback;
	}

}
