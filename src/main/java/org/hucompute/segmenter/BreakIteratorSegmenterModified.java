package org.hucompute.segmenter;

import java.text.BreakIterator;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.SegmenterBase;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * BreakIterator segmenter.
 */
@TypeCapability(
    outputs = { 
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class BreakIteratorSegmenterModified
    extends SegmenterBase
{
    /**
     * Per default the Java {@link BreakIterator} does not split off contractions like
     * {@code John's} into two tokens. When this parameter is enabled, a non-default token split is
     * generated when an apostrophe ({@code '}) is encountered.
     */
    public static final String PARAM_SPLIT_AT_APOSTROPHE = "splitAtApostrophe";
    @ConfigurationParameter(name = PARAM_SPLIT_AT_APOSTROPHE, mandatory = true, defaultValue = "false")
    private boolean splitAtApostrophe;
    
    /**
     * Per default the Java {@link BreakIterator} does not split off contractions like
     * {@code John's} into two tokens. When this parameter is enabled, a non-default token split is
     * generated when an apostrophe ({@code '}) is encountered.
     */
    public static final String PARAM_SPLIT_AT_MINUS = "splitAtMinus";
    @ConfigurationParameter(name = PARAM_SPLIT_AT_MINUS, mandatory = true, defaultValue = "true")
    private boolean splitAtMinus;

    @Override
    protected void process(JCas aJCas, String text, int zoneBegin)
        throws AnalysisEngineProcessException
    {
    	for (Token token : JCasUtil.select(aJCas, Token.class)) {
			token.removeFromIndexes();
		}
    	for (Sentence sentence : JCasUtil.select(aJCas, Sentence.class)) {
          processSentence(aJCas, sentence.getCoveredText(), sentence.getBegin());
		}
//        BreakIterator bi = BreakIterator.getSentenceInstance(getLocale(aJCas));
//        bi.setText(text);
//        int last = bi.first() + zoneBegin;
//        int cur = bi.next();
//        while (cur != BreakIterator.DONE) {
//            cur += zoneBegin;
//            if (isWriteSentence()) {
//                Annotation segment = createSentence(aJCas, last, cur);
//                if (segment != null) {
//                    processSentence(aJCas, segment.getCoveredText(), segment.getBegin());
//                }
//            }
//            else {
//                int[] span = new int[] { last, cur };
//                trim(aJCas.getDocumentText(), span);
//                processSentence(aJCas, aJCas.getDocumentText().substring(span[0], span[1]), span[0]);
//            }
//            last = cur;
//            cur = bi.next();
//        }
    }

    /**
     * Process the sentence to create tokens.
     */
    private void processSentence(JCas aJCas, String text, int zoneBegin)
    {
    	
    	
        BreakIterator bi = BreakIterator.getWordInstance(getLocale(aJCas));
        bi.setText(text);
        int last = bi.first() + zoneBegin;
        int cur = bi.next();
        while (cur != BreakIterator.DONE) {
            cur += zoneBegin;
            Annotation token = createToken(aJCas, last, cur);
            if (token != null) {
                if (splitAtApostrophe) {
                    int i = token.getCoveredText().indexOf("'");
                    if (i > 0) {
                        i += token.getBegin();
                        createToken(aJCas, i, token.getEnd());
                        token.setEnd(i);
                    }
                }
                if (splitAtMinus) {
                    int i = token.getCoveredText().indexOf("-");
                    while (i > 0 && token.getCoveredText().length()>1) {
                        i += token.getBegin();
                        createToken(aJCas, i, i+1);
                        Annotation tmp = createToken(aJCas, i+1, token.getEnd());
                        token.setEnd(i);
                        token = tmp;
                        i = token.getCoveredText().indexOf("-");
                    }
                }
            }

            last = cur;
            cur = bi.next();
        }
    }
}
