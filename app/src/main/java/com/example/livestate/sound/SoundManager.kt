package raw

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import com.example.livestate.R

class SoundManager(context: Context) {
    init {
        val mediaPlayer = MediaPlayer.create(context, R.raw.keyboard)
        mediaPlayer.start()
    }

}
