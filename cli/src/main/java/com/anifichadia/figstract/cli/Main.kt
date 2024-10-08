package com.anifichadia.figstract.cli

import com.anifichadia.figstract.cli.core.FigstractCommand
import com.anifichadia.figstract.cli.core.assets.RealAssetsCommand
import com.anifichadia.figstract.cli.core.variables.RealVariablesCommand
import com.github.ajalt.clikt.command.main

suspend fun main(args: Array<String>) {
    FigstractCommand(
        assetsCommand = RealAssetsCommand(),
        variablesCommand = RealVariablesCommand(),
    ).main(args)
}
