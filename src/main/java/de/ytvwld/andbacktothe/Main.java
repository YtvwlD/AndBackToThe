package de.ytvwld.andbacktothe;

import android.app.Activity;
import android.app.backup.BackupManager;
import android.content.Context;
import android.os.Bundle;
import java.lang.reflect.InvocationTargetException;

public class Main extends Activity
{
    private Context context = this;
    private BackupManager backupManager;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        backupManager = new BackupManager(context);
        try
        {
          boolean backupEnabled = (boolean)(backupManager.getClass().getMethod("isBackupEnabled").invoke(backupManager));
          String currentTransport = (String)(backupManager.getClass().getMethod("getCurrentTransport").invoke(backupManager));
          System.err.println("Backup enabled: " + backupEnabled);
          System.err.println("Transport: " + currentTransport);
        }
        catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e)
        {
          System.err.println("Must be a system app.");
        }
    }
}
