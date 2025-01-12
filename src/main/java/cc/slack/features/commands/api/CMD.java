package cc.slack.features.commands.api;

import cc.slack.utils.client.IMinecraft;
import lombok.Getter;

@Getter
public abstract class CMD implements IMinecraft {

    final CMDInfo cmdInfo = getClass().getAnnotation(CMDInfo.class);
    private final String name = cmdInfo.name();
    private final String description = cmdInfo.description();
    private final String alias = cmdInfo.alias();

    public abstract void onCommand(String[] args, String cmd);
}
