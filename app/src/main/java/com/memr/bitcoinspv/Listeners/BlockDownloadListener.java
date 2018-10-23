package com.memr.bitcoinspv.Listeners;

import java.util.Date;

public interface BlockDownloadListener {
    void progress(double pct, int blocksSoFar, Date date);

    void finishedDownload();
}
