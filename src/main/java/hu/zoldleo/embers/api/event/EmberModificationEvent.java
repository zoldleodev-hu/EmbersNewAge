package hu.zoldleo.embers.api.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class EmberModificationEvent extends LivingEvent {
    protected final Map<AttributeModifier.Operation, List<Double>> modifiers = new HashMap<>();
    protected final double amount;

    public EmberModificationEvent(LivingEntity entity, double amount) {
        super(entity);
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }

    public void addModifier(double amount, AttributeModifier.Operation operation) {
        modifiers.computeIfAbsent(operation, op -> new ArrayList<>()).add(amount);
    }

    public double getFinal() {
        double base = amount;
        for (double d : modifiers.getOrDefault(AttributeModifier.Operation.ADD_VALUE, List.of()))
            base += d;
        double total = base;
        for (double d : modifiers.getOrDefault(AttributeModifier.Operation.ADD_MULTIPLIED_BASE, List.of()))
            total += base * d;
        for (double d : modifiers.getOrDefault(AttributeModifier.Operation.ADD_MULTIPLIED_BASE, List.of()))
            total *= 1 + d;
        return Math.max(0, total);
    }

    public static class Add extends EmberModificationEvent {
        public Add(LivingEntity entity, double amount) {
            super(entity, amount);
        }
    }

    public static class Remove extends EmberModificationEvent {
        public Remove(LivingEntity entity, double amount) {
            super(entity, amount);
        }
    }
}