package edu.byui.cs246.snielson.threadedlinereader;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

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
    this.setProgressBar(this, 0);
  }

  public void createFile(final View view) {
    final File file = new File(view.getContext().getFilesDir(), NUMBERS_FILENAME);
    final FileManager ptr = this;
    new Thread(new Runnable() {
      @Override
      public void run() {
        try (PrintWriter out = new PrintWriter(new FileWriter(file))) {
          for (int i = 1; i <= 10; i++) {
            out.println(i);
            ptr.updateProgressBar(i, view);
            try {
              Thread.sleep(250);
            } catch (InterruptedException ex) {
              Log.wtf(FILE_MANAGER_FQN, ex);
            }
          }
          // once it's done reset the counter
          ptr.updateProgressBar(0, view);
        } catch (IOException ex) {
          Log.wtf(FILE_MANAGER_FQN, ex);
        }
      }
    }).start();
  }

  public void loadFile(final View view) {
    final File file = new File(view.getContext().getFilesDir(), NUMBERS_FILENAME);
    final FileManager ptr = this;
    new Thread(new Runnable() {
      @Override
      public void run() {
        if (!file.exists()) {
          Log.wtf(FILE_MANAGER_FQN, "File should exist before file is read");
          return;
        }
        final ArrayAdapter<String> numbers = new ArrayAdapter<>(ptr, android.R.layout.simple_list_item_1);
        try {
          int counter = 1;
          LineIterator iterator = FileUtils.lineIterator(file);
          while (iterator.hasNext()) {
            numbers.add(iterator.nextLine());
            try {
              ptr.updateProgressBar(counter++, view);
              Thread.sleep(250);
            } catch (InterruptedException ex) {
              Log.wtf(FILE_MANAGER_FQN, ex);
            }
          }
        } catch (IOException ex) {
          Log.wtf(FILE_MANAGER_FQN, "Could not read lines from file", ex);
          ex.printStackTrace();
        }
        // now that we finished... let's update the progress bar
        ptr.updateProgressBar(0, view);

        // now update with our list
        view.post(new Runnable() {
          @Override
          public void run() {
            ListView listView = (ListView) ptr.findViewById(R.id.listView);
            if (listView == null) {
              throw new RuntimeException("Could not find listview");
            }
            listView.setAdapter(numbers);
          }
        });
      }
    }).start();
  }

  private void updateProgressBar(final int currentProgress, View view) {
    final FileManager ptr = this;
    view.post(new Runnable() {
      @Override
      public void run() {
        ptr.setProgressBar(ptr, currentProgress);
      }
    });
  }

  private void setProgressBar(FileManager currentContext, int currentProgress) {
    ProgressBar bar = (ProgressBar) currentContext.findViewById(R.id.progressBar);
    if (bar == null) {
      throw new RuntimeException("Could not find progress bar by id");
    }
    bar.setProgress(currentProgress);
  }

  public void clearView(View v) {
    ListView listView = (ListView) this.findViewById(R.id.listView);
    if (listView != null && listView.getAdapter() != null
        && listView.getAdapter() instanceof ArrayAdapter) {
      ArrayAdapter array = (ArrayAdapter) listView.getAdapter();
      array.clear();
    }
  }
}