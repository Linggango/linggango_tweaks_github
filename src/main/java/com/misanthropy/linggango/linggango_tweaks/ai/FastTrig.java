package com.misanthropy.linggango.linggango_tweaks.ai;

public class FastTrig {
    private static final int ATAN2_DIM = (int) Math.sqrt(65536.0F);
    private static final float INV_ATAN2_DIM_MINUS_1 = 1.0F / (float) (ATAN2_DIM - 1);
    private static final float[] atan2 = new float[65536];
    public static void init() {
        for (int i = 0; i < ATAN2_DIM; ++i) {
            for (int j = 0; j < ATAN2_DIM; ++j) {
                float x0 = (float) i / (float) ATAN2_DIM;
                float y0 = (float) j / (float) ATAN2_DIM;
                atan2[j * ATAN2_DIM + i] = (float) Math.atan2(y0, x0);
            }
        }
    }

    public static float atan2(double y, double x) {
        float add;
        float mul;
        if (x < 0.0D) {if (y < 0.0D) { x = -x;y = -y; mul = 1.0F;
            } else { x = -x; mul = -1.0F;
            } add = -(float) Math.PI;
        } else {if (y < 0.0D) {y = -y;mul = -1.0F;} else {mul = 1.0F;} add = 0.0F;
        }

        double invDiv = 1.0D / ((Math.max(x, y)) * (double) INV_ATAN2_DIM_MINUS_1);
        int xi = (int) (x * invDiv);
        int yi = (int) (y * invDiv);
        return (atan2[yi * ATAN2_DIM + xi] + add) * mul;
    }
}