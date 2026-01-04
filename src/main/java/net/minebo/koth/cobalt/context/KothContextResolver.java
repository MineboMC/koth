package net.minebo.koth.cobalt.context;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.contexts.ContextResolver;
import net.minebo.koth.koth.Koth;

public class KothContextResolver implements ContextResolver<Koth, BukkitCommandExecutionContext> {

    @Override
    public Koth getContext(BukkitCommandExecutionContext commandExecutionContext) throws InvalidCommandArgument {
        String name = commandExecutionContext.popFirstArg();
        Koth koth = Koth.get(name);
        if (koth != null) return koth;
        throw new InvalidCommandArgument("No koth matching " + name + " could be found.");
    }

}
