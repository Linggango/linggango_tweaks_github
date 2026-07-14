package com.misanthropy.linggango.linggango_tweaks.tweaks.jei;

import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class JeiSortStuff {

    private static final List<String> TOP_TABS = List.of(
            "create:mixing",
            "create:milling",
            "create:pressing",
            "create:compacting",
            "create:mechanical_crafting",
            "create:sequenced_assembly",
            "goety:ritual",
            "goety:brazier",
            "forbidden_arcanus:hephaestus_smithing",
            "immersiveengineering",
            "mekanism",
            "evolvedmekanism",
            "naturesaura",
            "minecraft:crafting",
            "minecraft:stonecutting",
            "jei:information"
    );

    private static final List<String> BOTTOM_TABS = List.of(
            "anvil",
            "brewing",
            "blasting",
            "smelting"
    );

    public static void patchJeiSortOrder() {
        Path jeiConfigPath = FMLPaths.CONFIGDIR.get().resolve("jei/recipe-category-sort-order.ini");

        if (!Files.exists(jeiConfigPath)) {
            return;
        }

        try {
            List<String> lines = Files.readAllLines(jeiConfigPath);
            List<List<String>> topSlots = new ArrayList<>(TOP_TABS.size());
            for (int i = 0; i < TOP_TABS.size(); i++) {
                topSlots.add(new ArrayList<>());
            }
            List<String> middlePile = new ArrayList<>();
            List<String> bottomPile = new ArrayList<>();
            List<String> comments = new ArrayList<>();

            for (String line : lines) {
                if (line.trim().isEmpty() || line.startsWith("#")) {
                    comments.add(line);
                    continue;
                }

                boolean matchedTop = false;
                for (int i = 0; i < TOP_TABS.size(); i++) {
                    if (line.contains(TOP_TABS.get(i))) {
                        topSlots.get(i).add(line);
                        matchedTop = true;
                        break;
                    }
                }

                if (matchedTop) continue;

                boolean matchedBottom = false;
                for (String bottom : BOTTOM_TABS) {
                    if (line.contains(bottom)) {
                        bottomPile.add(line);
                        matchedBottom = true;
                        break;
                    }
                }

                if (!matchedBottom) {
                    middlePile.add(line);
                }
            }

            List<String> finalList = new ArrayList<>(comments);
            for (List<String> slot : topSlots) {
                finalList.addAll(slot);
            }
            finalList.addAll(middlePile);
            finalList.addAll(bottomPile);

            if (!lines.equals(finalList)) {
                Files.write(jeiConfigPath, finalList);
            }

        } catch (IOException e) {
            e.printStackTrace(); // dis shit gonna have no problems
        }
    }
}