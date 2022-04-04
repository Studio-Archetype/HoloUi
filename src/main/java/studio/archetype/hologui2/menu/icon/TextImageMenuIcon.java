package studio.archetype.hologui2.menu.icon;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.network.chat.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.bukkit.Location;
import org.bukkit.util.BoundingBox;
import studio.archetype.hologui2.HoloGUI;
import studio.archetype.hologui2.config.icon.TextImageIconData;
import studio.archetype.hologui2.menu.ArmorStandManager;
import studio.archetype.hologui2.utils.ArmorStandBuilder;
import studio.archetype.hologui2.utils.TextUtils;
import studio.archetype.hologui2.utils.math.CollisionPlane;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class TextImageMenuIcon extends MenuIcon<TextImageIconData> {

    private final List<Component> components;

    public TextImageMenuIcon(TextImageIconData data) {
        super(data);
        components = createComponents();
    }

    @Override
    protected Map<UUID, Location> createArmorStands(Location loc) {
        Map<UUID, Location> uuids = Maps.newHashMap();
        loc.add(0, components.size() / 2F  * NAMETAG_SIZE, 0);
        components.forEach(c -> {
            uuids.put(ArmorStandManager.add(ArmorStandBuilder.nametagArmorStand(c, loc)), loc.clone());
            loc.subtract(0, NAMETAG_SIZE, 0);
        });
        return uuids;
    }

    @Override
    protected CollisionPlane createBoundingBox(Location loc) {
        float width = 0;
        for(Component component : components)
            width = Math.max(width, component.getString().length() * NAMETAG_SIZE / 2);
        return new CollisionPlane(loc.toVector(), width, components.size() * NAMETAG_SIZE);
    }

    private List<Component> createComponents() {
        try {
            BufferedImage image = HoloGUI.INSTANCE.getConfigManager().hasImage(data.relativePath());
            List<Component> lines = Lists.newArrayList();
            for(int y = 0; y < image.getHeight(); y++) {
                MutableComponent component = new TextComponent("");
                for(int x = 0; x < image.getWidth(); x++) {
                    int colour = image.getRGB(x, y);
                    if(((colour >> 24) & 0x0000FF) < 255)
                        component.append(new TextComponent(" ").setStyle(Style.EMPTY.withBold(true))).append(new TextComponent(" "));
                    else
                        component.append(TextUtils.textColor("█", colour & 0x00FFFFFF));
                }

                lines.add(component);
            }
            return lines;
        } catch(IOException e) {
            HoloGUI.log(Level.WARNING, "Failed to load relative image \"%s\":", data.relativePath());
            HoloGUI.log(Level.WARNING, "\t%s" + (e.getMessage() != null ? " - %s" : ""), e.getClass().getSimpleName(), e.getMessage());
            HoloGUI.log(Level.WARNING, "Falling back to missing texture.");
            return MISSING;
        }
    }

    private static final List<Component> MISSING = Lists.newArrayList(
            TextUtils.textColor("████████", "#000000").append(TextUtils.textColor("████████", "#f800f8")),
            TextUtils.textColor("████████", "#000000").append(TextUtils.textColor("████████", "#f800f8")),
            TextUtils.textColor("████████", "#000000").append(TextUtils.textColor("████████", "#f800f8")),
            TextUtils.textColor("████████", "#000000").append(TextUtils.textColor("████████", "#f800f8")),
            TextUtils.textColor("████████", "#000000").append(TextUtils.textColor("████████", "#f800f8")),
            TextUtils.textColor("████████", "#000000").append(TextUtils.textColor("████████", "#f800f8")),
            TextUtils.textColor("████████", "#000000").append(TextUtils.textColor("████████", "#f800f8")),
            TextUtils.textColor("████████", "#000000").append(TextUtils.textColor("████████", "#f800f8")),
            TextUtils.textColor("████████", "#f800f8").append(TextUtils.textColor("████████", "#000000")),
            TextUtils.textColor("████████", "#f800f8").append(TextUtils.textColor("████████", "#000000")),
            TextUtils.textColor("████████", "#f800f8").append(TextUtils.textColor("████████", "#000000")),
            TextUtils.textColor("████████", "#f800f8").append(TextUtils.textColor("████████", "#000000")),
            TextUtils.textColor("████████", "#f800f8").append(TextUtils.textColor("████████", "#000000")),
            TextUtils.textColor("████████", "#f800f8").append(TextUtils.textColor("████████", "#000000")),
            TextUtils.textColor("████████", "#f800f8").append(TextUtils.textColor("████████", "#000000")),
            TextUtils.textColor("████████", "#f800f8").append(TextUtils.textColor("████████", "#000000")));
}