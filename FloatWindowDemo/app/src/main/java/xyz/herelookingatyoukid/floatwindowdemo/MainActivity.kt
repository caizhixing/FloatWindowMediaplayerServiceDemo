package xyz.herelookingatyoukid.floatwindowdemo

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import xyz.herelookingatyoukid.floatwindowdemo.databinding.ActivityMainBinding
import xyz.herelookingatyoukid.floatwindowdemo.permission.FloatWindowManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var service: PlayMusicService? = null
    private var connection: ServiceConnection? = null
    private var shouldUnBind = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initData()
        initListener()
    }

    private fun initData() {
        connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val playMusicBind = service as PlayMusicService.PlayMusicBind
                this@MainActivity.service = playMusicBind.getService()
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                service = null
            }

            override fun onBindingDied(name: ComponentName?) {
                service = null
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindPlayMusicService()
    }

    private fun initListener() {
        FloatWindowManager.getInstance()
            .setListener(object : FloatWindowManager.FloatWindowListener {
                override fun playClick() {
                    val intent = Intent(PLAY_MUSIC_INTENT_ACTION)
                    intent.putExtra(ACTION, ACTION_PLAY)
                    intent.putExtra(MUSIC_PATH, "")
                    sendBroadcast(intent)
                }

                override fun stopClick() {
                    val intent = Intent(PLAY_MUSIC_INTENT_ACTION)
                    intent.putExtra(ACTION, ACTION_STOP)
                    sendBroadcast(intent)
                }

            })
        binding.showFloatWindow.setOnClickListener {
            bindAction()
            FloatWindowManager.getInstance().applyOrShowFloatWindow(this)
        }
        binding.dismiss.setOnClickListener {
            FloatWindowManager.getInstance().dismissWindow()
        }
    }

    private fun unbindPlayMusicService() {
        if (shouldUnBind) {
            service = null
            unbindService(connection!!)
            shouldUnBind = false
        }
    }

    private fun bindAction() {
        val service = Intent(this, PlayMusicService::class.java)
        if (bindService(service, connection!!, Context.BIND_AUTO_CREATE)) {
            shouldUnBind = true
        }
    }
}
