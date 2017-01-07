/*
 * This is partly Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * see https://github.com/android/platform_frameworks_base/blob/master/core/java/com/android/internal/backup/LocalTransport.java
 */

package de.ytvwld.andbacktothe;

import android.app.backup.BackupTransport;
import android.app.backup.RestoreDescription;
import android.app.backup.RestoreSet;
import android.content.Context;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.ParcelFileDescriptor;
import android.system.ErrnoException;
import android.system.Os;
import android.system.StructStat;
import android.util.Log;
import java.io.IOException;

/**
 * Null backup transport.
 */

public class NullTransport extends BackupTransport {
    private static final String TAG = "NullTransport";
    private static final boolean DEBUG = true;
    private static final String TRANSPORT_DIR_NAME = "de.ytvwld.andbacktothe.NullTransport";
    private static final String TRANSPORT_DESTINATION_STRING = "Backing up to nowhere.";
    private static final String TRANSPORT_DATA_MANAGEMENT_LABEL = "";
    private static final long FULL_BACKUP_SIZE_QUOTA = 25 * 1024 * 1024;

    private Context context;
    private String fullTargetPackage;
    private ParcelFileDescriptor socket;
    private long fullBackupSize;
    private PackageInfo[] restorePackages = null;
    private int restorePackage = -1; // Index into restorePackages

    public NullTransport(Context context) {
        this.context = context;
    }

    @Override
    public String name() {
        return new ComponentName(context, this.getClass()).flattenToShortString();
    }

    @Override
    public Intent configurationIntent() {
        // The null transport is not user-configurable
        return null;
    }

    @Override
    public String currentDestinationString() {
        return TRANSPORT_DESTINATION_STRING;
    }

    public Intent dataManagementIntent() {
        // The null transport does not present a data-management UI
        return null;
    }

    public String dataManagementLabel() {
        return TRANSPORT_DATA_MANAGEMENT_LABEL;
    }

    @Override
    public String transportDirName() {
        return TRANSPORT_DIR_NAME;
    }

    @Override
    public long requestBackupTime() {
        // any time is a good time for no backup
        return 0;
    }

    @Override
    public int initializeDevice() {
        if (DEBUG) Log.v(TAG, "doing nothing");
        return TRANSPORT_OK;
    }

    @Override
    public int performBackup(PackageInfo packageInfo, ParcelFileDescriptor data) {
        if (DEBUG) {
            try {
            StructStat ss = Os.fstat(data.getFileDescriptor());
            Log.v(TAG, "performBackup() pkg=" + packageInfo.packageName
                    + " size=" + ss.st_size);
            } catch (ErrnoException e) {
                Log.w(TAG, "Unable to stat input file in performBackup() on "
                        + packageInfo.packageName);
            }
        }

      //nothing to to here
      return TRANSPORT_OK;
    }

    @Override
    public int clearBackupData(PackageInfo packageInfo) {
        if (DEBUG) Log.v(TAG, "clearBackupData() pkg=" + packageInfo.packageName);
        return TRANSPORT_OK;
    }

    @Override
    public int finishBackup() {
        if (DEBUG) Log.v(TAG, "finishBackup() of " + fullTargetPackage);
        return TRANSPORT_OK;
    }

    @Override
    public long requestFullBackupTime() {
        return 0;
    }

    @Override
    public int checkFullBackupSize(long size) {
        int result = TRANSPORT_OK;
        // Decline zero-size "backups"
        if (size <= 0) {
            result = TRANSPORT_PACKAGE_REJECTED;
        } else if (size > FULL_BACKUP_SIZE_QUOTA) {
            result = TRANSPORT_QUOTA_EXCEEDED;
        }
        if (result != TRANSPORT_OK) {
            if (DEBUG) {
                Log.v(TAG, "Declining backup of size " + size);
            }
        }
        return result;
    }

    @Override
    public int performFullBackup(PackageInfo targetPackage, ParcelFileDescriptor socket) {
        if (socket != null) {
            Log.e(TAG, "Attempt to initiate full backup while one is in progress");
            return TRANSPORT_ERROR;
        }

        if (DEBUG) {
            Log.i(TAG, "performFullBackup : " + targetPackage);
        }

        return TRANSPORT_OK;
    }

