package com.misanthropy.linggango.linggango_tweaks.tweaks;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.List;

@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TooltipTweaks {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        ResourceLocation name = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (name == null || !name.getNamespace().equals("goety_revelation")) return;

        List<Component> tooltip = event.getToolTip();
        Component itemName = tooltip.get(0);
        String path = name.getPath();
        CompoundTag nbt = stack.getOrCreateTag();

        if (path.equals("revelation")) {
            tooltip.clear();
            tooltip.add(itemName);

            if (Screen.hasShiftDown()) {
                tooltip.add(Component.literal("§o§8\"And when He ripped off the seventh seal, Heaven fell quiet. Complete silence for half an hour.\""));
            } else {
                tooltip.add(Component.literal("§4Hold §8Shift§4 to see details."));
            }
        } else if (path.equals("dimensional_will")) {
            tooltip.clear();
            tooltip.add(itemName);

            String damageRes = nbt.contains("DimensionalWillDamageResistance") ? String.valueOf(nbt.getInt("DimensionalWillDamageResistance")) : "0";
            String chance = nbt.contains("DimensionalWillChance") ? String.valueOf(nbt.getInt("DimensionalWillChance")) : "0";

            tooltip.add(Component.literal("§5A thing that should not exist formed by condensing the power of multiple dimensions. In the center you can see what is left of the core of a faded star.."));

            if (Screen.hasShiftDown()) {
                tooltip.add(Component.literal("§dPassive Ability:"));
                tooltip.add(Component.literal("§5While having it equipped, you gain §6" + damageRes + "% Damage Resistance§5 to §dall damage types§5. You also have a §6" + chance + "% §dChance§5 that otherwise lethal damage won't kill you."));
            } else {
                tooltip.add(Component.literal("§5Hold §6Shift§5 to see details."));
            }

            tooltip.add(Component.literal("§8§o1. \"We give thanks to you, Lord God Almighty, who is and who was, for you have taken your great power and begun to reign.\""));
        } else if (path.equals("halo_of_the_end")) {
            tooltip.clear();
            tooltip.add(itemName);

            if (Screen.hasShiftDown()) {
                tooltip.add(Component.literal("§7If this is indeed the end of everything,"));
                tooltip.add(Component.literal("§7then Fate shall meet is conclusion by my hand."));
                tooltip.add(Component.literal(" "));
                tooltip.add(Component.literal("§8§o\"After this I looked up, and there before me was a door standing open in Heaven. And the voice I first heard told me, 'Come up here, and I will show you what must take place after this.'\""));
                tooltip.add(Component.literal("§8§o(Revelation 4:1)"));
            } else if (Screen.hasAltDown()) {
                tooltip.add(Component.literal("§d§o§5§lActive Ability:"));
                tooltip.add(Component.literal("§o§dPause the time of the whole world."));
                tooltip.add(Component.literal("§o§dKeybind : §6K§d / Cooldown: §670s"));
                tooltip.add(Component.literal("§o§7§lFocus Effects:"));
                tooltip.add(Component.literal("§o§8• Fireball / Lava Bomb Focus now possess the power of finality:"));
                tooltip.add(Component.literal("§o§8Changes Hellfire to The-End Fire, which deals magic damage equal to 4.44% of the creature's maximum health at high speed to any creature standing in it. This effect stacks."));
                tooltip.add(Component.literal("§o§8• The Wither skulls from the Wither Skull Focus will be transformed into Star Arrows, which deal the following damage to creatures:"));
                tooltip.add(Component.literal("§o§8-> Base damage = x (with a max of 15.0, Player attack damage)."));
                tooltip.add(Component.literal("§o§8-> Deals x * (1+Potency enchantment level /10) True Damage."));
                tooltip.add(Component.literal("§o§8-> Deals x * 0.1 Absolute True Damage."));
                tooltip.add(Component.literal("§o§8-> Inflicts Instant Damage II, Weakness II (30s), Wither II (40s) and Blindness II (20s)."));
                tooltip.add(Component.literal("§o§8-> Also deals Area Of Effect damage to creatues within a 5x5x5 radius."));
                tooltip.add(Component.literal("§o§8• Barricade Focus will become your strongest shield:"));
                tooltip.add(Component.literal("§o§8Converts Totemic Walls into sturdier (Increased health to 200) Obsidian Monoliths for the player, up to 5 Monoliths can exist at a time. The Obsidian Monolith grants the player invulnerability and increased health regeneration."));
                tooltip.add(Component.literal("§o§7§lPassive Effects:"));
                tooltip.add(Component.literal("§o§5§lThe Powers of The Halo take priority over all other items."));
                tooltip.add(Component.literal("§o§d§lAll creatues will be friendly to the wearer of The Divine Halo, and all bosses will be neutral towards them."));
                tooltip.add(Component.literal("§o§8• The Power of Finality surrounds you, turning you into an Immortal:"));
                tooltip.add(Component.literal("§o§8You will have 85% Damage Reduction against projectiles and 66.6% Damage Reduction against the §5Void§8."));
                tooltip.add(Component.literal("§o§8You will have 50% Damage Reduction in the §2Overworld§8, 75% Damage Reduction in the §4Nether§8 and 99% Damage Reduction in the §5End§8."));
                tooltip.add(Component.literal("§o§8You have a 85% chance to deny §l§0Death§r§o§8."));
                tooltip.add(Component.literal("§o§4The Halo will withstand §lApollyon§r§o§4's Purgatory Barrier."));
                tooltip.add(Component.literal("§o§8You will have a 20 damage or 25% of your maximum health as a damage cap, with only the minimum of the two effects being applied."));
                tooltip.add(Component.literal("§o§8Increases your invulnerability frames to 30 ticks. (Including §4§lApollyon§r§8§o's machine gun bow)"));
                tooltip.add(Component.literal("§o§8You are immune to all negative effects, you ignore forced healing reductions and can fly freely like the §4§lCreator§r§8§o. (Ignores the no-fly zones of §cApostles§8 and §4§lApollyon§r§8§o.)"));
                tooltip.add(Component.literal("§o§8During §r§eDays§o§8 of the §r§2Overworld§o§8, you gain Saturation II, Resistance II and Regeneration II."));
                tooltip.add(Component.literal("§o§8During §r§7Nights§o§8 of the §r§2Overworld§o§8, you gain Night Vision, Swiftness III, Haste III and Glowing."));
                tooltip.add(Component.literal("§o§8In the §r§4Nether§o§8, i gain Corpse Eater IV, Strength IV and Repulsive IV."));
                tooltip.add(Component.literal("§o§8In the §r§5End§o§8, you inflict Curse IV, Spams IV, Slowness IV and Weakness IV to creatures in a 6 block radius."));
                tooltip.add(Component.literal("§o§8You are immune to §r§5Magic§o§8, §r§cFire§o§8, §r§7Cramming§o§8, §r§9Drowning§8, §4Lava§8, §6Explosion§8, §bFalling§8§o, §r§2Cactus§o§8, §r§eLightning§o§8, §r§aKinetic Energy§o§8, §r§3Freezing §o§8and §r§0Suffocation§8§o damages."));
                tooltip.add(Component.literal("§o§8• Your attacks are imbued with the power of §r§4§lThe Apocalypse§r§o§8, you are capable of breaking through the defenses of ordinary creatues:"));
                tooltip.add(Component.literal("§o§8Your attacks will bypass armor, common enchantments, potions, resistances and the damage limits of most bosses."));
                tooltip.add(Component.literal("§o§8• While wearing the Halo of The End, the divine essence of the §r§5End§o§8, will protect you, and neither §r§4§lPersonalization§r§o§8 nor §r§c§l§oRagnarök§r§8§o can strip away your divinity:"));
                tooltip.add(Component.literal("§4§lApollyon§r§8§o cannot disable or collect your items during §r§4§lThe Apocalypse§r§8§o phase, and §r§c§lRagnarök§r§8§o's malevolent influence cannot seal it but can seal other items."));
                tooltip.add(Component.literal("§o§8• The Halo of The End will forcibly nullify Curse of Binding:"));
                tooltip.add(Component.literal("§o§8This includes items that cannot be removed, such as The Ring of Seven Curses."));
                tooltip.add(Component.literal("§o§8However, the Curse of Binding on The Halo itself will be forcibly active and cannot be removed."));
                tooltip.add(Component.literal("§o§8• Your magical abilities surpass even the Gods:"));
                tooltip.add(Component.literal("§o§8All Spell Costs are reduced to 1, all spellcooldowns and universal cooldowns are reduced to 0.1 seconds."));
                tooltip.add(Component.literal("§o§8You cast spells 8 times faster than regular."));
                tooltip.add(Component.literal("§o§5§lWhen you die, you will be instantly healed to full health, gain complete invincibility for 60 seconds and pause the time of the whole world for 15 second."));
                tooltip.add(Component.literal("§o§5§lAfter triggering Ressurection, you gain a damaging aura that will infict 10 points of void damager per frame in a radius of 3 blocks lasting 600 seconds with a 1800 second cooldown."));
            } else {
                tooltip.add(Component.literal("§8Hold §7Shift§8 to see details."));
                tooltip.add(Component.literal("§8Hold §7Alt§8 to see effect details."));
            }
        } else if (path.equals("ascension_halo")) {
            tooltip.clear();
            tooltip.add(itemName);

            if (Screen.hasShiftDown()) {
                tooltip.add(Component.literal("§6§oThe Sacred Halo§5 symbolizes the elevation of life."));
                tooltip.add(Component.literal("§5§oUpon Selection, status and rank are stripped away, neither noble nor lowly, neither rich nor poor. While all are blessed by §4§lHis§r§o§5 grace, none can bear the §cApostolic§5 mission alone."));
                tooltip.add(Component.literal(" "));
                tooltip.add(Component.literal("§o§4§lHe§r§5 speaks §4\"I am the shepherd whose light shines out of the darkness.\""));
                tooltip.add(Component.literal("§o§5In §4§lHis§r§5 palm, "));
                tooltip.add(Component.literal("§o§5I saw the Vista, "));
                tooltip.add(Component.literal("§o§5Twelve §cApostles§5, their Millennium Tribulation passed,"));
                tooltip.add(Component.literal("§o§5Have welcome a new companion to follow §4§lHis§r§5 behests."));
                tooltip.add(Component.literal("§o§5When §6The Holy Fire of Judgement§5 reignites, §4§lHis§r§5§o Light shall illuminate all once more."));
                tooltip.add(Component.literal(" "));
                tooltip.add(Component.literal("§o§4§lYou§r§5, §owho care for and protect the §bsouls§5 of all,"));
                tooltip.add(Component.literal("§o§5We chant §4§lYour§r§5§o name in §8§lThe Eternal Darkness§r§5§o, so that the §6The Light§5 may never fail."));
                tooltip.add(Component.literal("§5§oUntil §6The Light§5 enlightens us once more, "));
                tooltip.add(Component.literal("§o§cWe§5 shall spread §6The Legend of the Holy Fire§5 throughout §8§lThe Eternal Night§r§5."));
                tooltip.add(Component.literal(" "));
                tooltip.add(Component.literal("§o§8\"They have as king over them, the angel of the Abyss, whose name in Hebrew is Abaddon, and in Greek is Apollyon (that is, Destroyer).\" "));
                tooltip.add(Component.literal("§o§8(Revelation 4:1)"));
            } else if (Screen.hasAltDown()) {
                tooltip.add(Component.literal("§o§7Focus Effects while casting using a Nether Staff:"));
                tooltip.add(Component.literal("§o§8§nFireball / Lava Bomb Focus will burn the enemies' souls:"));
                tooltip.add(Component.literal("§o§8Converts FireBall Focus into a Hellfire beam, Lava Bomb Focus into a lava blast and raises Hellfire's damage to 4.44% of the enemy's maximum health."));
                tooltip.add(Component.literal("§o§8§nHail Focus will turn into a lava rain and track enemies:"));
                tooltip.add(Component.literal("§o§8Converts Hail Focus into Infernal Clouds and expands their area of effect, allowing them to track the enemies and increasing their damage to 1% of the enemy's maximum health."));
                tooltip.add(Component.literal("§o§8§nBarricade Focus will protect your soul:"));
                tooltip.add(Component.literal("§o§8Converts Totemic Walls into Obsidian Monoliths for the player, only one Monolith can exist at a time and this effect has a 45 second cooldown. The Obsidian Monolith grants the player invulnerability and increased health regeneration."));
                tooltip.add(Component.literal("§o§8§nUpdraft Focus will summon fire blasts from the Nether:"));
                tooltip.add(Component.literal("§o§8Updraft Focus will summon Fireburst traps, increasing their damage to 40."));
                tooltip.add(Component.literal("§o§8§nCyclone Focus will be amplified by fire:"));
                tooltip.add(Component.literal("§o§8Cyclone Focus can summon burning tornadoes, similar to the ones Apostles summon."));
                tooltip.add(Component.literal("§o§8§nRotting Focus will always summon the most elite soldiers:"));
                tooltip.add(Component.literal("§o§8Rotting Focus will summon 2-4 Zombie Piglin Brute Minions, sneak casting will summon 1 Zombie Piglin Brute Minion with a full set of netherite armor and permanent swiftness II. "));
                tooltip.add(Component.literal("§o§8§nGhastly / Blazing Focus will call creatures from the nether to give you a helping hand:"));
                tooltip.add(Component.literal("§o§8Ghast Focus will summon Malghasts and Blazing Focus will summon Infernos."));
                tooltip.add(Component.literal("§o§8§nSkull Focus will summon souls from hell:"));
                tooltip.add(Component.literal("§o§8Skull Focus will summon Damned."));
                tooltip.add(Component.literal("§o§8§nWither Skull Focus will launch Nether Meteors."));
                tooltip.add(Component.literal("§o§7Passive Effects:"));
                tooltip.add(Component.literal("§o§8§nYou are no longer affected by mortal damages:"));
                tooltip.add(Component.literal("§o§8You are immune to §r§cFire§o§8, §r§7Cramming§o§8, §r§9Drown§8, §4Lava§8, §6Explosion§8, §bFall§8 §oand §r§0Suffocation§8§o damages."));
                tooltip.add(Component.literal("§o§8You will have 25% Damage Reduction in the §2Overworld§8 and 50% Damage Reduction in the §4Nether§8."));
                tooltip.add(Component.literal("§o§8§nPotion Effects will no longer affect you:"));
                tooltip.add(Component.literal("§o§8All Effects except night vision will not apply to players wearing the Halo of Ascension."));
                tooltip.add(Component.literal("§o§8§nSoul Costs of Focuses will be reduced to an unimaginable level:"));
                tooltip.add(Component.literal("§o§8Soul Costs will be reduced to 1/4 of their original cost."));
                tooltip.add(Component.literal("§o§8§nYou will cast spells at an extraordinary speed:"));
                tooltip.add(Component.literal("§o§8Casting speed is quadrupled to its original speed."));
                tooltip.add(Component.literal("§o§8§nYou will bask in the blood of the Nether:"));
                tooltip.add(Component.literal("§o§8You will have a self healing rate of 2 in the Nether."));
                tooltip.add(Component.literal("§o§8§lAll undead are friendly to you, except §cApostles§8 and §4§lApollyon§r§8. §o§8§lYou also have a 20 damage cap, similar to the defenses of those §cPale Faced Envoys of the Nether§r§8."));
                tooltip.add(Component.literal("§o§4§lWhen you die, you are instantly healed to 50% health and enter a 30 second invulnerability period. Simultaneously, you summon a Wither Servant, and the weather is locked to thunderstorms while raining down Nether Meteors. This effect lasts for 300 seconds and has 1800 seconds cooldown."));
            } else {
                tooltip.add(Component.literal("§5Hold §6Shift§5 to see details."));
                tooltip.add(Component.literal("§5Hold §6Alt§5 to see effect details."));
            }
        } else if (path.equals("bow_of_revelation")) {
            tooltip.clear();
            tooltip.add(itemName);

            if (Screen.hasShiftDown()) {
                tooltip.add(Component.literal("§o§8\"I looked up and saw a white horse standing there. It's rider held a bow, and a crown was placed on his head. He rode out as a conqueror bent on conquest, winning many victories as he went\""));
                tooltip.add(Component.literal("§o§8(Revelation 6:2)"));
            } else {
                tooltip.add(Component.literal("§dThe bow of the sacred Apostles, forged from what was left of their §6divinity§d."));
                tooltip.add(Component.literal("§dHold §6Shift§d to see details."));
            }
        } else if (path.equals("eternal_watch")) {
            tooltip.clear();
            tooltip.add(itemName);

            String mainDesc1 = "§5A golden relic that forces the universe to hold its breath.";
            String mainDesc2 = "§5Demonstrate the eternal power to stop The World..";
            String mainDesc3 = "§5Immortality is the only choice...";
            String keybind = "§dKeybind : §6K";
            String verse = "§o§8\"Never again shall there be night. They shall have no need of a lamp’s flickering glow nor the radiance of the sun, for the glory of the Lord God is all the light anyone needs. And they shall reign with Him throughout the eons of eternity.\"";
            String verseRef = "§o§8(Revelation 22:5)";

            tooltip.add(Component.literal(mainDesc1));
            tooltip.add(Component.literal(mainDesc2));
            tooltip.add(Component.literal(mainDesc3));

            if (Screen.hasShiftDown()) {
                tooltip.add(Component.literal("§dActive Ability:"));
                tooltip.add(Component.literal("§5Freeze time in the entire dimension for §69§5 seconds."));
                tooltip.add(Component.literal(" "));
                tooltip.add(Component.literal("§dCooldown: §670 seconds"));
                tooltip.add(Component.literal(" "));
                tooltip.add(Component.literal("§dPassive Ability:"));
                tooltip.add(Component.literal("§5If the wielder of this watch attacks an entity during the dimension freeze, they will deal §6* 10.0§5 True Damage to the entity and halt the time stop."));
            } else {
                tooltip.add(Component.literal("§5Hold §6Shift§5 to see details."));
            }

            tooltip.add(Component.literal(keybind));
            tooltip.add(Component.literal(verse));
            tooltip.add(Component.literal(verseRef));
        } else if (path.equals("broken_halo")) {
            tooltip.clear();
            tooltip.add(itemName);

            if (Screen.hasShiftDown()) {
                tooltip.add(Component.literal("§o§8\"Do not fear what you are about to suffer. Behold, The Devil is about to cast some of you into prison, so that you will be tested, and you will have Tribulation for ten days. Be faithful until death, and I will give you the crown of life\""));
                tooltip.add(Component.literal("§o§8(Revelation 2:10)"));
            } else if (Screen.hasAltDown()) {
                tooltip.add(Component.literal("§o§8You will have 15% Damage Reduction in the §2Overworld§8 and 30% Damage Reduction in the §4Nether§8."));
            } else {
                tooltip.add(Component.literal("§bEven if it is broken and tarnished, the remnants of a long lost §6godhood§b may still protect you from some damage."));
                tooltip.add(Component.literal(" "));
                tooltip.add(Component.literal("§bHold §6Shift§b to see details."));
                tooltip.add(Component.literal("§bHold §6Alt§b to see effect details."));
            }
        } else if (path.equals("the_needle")) {
            tooltip.clear();
            tooltip.add(itemName);

            boolean isReal = nbt.getBoolean("isRealNeedle");
            String enchantmentPiercing = nbt.contains("NeedleEnchantmentPiercing") ? String.valueOf(nbt.getInt("NeedleEnchantmentPiercing")) : "0";
            String armorPenetration = nbt.contains("NeedleArmorPenetration") ? String.valueOf(nbt.getInt("NeedleArmorPenetration")) : "0";

            tooltip.add(Component.literal("§5The Needle will allow you to §dpenetrate armor and enchantments§5."));

            if (isReal) {
                tooltip.add(Component.literal("§5Additionally, since this particular Needle was made by §4§lThe Witch King Apollyon§r§5, you can §dhold right click§5 to charge The Needle and §dimpale yourself§5 to siphon off §4§lApollyon§r§5's lingering essence and make yourself stronger."));
                tooltip.add(Component.literal("§5After an use, you will gain one of the follow stats: §dMax Health§5, §dAttack Damage§5, §dAttack Speed§5 and §dDamage Resistance§5. These will permanently increase your §dattribute values§5. "));
                tooltip.add(Component.literal("§5After siphoning the remnant §614§5 times, The Needle will §4lose this ability§5."));

                int uses = nbt.contains("RemainingUsages") ? nbt.getInt("RemainingUsages") : 14;
                tooltip.add(Component.literal("§5Remaining usages: §6" + uses));
            }

            tooltip.add(Component.literal(" "));
            tooltip.add(Component.literal("§6When on hands:"));
            tooltip.add(Component.literal("§5- §6+" + enchantmentPiercing + "% §dEnchantment Piercing"));
            tooltip.add(Component.literal("§5- §6+" + armorPenetration + "% §dArmor Penetration"));
            tooltip.add(Component.literal("§5- §6+1 §dHand Slots"));

            if (isReal) {
                String maxHealth = nbt.contains("NeedleMaxHealth") ? String.valueOf(nbt.getInt("NeedleMaxHealth")) : "0";
                String attackDamage = nbt.contains("NeedleAttackDamage") ? String.valueOf(nbt.getInt("NeedleAttackDamage")) : "0";
                String attackSpeed = nbt.contains("NeedleAttackSpeed") ? String.valueOf(nbt.getInt("NeedleAttackSpeed")) : "0";
                String damageResistance = nbt.contains("NeedleDamageResistance") ? String.valueOf(nbt.getInt("NeedleDamageResistance")) : "0";

                tooltip.add(Component.literal("§6Additionally, based on the siphons:"));
                tooltip.add(Component.literal("§5- §6+" + maxHealth + "% §dMax Health"));
                tooltip.add(Component.literal("§5- §6+" + attackDamage + "% §dAttack Damage"));
                tooltip.add(Component.literal("§5- §6+" + attackSpeed + "% §dAttack Speed"));
                tooltip.add(Component.literal("§5- §6+" + damageResistance + "% §dDamage Resistance"));
            }

            tooltip.add(Component.literal(" "));
            tooltip.add(Component.literal("§o§8\"They were told not to harm the grass of the Earth, nor any plant or tree, but only those people who did not have the seal of God on their foreheads.\""));
            tooltip.add(Component.literal("§o§8(Revelation 9:4)"));
        }
    }
}