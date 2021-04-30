package cn.flyrise.feep.utils;

import android.content.Context;
import android.os.FileObserver;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.flyrise.feep.commonality.view.LockPatternView;

/**
 * 图案解锁加密、解密工具类
 * @author way
 */
public class LockPatternUtils {
    private static final String TAG = "LockPatternUtils";
    private static final String LOCK_PATTERN_FILE = "gesture.key";
    /**
     * The minimum number of dots in a valid pattern.
     */
    public static final int MIN_LOCK_PATTERN_SIZE = 2;
    /**
     * The maximum number of incorrect attempts before the user is prevented from trying again for {@link #FAILED_ATTEMPT_TIMEOUT_MS}.
     */
    public static final int FAILED_ATTEMPTS_BEFORE_TIMEOUT = 5;
    /**
     * The minimum number of dots the user must include in a wrong pattern attempt for it to be counted against the counts that affect {@link #FAILED_ATTEMPTS_BEFORE_TIMEOUT} and
     */
    public static final int MIN_PATTERN_REGISTER_FAIL = MIN_LOCK_PATTERN_SIZE;
    /**
     * How long the user is prevented from trying again after entering the wrong pattern too many times.
     */
    public static final long FAILED_ATTEMPT_TIMEOUT_MS = 30000L;

    private static File sLockPatternFilename;
    private static final AtomicBoolean sHaveNonZeroPatternFile = new AtomicBoolean(false);

    private static class LockPatternFileObserver extends FileObserver {
        public LockPatternFileObserver(String path, int mask) {
            super(path, mask);
        }

        @Override
        public void onEvent(int event, String path) {
            Log.d(TAG, "file path" + path);
            if (LOCK_PATTERN_FILE.equals(path)) {
                Log.d(TAG, "lock pattern file changed");
                sHaveNonZeroPatternFile.set(sLockPatternFilename.length() > 0);
            }
        }
    }

    public LockPatternUtils(Context context) {
        if (sLockPatternFilename == null) {
            final String dataSystemDirectory = context.getFilesDir().getAbsolutePath();
            sLockPatternFilename = new File(dataSystemDirectory, LOCK_PATTERN_FILE);
            sHaveNonZeroPatternFile.set(sLockPatternFilename.length() > 0);
            final int fileObserverMask = FileObserver.CLOSE_WRITE | FileObserver.DELETE | FileObserver.MOVED_TO | FileObserver.CREATE;
            final FileObserver sPasswordObserver = new LockPatternFileObserver(dataSystemDirectory, fileObserverMask);
            sPasswordObserver.startWatching();
        }
    }

    /**
     * Check to see if the user has stored a lock pattern.
     * @return Whether a saved pattern exists.
     */
    public boolean savedPatternExists() {
        return sHaveNonZeroPatternFile.get();
    }

    public void clearLock() {
        saveLockPattern(null);
    }

    /**
     * Deserialize a pattern. 解密,用于保存状态
     * @param string The pattern serialized with {@link #patternToString}
     * @return The pattern.
     */
    public static List<LockPatternView.Cell> stringToPattern(String string) {
        final List<LockPatternView.Cell> result = new ArrayList<>();

        final byte[] bytes = string.getBytes();
        for (final byte b : bytes) {
            result.add(LockPatternView.Cell.of(b / 3, b % 3));
        }
        return result;
    }

    /**
     * Serialize a pattern. 加密
     * @param pattern The pattern.
     * @return The pattern in string form.
     */
    public static String patternToString(List<LockPatternView.Cell> pattern) {
        if (pattern == null) {
            return "";
        }
        final int patternSize = pattern.size();

        final byte[] res = new byte[patternSize];
        for (int i = 0; i < patternSize; i++) {
            final LockPatternView.Cell cell = pattern.get(i);
            res[i] = (byte) (cell.getRow() * 3 + cell.getColumn());
        }
        return new String(res);
    }

    /**
     * Save a lock pattern.
     * @param pattern The new pattern to save.
     */
    public void saveLockPattern(List<LockPatternView.Cell> pattern) {
        // Compute the hash
        final byte[] hash = LockPatternUtils.patternToHash(pattern);
        try {
            // Write the hash to file
            final RandomAccessFile raf = new RandomAccessFile(sLockPatternFilename, "rwd");
            // Truncate the file if pattern is null, to clear the lock
            if (pattern == null) {
                raf.setLength(0);
            }
            else {
                raf.write(hash, 0, hash.length);
            }
            raf.close();
        } catch (final IOException ioe) {
            // Cant do much
            Log.e(TAG, "Unable to save lock pattern to " + sLockPatternFilename);
        }
    }

    /*
     * Generate an SHA-1 hash for the pattern. Not the most secure, but it is at least a second level of protection. First level is that the file is in a location only readable by the system process.
     * @param pattern the gesture pattern.
     * @return the hash of the pattern in a byte array.
     */
    private static byte[] patternToHash(List<LockPatternView.Cell> pattern) {
        if (pattern == null) {
            return null;
        }

        final int patternSize = pattern.size();
        final byte[] res = new byte[patternSize];
        for (int i = 0; i < patternSize; i++) {
            final LockPatternView.Cell cell = pattern.get(i);
            res[i] = (byte) (cell.getRow() * 3 + cell.getColumn());
        }
        try {
            final MessageDigest md = MessageDigest.getInstance("SHA-1");
            return md.digest(res);
        } catch (final NoSuchAlgorithmException nsa) {
            return res;
        }
    }

    /**
     * Check to see if a pattern matches the saved pattern. If no pattern exists, always returns true.
     * @param pattern The pattern to check.
     * @return Whether the pattern matches the stored one.
     */
    public boolean checkPattern(List<LockPatternView.Cell> pattern) {
        try {
            // Read all the bytes from the file
            final RandomAccessFile raf = new RandomAccessFile(sLockPatternFilename, "r");
            final byte[] stored = new byte[(int) raf.length()];
            final int got = raf.read(stored, 0, stored.length);
            raf.close();
            if (got <= 0) {
                return true;
            }
            // Compare the hash from the file with the entered pattern's hash
            return Arrays.equals(stored, LockPatternUtils.patternToHash(pattern));
        } catch (final FileNotFoundException fnfe) {
            return true;
        } catch (final IOException ioe) {
            return true;
        }
    }
}
