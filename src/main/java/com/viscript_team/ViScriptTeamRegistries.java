package com.viscript_team;

import com.lowdragmc.lowdraglib2.registry.AutoRegistry;
import com.viscript_team.command.ICommand;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public class ViScriptTeamRegistries {
    public static AutoRegistry.LDLibRegister<ICommand, Supplier<ICommand>> COMMANDS;

    static {
        COMMANDS = AutoRegistry.LDLibRegister
                .create(ResourceLocation.parse(ICommand.COMMAND_ID), ICommand.class, AutoRegistry::noArgsCreator);
    }
}
