// Slack Client (discord.gg/slackclient)

package cc.slack.features.modules.api.settings.impl;

import cc.slack.features.modules.api.settings.Value;
import lombok.Getter;

@Getter
public class BooleanValue extends Value<Boolean> {

    public BooleanValue(String name, boolean defaultValue) {
        super(name, defaultValue, null);
    }
}
