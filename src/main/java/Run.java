import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;

import models.Sentence;
import org.slf4j.Logger;

public class Run {

	private static final Logger logger = getLogger(Run.class);

	public static void main(String[] args) throws Exception {

		ArrayList<Sentence> sentences = Sentence.getAll();

		logger.info(sentences.size() + " sentences fetch from the database.");

		int count = 0;
		for (Sentence sentence: sentences) {
			if(sentence.getText().contains("argu") ||
					sentence.getText().contains("contest") ||
					sentence.getText().contains("agree")){
				count++;
				System.out.println(sentence.getText());
				System.out.println();
			}
		}
		System.out.println(count);

	}

}
