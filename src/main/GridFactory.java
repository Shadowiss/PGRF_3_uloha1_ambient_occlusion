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

//        float[] vb = new float[a * b * 2];
//        int index = 0;
//
//        for (int j = 0; j < b; j++) {
//            float y = j / (float) (b - 1);
//            for (int i = 0; i < a; i++) {
////                System.out.println((i / (float) (a - 1) + " " + y));
//                float x = i / (float) (a - 1);
//                vb[index++] = x;
//                vb[index++] = y;
//            }
//        }
//
//        int[] ib = new int[(a - 1) * (b - 1) * 2 * 3];
//        int index2 = 0;
//
//        for (int r = 0; r < b - 1; r++) {
//            int offset = r * a;
//            for (int c = 0; c < a - 1; c++) {
//                ib[index2++] = offset + c;
//                ib[index2++] = offset + c + 1;
//                ib[index2++] = offset + c + a;
//                ib[index2++] = offset + c + 1;
//                ib[index2++] = offset + c + 1 + a;
//                ib[index2++] = offset + c + a;
//            }
//        }

        float[] vb= new float[(a+1)*(b+1)*2];
        int index = 0;
        for(int i = 0; i<=b;i++) {
            for (float j = 0; j <= a; j++) {
                vb[index++] = j*1f /a;
                vb[index++] = i*1f/b;
            }

        }

        int[] ib= new int[a*b*6];


        int index2 = 0;
        int k = 0;
        for(int y = 0; y<b;y++) {

            for(int x= 0;x<a;x++)
            {
                k=x+y*(a+1);
                ib[index2++]=k;
                ib[index2++]=k+(a+1);
                ib[index2++]=k+(a+1)+1;
                ib[index2++]=k;
                ib[index2++]=k+(a+1)+1;
                ib[index2++]=k+1;
            }

        }

        int[] ibs = new int[((a+1)*(b+1)*2)-2];
        int index3 = 0;
        k = 0;

        for (int j = 0; j < b ; j++) {
            k = j * (a + 1);
            for (int i = 0; i < a+1; i++) {
                if (j % 2 == 0) {
                    if (i == a) {
                        ibs[index3++] = k + i;
                        ibs[index3++] = k + (b + 1) + i;
                        ibs[index3++] = k + (b + 1) + i;
                        ibs[index3++] = k + (b + 1) + i;
                    } else {
                        ibs[index3++] = k + i;
                        ibs[index3++] = k + (b + 1) + i;
                    }
                } else {
                    if (i == a) {
                        ibs[index3++] = k + (b + 1);
                        ibs[index3++] = k;
                        ibs[index3++] = k;
                        ibs[index3++] = k;
                    } else {
                        ibs[index3++] = k + 2 * (a + 1) - 1 - i;
                        ibs[index3++] = k + a - i;
                    }
                }

            }
        }

        OGLBuffers.Attrib[] attributes = {
             new OGLBuffers.Attrib("inPosition", 2) // 2 floats per vertex
        };
        switch (mode) {
            case 0 -> buffer = new OGLBuffers(vb, attributes, ib);
            case 1 -> buffer= new OGLBuffers(vb, attributes, ibs);
            default -> throw new IllegalStateException("Unexpected value: " + mode);
        }
        return buffer;
        //return new OGLBuffers(vb, attributes, ib);
    }

}
