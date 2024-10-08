package com.anifichadia.figstract.cli.core

import com.github.ajalt.clikt.core.MutuallyExclusiveGroupException
import com.github.ajalt.clikt.core.ParameterHolder
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.unique

class IncludeOrExcludeFilterOptionGroup(
    prefix: String? = null,
    suffix: String,
) : DelegatableOptionGroup() {
    private val includeOptionName = name(prefix, true, suffix)
    private val includesOption = createOption(includeOptionName)
    val includes by includesOption

    private val excludeOptionName = name(prefix, false, suffix)
    private val excludesOption = createOption(excludeOptionName)
    val excludes by excludesOption

    fun error(): UsageError? {
        return if (includes.isNotEmpty() && excludes.isNotEmpty()) {
            MutuallyExclusiveGroupException(listOf(includeOptionName, excludeOptionName))
        } else {
            null
        }
    }

    companion object {
        fun name(prefix: String?, isInclude: Boolean, suffix: String): String {
            return buildString {
                if (prefix != null) {
                    append(prefix)
                    append("Filter")
                } else {
                    append("filter")
                }

                if (isInclude) {
                    append("Included")
                } else {
                    append("Excluded")
                }

                append(suffix.replaceFirstChar { it.uppercase() })
            }
        }

        fun ParameterHolder.createOption(name: String) = option("--$name")
            .convert { it.toRegex() }
            .multiple()
            .unique()
    }
}
