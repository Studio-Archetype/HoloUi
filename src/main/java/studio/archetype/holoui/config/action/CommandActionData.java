package studio.archetype.holoui.config.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import studio.archetype.holoui.enums.MenuActionCommandSource;
import studio.archetype.holoui.enums.MenuActionType;

public record CommandActionData(MenuActionCommandSource source, String command) implements MenuActionData {

    public MenuActionType getType() { return MenuActionType.COMMAND; }

    public static final Codec<CommandActionData> CODEC = RecordCodecBuilder.create(i -> i.group(
            MenuActionCommandSource.CODEC.fieldOf("source").forGetter(CommandActionData::source),
            Codec.STRING.fieldOf("command").forGetter(CommandActionData::command)
    ).apply(i, CommandActionData::new));
}
