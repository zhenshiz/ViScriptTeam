package com.viscript_team.command;

import com.lowdragmc.lowdraglib2.registry.ILDLRegister;
import com.mojang.brigadier.CommandDispatcher;
import com.viscript_team.ViScriptTeam;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.function.Supplier;

public interface ICommand extends ILDLRegister<ICommand, Supplier<ICommand>> {
    String COMMAND_ID = ViScriptTeam.MOD_ID + ":command";

    void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext, Commands.CommandSelection commandSelection);
}
