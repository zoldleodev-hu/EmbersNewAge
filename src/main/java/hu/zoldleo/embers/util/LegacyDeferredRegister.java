package hu.zoldleo.embers.util;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class LegacyDeferredRegister<T> {

	DeferredRegister<T> current;
	DeferredRegister<T> old;

	public LegacyDeferredRegister(DeferredRegister<T> current, DeferredRegister<T> old) {
		this.current = current;
		this.old = old;
	}

	public <I extends T> DeferredHolder<T, I> register(final String name, final Supplier<? extends I> sup) {
        DeferredHolder<T, I> reg = this.current.register(name, sup);

		this.old.register(name, sup);
		return reg;
	}
}