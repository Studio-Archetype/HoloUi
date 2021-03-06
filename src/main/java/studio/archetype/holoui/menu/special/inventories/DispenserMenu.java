package studio.archetype.holoui.menu.special.inventories;

import com.google.common.collect.Lists;
import org.bukkit.block.*;
import org.bukkit.inventory.Inventory;
import studio.archetype.holoui.config.MenuComponentData;

import java.util.List;

public class DispenserMenu implements InventoryPreviewMenu<Inventory> {

    private static final float X_START = -.5F;

    @Override
    public void supply(Container b, List<MenuComponentData> components) {
        Inventory inv = getInventory(b);
        components.addAll(getLine(inv, 0, .75F));
        components.addAll(getLine(inv, 3, .25F));
        components.addAll(getLine(inv, 6, -.25F));
    }

    @Override
    public boolean isValid(Container b) {
        return b instanceof Dispenser || b instanceof Dropper;
    }

    private List<MenuComponentData> getLine(Inventory inv, int startIndex, float yOffset) {
        List<MenuComponentData> line = Lists.newArrayList();
        for(int i = 0; i < 3; i++)
            line.add(component("slot" + (i + startIndex), X_START + (i * .5F), yOffset, 0, new InventorySlotComponent.Data(inv, i + startIndex)));
        return line;
    }
}
