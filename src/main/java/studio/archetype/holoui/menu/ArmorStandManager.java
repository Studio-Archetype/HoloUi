package studio.archetype.holoui.menu;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Consumer;
import org.bukkit.util.Vector;
import studio.archetype.holoui.HoloUI;
import studio.archetype.holoui.utils.NMSUtils;
import studio.archetype.holoui.utils.PacketUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class ArmorStandManager {

    private static final Map<UUID, ArmorStand> armorStands = new HashMap<>();
    private static final Map<UUID, Player> playerVisibility = new HashMap<>();

    public static UUID add(ArmorStand stand) {
        UUID uuid = UUID.randomUUID();
        armorStands.put(uuid, stand);
        return uuid;
    }

    public static void spawn(UUID uuid, Player p) {
        if(!armorStands.containsKey(uuid))
            return;

        sendSpawnPackets(p, armorStands.get(uuid));
        playerVisibility.put(uuid, p);
    }

    public static void despawn(UUID uuid) {
        if(!armorStands.containsKey(uuid) || !playerVisibility.containsKey(uuid))
            return;
        ArmorStand stand = armorStands.get(uuid);
        PacketUtils.send(playerVisibility.get(uuid), new ClientboundRemoveEntitiesPacket(stand.getId()));
        stand.getPassengers().forEach(e -> PacketUtils.send(playerVisibility.get(uuid), new ClientboundRemoveEntitiesPacket(e.getId())));
        playerVisibility.remove(uuid);

    }

    public static void delete(UUID uuid) {
        if(!armorStands.containsKey(uuid))
            return;

        despawn(uuid);
        armorStands.get(uuid).remove(Entity.RemovalReason.DISCARDED);
        armorStands.remove(uuid);
        playerVisibility.remove(uuid);
    }

    public static Vector location(UUID uuid) {
        if(!armorStands.containsKey(uuid))
            return new Vector();
        return NMSUtils.vector(armorStands.get(uuid).position());
    }

    public static void goTo(UUID uuid, Location loc) {
        if(!armorStands.containsKey(uuid))
            return;
        ArmorStand stand = armorStands.get(uuid);
        stand.setPos(NMSUtils.vec3(loc.toVector()));
        PacketUtils.send(playerVisibility.get(uuid), new ClientboundTeleportEntityPacket(stand));
    }

    public static void move(UUID uuid, Vector offset) {
        if(!armorStands.containsKey(uuid))
            return;
        ArmorStand stand = armorStands.get(uuid);
        stand.setPos(stand.getPosition(1).add(NMSUtils.vec3(offset)));
        PacketUtils.send(playerVisibility.get(uuid), new ClientboundTeleportEntityPacket(stand));
    }

    public static void changeName(UUID uuid, Component name) {
        if(!armorStands.containsKey(uuid))
            return;
        ArmorStand stand = armorStands.get(uuid);
        stand.setCustomName(name);
        PacketUtils.send(playerVisibility.get(uuid), new ClientboundSetEntityDataPacket(stand.getId(), stand.getEntityData().getNonDefaultValues()));
    }

    public static void rotate(UUID uuid, float yaw) {
        if(!armorStands.containsKey(uuid))
            return;
        ArmorStand stand = armorStands.get(uuid);
        stand.setYRot(yaw);
        PacketUtils.send(playerVisibility.get(uuid), new ClientboundTeleportEntityPacket(stand));
    }

    private static void sendSpawnPackets(Player p, ArmorStand stand) {
        PacketUtils.send(p, stand.getAddEntityPacket());
        PacketUtils.send(p, new ClientboundSetEntityDataPacket(stand.getId(), stand.getEntityData().getNonDefaultValues()));
        List<Pair<EquipmentSlot, ItemStack>> stacks = Lists.newArrayList();
        for(EquipmentSlot s : EquipmentSlot.values())
            if(stand.hasItemInSlot(s))
                stacks.add(new Pair<>(s, stand.getItemBySlot(s)));
        if(!stacks.isEmpty())
            PacketUtils.send(p, new ClientboundSetEquipmentPacket(stand.getId(), stacks));
        if(!stand.getPassengers().isEmpty()) {
            stand.getPassengers().forEach(e -> {
                PacketUtils.send(p, e.getAddEntityPacket());
                PacketUtils.send(p, new ClientboundSetEntityDataPacket(e.getId(), e.getEntityData().getNonDefaultValues()));
            });
            PacketUtils.send(p, new ClientboundSetPassengersPacket(stand));
        }
    }
}
