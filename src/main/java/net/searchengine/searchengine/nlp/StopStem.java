package net.searchengine.searchengine.nlp;


import net.searchengine.searchengine.nlp.IRUtilities.Porter;

import java.io.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;

public class StopStem
{
	private static final String STOPWORD_RESOURCE = "stopwords.txt";

	private final Porter porter;
	private final HashSet<String> stopWords;

	public StopStem() {
		porter = new Porter();
		stopWords = new HashSet<>();

		try {
			BufferedReader in = new BufferedReader(new FileReader(this.getClass().getClassLoader().getResource(STOPWORD_RESOURCE).getPath()));
			String word;
			while((word = in.readLine()) != null) {
				stopWords.add(word);
			}
		}
		catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		}
		catch (IOException e) {
			System.out.println(e.getMessage());
		}

	}

	public boolean isStopWord(String str) {
		return stopWords.contains(str);
	}

	public String stem(String str) {
		return porter.stripAffixes(str);
	}

}
