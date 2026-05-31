package com.dinoll288.housebuilder.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.dinoll288.housebuilder.DinoHouseBuilder;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class HouseBuilderConfigStore {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("dinoll288s-housebuilder-client.json");

    private HouseBuilderConfigStore() {
    }

    public static HouseBuilderSettings load() {
        if (Files.notExists(CONFIG_PATH)) {
            HouseBuilderSettings defaults = HouseBuilderSettings.defaults();
            defaults.clamp();
            save(defaults);
            return defaults;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            HouseBuilderSettings loaded = GSON.fromJson(reader, HouseBuilderSettings.class);
            if (loaded == null) {
                loaded = HouseBuilderSettings.defaults();
            }
            loaded.clamp();
            return loaded;
        } catch (IOException | JsonParseException exception) {
            DinoHouseBuilder.LOGGER.warn("Failed to read DinoLL288s Housebuilder config, using defaults.", exception);
            HouseBuilderSettings defaults = HouseBuilderSettings.defaults();
            defaults.clamp();
            return defaults;
        }
    }

    public static void save(HouseBuilderSettings settings) {
        settings.clamp();
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(settings, writer);
            }
        } catch (IOException exception) {
            DinoHouseBuilder.LOGGER.warn("Failed to save DinoLL288s Housebuilder config.", exception);
        }
    }
}
