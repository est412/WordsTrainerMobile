package ru.est412.wordstrainermobile.model;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class XLSXPoiDictionary implements Dictionary {
	protected int wordsNum; // количество слов в словаре = к-во строк в файле
	private XSSFWorkbook wb; //таблица
	private XSSFSheet ws; //вкладка словаря
	private XSSFSheet ws1; //вкладка с метками повторения
	String fileName; //имя файла

	//словарь существует только в свЯзи с открытым файлом
	public XLSXPoiDictionary(String fileName) throws IOException {
		this.fileName = fileName;
		open(fileName);
	}

    public XLSXPoiDictionary(InputStream is) throws IOException {
        open(is);
    }
	@Override
	public void open(InputStream is) throws IOException {
		try (InputStream fis = is) {
			wb = new XSSFWorkbook(fis);
			if (wb.getNumberOfSheets() < 1) {
				wb.createSheet();
			}
			ws = wb.getSheetAt(0);
			setWordsNum();
			if (wb.getNumberOfSheets() < 2) {
				wb.createSheet();
			}
			ws1 = wb.getSheetAt(1);
		}
	}

    private void setWordsNum() {
        int lastRowCandidate = ws.getLastRowNum();
        XSSFCell cell = ws.getRow(lastRowCandidate).getCell(0);
        if (cell != null && !cell.toString().trim().isEmpty()) {
            wordsNum = lastRowCandidate + 1;
            return;
        }
        for (wordsNum = 0; wordsNum <= lastRowCandidate; wordsNum++) {
            cell = ws.getRow(wordsNum).getCell(0);
            if (cell == null || cell.toString().trim().isEmpty()) {
                break;
            }
        }
    }

    @Override
    public void open(String fileName) throws IOException {
	    open(new FileInputStream(new File(fileName)));
    }

	@Override
	public void save() throws IOException {
		try (FileOutputStream fos = new FileOutputStream(fileName)) {
			wb.write(fos);
		}
	}

    @Override
    public void save(OutputStream os) throws IOException {
	    try (OutputStream fos = os) {
	        wb.write(fos);
        }
    };

	@Override
    public void close() throws IOException {
        save();
        if (wb != null) wb.close();
    }

    @Override
    public void close(OutputStream os) throws IOException {
        save(os);
        if (wb != null) wb.close();
    }

	@Override
	public int getWordsNumber() {
		return wordsNum;
	}

	//выдает нужное слово нужного языка
	@Override
	public String getWord(int lang, int count) {
		XSSFRow row = ws.getRow(count);
		if (row == null) return "";
		XSSFCell cell = row.getCell(lang);
		if (cell == null) return "";
		return cell.toString();
	}

	//проверяет наличие метки повтора
	@Override
	public boolean isToRepeat(int lang, int count) {
		XSSFRow row = ws1.getRow(count);
		if (row == null) return false;
		XSSFCell cell = row.getCell(lang);
		if (cell == null) return false;
		if ("".equals(cell.toString().trim())) return false;
		return true;
	}

	//устанавливает/или очищает метку повтора и сохраняет файл
	@Override
	public void setToRepeat(int lang, int count, boolean is) {
		XSSFRow row = ws1.getRow(count);
		if (row == null) {
			row = ws1.createRow(count);
		}
		// некрасиво, но уж как есть
		XSSFCell cell = row.getCell(lang);
		if (cell == null) {
			cell = row.createCell(lang);
		}
		cell.setCellValue(is ? "1" : "");
	}

	//парсит ячейку с примерами и выдает пример нужного языка
	@Override
	public String getExample(int lang, int count) {
		XSSFRow row = ws.getRow(count);
		if (row == null) return "";
		XSSFCell cell = row.getCell(2);
		if (cell == null) return "";
		String str = cell.toString();
		if ("".equals(str)) return "";
		String[] str1 = str.split("\n"); // разделитель между примерами
		String[] str2;
		str = "";
		for (int i = 0; i < str1.length; i++ ) {
			str2 = str1[i].split(" — "); // разделитель между языками
			str = str + (i+1) + ": " + str2[lang] + "\n";
		}
		return str;
	}
}
