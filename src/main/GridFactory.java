package main;

import lwjglutils.OGLBuffers;

public class GridFactory {

    /**
     * @param a počet vrcholů na řádku
     * @param b počet vrcholů ve sloupci
     * @return OGLBuffers
     */
    public static OGLBuffers generateGrid(int a, int b) {
        float[] vb = new float[a * b * 2];
        int index = 0;

        for (int j = 0; j < b; j++) {
            float y = j / (float) (b - 1);
            for (int i = 0; i < a; i++) {
//                System.out.println((i / (float) (a - 1) + " " + y));
                float x = i / (float) (a - 1);
                vb[index++] = x;
                vb[index++] = y;
            }
        }

        int[] ib = new int[(a - 1) * (b - 1) * 2 * 3];
        int index2 = 0;

        for (int r = 0; r < b - 1; r++) {
            int offset = r * a;
            for (int c = 0; c < a - 1; c++) {
                ib[index2++] = offset + c;
                ib[index2++] = offset + c + 1;
                ib[index2++] = offset + c + a;
                ib[index2++] = offset + c + 1;
                ib[index2++] = offset + c + 1 + a;
                ib[index2++] = offset + c + a;
            }
        }

        OGLBuffers.Attrib[] attributes = {
             new OGLBuffers.Attrib("inPosition", 2) // 2 floats per vertex
        };
        return new OGLBuffers(vb, attributes, ib);
    }

}
