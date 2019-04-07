package ru.est412.wordstrainermobile.model;

import java.util.ArrayList;
import java.util.List;

public class DictionaryIterator {
	private Dictionary dict;
	private int curLang; // 0 = foreing, 1 = russian
	private int activeLangs; // 0 = foreing, 1 = russian; 2 = both
	
	//индекс для хранения двух списков - по языкам
	private List<List<Integer>> wordsIndex = new ArrayList<List<Integer>>();
		
	private int curWordIdx;
	private int curWordPos;
	private boolean[] idxEmpty = new boolean[3];
	private boolean langRnd;
	private String[] curWord = new String[2];
	private String[] curExample = new String[2];
	private int[] idxWordsNumber = new int[3];
	private int[] idxWordsCounter = new int[3];
	private boolean showExample;
	public boolean toRepeat;
	private String[] buffer = new String[4];
	private boolean[] repBuffer = new boolean[2];
	public boolean repetition;
	public int showNumber;
	public int showCounter;
	public int showEmpty;
	

	public DictionaryIterator() {
        wordsIndex.add(new ArrayList<Integer>());
        wordsIndex.add(new ArrayList<Integer>());
    }

	public boolean isIdxEmpty(int lang) {
		return idxEmpty[lang];
	}
	
	public boolean isLangRnd() {
		return langRnd;
	}
	
	public String getCurWord(int lang) {
		return curWord[lang];
	}

	public String[] getCurWord() {
		return curWord;
	}
	
	public String getCurExample(int lang) {
		return curExample[lang];
	}
	
	public int getIdxWordsNumber(int lang) {
		return idxWordsNumber[lang];
	}
	
	public int getIdxWordsCounder(int lang) {
		return idxWordsCounter[lang];
	}
	
	public boolean showExampleProperty() {
		return showExample;
	}
	
	public void setDictionary(Dictionary dict) {
		this.dict = dict;
		initIndex();
	}
	
	public void initIndex() {
		idxWordsNumber[0] = dict.getWordsNumber();
		wordsIndex.get(0).clear();
		wordsIndex.get(1).clear();
		//wordsIndex.clear();
				
		//System.out.println("initIndex " + repetition.get() + " " + curLang);
		if (!repetition) {
			for (int i = 0; i < dict.getWordsNumber(); i++) {
				wordsIndex.get(0).add(i);
				wordsIndex.get(1).add(i);
			}
		} else {
			for (int i = 0; i < dict.getWordsNumber(); i++) {
				if (dict.isToRepeat(0, i)) wordsIndex.get(0).add(i);
				if (dict.isToRepeat(1, i)) wordsIndex.get(1).add(i);
			}
		}
		idxWordsNumber[0] = wordsIndex.get(0).size();
		idxWordsNumber[1] = wordsIndex.get(1).size();
		idxWordsCounter[0] = 0;
		idxWordsCounter[1] = 0;
	} // initIndex()

    public boolean isRepetition() {
        return repetition;
    }

    public void setRepetition(boolean repetition) {
        this.repetition = repetition;
        initIndex();
    }

    public void clearCurWord() {
		curWord[0] = "";
		curWord[1] = "";
		hideExamples();
	}
	
	public void hideExamples() {
		curExample[0] = "";
		curExample[1] = "";
	}
	
	private void nextLang() {
		if (activeLangs == 2) switchCurLang();
		else setCurLang(activeLangs);
	}
	
	public void nextWord() {
		clearCurWord();
		nextLang();
		curWordIdx = (int) (Math.random() * wordsIndex.get(curLang).size());
		curWordPos = wordsIndex.get(curLang).get(curWordIdx);
		buffer[0] = dict.getWord(0, curWordPos);
		buffer[1] = dict.getWord(1, curWordPos);
		buffer[2] = dict.getExample(0, curWordPos);
		buffer[3] = dict.getExample(1, curWordPos);
		repBuffer[0] = dict.isToRepeat(0, curWordPos);
		repBuffer[1] = dict.isToRepeat(1, curWordPos);
		curWord[curLang] = buffer[curLang];
		toRepeat = repBuffer[curLang];
		
		wordsIndex.get(curLang).remove(curWordIdx);
		idxWordsCounter[0] = idxWordsNumber[0] - wordsIndex.get(0).size();
		idxWordsCounter[1] = idxWordsNumber[1] - wordsIndex.get(1).size();
	}

	public boolean isToRepeat() {
		return toRepeat;
	}

    public void setToRepeat(boolean toRepeat) {
        this.toRepeat = toRepeat;
        dict.setToRepeat(curLang, curWordPos, toRepeat);
    }

    public void translateCurWord() {
		int lang = (curLang == 0 ? 1 : 0);
		curWord[lang] = buffer[lang];
	}
	
	public void showExample() {
		if (buffer[curLang+2].equals("")) buffer[curLang+2] = "---";
		curExample[curLang] = buffer[curLang+2];
	}
	
	public void showTrExample() {
		int lang = (curLang == 0 ? 1 : 0);
		if (buffer[lang+2].equals("")) buffer[lang+2] = "---";
		curExample[lang] = buffer[lang+2];
	}
	
	public void setActiveLangs(int langs) {
		activeLangs = langs;
	}
	
	public void setCurLang(int lang) {
		curLang = lang;
	}

	public int getCurLang() {
		return curLang;
	}

	public void switchCurLang() {
		if (isLangRnd()) 
			do {
				curLang = (int) (Math.random() * 2); 
			} while (isIdxEmpty(curLang));
		else {
			curLang = (curLang == 0 ? 1 : 0);
			if (isIdxEmpty(curLang)) curLang = (curLang == 0 ? 1 : 0);
		}
	}

	public boolean isWordsRemain() {
        return wordsIndex.get(curLang).size() > 0;
    }

} // class
