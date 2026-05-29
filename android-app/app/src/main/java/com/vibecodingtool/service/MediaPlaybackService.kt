package com.vibecodingtool.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.vibecodingtool.MainActivity
import com.vibecodingtool.R

class MediaPlaybackService : Service() {
    
    companion object {
        const val CHANNEL_ID = "media_playback_channel"
        const val NOTIFICATION_ID = 1
        
        const val ACTION_PLAY = "com.vibecodingtool.action.PLAY"
        const val ACTION_PAUSE = "com.vibecodingtool.action.PAUSE"
        const val ACTION_NEXT = "com.vibecodingtool.action.NEXT"
        const val ACTION_PREVIOUS = "com.vibecodingtool.action.PREVIOUS"
    }
    
    private val binder = LocalBinder()
    private var notificationManager: NotificationManager? = null
    
    inner class LocalBinder : Binder() {
        fun getService(): MediaPlaybackService = this@MediaPlaybackService
    }
    
    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
    
    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                // Handle play action
                updateNotification(true)
            }
            ACTION_PAUSE -> {
                // Handle pause action
                updateNotification(false)
            }
            ACTION_NEXT -> {
                // Handle next action
            }
            ACTION_PREVIOUS -> {
                // Handle previous action
            }
        }
        
        return START_STICKY
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "媒体播放",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "媒体播放控制"
                setShowBadge(false)
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }
    
    fun updateNotification(isPlaying: Boolean) {
        val notification = buildNotification(isPlaying)
        startForeground(NOTIFICATION_ID, notification)
    }
    
    private fun buildNotification(isPlaying: Boolean): Notification {
        // Main activity intent
        val mainIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val mainPendingIntent = PendingIntent.getActivity(
            this, 0, mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Play/Pause action
        val playPauseIntent = Intent(this, MediaPlaybackService::class.java).apply {
            action = if (isPlaying) ACTION_PAUSE else ACTION_PLAY
        }
        val playPausePendingIntent = PendingIntent.getService(
            this, 1, playPauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Previous action
        val previousIntent = Intent(this, MediaPlaybackService::class.java).apply {
            action = ACTION_PREVIOUS
        }
        val previousPendingIntent = PendingIntent.getService(
            this, 2, previousIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Next action
        val nextIntent = Intent(this, MediaPlaybackService::class.java).apply {
            action = ACTION_NEXT
        }
        val nextPendingIntent = PendingIntent.getService(
            this, 3, nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("VibeCoding Tool")
            .setContentText(if (isPlaying) "播放中" else "已暂停")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(mainPendingIntent)
            .setOngoing(true)
            .addAction(
                R.drawable.ic_previous,
                "上一曲",
                previousPendingIntent
            )
            .addAction(
                if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
                if (isPlaying) "暂停" else "播放",
                playPausePendingIntent
            )
            .addAction(
                R.drawable.ic_next,
                "下一曲",
                nextPendingIntent
            )
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .build()
    }
    
    fun stopService() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
}