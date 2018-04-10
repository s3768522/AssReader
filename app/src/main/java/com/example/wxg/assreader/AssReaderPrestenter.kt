package com.example.wxg.assreader

import android.content.Context
import android.os.Handler
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.math.BigDecimal
import java.util.*
import java.util.regex.Pattern
import kotlin.concurrent.timerTask

class AssReaderPrestenter(private val mZiMuView: IZimuView, private val context: Context) {

    private var mZiMuTime = 0
    private var mZiMuIndex = 0
    private val mZimuList: ArrayList<ZiMuBean> by lazy { ArrayList<ZiMuBean>() }
    private var mTimer: Timer ?= null
    private var mTimerTask: TimerTask ?= null
    private val mHandler = Handler()

    fun read(path: String) {
        var zimuLine: String? = null
        var startCompile = false
        var startIndex = -1
        var endIndex = -1
        var textIndex = -1
        val fileInput = context.assets.open(path)
        try {
            val zimuFileReader = InputStreamReader(fileInput)
            val zimuReader = BufferedReader(zimuFileReader)
            zimuLine = zimuReader.readLine()
            while (null != zimuLine) {
                if ("[Events]" == zimuLine) {
                    startCompile = true
                }
                if (startCompile) {
                    if (zimuLine.startsWith("Format:")) {
                        val s = zimuLine.replace("Format:", "").replace(" ", "")
                        val names = s.split(",")
                        startIndex = names.indexOf("Start")
                        endIndex = names.indexOf("End")
                        textIndex = names.indexOf("Text")
                    } else if (zimuLine.startsWith("Dialogue:")) {
                        zimuLine.replace("Dialogue:", "")
                        val list = zimuLine.split(",")
                        val bean = ZiMuBean()
                        bean.start = formatTime(list[startIndex])
                        bean.end = formatTime(list[endIndex])
                        var text = list[textIndex]
                        if (list.size - 1 > textIndex) {
                            for (i in textIndex + 1 until list.size) {
                                text += ",${list[i]}"
                            }
                        }
                        bean.text = formatText(text)
                        mZimuList.add(bean)
                    }
                }
                zimuLine = zimuReader.readLine()
            }
        } catch (e: Exception) {
            Log.i("zimu_info", e.toString())
        } finally {
            fileInput.close()
        }
        startRead()
    }

    private fun startRead() {
        if (mTimer == null) {
            mTimer = Timer()
        }
        if (mTimerTask == null) {
            mTimerTask = timerTask {
                if (mZiMuTime > mZimuList[mZiMuIndex].end) {
                    mZiMuIndex++
                }
                if (mZiMuTime >= mZimuList[mZiMuIndex].start && mZiMuTime < mZimuList[mZiMuIndex].end) {
                    mHandler.post { mZiMuView.setZiMu(mZimuList[mZiMuIndex].text) }
                    if (mZiMuIndex < mZimuList.size - 1 && mZimuList[mZiMuIndex].end >= mZimuList[mZiMuIndex + 1].start) {
                        mZiMuIndex++
                    }
                } else if (mZiMuTime == mZimuList[mZiMuIndex].end) {
                    mHandler.post { mZiMuView.setZiMu("") }
                    mZiMuIndex++
                }
                Log.i("zimu_info", "time: $mZiMuTime, index: $mZiMuIndex")
                mZiMuTime++
                if (mZiMuIndex >= mZimuList.size) {
                    mTimer?.cancel()
                }
            }
        }
        mTimer?.schedule(mTimerTask, 0, 1000)
    }

    fun seekTo(time: Int) {
        mZiMuView.setZiMu("")
        mTimer?.cancel()
        mZiMuTime = time
        mZimuList.forEachIndexed { index, bean ->
            if (time >= bean.start && time < bean.end) {
                mZiMuIndex = index
            }
        }
        startRead()
    }

    private fun formatText(text: String): String {
        var content = text
        val pattern = Pattern.compile("(?<=\\{)(\\S+)(?=\\})")
        val matcher = pattern.matcher(content)
        while (matcher.find()) {
            content = content.replace(matcher.group(), "").replace("{", "").replace("}", "")
        }
        content = content.replace("\\N", "\n")
        Log.i("text_info", "content: $content")
        return content
    }

    private fun formatTime(time: String): Int {
        var timeInSecend = -1
        try {
            val list = time.split(":")
            timeInSecend = list[0].toInt() * 3600 + list[1].toInt() * 60 + BigDecimal(list[2]).setScale(0, BigDecimal.ROUND_HALF_UP).toInt()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return timeInSecend
    }

    fun cancle() {
        mTimer?.cancel()
    }
}