package com.venom7t.lolguide.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.venom7t.lolguide.MainActivity
import com.venom7t.lolguide.R
import com.venom7t.lolguide.domain.patch.usecase.ResolvePatchUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * Home-screen widget shell (Phase 3 plan §Widgets).
 *
 * **Shows the patch badge only.** Free champion rotation -- the content this
 * widget was originally requested for -- needs Riot's CHAMPION-V3 endpoint,
 * which is keyed (Phase 4). Shipping a widget that claims to show rotation
 * with nothing behind it would be worse than not shipping one; this ships the
 * infrastructure now and the real content lands as a follow-up once Phase 4
 * exists, rather than building the same widget twice.
 */
class PatchStatusWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // GlanceAppWidget is not constructed by Hilt, so its dependencies are
        // pulled through an EntryPoint rather than @Inject -- the standard
        // pattern for classes outside Hilt's own construction, such as
        // widgets and content providers.
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java,
        )
        val patch = entryPoint.resolvePatchUseCase().invoke().getOrNull()

        provideContent {
            WidgetContent(patchVersion = patch?.version)
        }
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun resolvePatchUseCase(): ResolvePatchUseCase
    }
}

@Composable
private fun WidgetContent(patchVersion: String?) {
    val context = LocalContext.current

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(R.color.widget_background))
            .padding(12.dp)
            .clickable(actionStartActivity<MainActivity>()),
    ) {
        Text(
            text = context.getString(R.string.app_name),
            style = TextStyle(color = ColorProvider(R.color.widget_accent)),
        )
        Text(
            text = patchVersion
                ?.let { context.getString(R.string.widget_patch_label, it) }
                ?: context.getString(R.string.widget_open_app),
            style = TextStyle(color = ColorProvider(R.color.widget_text)),
        )
    }
}

class PatchStatusWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PatchStatusWidget()
}
