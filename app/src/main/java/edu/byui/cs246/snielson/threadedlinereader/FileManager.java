package edu.byui.cs246.snielson.threadedlinereader;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class FileManager extends AppCompatActivity {

  public static final String NUMBERS_FILENAME = "numbers.txt";
  public static final String FILE_MANAGER_FQN = "edu.byui.cs246.snielson.threadedlinereader.FileManager";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_file_manager);
  }

  public void createFile(View view) {
    File file = new File(view.getContext().getFilesDir(), NUMBERS_FILENAME);

    try (PrintWriter out = new PrintWriter(new FileWriter(file))) {
      for (int i = 0; i < 10; i++) {
        out.println(i + 1);
        try {
          Thread.sleep(250);
        }
        catch (InterruptedException ex) {
          Log.wtf(FILE_MANAGER_FQN, ex);
        }
      }
    }
    catch (IOException ex) {
      Log.wtf(FILE_MANAGER_FQN, ex);
    }
  }

  public void loadFile(View view) {
    File file = new File(view.getContext().getFilesDir(), NUMBERS_FILENAME);
    if (!file.exists()) {
      Log.wtf(FILE_MANAGER_FQN, "File should exist before file is read");
      return;
    }
    ArrayAdapter<String> numbers = new ArrayAdapter<>(this,
        android.R.layout.simple_list_item_1);
    try {
      LineIterator iterator = FileUtils.lineIterator(file);
      while (iterator.hasNext()) {
        numbers.add(iterator.nextLine());
        try {
          Thread.sleep(250);
        }
        catch (InterruptedException ex) {
          Log.wtf(FILE_MANAGER_FQN, ex);
        }
      }
    }
    catch (IOException ex) {
      Log.wtf(FILE_MANAGER_FQN, "Could not read lines from file", ex);
      ex.printStackTrace();
    }

    ListView listView = (ListView)this.findViewById(R.id.listView);
    if (listView == null) {
      throw new RuntimeException("Could not find listview");
    }
    listView.setAdapter(numbers);
  }

  public void clearView(View v) {
    ListView listView = (ListView)this.findViewById(R.id.listView);
    if (listView != null && listView.getAdapter() != null
        && listView.getAdapter() instanceof ArrayAdapter) {
      ArrayAdapter array = (ArrayAdapter)listView.getAdapter();
      array.clear();
    }
  }
}