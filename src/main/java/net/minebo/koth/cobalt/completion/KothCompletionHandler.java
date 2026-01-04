package net.minebo.koth.cobalt.completion;

import co.aikar.commands.CommandCompletionContext;
import co.aikar.commands.CommandCompletions;
import co.aikar.commands.InvalidCommandArgument;
import net.minebo.koth.koth.Koth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KothCompletionHandler implements CommandCompletions.CommandCompletionHandler {

    @Override
    public Collection<String> getCompletions(CommandCompletionContext context) throws InvalidCommandArgument {
        List<String> completions = new ArrayList<>();

        Koth.koths.forEach(koth -> completions.add(koth.getName()));

        return completions;
    }

}
