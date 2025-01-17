package com.flipperdevices.deeplink.impl.dispatcher

import androidx.navigation.NavController
import com.flipperdevices.core.di.AppGraph
import com.flipperdevices.core.log.LogTagProvider
import com.flipperdevices.core.log.info
import com.flipperdevices.deeplink.api.DeepLinkDispatcher
import com.flipperdevices.deeplink.api.DeepLinkHandler
import com.flipperdevices.deeplink.model.Deeplink
import com.squareup.anvil.annotations.ContributesBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ContributesBinding(AppGraph::class, DeepLinkDispatcher::class)
class DeepLinkDispatcherImpl @Inject constructor(
    private val handlers: MutableSet<DeepLinkHandler>
) : DeepLinkDispatcher, LogTagProvider {
    override val TAG = "DeepLinkDispatcher"

    override suspend fun process(
        navController: NavController,
        deeplink: Deeplink
    ): Boolean = withContext(Dispatchers.Main) {
        val supportedHandlers = handlers.map { it.isSupportLink(deeplink) to it }
            .filter { it.first != null }

        info { "Found ${supportedHandlers.size} supported handlers: $supportedHandlers" }

        val processHandler = supportedHandlers
            .maxByOrNull { it.first!! } ?: return@withContext false

        info {
            "Choice handler ${processHandler.second.javaClass} " +
                "with priority ${processHandler.first}"
        }

        processHandler.second.processLink(navController = navController, deeplink)

        return@withContext true
    }
}
