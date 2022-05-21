package me.realized.duels.command.commands.party;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.Permissions;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.command.commands.party.subcommands.DisbandCommand;
import me.realized.duels.command.commands.party.subcommands.KickCommand;
import me.realized.duels.command.commands.party.subcommands.AcceptCommand;
import me.realized.duels.command.commands.party.subcommands.LeaveCommand;
import me.realized.duels.command.commands.party.subcommands.ListCommand;
import me.realized.duels.command.commands.party.subcommands.ToggleCommand;
import me.realized.duels.command.commands.party.subcommands.TransferCommand;
import me.realized.duels.data.UserData;
import me.realized.duels.party.Party;

public class PartyCommand extends BaseCommand {
    
    public PartyCommand(final DuelsPlugin plugin) {
        super(plugin, "party", Permissions.PARTY, true);
        child(
            new AcceptCommand(plugin),
            new ListCommand(plugin),
            new LeaveCommand(plugin),
            new KickCommand(plugin),
            new DisbandCommand(plugin),
            new ToggleCommand(plugin),
            new TransferCommand(plugin)
        );
    }

    @Override
    protected boolean executeFirst(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;

        if (args.length == 0) {
            lang.sendMessage(sender, "COMMAND.party.usage", "command", label);
            return true;
        }

        if (isChild(args[0])) {
            return false;
        }

        final Player target = Bukkit.getPlayerExact(args[0]);

        if (target == null || !player.canSee(target)) {
            lang.sendMessage(sender, "ERROR.player.not-found", "name", args[0]);
            return true;
        }

        if (player.equals(target)) {
            lang.sendMessage(sender, "ERROR.party.is-self");
            return true;
        }

        final UserData user = userManager.get(target);

        if (user == null) {
            lang.sendMessage(sender, "ERROR.data.not-found", "name", target.getName());
            return true;
        }

        if (!user.canPartyRequest()) {
            lang.sendMessage(sender, "ERROR.party.requests-disabled", "name", target.getName());
            return true;
        }

        if (partyManager.isInParty(target)) {
            lang.sendMessage(sender, "ERROR.party.already-in-party.target", "name", target.getName());
            return true;
        }

        if (partyManager.hasInvite(player, target)) {
            lang.sendMessage(sender, "ERROR.party.already-has-invite", "name", target.getName());
            return true;
        }
        
        final Party party = partyManager.getOrCreate(player);

        if (!party.isOwner(player)) {
            lang.sendMessage(sender, "ERROR.party.is-not-owner");
            return true;
        }

        partyManager.sendInvite(player, target, party);
        lang.sendMessage(party.getOnlineMembers(), "COMMAND.party.invite.send.members", "name", target.getName());
        lang.sendMessage(target, "COMMAND.party.invite.send.receiver", "name", sender.getName());
        return true;
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {}

    // Disables default TabCompleter
    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        return null;
    }
}
