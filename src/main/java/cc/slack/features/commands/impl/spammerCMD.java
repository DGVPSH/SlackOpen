package cc.slack.features.commands.impl;

import cc.slack.features.commands.api.CMD;
import cc.slack.features.commands.api.CMDInfo;
import cc.slack.features.modules.api.Module;
import cc.slack.features.modules.api.settings.Value;
import cc.slack.features.modules.api.settings.impl.BooleanValue;
import cc.slack.features.modules.api.settings.impl.ModeValue;
import cc.slack.features.modules.api.settings.impl.NumberValue;
import cc.slack.features.modules.api.settings.impl.StringValue;
import cc.slack.features.modules.impl.other.Test;
import cc.slack.start.Slack;
import cc.slack.utils.other.PrintUtil;

import java.util.Arrays;
import java.util.List;

@CMDInfo(
        name = "Spammer",
        alias = "sp",
        description = "Usage: .sp [add/clear] [message]"
)
public class spammerCMD extends CMD {

    @Override
    public void onCommand(String[] args, String command) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("clear")) {
                Slack.getInstance().getModuleManager().getInstance(Test.class).messages.clear();
                PrintUtil.message("Cleared Spammer List.");
            }
            return;
        }

        if (args[0].equalsIgnoreCase("add")) {
            String message = String.join(" ", args).replaceFirst("add ", "");
            Slack.getInstance().getModuleManager().getInstance(Test.class).messages.add(message);
            PrintUtil.message("Added Message To Spammer List.");

            for (String s :Slack.getInstance().getModuleManager().getInstance(Test.class).messages ) {
                PrintUtil.message(s);
            }
        }
    }
}
