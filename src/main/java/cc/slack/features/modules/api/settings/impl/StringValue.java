// Slack Client (discord.gg/paGUcq2UTb)

package cc.slack.features.modules.api.settings.impl;

import cc.slack.features.modules.api.settings.Value;

public class StringValue extends Value<String> {

    public StringValue(String name, String defaultValue) {
        super(name, defaultValue, null);
    }
}