    @Override
    public int sendBackupData(final int numBytes) {
        if (socket == null) {
            Log.w(TAG, "Attempted sendBackupData before performFullBackup");
            return TRANSPORT_ERROR;
        }

        fullBackupSize += numBytes;
        if (fullBackupSize > FULL_BACKUP_SIZE_QUOTA) {
            return TRANSPORT_QUOTA_EXCEEDED;
        }
        return TRANSPORT_OK;
    }

    // For now we can't roll back, so just tear everything down.
    @Override
    public void cancelFullBackup() {
        if (DEBUG) {
            Log.i(TAG, "Canceling full backup of " + fullTargetPackage);
        }
    }

    @Override
    public RestoreSet[] getAvailableRestoreSets() {
        int num = 0;

        RestoreSet[] available = new RestoreSet[num];
        for (int i = 0; i < available.length; i++) {
            available[i] = new RestoreSet("Local disk image", "flash", 0);
        }
        return available;
    }

    @Override
    public long getCurrentRestoreSet() {
        return 1;
    }

    @Override
    public int startRestore(long token, PackageInfo[] packages) {
        if (DEBUG) Log.v(TAG, "start restore " + token + " : " + packages.length
                + " matching packages");
        restorePackages = packages;
        restorePackage = -1;
        return TRANSPORT_OK;
    }

    @Override
    public RestoreDescription nextRestorePackage() {
        if (DEBUG) {
            Log.v(TAG, "nextRestorePackage() : restorePackage=" + restorePackage
                    + " length=" + restorePackages.length);
        }
        if (DEBUG) Log.v(TAG, "  no more packages to restore");
        return RestoreDescription.NO_MORE_PACKAGES;
    }

    @Override
    public int getRestoreData(ParcelFileDescriptor outFd) {
        return TRANSPORT_OK;
    }

    @Override
    public void finishRestore() {
        if (DEBUG) Log.v(TAG, "finishRestore()");
    }

    /**
     * Ask the transport to provide data for the "current" package being restored.  The
     * transport then writes some data to the socket supplied to this call, and returns
     * the number of bytes written.  The system will then read that many bytes and
     * stream them to the application's agent for restore, then will call this method again
     * to receive the next chunk of the archive.  This sequence will be repeated until the
     * transport returns zero indicating that all of the package's data has been delivered
     * (or returns a negative value indicating some sort of hard error condition at the
     * transport level).
     *
     * <p>After this method returns zero, the system will then call
     * {@link #getNextFullRestorePackage()} to begin the restore process for the next
     * application, and the sequence begins again.
     *
     * @param socket The file descriptor that the transport will use for delivering the
     *    streamed archive.
     * @return 0 when no more data for the current package is available.  A positive value
     *    indicates the presence of that much data to be delivered to the app.  A negative
     *    return value is treated as equivalent to {@link BackupTransport#TRANSPORT_ERROR},
     *    indicating a fatal error condition that precludes further restore operations
     *    on the current dataset.
     */
    @Override
    public int getNextFullRestoreDataChunk(ParcelFileDescriptor socket) {
        return 0;
    }

    /**
     * If the OS encounters an error while processing {@link RestoreDescription#TYPE_FULL_STREAM}
     * data for restore, it will invoke this method to tell the transport that it should
     * abandon the data download for the current package.  The OS will then either call
     * {@link #nextRestorePackage()} again to move on to restoring the next package in the
     * set being iterated over, or will call {@link #finishRestore()} to shut down the restore
     * operation.
     *
     * @return {@link #TRANSPORT_OK} if the transport was successful in shutting down the
     *    current stream cleanly, or {@link #TRANSPORT_ERROR} to indicate a serious
     *    transport-level failure.  If the transport reports an error here, the entire restore
     *    operation will immediately be finished with no further attempts to restore app data.
     */
    @Override
    public int abortFullRestore() {
        return TRANSPORT_OK;
    }

    @Override
    public long getBackupQuota(String packageName, boolean isFullBackup) {
      return isFullBackup ? FULL_BACKUP_SIZE_QUOTA : Long.MAX_VALUE;
    }
}
