package edu.byui.cs246.snielson.threadedlinereader;

import android.os.AsyncTask;
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
import java.util.ArrayList;
import java.util.List;

/**
 * File manager API for creating, reading, and displaying a list of numbers.
 */
public class FileManager extends AppCompatActivity {

  /** The name of the file to save our list of 
    * numbers to and read them from. 
  **/
  public static final String NUMBERS_FILENAME = "numbers.txt";
  /**
   * Name to use for reporting exceptions to the debug log.
   */
  public static final String FILE_MANAGER_FQN 
      = "edu.byui.cs246.snielson.threadedlinereader.FileManager";
  /**
   * The highest number to be written out (from 1 to this number) 
   * in our files and read from them.
   */
  public static final int MAX_NUMBERS = 10;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_file_manager);
    setProgressBarMaxProgress(MAX_NUMBERS);
    setProgressBar(0);
  }

  /** Button action to clear the list if there is one.
    * @param view the button that was clicked on
    **/
  public void clearView(View view) {
    ListView listView = (ListView) this.findViewById(R.id.listView);
    if (listView != null && listView.getAdapter() != null
        && listView.getAdapter() instanceof ArrayAdapter) {
      ArrayAdapter array = (ArrayAdapter) listView.getAdapter();
      array.clear();
    }
  }

  /** Button action to create a file on the android 
    * filesystem with a list of numbers.
    * @param view the button that was clicked on
    **/
  public void createFile(final View view) {
    final File file = new File(view.getContext().getFilesDir(), NUMBERS_FILENAME);
    CreateFileAsyncTask task = new CreateFileAsyncTask();
    task.execute(file);
  }

  /** Button action to load a list of items on the gui screen from the filesystem.
    * @param view the button that was clicked on
    **/
  public void loadFile(final View view) {
    final File file = new File(view.getContext().getFilesDir(), NUMBERS_FILENAME);
    LoadFileAsyncTask task = new LoadFileAsyncTask();
    task.execute(file);
  }


  /**
   * Loads a file and returns that file as a list of strings with one string per line.
   * The task also gives status updates as it progresses.
   */
  private class LoadFileAsyncTask extends AsyncTask<File, Integer, List<String>> {
    @Override
    protected List<String> doInBackground(File... params) {
      List<String> numbers = new ArrayList<>();
      try {
        int counter = 1;
        LineIterator iterator = FileUtils.lineIterator(params[0]);
        while (iterator.hasNext()) {
          numbers.add(iterator.nextLine());
          try {
            publishProgress(counter++);
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
      publishProgress(0);
      return numbers;
    }

    @Override
    protected void onPostExecute(List<String> readLines) {
      super.onPostExecute(readLines);

      if (isFinishing()) {
        return; // do nothing if the view is terminated / being terminated.
      }
      setListViewData(readLines);
    }



    @Override
    protected void onProgressUpdate(Integer... values) {
      super.onProgressUpdate(values);

      setProgressBar(values[0]);
    }
  }

  /**
   * Creates a file at the given location with a list of numbers with 1 number per line.
   * The task also gives status updates as it progresses.
   */
  private class CreateFileAsyncTask extends AsyncTask<File, Integer, Void> {
    @Override
    protected Void doInBackground(File... params) {
      File file = params[0];
      try (PrintWriter out = new PrintWriter(new FileWriter(file))) {
        for (int i = 1; i <= MAX_NUMBERS; i++) {
          out.println(i);
          publishProgress(i);
          try {
            Thread.sleep(250);
          } catch (InterruptedException ex) {
            Log.wtf(FILE_MANAGER_FQN, ex);
          }
        }
        publishProgress(0);
      } catch (IOException ex) {
        Log.wtf(FILE_MANAGER_FQN, ex);
      }
      return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
      super.onProgressUpdate(values);
      // avoid memory leaks and things being null as they are destroyed
      // @see http://stackoverflow.com/questions/3033007/android-memory-leak-due-to-asynctask
      if (isFinishing()) {
        return;
      }

      ProgressBar bar = (ProgressBar) findViewById(R.id.progressBar);
      if (bar == null) {
        throw new RuntimeException("Could not find progress bar by id");
      }
      bar.setProgress(values[0]);
    }
  }

  /**
   * Given a list of strings it displays that list of strings in our list view
   * @param data The list of strings to display on the screen in the list view.
   */
  private void setListViewData(List<String> data) {
    ArrayAdapter<String> numbers 
        = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, data);
    ListView listView = (ListView) findViewById(R.id.listView);
    if (listView == null) {
      throw new RuntimeException("Could not find listview");
    }
    listView.setAdapter(numbers);
  }

  /**
   * Sets the maximum value the progress bar can be.
   * @param maxProgress The maximum integer value that the progress bar can progress to.
   */
  private void setProgressBarMaxProgress(int maxProgress) {
    ProgressBar bar = (ProgressBar)findViewById(R.id.progressBar);
    if (bar == null) {
      throw new RuntimeException("Could not find progress bar by id");
    }
    bar.setMax(maxProgress);
  }

  /**
   * Sets the current progress value of the progress bar.
   * @param currentProgress The current value of the progress bar.
   */
  private void setProgressBar(int currentProgress) {
    ProgressBar bar = (ProgressBar)findViewById(R.id.progressBar);
    if (bar == null) {
      throw new RuntimeException("Could not find progress bar by id");
    }
    bar.setProgress(currentProgress);
  }
}
