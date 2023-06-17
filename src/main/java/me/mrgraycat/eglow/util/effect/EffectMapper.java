package me.mrgraycat.eglow.util.effect;

import com.google.common.collect.ImmutableMap;
import lombok.experimental.UtilityClass;
import me.mrgraycat.eglow.manager.DataManager;
import me.mrgraycat.eglow.manager.glow.IEGlowEffect;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

@UtilityClass
public class EffectMapper {

    private final Map<Predicate<String>, Function<String, IEGlowEffect>> SWITCH_EFFECT_SPEED_MAP = ImmutableMap.of(
            (effectName) -> effectName.contains("slow"), (effectName) -> DataManager.getEGlowEffect(effectName.replace("slow", "fast")),
            (effectName) -> effectName.contains("fast"), (effectName) -> DataManager.getEGlowEffect(effectName.replace("fast", "slow")
    ));

    public IEGlowEffect flip(IEGlowEffect effect) {
        return flip(effect.getName());
    }

    public IEGlowEffect flip(String effectName) {
        return SWITCH_EFFECT_SPEED_MAP.entrySet().stream()
                .filter((entry) -> entry.getKey().test(effectName))
                .map((entry) -> entry.getValue().apply(effectName))
                .findFirst()
                .orElse(null);
    }
}
