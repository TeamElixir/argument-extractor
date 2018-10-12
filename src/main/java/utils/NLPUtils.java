package utils;

import java.util.Properties;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.pipeline.StanfordCoreNLPClient;

public class NLPUtils {

	private AnnotationPipeline pipeline;

	public NLPUtils(String annotatorList) {
		Properties props = new Properties();
		props.setProperty("annotators", annotatorList);
		this.pipeline = new StanfordCoreNLP(props);
	}

	public NLPUtils(Properties properties) {
		this.pipeline = new StanfordCoreNLP(properties);
	}

	public NLPUtils(Properties properties, String host, int port, int threads) {
		this.pipeline = new StanfordCoreNLPClient(properties, host, port, threads);
	}

	public Annotation annotate(String text) {
		Annotation annotation = new Annotation(text);
		pipeline.annotate(annotation);

		return annotation;
	}

	public AnnotationPipeline getPipeline() {
		return pipeline;
	}
}
