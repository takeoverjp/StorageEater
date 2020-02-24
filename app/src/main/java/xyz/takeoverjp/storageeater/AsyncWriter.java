package xyz.takeoverjp.storageeater;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.storage.StorageManager;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class AsyncWriter extends AsyncTask<Integer, Integer, Integer> {
    private File dir;
    private StorageManager sm;
    private ProgressBar progressBar;
    private ProgressBar loadingCircle;
    private TextView freeSpaceView;
    private TextView usableSpaceView;
    private TextView allocatableBytesView;
    private TextView startButtonView;
    private Timer mTimer = null;
    private Handler mHandler = new Handler();

    public AsyncWriter(StorageManager sm, File dir, ProgressBar progressBar, ProgressBar loadingCircle,
                       TextView freeSpaceView, TextView usableSpaceView, TextView allocatableBytesView,
                       TextView startButtonView) {
        super();
        this.sm = sm;
        this.dir = dir;
        this.progressBar = progressBar;
        this.loadingCircle = loadingCircle;
        this.freeSpaceView = freeSpaceView;
        this.usableSpaceView = usableSpaceView;
        this.allocatableBytesView = allocatableBytesView;
        this.startButtonView = startButtonView;
    }

    private void updateView () {
        long total = dir.getTotalSpace();
        long free = dir.getFreeSpace();
        int ratio = (int)((total - free) * 100 / total);
        long usable = dir.getUsableSpace();
        freeSpaceView.setText(String.format("%,d byte", free));
        usableSpaceView.setText(String.format("%,d byte", usable));
        progressBar.setProgress(ratio, true);
        try {
            allocatableBytesView.setText(String.format("%,d byte", sm.getAllocatableBytes(sm.UUID_DEFAULT)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPreExecute() {
        startButtonView.setText("CANCEL EAT");
        loadingCircle.setVisibility(View.VISIBLE);

        mTimer = new Timer(true);
        mTimer.schedule(new TimerTask(){
            @Override
            public void run() {
                mHandler.post( new Runnable() {
                    @Override
                    public void run() {
                        updateView();
                    }
                });
            }
        }, 1000, 1000);
    }

    @Override
    protected Integer doInBackground(Integer... value) {
        long free = dir.getFreeSpace();
        long write_size = free * 120 / 1024 / 100;

        try {
            FileOutputStream file = new FileOutputStream(dir + "/garbage", true);
            try {
                byte[] bytes = new byte[Math.toIntExact(write_size)];
                for (int i = 0; i < 100; i++) {
                    for (int j = 0; j < 1024; j++) {
                        file.write(bytes);
                        if (isCancelled()) {
                            file.close();
                            return null;
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                file.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Integer result) {
        mTimer.cancel();
        updateView();
        loadingCircle.setVisibility(View.INVISIBLE);
        startButtonView.setText("START EAT");
    }

    @Override
    protected void onCancelled() {
        mTimer.cancel();
        updateView();
        loadingCircle.setVisibility(View.INVISIBLE);
        startButtonView.setText("START EAT");
    }
}
