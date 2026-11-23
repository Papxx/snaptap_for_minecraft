package com.tore.snaptap.client.mixin;

import com.tore.snaptap.client.SocdHandler;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Thin bridge between vanilla keyboard input and {@link SocdHandler}.
 *
 * <p>Since Minecraft 1.21.2/26.x {@code KeyboardInput} no longer exposes the mutable
 * {@code forwardImpulse}/{@code leftImpulse} floats. Its {@code tick()} builds an immutable
 * {@link Input} record and a normalised {@link Vec2} called {@code moveVector}. This mixin therefore
 * lets vanilla build both fields and then immediately rewrites them with the SOCD-resolved values.</p>
 */
@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends ClientInput {

    @Inject(method = "tick", at = @At("TAIL"))
    private void snaptap$applySocd(CallbackInfo ci) {
        if (!SocdHandler.isEnabled()) {
            return;
        }

        // keyPresses/moveVector are inherited from ClientInput, which this mixin extends so that
        // the protected moveVector field can be written directly.
        Input pressed = this.keyPresses;

        boolean forward = pressed.forward();
        boolean backward = pressed.backward();
        boolean left = pressed.left();
        boolean right = pressed.right();

        float vertical = SocdHandler.resolveVertical(forward, backward);
        float horizontal = SocdHandler.resolveHorizontal(left, right);

        // resolveVertical(): +1 = forward, -1 = backward; resolveHorizontal(): -1 = left, +1 = right.
        boolean resolvedForward = vertical > 0.0F;
        boolean resolvedBackward = vertical < 0.0F;
        boolean resolvedLeft = horizontal < 0.0F;
        boolean resolvedRight = horizontal > 0.0F;

        if (resolvedForward != forward || resolvedBackward != backward
                || resolvedLeft != left || resolvedRight != right) {
            this.keyPresses = new Input(
                    resolvedForward,
                    resolvedBackward,
                    resolvedLeft,
                    resolvedRight,
                    pressed.jump(),
                    pressed.shift(),
                    pressed.sprint()
            );
        }

        // In this Minecraft version Vec2.x is the left impulse and Vec2.y the forward impulse,
        // both positive along their axis; vanilla normalises the vector, so we do the same.
        this.moveVector = new Vec2(-horizontal, vertical).normalized();
    }
}
