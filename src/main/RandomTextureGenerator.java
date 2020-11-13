package main;

import lwjglutils.OGLTexImageFloat;
import lwjglutils.OGLTexture2D;
import transforms.Vec3D;

import java.util.Optional;
import java.util.Random;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class RandomTextureGenerator {

    public static OGLTexture2D getTexture() {
        int dataWidth = 128;

        Random random = new Random();
        // pole náhodných dat
        OGLTexImageFloat image = new OGLTexImageFloat(dataWidth, 1, 4);

        for (int i = 0; i < dataWidth; i++) {
            Vec3D v = new Vec3D(
                    random.nextFloat() * 2 - 1,
                    random.nextFloat() * 2 - 1,
                    random.nextFloat() * 2 - 1
            ); // v krychli

            Optional<Vec3D> vOptional = v.normalized();
            if (vOptional.isEmpty()) continue;

            Vec3D u = vOptional.get();// na obvodu koule
            Vec3D w = u.mul(random.nextFloat());// rozmístěno v prostoru koule

            float scale = i / (float) dataWidth;
            scale = lerp(0.1f, 1, scale * scale);
            Vec3D t = w.mul(scale); // umístit blíže ke středu koule

            image.setPixel(i, 0, 0, (float) t.getX());
            image.setPixel(i, 0, 1, (float) t.getY());
            image.setPixel(i, 0, 2, (float) t.getZ());
        }

        OGLTexture2D texture = new OGLTexture2D(image);
        texture.bind();
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        return texture;
    }

    @SuppressWarnings("SameParameterValue")
    private static float lerp(float a, float b, float f) {
        return a + f * (b - a);
    }

}
