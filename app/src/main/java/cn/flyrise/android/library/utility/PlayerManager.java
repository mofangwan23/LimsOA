/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-1-24 上午11:07:36
 */
package cn.flyrise.android.library.utility;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * 类功能描述：</br>
 *
 * @author 钟永健
 * @version 1.0</br> 修改时间：2013-1-24</br> 修改备注：</br>
 */
public class PlayerManager {
    private static HashMap<String, FEMediaPlayer> players = new HashMap<>();
    private static PlayerManager playerManager;

    private PlayerManager() {
    }

    /**
     * 获得实例
     */
    public static PlayerManager getInstance() {
        if (playerManager == null) {
            playerManager = new PlayerManager();
        }
        return playerManager;
    }

    /**
     * 获取播放器
     */
    public FEMediaPlayer addPlayer(String path, FEMediaPlayer player) {
        if (!players.containsKey(path)) {
            if (player == null) {
                player = new FEMediaPlayer();
            }
            players.put(path, player);
        }
        return players.get(path);
    }

    /**
     * 停止音频的播放
     */
    public int stopPlaying() {
        final Iterator<Entry<String, FEMediaPlayer>> it = players.entrySet().iterator();
        int count = 0;
        while (it.hasNext()) {
            final Entry<String, FEMediaPlayer> next = it.next();
            final FEMediaPlayer player = next.getValue();
            if (player != null && player.isPlaying()) {
                player.stop();
                count++;
            }
            else if (player != null) {
                player.changeToStopState();
                count++;
            }
        }
        clearPlayers();
        return count;
    }

    /**
     * 清空播放器缓存
     */
    public void clearPlayers() {
        players.clear();
    }

    /**
     * 根据文件路径获取播放器
     *
     * @return 播放器
     */
    public FEMediaPlayer getPlayer(String path) {
        if (players.containsKey(path)) {
            return players.get(path);
        }
        return null;
    }
}
