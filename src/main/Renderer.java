package main;

import lwjglutils.*;
import org.lwjgl.glfw.*;
import transforms.*;

import java.io.IOException;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Renderer extends AbstractRenderer {

    private int shaderProgram1, shaderProgram2;
    private OGLBuffers buffers;

    private int locView, locProjection, locTemp, locLightPos;
    private int locView2, locProjection2;

    private Camera camera;
    private Camera cameraLight;
    private Mat4 projection;

    private OGLTexture2D texture1;
    private OGLTexture2D.Viewer viewer;
    private OGLRenderTarget renderTarget;

    private int displayMode=0;

    @Override
    public void init() {
        OGLUtils.printOGLparameters();
        OGLUtils.printLWJLparameters();
        OGLUtils.printJAVAparameters();
        OGLUtils.shaderCheck();

        textRenderer = new OGLTextRenderer(LwjglWindow.WIDTH, LwjglWindow.HEIGHT);

        shaderProgram1 = ShaderUtils.loadProgram("/start");
        shaderProgram2 = ShaderUtils.loadProgram("/postProc");

        locLightPos = glGetUniformLocation(shaderProgram1, "lightPos");
        locView = glGetUniformLocation(shaderProgram1, "view");
        locProjection = glGetUniformLocation(shaderProgram1, "projection");
        locTemp = glGetUniformLocation(shaderProgram1, "temp");

        locView2 = glGetUniformLocation(shaderProgram2, "view");
        locProjection2 = glGetUniformLocation(shaderProgram2, "projection");

        buffers = GridFactory.generateGrid(100, 100);

//        camera = new Camera(
//                new Vec3D(6, 6, 5),
//                5 / 4f * Math.PI,
//                -1 / 5f * Math.PI,
//                1,
//                true
//        );
        camera = new Camera()
                .withPosition(new Vec3D(2, 2, 2)) // pozice pozorovatele
                .withAzimuth(5 / 4f * Math.PI) // otočení do strany o (180+45) stupňů v radiánech
                .withZenith(-1 / 5f * Math.PI); // otočení (90/5) stupňů dolů

        cameraLight = new Camera().withPosition(new Vec3D(-5, -3, -1));

//        view = new Mat4ViewRH(
//                new Vec3D(4, 4, 4),
//                new Vec3D(-1, -1, -1),
//                new Vec3D(0, 0, 1)
//        );

        projection = new Mat4PerspRH(Math.PI / 3, 600 / 800f, 1, 20);
//        projection = new Mat4OrthoRH(10, 7, 1, 20);

        try {
            texture1 = new OGLTexture2D("./textures/bricks.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }

        viewer = new OGLTexture2D.Viewer();

        renderTarget = new OGLRenderTarget(1024, 1024);
    }

    @Override
    public void display() {
        glEnable(GL_DEPTH_TEST);
        if (displayMode == 0) {
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        } else {
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        }

        render1();
        //render2();

        //viewer.view(texture1, -1, -1, 0.5);
        //viewer.view(renderTarget.getColorTexture(), -1, -0.5, 0.5);

        textRenderer.addStr2D(LwjglWindow.WIDTH - 90, LwjglWindow.HEIGHT - 3, "PGRF");
    }

    private void render1() {
        glUseProgram(shaderProgram1);

        renderTarget.bind();
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glClearColor(0.4f, 0, 0, 1);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glUniform3fv(locLightPos, ToFloatArray.convert(cameraLight.getPosition()));
        glUniformMatrix4fv(locView, false, camera.getViewMatrix().floatArray());
        glUniformMatrix4fv(locProjection, false, projection.floatArray());

        //texture1.bind(shaderProgram1, "texture1", 0);

        glUniform1f(locTemp, 1.0f);
        buffers.draw(GL_TRIANGLES, shaderProgram1);
        //buffers.draw(GL_TRIANGLE_STRIP, shaderProgram1);
    }

    private void render2() {
        glUseProgram(shaderProgram2);
        renderTarget.bind();

        //glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glClearColor(0, 0.6f, 0, 1);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glViewport(0, 0, LwjglWindow.WIDTH, LwjglWindow.HEIGHT);

        glUniformMatrix4fv(locView2, false, camera.getViewMatrix().floatArray());
        glUniformMatrix4fv(locProjection2, false, projection.floatArray());

        //texture1.bind(shaderProgram2, "texture1", 0);
        renderTarget.getColorTexture().bind(shaderProgram2, "texture1", 0);

        buffers.draw(GL_TRIANGLES, shaderProgram2);
    }

//    @Override
//    public GLFWWindowSizeCallback getWsCallback() {
//        return null; // FIXME
//    }

    @Override
    public GLFWCursorPosCallback getCursorCallback() {
        return cursorPosCallback;
    }

    @Override
    public GLFWMouseButtonCallback getMouseCallback() {
        return mouseButtonCallback;
    }

    @Override
    public GLFWKeyCallback getKeyCallback() {
        return keyCallback;
    }

    private double oldMx, oldMy;
    private boolean mousePressed;

    private final GLFWCursorPosCallback cursorPosCallback = new GLFWCursorPosCallback() {
        @Override
        public void invoke(long window, double x, double y) {
            if (mousePressed) {
                camera = camera.addAzimuth(Math.PI * (oldMx - x) / LwjglWindow.WIDTH);
                camera = camera.addZenith(Math.PI * (y - oldMy) / LwjglWindow.HEIGHT);
                oldMx = x;
                oldMy = y;
            }
        }
    };

    private final GLFWMouseButtonCallback mouseButtonCallback = new GLFWMouseButtonCallback() {
        @Override
        public void invoke(long window, int button, int action, int mods) {
            if (button == GLFW_MOUSE_BUTTON_LEFT) {
                double[] xPos = new double[1];
                double[] yPos = new double[1];
                glfwGetCursorPos(window, xPos, yPos);
                oldMx = xPos[0];
                oldMy = yPos[0];
                mousePressed = action == GLFW_PRESS;
            }
        }
    };

    private final GLFWKeyCallback keyCallback = new GLFWKeyCallback() {
        @Override
        public void invoke(long window, int key, int scancode, int action, int mods) {
            if (action == GLFW_PRESS || action == GLFW_REPEAT) {
                switch (key) {
                    case GLFW_KEY_A -> camera = camera.left(0.1);
                    case GLFW_KEY_D -> camera = camera.right(0.1);
                    case GLFW_KEY_W -> camera = camera.forward(0.1);
                    case GLFW_KEY_S -> camera = camera.backward(0.1);
                    case GLFW_KEY_R -> camera = camera.up(0.1);
                    case GLFW_KEY_F -> camera = camera.down(0.1);
                }
            }
        }
    };

}
