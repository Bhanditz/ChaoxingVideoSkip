/*
 * Copyright 2016 Alex Zhang aka. ztc1997
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ztc1997.chaoxingvideoskip.xposed

import android.app.Activity
import android.content.res.Resources
import android.content.res.XModuleResources
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import org.jetbrains.anko.toast
import ztc1997.chaoxingvideoskip.R
import ztc1997.chaoxingvideoskip.extentions.methodHookCallback
import java.util.*

class XposedModule : IXposedHookLoadPackage, IXposedHookZygoteInit {
    companion object {
        const val PACKAGE_NAME_CHAOXING = "com.chaoxing.mobile"
        const val NAME_VIDEO_PLAYER_ACTIVITY = "com.chaoxing.fanya.aphone.ui.video.VideoPlayerActicity"
    }

    private lateinit var res: Resources

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        res = XModuleResources.createInstance(startupParam.modulePath, null)
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
        if (lpparam == null) return
        if (lpparam.packageName == PACKAGE_NAME_CHAOXING) {
            hookChaoxing(lpparam.classLoader)
        }
    }


    private fun hookChaoxing(loader: ClassLoader) {
        val CLASS_VIDEO_PLAYER_ACTIVITY = XposedHelpers.findClass(NAME_VIDEO_PLAYER_ACTIVITY, loader)

        XposedHelpers.findAndHookMethod(CLASS_VIDEO_PLAYER_ACTIVITY, "a", Int::class.java,
                Int::class.java, Int::class.java, methodHookCallback {
            beforeHookedMethod {
                val activity = it.thisObject as Activity

                val lastPlayingTime = XposedHelpers.getAdditionalInstanceField(activity, "lastPlayingTime") as Int?

                if (lastPlayingTime != null) {
                    it.args[1] = lastPlayingTime
                } else {
                    //取得VideoBean数据类实例
                    val videoBean = XposedHelpers.getObjectField(activity, "E")

                    //取得视频总时长
                    val duration = XposedHelpers.callMethod(videoBean, "getDuration") as Int

                    //使得已播放的时长等于视频总时长，减去一个20秒内的随机数，增加数据真实性
                    it.args[1] = (duration * 1000 - Random().nextFloat() * 20000).toInt()

                    XposedHelpers.setAdditionalInstanceField(activity, "lastPlayingTime", it.args[1])

                    val playingTime = it.args[1]
                    XposedBridge.log("playingTime = $playingTime, duration = $duration")
                    activity.runOnUiThread { activity.toast(res.getString(R.string.toast_video_playing_edited)) }
                }
            }
        })
    }

}
