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
    private OGLBuffers buffers, quad;

    private int sceneProgram, ssaoProgram, shadeProgram, objProgram;
    private int locSceneView, locSceneProjection, locSceneTemp,
            locSsaoProjection, locShadeView, locShadeLight, locObjMat,
            locLightRotZ1, locLightRotZ2, locSceneLight, locSceneTexture;

    private Camera camera;
    private Mat4 projection;
    private OGLTexture2D texture1;
    private OGLTexture2D.Viewer viewer;
    private OGLRenderTarget sceneRT, ssaoRT;
    private OGLTexture2D randomTexture;

    private int displayMode = 0;
    private int object = 1;
    private int triangleMode = 0;
    private int animation = 1;
    private int cameraMode = 0;
    private int mode = 0;
    private int lightMode = 0;
    private int ducky = 0;
    private float radians = 0;
    private int textureMode = 1;

    private OGLModelOBJ model;
    Mat4 swapYZ = new Mat4(new double[]{
            1, 0, 0, 0,
            0, 0, 1, 0,
            0, 1, 0, 0,
            0, 0, 0, 1,
    });
    private Mat4RotZ rotZ = new Mat4RotZ(Math.toRadians(radians));

    @Override
    public void init() {
        OGLUtils.printOGLparameters();
        OGLUtils.printLWJLparameters();
        OGLUtils.printJAVAparameters();
        OGLUtils.shaderCheck();

        textRenderer = new OGLTextRenderer(LwjglWindow.WIDTH, LwjglWindow.HEIGHT);

        sceneProgram = ShaderUtils.loadProgram("/scene");
        locSceneView = glGetUniformLocation(sceneProgram, "view");
        locSceneProjection = glGetUniformLocation(sceneProgram, "projection");
        locSceneTemp = glGetUniformLocation(sceneProgram, "temp");
        locLightRotZ1 = glGetUniformLocation(sceneProgram, "rotZ");
        locSceneLight = glGetUniformLocation(sceneProgram, "lightMode");
        locSceneTexture = glGetUniformLocation(sceneProgram, "textureMode");

        ssaoProgram = ShaderUtils.loadProgram("/ssao");
        locSsaoProjection = glGetUniformLocation(ssaoProgram, "projection");

        shadeProgram = ShaderUtils.loadProgram("/shade");
        locShadeView = glGetUniformLocation(shadeProgram, "view");
        locShadeLight = glGetUniformLocation(shadeProgram, "lightMode");
        locLightRotZ2 = glGetUniformLocation(shadeProgram, "rotZ");

        objProgram = ShaderUtils.loadProgram("/ducky");
        locObjMat = glGetUniformLocation(objProgram, "mat");

        quad = QuadFactory.getQuad();

        camera = new Camera()
                .withPosition(new Vec3D(2, 2, 2)) // pozice pozorovatele
                .withAzimuth(5 / 4f * Math.PI) // otočení do strany o (180+45) stupňů v radiánech
                .withZenith(-1 / 5f * Math.PI); // otočení (90/5) stupňů dolů

        try {
            texture1 = new OGLTexture2D("./textures/earth.jpg");
            model = new OGLModelOBJ("/obj/ducky.obj");
        } catch (IOException e) {
            e.printStackTrace();
        }

        viewer = new OGLTexture2D.Viewer();

        final int size = 1024;
        sceneRT = new OGLRenderTarget(size, size, 5);
        ssaoRT = new OGLRenderTarget(size, size);
        randomTexture = RandomTextureGenerator.getTexture();
    }

    @Override
    public void display() {
        glEnable(GL_DEPTH_TEST);
        /*
        Switch between camera mode
         */
        switch (cameraMode) {
            case 0 -> projection = new Mat4PerspRH(Math.PI / 3, 600 / 800f, 1, 20);
            case 1 -> projection = new Mat4OrthoRH(10, 7, 1, 20);
        }
        /*
        Animation
         */
        if (animation == 1) {
            if (radians > 360) {
                radians = 0;
            } else {
                radians += 0.05;
            }
        }
        /*
        Render scene + grid mode
         */
        setTriangleMode();
        renderScene();
        renderSSAO();
        renderFinal();
        /*
        Small windows
         */
        viewer.view(sceneRT.getColorTexture(0), -1, 0, 0.5);
        viewer.view(sceneRT.getColorTexture(1), -1, -0.5, 0.5);
        viewer.view(sceneRT.getColorTexture(2), -1, -1, 0.5);
        viewer.view(sceneRT.getDepthTexture(), -0.5, -1, 0.5);
        viewer.view(ssaoRT.getColorTexture(), -1, 0.5, 0.5);

        /*
          Renders text in window
         */
        textRenderer.addStr2D(15, 20, "Camera [MOUSE], Movement [WASD],Change camera(persp,ortho) [C], Fill,line,dot [1(non numeric)]," +
                " Animation(on/off) [2(non numeric)], Triangle(list/strip) [T]," +
                "Change object [O], Show ducky [Y], Change light [X], Show plane [M]");
        textRenderer.addStr2D(15, 35, "If in LightMode 5 (Texture and Blinn-phong) hide texture [U]");
        setString();
        textRenderer.addStr2D(LwjglWindow.WIDTH - 190, LwjglWindow.HEIGHT - 3, "PGRF - Jirasek Jiri 2020");
    }

    private void renderScene() {
        /*
        Switch between ducky program or object program
         */
        switch (ducky) {
            case 0 -> glUseProgram(sceneProgram);
            case 1 -> glUseProgram(objProgram);
        }
        /*
        Switch between FILL/LINE/DOT style
         */
        switch (displayMode) {
            case 0 -> glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
            case 1 -> glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
            case 2 -> glPolygonMode(GL_FRONT_AND_BACK, GL_POINT);
        }

        sceneRT.bind();
        glClearColor(0.4f, 0, 0, 1);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glUniformMatrix4fv(locSceneView, false, camera.getViewMatrix().floatArray());
        glUniformMatrix4fv(locSceneProjection, false, projection.floatArray());
        glUniformMatrix4fv(locObjMat, false, ToFloatArray.convert(swapYZ.mul(camera.getViewMatrix()).mul(projection)));
        glUniformMatrix4fv(locLightRotZ1, false, rotZ.floatArray());
        glUniform1i(locSceneTemp, object);
        glUniform1i(locSceneLight, lightMode);
        glUniform1i(locSceneTexture, textureMode);
        /*
        Switch between ducky and objects + mode ( with plane or without plane)
         */
        switch (ducky) {
            case 1 -> {
                buffers = model.getBuffers();
                buffers.draw(model.getTopology(), objProgram);
            }
            case 0 -> {

                texture1.bind(sceneProgram, "texture1", 0);
                setTriangleMode();
                glUniform1i(locSceneTemp, 10);
                setTriangleMode();
                if (mode == 1) {
                    glUniform1i(locSceneTemp, 8);
                    setTriangleMode();
                    glUniform1i(locSceneTemp, 9);
                    setTriangleMode();
                }
            }
        }
    }

    private void renderSSAO() {
        glUseProgram(ssaoProgram);
        /*
        Switch between FILL/LINE/DOT style
         */
        switch (displayMode) {
            case 0 -> glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
            case 1 -> glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
            case 2 -> glPolygonMode(GL_FRONT_AND_BACK, GL_POINT);
        }
        ssaoRT.bind();
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glDisable(GL_DEPTH_TEST);

        sceneRT.bindColorTexture(ssaoProgram, "positionTexture", 0, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        sceneRT.bindColorTexture(ssaoProgram, "normalTexture", 1, 1);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        randomTexture.bind(ssaoProgram, "randomTexture", 2);

        glUniformMatrix4fv(locSsaoProjection, false, projection.floatArray());
        quad.draw(GL_TRIANGLES, ssaoProgram);
    }

    private void renderFinal() {
        glUseProgram(shadeProgram);
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, LwjglWindow.WIDTH, LwjglWindow.HEIGHT);
        glClearColor(0.1f, 0.1f, 0.1f, 1);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glUniform1i(locShadeLight, lightMode);
        rotZ = new Mat4RotZ(radians);
        glUniformMatrix4fv(locLightRotZ2, false, rotZ.floatArray());
        glUniform1i(locShadeLight, lightMode);

        sceneRT.bindColorTexture(shadeProgram, "positionTexture", 0, 0);
        sceneRT.bindColorTexture(shadeProgram, "normalTexture", 1, 1);
        sceneRT.bindDepthTexture(shadeProgram, "depthTexture", 2);
        ssaoRT.bindColorTexture(shadeProgram, "ssaoTexture", 3, 0);
        sceneRT.bindColorTexture(shadeProgram, "imageTexture", 4, 3);

        glUniformMatrix4fv(locShadeView, false, camera.getViewMatrix().floatArray());

        quad.draw(GL_TRIANGLES, shadeProgram);
    }

    /**
     * Method that sets mode of grid. Defines which index buffer is used (Triangle List/Strip) and generates new grid
     */
    private void setTriangleMode() {
        switch (triangleMode) {
            case 0 -> {
                buffers = GridFactory.generateGrid(50, 50, triangleMode);
                buffers.draw(GL_TRIANGLES, sceneProgram);
            }
            case 1 -> {
                buffers = GridFactory.generateGrid(50, 50, triangleMode);
                buffers.draw(GL_TRIANGLE_STRIP, sceneProgram);
            }
        }
    }

    /**
     * Generates string which function is active "Plane: On/Off etc..."
     */
    private void setString() {
        String display;
        String camera;
        String anim;
        String triangle;
        String light;
        String plane;
        String texture;

        switch (displayMode) {
            case 0 -> display = "Fill";
            case 1 -> display = "Line";
            case 2 -> display = "Dot";
            default -> throw new IllegalStateException("Unexpected value: " + displayMode);
        }
        switch (cameraMode) {
            case 0 -> camera = "Persp";
            case 1 -> camera = "Ortho";
            default -> throw new IllegalStateException("Unexpected value: " + cameraMode);
        }
        switch (animation) {
            case 0 -> anim = "off";
            case 1 -> anim = "on";
            default -> throw new IllegalStateException("Unexpected value: " + animation);
        }
        switch (triangleMode) {
            case 0 -> triangle = "List";
            case 1 -> triangle = "Strip";
            default -> throw new IllegalStateException("Unexpected value: " + triangleMode);
        }
        switch (lightMode) {
            case 0 -> light = "Blinn-Phong(A,D,S) + color";
            case 1 -> light = "Ambient";
            case 2 -> light = "Ambient + Diffuse";
            case 3 -> light = "Ambient + Diffuse + Specular";
            case 4 -> light = "Reflector";
            case 5 -> light = "Texture + Blinn-Phong";
            default -> throw new IllegalStateException("Unexpected value: " + lightMode);
        }
        switch (mode) {
            case 0 -> plane = "Off";
            case 1 -> plane = "On";
            default -> throw new IllegalStateException("Unexpected value: " + mode);
        }
        switch (textureMode) {
            case 0 -> texture = "Off";
            case 1 -> texture = "On";
            default -> throw new IllegalStateException("Unexpected value: " + mode);
        }
        textRenderer.addStr2D(LwjglWindow.WIDTH / 2 + 5, LwjglWindow.HEIGHT - 95, "Mode: " + display);
        textRenderer.addStr2D(LwjglWindow.WIDTH / 2 + 5, LwjglWindow.HEIGHT - 80, "Camera: " + camera);
        textRenderer.addStr2D(LwjglWindow.WIDTH / 2 + 5, LwjglWindow.HEIGHT - 65, "Animation: " + anim);
        textRenderer.addStr2D(LwjglWindow.WIDTH / 2 + 5, LwjglWindow.HEIGHT - 50, "Triangle mode: " + triangle);
        textRenderer.addStr2D(LwjglWindow.WIDTH / 2 + 5, LwjglWindow.HEIGHT - 35, "Light mode: " + light);
        textRenderer.addStr2D(LwjglWindow.WIDTH / 2 + 5, LwjglWindow.HEIGHT - 20, "Planes: " + plane);
        textRenderer.addStr2D(LwjglWindow.WIDTH / 2 + 5, LwjglWindow.HEIGHT - 5, "Texture : " + texture);

    }

    @Override
    public GLFWWindowSizeCallback getWsCallback() {
        return WsCallback;
    }

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

    private final GLFWWindowSizeCallback WsCallback = new GLFWWindowSizeCallback() {
        @Override
        public void invoke(long window, int w, int h) {
            if (w > 0 && h > 0 &&
                    (w != LwjglWindow.WIDTH || h != LwjglWindow.HEIGHT)) {
                LwjglWindow.WIDTH = w;
                LwjglWindow.HEIGHT = h;
                projection = new Mat4PerspRH(Math.PI / 4, LwjglWindow.WIDTH / (double) LwjglWindow.HEIGHT, 0.1, 100.0);
                if (textRenderer != null)
                    textRenderer.resize(LwjglWindow.WIDTH, LwjglWindow.HEIGHT);
            }
        }
    };

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
                    case GLFW_KEY_1 -> {
                        if (displayMode == 2)
                            displayMode = 0;
                        else {
                            displayMode += 1;
                        }

                    }
                    case GLFW_KEY_2 -> {
                        if (animation == 1)
                            animation = 0;
                        else {
                            animation += 1;
                        }
                    }
                    case GLFW_KEY_O -> {
                        if (object == 7)
                            object = 1;
                        else {
                            object += 1;
                        }
                    }
                    case GLFW_KEY_T -> {
                        if (triangleMode == 1) {
                            triangleMode = 0;
                        } else {
                            triangleMode += 1;
                        }
                    }
                    case GLFW_KEY_C -> {
                        if (cameraMode == 1)
                            cameraMode = 0;
                        else {
                            cameraMode += 1;
                        }
                    }
                    case GLFW_KEY_M -> {
                        if (mode == 1)
                            mode = 0;
                        else {
                            mode += 1;
                        }
                    }
                    case GLFW_KEY_X -> {
                        if (lightMode == 5)
                            lightMode = 0;
                        else {
                            lightMode += 1;
                        }
                    }
                    case GLFW_KEY_Z -> {
                        if (ducky == 1)
                            ducky = 0;
                        else {
                            ducky += 1;
                        }
                    }
                    case GLFW_KEY_U -> {
                        if (textureMode == 1)
                            textureMode = 0;
                        else {
                            textureMode += 1;
                        }
                    }
                }
            }
        }
    };

}
