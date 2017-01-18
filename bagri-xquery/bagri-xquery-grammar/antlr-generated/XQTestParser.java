// $ANTLR 3.5 C:\\Work\\Bagri\\project\\trunk\\bagri-xquery\\src\\main\\antlr\\XQTest.g 2013-10-07 02:12:43

import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

import org.antlr.runtime.tree.*;


@SuppressWarnings("all")
public class XQTestParser extends Parser {
	public static final String[] tokenNames = new String[] {
		"<invalid>", "<EOR>", "<DOWN>", "<UP>", 
	};
	public static final int EOF=-1;

	// delegates
	public Parser[] getDelegates() {
		return new Parser[] {};
	}

	// delegators


	public XQTestParser(TokenStream input) {
		this(input, new RecognizerSharedState());
	}
	public XQTestParser(TokenStream input, RecognizerSharedState state) {
		super(input, state);
	}

	protected TreeAdaptor adaptor = new CommonTreeAdaptor();

	public void setTreeAdaptor(TreeAdaptor adaptor) {
		this.adaptor = adaptor;
	}
	public TreeAdaptor getTreeAdaptor() {
		return adaptor;
	}
	@Override public String[] getTokenNames() { return XQTestParser.tokenNames; }
	@Override public String getGrammarFileName() { return "C:\\Work\\Bagri\\project\\trunk\\bagri-xquery\\src\\main\\antlr\\XQTest.g"; }


	public static class rule_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "rule"
	// C:\\Work\\Bagri\\project\\trunk\\bagri-xquery\\src\\main\\antlr\\XQTest.g:8:1: rule :;
	public final XQTestParser.rule_return rule() throws RecognitionException {
		XQTestParser.rule_return retval = new XQTestParser.rule_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		try {
			// C:\\Work\\Bagri\\project\\trunk\\bagri-xquery\\src\\main\\antlr\\XQTest.g:8:5: ()
			// C:\\Work\\Bagri\\project\\trunk\\bagri-xquery\\src\\main\\antlr\\XQTest.g:8:7: 
			{
			root_0 = (Object)adaptor.nil();


			}

			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "rule"

	// Delegated rules



}
