package ru.est412.wordstrainermobile.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Dictionary {
	void open(String fileName) throws IOException;
	void open(InputStream is) throws IOException;
	void save() throws IOException;
    void save(OutputStream os) throws IOException;
	void close() throws IOException;
    void close(OutputStream os) throws IOException;
	int getWordsNumber();
	String getWord(int lang, int count);
	boolean isToRepeat(int lang, int count);
	void setToRepeat(int lang, int count, boolean is);
	String getExample(int lang, int count);
}