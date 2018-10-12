package models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import utils.SQLiteUtils;

public class Sentence {

	public static final String TABLE_NAME = "LEGAL_SENTENCE";

	private int dbId;

	private String text;

	private String legalCase;

	private static SQLiteUtils sqLiteUtils;

	static {
		try {
			sqLiteUtils = new SQLiteUtils();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public int getDbId() {
		return dbId;
	}

	public void setDbId(int dbId) {
		this.dbId = dbId;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getLegalCase() {
		return legalCase;
	}

	public void setLegalCase(String legalCase) {
		this.legalCase = legalCase;
	}

	public static ArrayList<Sentence> getAll() throws Exception {
		String sql = "SELECT * FROM " + Sentence.TABLE_NAME + ";";
		ResultSet resultSet = sqLiteUtils.executeQuery(sql);

		if (resultSet.isClosed()) {
			return null;
		}

		ArrayList<Sentence> sentences = new ArrayList<>();

		while (resultSet.next()) {
			Sentence sentence = new Sentence();
			sentence.setDbId(resultSet.getInt("ID"));
			sentence.setLegalCase(resultSet.getString("CASE_FILE"));
			sentence.setText(resultSet.getString("SENTENCE"));

			sentences.add(sentence);
		}

		return sentences;
	}
}
