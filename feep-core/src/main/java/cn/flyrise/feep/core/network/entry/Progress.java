package cn.flyrise.feep.core.network.entry;

import java.io.Serializable;

/**
 * @author ZYP
 * @since 2016-09-05 22:59
 */
public class Progress implements Serializable {

    public long currentBytes;
    public long contentLength;
    public boolean done;

    public Progress() { }

    public Progress(long currentBytes, long contentLength, boolean done) {
        this.currentBytes = currentBytes;
        this.contentLength = contentLength;
        this.done = done;
    }
}
