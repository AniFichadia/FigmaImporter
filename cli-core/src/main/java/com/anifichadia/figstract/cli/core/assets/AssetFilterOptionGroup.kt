package com.anifichadia.figstract.cli.core.assets

import com.anifichadia.figstract.cli.core.IncludeOrExcludeFilterOptionGroup
import com.anifichadia.figstract.cli.core.IncludeOrExcludeFilterOptionGroup.Companion.provideDelegate
import com.anifichadia.figstract.importer.asset.model.NodeFilter
import com.github.ajalt.clikt.core.MultiUsageError
import com.github.ajalt.clikt.parameters.groups.OptionGroup

class AssetFilterOptionGroup(prefix: String) : OptionGroup() {
    private val canvases by IncludeOrExcludeFilterOptionGroup(prefix, "Canvas")
    private val nodes by IncludeOrExcludeFilterOptionGroup(prefix, "Node")
    private val parentNodes by IncludeOrExcludeFilterOptionGroup(prefix, "ParentNode")

    fun toAssetFilter(): AssetFilter {
        val errors = listOfNotNull(
            canvases.error(),
            nodes.error(),
            parentNodes.error(),
        )
        if (errors.isNotEmpty()) throw MultiUsageError(errors)

        return AssetFilter(
            canvasNameFilter = NodeFilter(
                include = canvases.includes,
                exclude = canvases.excludes,
            ),
            nodeNameFilter = NodeFilter(
                include = nodes.includes,
                exclude = nodes.excludes,
            ),
            parentNameFilter = NodeFilter(
                include = parentNodes.includes,
                exclude = parentNodes.excludes,
            ),
        )
    }
}