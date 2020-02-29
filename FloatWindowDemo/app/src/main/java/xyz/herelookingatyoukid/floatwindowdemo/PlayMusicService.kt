package xyz.herelookingatyoukid.floatwindowdemo

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.util.Log
import java.io.File

const val TAG = "PlayMusicService"
const val ACTION = "action"
const val ACTION_PLAY = "play"
const val ACTION_STOP = "stop"
const val MUSIC_PATH = "path"
const val PLAY_MUSIC_INTENT_ACTION = "com.herelookingatyoukid.caizhixing"
class PlayMusicService : Service() {

    private val bind = PlayMusicBind()
    private var player: MediaPlayer? = null

    override fun onCreate() {
        super.onCreate()
        initPlayer()
        registerMusicControlReceiver()
        Log.d(TAG, "------onCreate------")
    }

    private fun registerMusicControlReceiver() {
        val intentFilter = IntentFilter(PLAY_MUSIC_INTENT_ACTION)
        registerReceiver(object :BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let{
                    val action = intent.getStringExtra(ACTION)
                    if(action != null && ACTION_PLAY == action){
                        val path = intent.getStringExtra(MUSIC_PATH)
                        path?.let{
                            playMusic(File(it))
                        }
                    }else{
                        stopMusic()
                    }
                }
            }
        },intentFilter)
    }

    private fun initPlayer() {
        val audioAttributes = AudioAttributes.Builder().apply {
            setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            setFlags(AudioAttributes.CONTENT_TYPE_MUSIC)
            setUsage(AudioAttributes.USAGE_MEDIA).build()
        }.build()
        player = MediaPlayer().apply {
            setAudioAttributes(audioAttributes)
            setOnErrorListener { mp, what, extra ->
                Log.e(TAG, "MediaPlayer onError what = $what extra = $extra")
                mp?.reset()
                true
            }
        }
        player!!.setOnPreparedListener {
            player!!.start()
        }
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "------onBind--------")
        return bind
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "------onUnbind------")
        return super.onUnbind(intent)
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        Log.d(TAG, "------onRebind------")
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        Log.d(TAG, "------onDestroy------")
    }

    inner class PlayMusicBind : Binder() {
        fun getService(): PlayMusicService {
            return this@PlayMusicService
        }
    }

    fun playMusic(file: File) {
        player?.let {
            if (player!!.isPlaying) {
                stopMusic()
            }
            val afd: AssetFileDescriptor = resources.openRawResourceFd(R.raw.demo)
            player!!.setDataSource(afd.fileDescriptor, afd.startOffset, afd.declaredLength)
            player!!.prepareAsync()
        }
    }

    fun stopMusic() {
        if (player != null && player!!.isPlaying) {
            player!!.reset()
        }
    }
}
