package com.tbread

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.scene.web.WebView
import javafx.stage.Stage
import javafx.stage.StageStyle
import java.io.File

class BrowserApp : Application() {


    override fun start(stage: Stage) {
        val webView = WebView()
        val engine = webView.engine
        engine.load(File("index.html").toURI().toString())

        val scene = Scene(webView, 1000.0, 800.0)
        scene.fill = Color.TRANSPARENT

        try {
            val pageField = engine.javaClass.getDeclaredField("page")
            pageField.isAccessible = true
            val page = pageField.get(engine)

            val setBgMethod = page.javaClass.getMethod("setBackgroundColor", Int::class.javaPrimitiveType)
            setBgMethod.isAccessible = true
            setBgMethod.invoke(page, 0)
        } catch (e: Exception) {
            println("리플렉션 실패: ${e.message}")
        }

        stage.initStyle(StageStyle.TRANSPARENT)
        stage.scene = scene
        stage.isAlwaysOnTop = true
        stage.title = "Aion2 Dps Overlay"

        stage.show()
    }
}
