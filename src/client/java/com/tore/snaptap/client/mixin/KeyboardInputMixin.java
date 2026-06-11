package com.tore.snaptap.client.mixin;

import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends ClientInput {
    @Unique private boolean snaptap$wasLeft, snaptap$wasRight;
    @Unique private boolean snaptap$wasForward, snaptap$wasBackward;
    @Unique private int snaptap$lastHorizontal = 0; // -1 = links, 1 = rechts
    @Unique private int snaptap$lastVertical = 0;   //  1 = vorwärts, -1 = rückwärts

    @Inject(method = "tick", at = @At("TAIL"))
    private void snaptap$apply(CallbackInfo ci) {
        Input in = this.keyPresses;

        // Steigende Flanke: welche Taste wurde zuletzt NEU gedrückt?
        if (in.left() && !snaptap$wasLeft) snaptap$lastHorizontal = -1;
        if (in.right() && !snaptap$wasRight) snaptap$lastHorizontal = 1;
        if (in.forward() && !snaptap$wasForward) snaptap$lastVertical = 1;
        if (in.backward() && !snaptap$wasBackward) snaptap$lastVertical = -1;

        boolean left = in.left(), right = in.right();
        boolean forward = in.forward(), backward = in.backward();

        if (left && right) {
            left = (snaptap$lastHorizontal == -1);
            right = !left;
        }
        if (forward && backward) {
            forward = (snaptap$lastVertical == 1);
            backward = !forward;
        }

        if (left != in.left() || right != in.right()
                || forward != in.forward() || backward != in.backward()) {
            this.keyPresses = new Input(forward, backward, left, right,
                    in.jump(), in.shift(), in.sprint());
            float fwdImpulse  = forward == backward ? 0.0f : (forward ? 1.0f : -1.0f);
            float leftImpulse = left == right       ? 0.0f : (left    ? 1.0f : -1.0f);
            this.moveVector = new Vec2(leftImpulse, fwdImpulse);
        }

        snaptap$wasLeft = in.left();
        snaptap$wasRight = in.right();
        snaptap$wasForward = in.forward();
        snaptap$wasBackward = in.backward();
    }
}