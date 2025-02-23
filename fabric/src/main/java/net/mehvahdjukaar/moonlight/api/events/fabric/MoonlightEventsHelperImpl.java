package net.mehvahdjukaar.moonlight.api.events.fabric;

import net.mehvahdjukaar.moonlight.api.events.SimpleEvent;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.HitResult;

import java.util.*;
import java.util.function.Consumer;

public class MoonlightEventsHelperImpl {

    private static final Map<Class<? extends SimpleEvent>, List<Consumer<? extends SimpleEvent>>> LISTENERS = new IdentityHashMap<>();


    public static <T extends SimpleEvent> void addListener(Consumer<T> listener, Class<T> eventClass) {
        LISTENERS.computeIfAbsent(eventClass, e -> new ArrayList<>()).add(listener);
    }

    @SuppressWarnings("unchecked")
    public static <T extends SimpleEvent> void postEvent(T event, Class<T> eventClass) {
        var consumers = LISTENERS.get(eventClass);
        if (consumers != null) {
            ((List<Consumer<T>>) (Object) consumers).forEach(e -> e.accept(event));
        }
    }

}
