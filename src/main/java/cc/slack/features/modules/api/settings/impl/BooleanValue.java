// Slack Client (discord.gg/paGUcq2UTb)

package cc.slack.features.modules.api.settings.impl;

import cc.slack.features.modules.api.settings.Value;
import lombok.Getter;

@Getter
public class BooleanValue extends Value<Boolean> {

    public BooleanValue(String name, boolean defaultValue) {
        super(name, defaultValue, null);
    }
}
