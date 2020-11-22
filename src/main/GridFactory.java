package main;

import lwjglutils.OGLBuffers;

public class GridFactory {

    /**
     * @param a počet vrcholů na řádku
     * @param b počet vrcholů ve sloupci
     * @return OGLBuffers
     */
    public static OGLBuffers generateGrid(int a, int b, int mode) {
        OGLBuffers buffer;
        /*
        Vertex buffer
         */
        float[] vb = new float[a * b * 2];
        int index = 0;

        for (int j = 0; j < b; j++) {
            float y = j / (float) (b - 1);
            for (int i = 0; i < a; i++) {
                //System.out.println((i / (float) (a - 1) + " " + y));
                float x = i / (float) (a - 1);
                vb[index++] = x;
                vb[index++] = y;
            }
        }
        /*
        Index buffer for triangle list
         */
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
        /*
        Index buffer for Triangle strip
         */
        int[] ibs = new int[(2*a)*(b)-2];
        int index3 = 0;
        int k;

        for (int r = 0; r < b-1; r++) {
            k = r * a;
            for (int c = 0; c < a; c++) {
                if (r % 2 == 0) {
                    if (c == a-1) {
                        ibs[index3++] = k + c;
                        ibs[index3++] = k + (b) + c;
                        ibs[index3++] = k + (b) + c;
                        ibs[index3++] = k + (b) + c;
                    } else {
                        ibs[index3++] = k + c;
                        ibs[index3++] = k + (b) + c;
                    }
                } else {
                    if (c == a-1) {
                        ibs[index3++] = k +b;
                        ibs[index3++] = k ;
                        ibs[index3++] = k;
                        ibs[index3++] = k;
                    } else {
                        ibs[index3++] = k + 2 * (a) - 1 - c;
                        ibs[index3++] = (k-1) + a - c;
                    }
                }
            }
        }


        OGLBuffers.Attrib[] attributes = {
             new OGLBuffers.Attrib("inPosition", 2) // 2 floats per vertex
        };
        /*
        Switch between IB and IBS
         */
        switch (mode) {
            case 0 -> buffer = new OGLBuffers(vb, attributes, ib);
            case 1 -> buffer= new OGLBuffers(vb, attributes, ibs);
            default -> throw new IllegalStateException("Unexpected value: " + mode);
        }
        return buffer;
    }

}
