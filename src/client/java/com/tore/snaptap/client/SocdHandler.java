package com.tore.snaptap.client;

/**
 * SnapTap / SOCD (Simultaneous Opposite Cardinal Direction) state and resolution logic.
 *
 * <p>On every axis the most recently pressed direction wins ("last input priority"), which is
 * exactly the behaviour Razer markets as SnapTap. The state is static on purpose: a single
 * client input instance is polled every tick and there is nothing to allocate per-input.</p>
 */
public final class SocdHandler {
    private static boolean enabled = true;

    private static boolean prevLeft = false;
    private static boolean prevRight = false;
    private static int lastHorizontalDir = 0; // -1 = links, +1 = rechts

    private static boolean prevForward = false;
    private static boolean prevBackward = false;
    private static int lastVerticalDir = 0; // +1 = vorwärts, -1 = rückwärts

    private SocdHandler() {}

    public static boolean isEnabled() {
        return enabled;
    }

    public static boolean toggleEnabled() {
        enabled = !enabled;
        if (!enabled) {
            reset();
        }
        return enabled;
    }

    public static void reset() {
        prevLeft = false;
        prevRight = false;
        lastHorizontalDir = 0;
        prevForward = false;
        prevBackward = false;
        lastVerticalDir = 0;
    }

    public static float resolveHorizontal(boolean leftPressed, boolean rightPressed) {
        updateHorizontalState(leftPressed, rightPressed);

        if (!enabled) {
            return (rightPressed ? 1.0f : 0.0f) - (leftPressed ? 1.0f : 0.0f);
        }

        if (leftPressed && rightPressed) {
            return lastHorizontalDir != 0 ? (float) lastHorizontalDir : 0.0f;
        }
        if (leftPressed) {
            return -1.0f;
        }
        if (rightPressed) {
            return 1.0f;
        }
        return 0.0f;
    }

    public static float resolveVertical(boolean forwardPressed, boolean backwardPressed) {
        updateVerticalState(forwardPressed, backwardPressed);

        if (!enabled) {
            return (forwardPressed ? 1.0f : 0.0f) - (backwardPressed ? 1.0f : 0.0f);
        }

        if (forwardPressed && backwardPressed) {
            return lastVerticalDir != 0 ? (float) lastVerticalDir : 0.0f;
        }
        if (forwardPressed) {
            return 1.0f;
        }
        if (backwardPressed) {
            return -1.0f;
        }
        return 0.0f;
    }

    private static void updateHorizontalState(boolean left, boolean right) {
        if (left && !prevLeft) {
            lastHorizontalDir = -1;
        }
        if (right && !prevRight) {
            lastHorizontalDir = 1;
        }
        if (!left && lastHorizontalDir == -1) {
            lastHorizontalDir = right ? 1 : 0;
        }
        if (!right && lastHorizontalDir == 1) {
            lastHorizontalDir = left ? -1 : 0;
        }
        prevLeft = left;
        prevRight = right;
    }

    private static void updateVerticalState(boolean forward, boolean backward) {
        if (forward && !prevForward) {
            lastVerticalDir = 1;
        }
        if (backward && !prevBackward) {
            lastVerticalDir = -1;
        }
        if (!forward && lastVerticalDir == 1) {
            lastVerticalDir = backward ? -1 : 0;
        }
        if (!backward && lastVerticalDir == -1) {
            lastVerticalDir = forward ? 1 : 0;
        }
        prevForward = forward;
        prevBackward = backward;
    }
}
