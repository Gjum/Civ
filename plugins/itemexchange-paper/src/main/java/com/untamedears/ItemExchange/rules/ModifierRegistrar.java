package com.untamedears.itemexchange.rules;

import com.untamedears.itemexchange.ItemExchangePlugin;
import com.untamedears.itemexchange.rules.interfaces.ModifierData;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 *
 */
public final class ModifierRegistrar {

	private final ItemExchangePlugin plugin;

	private final Set<ModifierData<?>> modifiers = new HashSet<>();

	public ModifierRegistrar(ItemExchangePlugin plugin) {
		this.plugin = plugin;
	}

	/**
	 * Registers a modifier.
	 *
	 * @param <T> The type of the modifier.
	 * @param modifier The modifier to register.
	 */
	public <T extends ModifierData<T>> void registerModifier(T modifier) {
		if (modifier == null) {
			throw new IllegalArgumentException("Cannot register a null modifier.");
		}
		if (this.modifiers.contains(modifier)) {
			throw new IllegalArgumentException("That modifier is already registered.");
		}
		if (!Modifier.isFinal(modifier.getClass().getModifiers())) {
			throw new IllegalArgumentException("That modifier is not final.");
		}
		this.modifiers.add(modifier);
		this.plugin.commandManager().registerCommand(modifier);
	}

	/**
	 * Deregisters a modifier from use.
	 *
	 * @param modifier The modifier to deregister.
	 */
	public void deregisterModifier(ModifierData<?> modifier) {
		if (modifier == null) {
			return;
		}
		this.modifiers.remove(modifier);
		this.plugin.commandManager().deregisterCommand(modifier);
	}

	/**
	 * Determines whether a modifier has been registered.
	 *
	 * @param clazz The class of the modifier to check.
	 * @return Returns a template modifier if the class is registered.
	 */
	@SuppressWarnings("unchecked")
	public <T extends ModifierData<T>> T getModifier(Class<T> clazz) {
		if (clazz == null) {
			return null;
		}
		return (T) this.modifiers.stream()
				.filter(template -> clazz == template.getClass())
				.findFirst().orElse(null);
	}

	/**
	 * Retrieves all registered modifiers in order.
	 *
	 * @return Returns a stream of all registered modifiers in order.
	 */
	public Stream<ModifierData<?>> getModifiers() {
		return this.modifiers.stream().filter(Objects::nonNull).sorted();
	}

	/**
	 * De-registers all registered modifiers.
	 */
	public void reset() {
		this.modifiers.clear();
	}

}
