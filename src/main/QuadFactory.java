package main;

import lwjglutils.OGLBuffers;

public class QuadFactory {

    public static OGLBuffers getQuad() {
        float[] vbData = {-1, -1, 1, -1, 1, 1, -1, 1};
        int[] ibData = {0, 1, 2, 0, 2, 3};

        OGLBuffers.Attrib[] attributes = {
                new OGLBuffers.Attrib("inPosition", 2)
        };

        return new OGLBuffers(vbData, attributes, ibData);
    }

}
