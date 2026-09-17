package main.reachhud.projectile;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public final class ProjectileWeaponState {

    private static final int BOW_FULL_DRAW_TICKS = 20;

    public enum WeaponType {
        NONE,
        BOW,
        CROSSBOW
    }

    public enum ProjectileType {
        NONE,
        ARROW,
        FIREWORK
    }

    private final WeaponType weaponType;
    private final ProjectileType projectileType;
    private final boolean usingWeapon;
    private final boolean loaded;
    private final int useTicks;
    private final float drawProgress;
    private final int multishotLevel;

    private ProjectileWeaponState(
            WeaponType weaponType,
            ProjectileType projectileType,
            boolean usingWeapon,
            boolean loaded,
            int useTicks,
            float drawProgress,
            int multishotLevel
    ) {
        this.weaponType = weaponType;
        this.projectileType = projectileType;
        this.usingWeapon = usingWeapon;
        this.loaded = loaded;
        this.useTicks = useTicks;
        this.drawProgress = drawProgress;
        this.multishotLevel = multishotLevel;
    }

    public static ProjectileWeaponState detect(Minecraft client) {
        if (client.player == null) {
            return none();
        }

        ItemStack mainHand = client.player.getMainHandItem();
        ItemStack offHand = client.player.getOffhandItem();

        ItemStack weapon = findWeapon(mainHand, offHand);

        if (weapon.isEmpty()) {
            return none();
        }

        boolean usingWeapon = client.player.isUsingItem();

        if (weapon.is(Items.BOW)) {
            int useTicks = usingWeapon
                    ? client.player.getTicksUsingItem()
                    : 0;

            float drawProgress =
                    calculateBowDrawProgress(useTicks);

            return new ProjectileWeaponState(
                    WeaponType.BOW,
                    ProjectileType.ARROW,
                    usingWeapon,
                    false,
                    useTicks,
                    drawProgress,
                    0
            );
        }

        if (weapon.is(Items.CROSSBOW)) {
            boolean loaded = isCrossbowLoaded(weapon);
            int multishotLevel = getMultishotLevel(weapon);

            ProjectileType projectileType =
                    getCrossbowProjectileType(weapon);

            return new ProjectileWeaponState(
                    WeaponType.CROSSBOW,
                    projectileType,
                    usingWeapon,
                    loaded,
                    0,
                    0.0F,
                    multishotLevel
            );
        }

        return none();
    }

    private static boolean isCrossbowLoaded(ItemStack weapon) {
        var chargedProjectiles =
                weapon.get(DataComponents.CHARGED_PROJECTILES);

        return chargedProjectiles != null
                && !chargedProjectiles.isEmpty();
    }

    private static ProjectileType getCrossbowProjectileType(
            ItemStack weapon
    ) {
        var chargedProjectiles =
                weapon.get(DataComponents.CHARGED_PROJECTILES);

        if (chargedProjectiles == null
                || chargedProjectiles.isEmpty()) {
            return ProjectileType.NONE;
        }

        var projectiles =
                chargedProjectiles.itemCopies();

        if (projectiles.isEmpty()) {
            return ProjectileType.NONE;
        }

        ItemStack projectile =
                projectiles.get(0);

        if (projectile.is(Items.FIREWORK_ROCKET)) {
            return ProjectileType.FIREWORK;
        }

        if (projectile.is(Items.ARROW)
                || projectile.is(Items.TIPPED_ARROW)
                || projectile.is(Items.SPECTRAL_ARROW)) {
            return ProjectileType.ARROW;
        }

        return ProjectileType.NONE;
    }

    private static int getMultishotLevel(ItemStack weapon) {
        ItemEnchantments enchantments =
                weapon.get(DataComponents.ENCHANTMENTS);

        if (enchantments == null) {
            return 0;
        }

        for (var entry : enchantments.entrySet()) {
            Holder<Enchantment> enchantment =
                    entry.getKey();

            boolean isMultishot =
                    enchantment.unwrapKey()
                            .map(key ->
                                    key.toString()
                                            .equals(
                                                    "ResourceKey[minecraft:enchantment / minecraft:multishot]"
                                            )
                            )
                            .orElse(false);

            if (isMultishot) {
                return entry.getIntValue();
            }
        }

        return 0;
    }

    private static float calculateBowDrawProgress(int useTicks) {
        if (useTicks <= 0) {
            return 0.0F;
        }

        return Math.min(
                useTicks / (float) BOW_FULL_DRAW_TICKS,
                1.0F
        );
    }

    private static ItemStack findWeapon(
            ItemStack mainHand,
            ItemStack offHand
    ) {
        if (mainHand.is(Items.BOW)
                || mainHand.is(Items.CROSSBOW)) {
            return mainHand;
        }

        if (offHand.is(Items.BOW)
                || offHand.is(Items.CROSSBOW)) {
            return offHand;
        }

        return ItemStack.EMPTY;
    }

    private static ProjectileWeaponState none() {
        return new ProjectileWeaponState(
                WeaponType.NONE,
                ProjectileType.NONE,
                false,
                false,
                0,
                0.0F,
                0
        );
    }

    public WeaponType getWeaponType() {
        return weaponType;
    }

    public ProjectileType getProjectileType() {
        return projectileType;
    }

    public boolean isUsingWeapon() {
        return usingWeapon;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public int getUseTicks() {
        return useTicks;
    }

    public float getDrawProgress() {
        return drawProgress;
    }

    public int getMultishotLevel() {
        return multishotLevel;
    }

    public boolean hasMultishot() {
        return multishotLevel > 0;
    }

    public boolean isBow() {
        return weaponType == WeaponType.BOW;
    }

    public boolean isCrossbow() {
        return weaponType == WeaponType.CROSSBOW;
    }

    public boolean isArrow() {
        return projectileType == ProjectileType.ARROW;
    }

    public boolean isFirework() {
        return projectileType == ProjectileType.FIREWORK;
    }

    public boolean isProjectileWeapon() {
        return weaponType != WeaponType.NONE;
    }
}