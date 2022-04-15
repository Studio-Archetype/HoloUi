package studio.archetype.holoui.config;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import lombok.Getter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import studio.archetype.holoui.HoloUI;
import studio.archetype.holoui.utils.SchedulerUtils;
import studio.archetype.holoui.utils.file.FolderWatcher;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;

public final class ConfigManager {

    private final Map<String, MenuDefinitionData> menuRegistry = Maps.newHashMap();

    private final File menuDir, imageDir;
    private final FolderWatcher menuDefinitionFolder;

    @Getter
    private final HuiSettings settings;

    public ConfigManager(File configDir) {
        this.imageDir = new File(configDir, "images");
        if(!imageDir.exists())
            imageDir.mkdirs();
        this.menuDir = new File(configDir, "menus");
        if(!menuDir.exists())
            menuDir.mkdirs();

        menuDefinitionFolder = new FolderWatcher(menuDir);
        settings = new HuiSettings(configDir);

        loadConfigs();

        SchedulerUtils.scheduleSyncTask(HoloUI.INSTANCE, 5L, () -> {
            if(menuDefinitionFolder.checkModifiedFast()) {
                menuDefinitionFolder.getChanged().forEach(f -> {
                    String name = FilenameUtils.getBaseName(f.getName());
                    Optional<MenuDefinitionData> data = loadConfig(name, f);
                    data.ifPresent(d -> {
                        HoloUI.INSTANCE.getSessionManager().byId(name).forEach(s -> {
                            Player p = s.getPlayer();
                            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§2Config \"" + name + "\" reloaded."));
                            p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, .5F, 1);
                        });
                        HoloUI.INSTANCE.getSessionManager().destroyAllType(name);
                        menuRegistry.put(name, d);
                        HoloUI.log(Level.INFO, "Menu config \"%s\" has been changed and re-registered.", name);
                    });
                });
            }
            settings.update();
        }, true);
        SchedulerUtils.scheduleSyncTask(HoloUI.INSTANCE, 20L, () -> {
            if(menuDefinitionFolder.checkModified()) {
                menuDefinitionFolder.getCreated().forEach(f -> {
                    String name = FilenameUtils.getBaseName(f.getName());
                    Optional<MenuDefinitionData> data = loadConfig(name, f);
                    data.ifPresent(d -> {
                        menuRegistry.put(name, d);
                        HoloUI.log(Level.INFO, "New menu config \"%s\" detected and registered.", name);
                    });
                });
                menuDefinitionFolder.getDeleted().forEach(f -> {
                    String name = FilenameUtils.getBaseName(f.getName());
                    if(menuRegistry.containsKey(name)) {
                        HoloUI.INSTANCE.getSessionManager().destroyAllType(name);
                        menuRegistry.remove(name);
                        HoloUI.log(Level.INFO, "Menu config \"%s\" has been deleted and unregistered.", name);
                    }
                });
            }
        }, true);
    }

    public void shutdown() {
        settings.write();
    }

    public Set<String> keys() {
        return menuRegistry.keySet();
    }

    public Optional<MenuDefinitionData> get(String key) {
        return exists(key) ? Optional.of(menuRegistry.get(key)) : Optional.empty();
    }

    public boolean exists(String key) {
        return menuRegistry.containsKey(key);
    }

    public BufferedImage hasImage(String relative) throws IOException {
        File f = new File(imageDir, relative.endsWith(".png") ? relative : relative + ".png");
        if(!f.exists() || f.isDirectory())
            throw new FileNotFoundException();
        return ImageIO.read(f);
    }

    private void loadConfigs() {
        menuDefinitionFolder.getWatchers().keySet().forEach(f -> {
            if(f.getPath().contains("menus")) {
                String name = FilenameUtils.getBaseName(f.getName());
                Optional<MenuDefinitionData> data = loadConfig(name, f);
                data.ifPresent(d -> {
                    menuRegistry.put(name, d);
                    HoloUI.log(Level.INFO, "Registered menu config \"%s\".", name);
                });
            }
        });
    }

    private Optional<MenuDefinitionData> loadConfig(String menuName, File f) {
        try(FileReader reader = new FileReader(f)) {
            if(FileUtils.sizeOf(f) == 0) {
                HoloUI.log(Level.WARNING, "Menu config \"%s.json\" is empty, ignoring.", menuName);
                return Optional.empty();
            }

            DataResult<Pair<MenuDefinitionData, JsonElement>> result = JsonOps.INSTANCE.withDecoder(MenuDefinitionData.CODEC).apply(JsonParser.parseReader(reader));
            if(result.error().isPresent())
                HoloUI.log(Level.WARNING, "Failed to parse menu config \"%s.json\":\n\t%s", menuName, result.error().get().message());
            else {
                if(result.result().isEmpty())
                    HoloUI.log(Level.WARNING, "An unknown error occurred while parsing menu config \"%s.json\"! Skipping.", menuName);
                else {
                    MenuDefinitionData data = result.result().get().getFirst();
                    data.setId(menuName);
                    return Optional.of(data);
                }
            }
        } catch(IOException | JsonParseException ex) {
            HoloUI.log(Level.WARNING, "A %s occurred while parsing menu config \"%s.json\":\n\t", ex.getClass().getSimpleName(), menuName, ex.getMessage());
        }
        return Optional.empty();
    }
}