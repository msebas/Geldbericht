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
	private Consumer<List<String>> matchedGroupsCallback = null;
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

		checkEndEdit(change.getControlNewText());

		if (matcher.hitEnd() || matcher.matches()) {
			if (matchedGroupsCallback != null && matcher.matches()) {
				List<String> results = new ArrayList<String>(matcher.groupCount());
				for (int i = 1; i < matcher.groupCount() + 1; ++i) {
					results.add(matcher.group(i));
				}
				matchedGroupsCallback.accept(results);
			}
			if (matchCallback != null) {
				matchCallback.accept(matcher.matches());
			}
			
			if(change.getAnchor()!=change.getCaretPosition() ||
					change.getAnchor()!=change.getControlNewText().length())
				return change;
				
			String completedSqence=matcher.completeSquence();
			if(completedSqence!=null) {
				change.setText(change.getText().concat(completedSqence));
				change.setAnchor(change.getControlNewText().length());
				change.setCaretPosition(change.getControlNewText().length());
			}
			checkEndEdit(change.getControlNewText());			
			
			return change;
		}

		return null;
	}

	private void checkEndEdit(String actText) {
		matcher.reset(actText);

		if (matcher.matches()) {
			if (matcher.requireEnd() && endEditCallback != null) {
				endEditCallback.accept(actText);
			}
		}
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

	public final void setMatchedGroupsCallback(Consumer<List<String>> matchedGroupsCallback) {
		this.matchedGroupsCallback = matchedGroupsCallback;
	}

	/**
	 * @param matchCallback the matchCallback to set
	 */
	public final void setMatchCallback(Consumer<Boolean> matchCallback) {
		this.matchCallback = matchCallback;
	}

}