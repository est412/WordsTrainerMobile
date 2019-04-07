package ru.est412.wordstrainermobile;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import ru.est412.wordstrainermobile.model.Dictionary;
import ru.est412.wordstrainermobile.model.DictionaryIterator;
import ru.est412.wordstrainermobile.model.XLSXPoiDictionary;

public class MainActivity extends AppCompatActivity {

    private static final int EDIT_REQUEST_CODE = 44;

    private Dictionary dict;
    private Uri uri;
    private DictionaryIterator dictIterator;
    private int toShow;

    TextView[] tvLang;
    TextView tvCount;
    TextView tvTotal;
    TextView tvURI;
    Button btnNext;
    CheckBox cbNativeFirst;
    CheckBox cbRepeat;
    CheckBox cbReprtition;
    Menu menu;

    public static final String PREF_LAST_FILE = "lastFile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl");

        setContentView(R.layout.activity_main);

        dictIterator = new DictionaryIterator();
        tvLang = new TextView[]{findViewById(R.id.tvLang0), findViewById(R.id.tvLang1)};
        tvCount = findViewById(R.id.tvCount);
        tvTotal = findViewById(R.id.tvTotal);
        tvURI = findViewById(R.id.tvURI);
        btnNext = findViewById(R.id.btnNext);
        cbNativeFirst = findViewById(R.id.cbNativeFirst);
        cbRepeat = findViewById(R.id.cbRepeat);
        cbReprtition = findViewById(R.id.cbRepetition);

        SharedPreferences sPref = getPreferences(MODE_PRIVATE);
        String lastFile = sPref.getString(PREF_LAST_FILE, "");
        if (!lastFile.isEmpty()) {
            tvURI.setText("Last: " + lastFile);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        this.menu = menu;
        return true;
    }

    public void onMenuFile(MenuItem item) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        startActivityForResult(intent, EDIT_REQUEST_CODE);
    }

    public void onMenuRestart(MenuItem item) {
        dictIterator.setDictionary(dict);
        restart();
    }

    @Override
    public void onDestroy() {
        if (uri != null) {
            try {
                dict.close(getContentResolver().openOutputStream(uri));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }

    @Override
    public void onPause() {
        if (uri != null) {
            try {
                dict.save(getContentResolver().openOutputStream(uri));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != EDIT_REQUEST_CODE || resultCode != Activity.RESULT_OK || data == null) {
            return;
        }
        Uri oldUri = uri;
        Dictionary oldDict = dict;
        uri = data.getData();
        try {
            dict = new XLSXPoiDictionary(getContentResolver().openInputStream(uri));
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }
        if (oldUri != null) {
            try {
                oldDict.close(getContentResolver().openOutputStream(oldUri));
            } catch (IOException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        String path = "File name unknown";
        try {
            path = getFileName(uri);
        } catch (NullPointerException ignored) {
            // do nothing
        }
        tvURI.setText(path);
        dictIterator.setDictionary(dict);
        restart();

        SharedPreferences sPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(PREF_LAST_FILE, path);
        ed.apply();

        menu.findItem(R.id.restart).setEnabled(true);
    }

    public String getFileName(Uri uri) {
        String result = null;
        // https://stackoverflow.com/questions/44735310/get-filename-from-google-drive-uri
        result = DocumentFile.fromSingleUri(this, uri).getName();
        if (result != null) return result;

        // https://stackoverflow.com/questions/5568874/how-to-extract-the-file-name-from-uri-returned-from-intent-action-get-content/25005243#25005243
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void restart() {
        dictIterator.clearCurWord();
        toShow = 1;
        btnNext.setEnabled(true);
        cbNativeFirst.setEnabled(true);
        btnNext.setText("Go !");
        tvLang[0].setText("");
        tvLang[1].setText("");
        tvCount.setText("" + dictIterator.getIdxWordsCounder(dictIterator.getCurLang()));
        tvTotal.setText("" + dictIterator.getIdxWordsNumber(dictIterator.getCurLang()));
        cbRepeat.setChecked(false);
        cbRepeat.setEnabled(false);
        cbReprtition.setEnabled(true);
        //dictIterator.setCurLang(cbNativeFirst.isChecked() ? 1 : 0);
    }

    public void onCbNativeFirst(View view) {
        dictIterator.setActiveLangs(cbNativeFirst.isChecked() ? 1 : 0);
        tvCount.setText("" + dictIterator.getIdxWordsCounder(dictIterator.getCurLang()));
        tvTotal.setText("" + dictIterator.getIdxWordsNumber(dictIterator.getCurLang()));
        float tmp0 = tvLang[0].getTextSize();
        float tmp1 = tvLang[1].getTextSize();
        tvLang[0].setTextSize(TypedValue.COMPLEX_UNIT_PX, tmp1);
        tvLang[1].setTextSize(TypedValue.COMPLEX_UNIT_PX, tmp0);
    }

    public void onCbRepeat(View view) {
        dictIterator.setToRepeat(cbRepeat.isChecked());
    }

    public void onCbRepetition(View  view) {
        dictIterator.setRepetition(cbReprtition.isChecked());
        tvCount.setText("" + dictIterator.getIdxWordsCounder(dictIterator.getCurLang()));
        tvTotal.setText("" + dictIterator.getIdxWordsNumber(dictIterator.getCurLang()));
    }

    public void onBtnNext(View view) {
        cbNativeFirst.setEnabled(false);
        cbRepeat.setEnabled(true);
        cbReprtition.setEnabled(false);
        if (toShow == 1) {
            //buttonNext.disableProperty().unbind();
            if (!dictIterator.isWordsRemain()) {
                btnNext.setEnabled(false);
                return;
            }
            dictIterator.nextWord();
            if (!cbNativeFirst.isChecked()) {
                tvLang[0].setText(dictIterator.getCurWord()[0]);
                tvLang[1].setText(dictIterator.getCurWord()[1]);
            } else {
                tvLang[0].setText(dictIterator.getCurWord()[1]);
                tvLang[1].setText(dictIterator.getCurWord()[0]);
            }
            tvCount.setText("" + dictIterator.getIdxWordsCounder(dictIterator.getCurLang()));
            tvTotal.setText("" + dictIterator.getIdxWordsNumber(dictIterator.getCurLang()));
            cbRepeat.setChecked(dictIterator.isToRepeat());
            //hboxLang.setDisable(true);
//            if (checkboxExample.isSelected()) toShow = 2;
//            else toShow = 3;
            toShow = 3;
        }
//        else if (toShow == 2 && checkboxExample.isSelected()) {
//            dictIterator.showExample();
//            toShow = 3;
//        }
        else if (toShow == 3) {
            dictIterator.translateCurWord();
            if (!cbNativeFirst.isChecked()) {
                tvLang[0].setText(dictIterator.getCurWord()[0]);
                tvLang[1].setText(dictIterator.getCurWord()[1]);
            } else {
                tvLang[0].setText(dictIterator.getCurWord()[1]);
                tvLang[1].setText(dictIterator.getCurWord()[0]);
            }
            if (!dictIterator.isWordsRemain()) {
                btnNext.setEnabled(false);
                return;
            }
            //hboxLang.setDisable(false);
//            if (checkboxExample.isSelected()) toShow = 4;
//            else {
//                toShow = 1;
//                buttonNext.disableProperty().bind(dictIterator.showEmpty);
//            }
            toShow = 1;
        }
//        else if (toShow == 4 && checkboxExample.isSelected()) {
//            dictIterator.showTrExample();
//            toShow = 1;
//            shown = 4;
//            buttonNext.disableProperty().bind(dictIterator.showEmpty);
//        }
        btnNext.setText("Next");
    }

}
