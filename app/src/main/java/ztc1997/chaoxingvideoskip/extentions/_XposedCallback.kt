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

package ztc1997.chaoxingvideoskip.extentions

import de.robv.android.xposed.XC_MethodHook

fun methodHookCallback(init: _XC_MethodHook.() -> Unit) = _XC_MethodHook().apply(init)

class _XC_MethodHook : XC_MethodHook() {
    private var _beforeHookedMethod: ((MethodHookParam) -> Unit)? = null
    private var _afterHookedMethod: ((MethodHookParam) -> Unit)? = null

    fun beforeHookedMethod(listener: (MethodHookParam) -> Unit) {
        _beforeHookedMethod = listener
    }

    fun afterHookedMethod(listener: (MethodHookParam) -> Unit) {
        _afterHookedMethod = listener
    }

    override fun beforeHookedMethod(param: MethodHookParam) {
        super.beforeHookedMethod(param)
        _beforeHookedMethod?.invoke(param)
    }

    override fun afterHookedMethod(param: MethodHookParam) {
        super.afterHookedMethod(param)
        _afterHookedMethod?.invoke(param)
    }
}