package studio.archetype.holoui.menu.icon;

import com.google.common.collect.Lists;
import me.clip.placeholderapi.PlaceholderAPI;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import studio.archetype.holoui.config.icon.TextIconData;
import studio.archetype.holoui.menu.ArmorStandManager;
import studio.archetype.holoui.utils.ArmorStandBuilder;
import studio.archetype.holoui.utils.math.CollisionPlane;

import java.util.List;
import java.util.UUID;

public class TextMenuIcon extends MenuIcon<TextIconData> {

    private final List<Component> components;

    public TextMenuIcon(Player p, Location loc, TextIconData data) {
        super(p, loc, data);
        components = Lists.newArrayList();
        for(String s : data.text().split("\n"))
            components.add(new TextComponent(PlaceholderAPI.setPlaceholders(p, s)));
    }

    @Override
    protected List<UUID> createArmorStands(Location loc) {
        List<UUID> uuids = Lists.newArrayList();
        loc.add(0, ((components.size() - 1) / 2F  * NAMETAG_SIZE) - NAMETAG_SIZE, 0);
        components.forEach(c -> {
            uuids.add(ArmorStandManager.add(ArmorStandBuilder.nametagArmorStand(c, loc)));
            loc.subtract(0, NAMETAG_SIZE, 0);
        });
        System.out.println(loc);
        return uuids;
    }

    @Override
    public CollisionPlane createBoundingBox() {
        float width = 0;
        for(Component component : components)
            width = Math.max(width, component.getString().length() * NAMETAG_SIZE / 2);
        return new CollisionPlane(position.toVector(), width, components.size() * NAMETAG_SIZE);
    }
}