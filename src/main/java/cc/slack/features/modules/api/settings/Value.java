// Slack Client (discord.gg/paGUcq2UTb)

package cc.slack.features.modules.api.settings;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public abstract class Value<T> {

    private String name;
    @Setter
    private T value;
    private VisibilityCheck check;

    public <Type extends Value<T>> Type require(VisibilityCheck check) {
        this.name = name;
        this.value = value;
        this.check = check;
        return (Type) this;
    }

    public boolean isVisible() {
        if (check != null)
            return check.check();
        return true;
    }

    public interface VisibilityCheck {
        boolean check();
    }
}
