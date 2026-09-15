package main.reachhud;

import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ReachHUD {

	public static final String MOD_ID = "reachhud";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private ReachHUD() {
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}