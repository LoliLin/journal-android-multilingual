/*
 * Copyright (c) 2022. Isaak Hanimann.
 * This file is part of PsychonautWiki Journal.
 *
 * PsychonautWiki Journal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * PsychonautWiki Journal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PsychonautWiki Journal.  If not, see https://www.gnu.org/licenses/gpl-3.0.en.html.
 */

package com.isaakhanimann.journal.di

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.intercept.Interceptor
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class JournalApplication : Application(), ImageLoaderFactory {

    /**
     * 全局禁用硬件加速位图，确保 Compose 视图能被绘制到软件 Canvas（导出分享图时必需）。
     * 同时也让 LocalContext.current.imageLoader 在任意 Compose 上下文中拿到同一个加载器。
     */
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(
                    Interceptor { chain ->
                        val newRequest = chain.request.newBuilder()
                            .allowHardware(false)
                            .build()
                        chain.proceed(newRequest)
                    }
                )
            }
            .build()
    }
}
