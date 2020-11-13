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

    //private int shaderProgram1, shaderProgram2;
    private OGLBuffers buffers, quad;

    private int sceneProgram, ssaoProgram, shadeProgram;
    private int locSceneView, locSceneProjection, locSceneTemp,
            locSsaoProjection, locShadeView, locSceneTime, locShadeLight;

//    private int locView, locProjection, locTemp, locLightPos, locTime;
//    private int locView2, locProjection2;

    private Camera camera;
    private Camera cameraLight;
    private Mat4 projection;

    private OGLTexture2D texture1;
    private OGLTexture2D.Viewer viewer;
    //    private OGLRenderTarget renderTarget;
    private OGLRenderTarget sceneRT, ssaoRT;
    private OGLTexture2D randomTexture;

    private int displayMode=0;
    private int object = 1;
    private int triangleMode = 0;
    private float time = 1;
    private double difftime=0.01;
    private int animation = 1;
    private int cameraMode = 0;
    private int mode = 0;
    private int lightMode = 0;

    //private OGLModelOBJ model;

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
        locSceneTime = glGetUniformLocation(sceneProgram, "time");


        ssaoProgram = ShaderUtils.loadProgram("/ssao");
        locSsaoProjection = glGetUniformLocation(ssaoProgram, "projection");

        shadeProgram = ShaderUtils.loadProgram("/shade");
        locShadeView = glGetUniformLocation(shadeProgram, "view");
        locShadeLight = glGetUniformLocation(shadeProgram, "lightMode");

//        shaderProgram1 = ShaderUtils.loadProgram("/start");
//        shaderProgram2 = ShaderUtils.loadProgram("/postProc");
//
//        locLightPos = glGetUniformLocation(shaderProgram1, "lightPos");
//        locView = glGetUniformLocation(shaderProgram1, "view");
//        locProjection = glGetUniformLocation(shaderProgram1, "projection");
//        locTemp = glGetUniformLocation(shaderProgram1, "temp");
//
//        locView2 = glGetUniformLocation(shaderProgram2, "view");
//        locProjection2 = glGetUniformLocation(shaderProgram2, "projection");
//
//        locTime = glGetUniformLocation(shaderProgram1, "time");

        buffers = GridFactory.generateGrid(50,50, triangleMode);
        quad = QuadFactory.getQuad();

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
//        camera = new Camera()
//                .withPosition(new Vec3D(4, 6, 6))
//                .withAzimuth(Math.PI * 1.25)
//                .withZenith(Math.PI * -0.125);

        cameraLight = new Camera().withPosition(new Vec3D(-5, -3, -1));

//        view = new Mat4ViewRH(
//                new Vec3D(4, 4, 4),
//                new Vec3D(-1, -1, -1),
//                new Vec3D(0, 0, 1)
//        );



        try {
            texture1 = new OGLTexture2D("./textures/mosaic.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }

        viewer = new OGLTexture2D.Viewer();

        //renderTarget = new OGLRenderTarget(1024, 1024);
        final int size = 1024;
        sceneRT = new OGLRenderTarget(size, size, 4);
        ssaoRT = new OGLRenderTarget(size, size);

        randomTexture = RandomTextureGenerator.getTexture();
    }

    @Override
    public void display() {
        glEnable(GL_DEPTH_TEST);


        switch (cameraMode) {
            case 0 -> projection = new Mat4PerspRH(Math.PI / 3, 600 / 800f, 1, 20);
            case 1 -> projection = new Mat4OrthoRH(10, 7, 1, 20);
        }

        //time for animation
        if(animation == 1) {
            if (time > 2.0) {
                difftime = (-0.01);
            }
            if (time < 1.0) {
                difftime = 0.01;
            }
            time += difftime;
        }
        if(triangleMode == 0) {
            buffers = GridFactory.generateGrid(50, 50, triangleMode);
            buffers.draw(GL_TRIANGLES, sceneProgram);
        }else{
            buffers = GridFactory.generateGrid(50, 50, triangleMode);
            buffers.draw(GL_TRIANGLE_STRIP, sceneProgram);
        }

        renderScene();
        renderSSAO();
        renderFinal();

        viewer.view(sceneRT.getColorTexture(0), -1, 0, 0.5);
        viewer.view(sceneRT.getColorTexture(1), -1, -0.5, 0.5);
        viewer.view(sceneRT.getColorTexture(2), -1, -1, 0.5);
        viewer.view(sceneRT.getDepthTexture(), -0.5, -1, 0.5);
        viewer.view(ssaoRT.getColorTexture(), -1, 0.5, 0.5);

        textRenderer.addStr2D(LwjglWindow.WIDTH - 90, LwjglWindow.HEIGHT - 3, "PGRF");
    }

    private void renderScene() {
        glUseProgram(sceneProgram);
//        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        switch (displayMode){
            case 0 -> glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
            case 1 -> glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
            case 2 -> glPolygonMode(GL_FRONT_AND_BACK, GL_POINT);
        }
        sceneRT.bind();
        glClearColor(0.4f, 0, 0, 1);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glUniformMatrix4fv(locSceneView, false, camera.getViewMatrix().floatArray());
        glUniformMatrix4fv(locSceneProjection, false, projection.floatArray());

        texture1.bind(sceneProgram, "texture1", 0);

        glUniform1f(locSceneTime, time);

        glUniform1i(locSceneTemp, object);
        buffers.draw(GL_TRIANGLES, sceneProgram);
        if(mode == 1) {
            glUniform1i(locSceneTemp, 8);
            buffers.draw(GL_TRIANGLES, sceneProgram);
            glUniform1i(locSceneTemp, 9);
            buffers.draw(GL_TRIANGLES, sceneProgram);
        }
//        switch (mode){
//            case 0:
//                glUniform1i(locSceneTemp, object);
//                buffers.draw(GL_TRIANGLES, sceneProgram);
//            case 1:
//                glUniform1i(locSceneTemp, object);
//                buffers.draw(GL_TRIANGLES, sceneProgram);
//                glUniform1i(locSceneTemp, 7);
//                buffers.draw(GL_TRIANGLES, sceneProgram);
//                glUniform1i(locSceneTemp, 8);
//                buffers.draw(GL_TRIANGLES, sceneProgram);
//            case 2:
//                model = new OGLModelOBJ("/obj/ducky.obj");
//                buffers = model.getBuffers();
//                buffers.draw(GL_TRIANGLES, sceneProgram);
//        }
    }

    private void renderSSAO() {
        glUseProgram(ssaoProgram);
//        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        switch (displayMode){
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

        sceneRT.bindColorTexture(shadeProgram, "positionTexture", 0, 0);
        sceneRT.bindColorTexture(shadeProgram, "normalTexture", 1, 1);
        sceneRT.bindDepthTexture(shadeProgram, "depthTexture", 2);
        ssaoRT.bindColorTexture(shadeProgram, "ssaoTexture", 3, 0);
        sceneRT.bindColorTexture(shadeProgram, "imageTexture", 4,3);

        glUniformMatrix4fv(locShadeView, false, camera.getViewMatrix().floatArray());

        quad.draw(GL_TRIANGLES, shadeProgram);
    }

//    private void render1() {
//        glUseProgram(shaderProgram1);
//
//        renderTarget.bind();
//        glBindFramebuffer(GL_FRAMEBUFFER, 0);
//        glClearColor(0.4f, 0, 0, 1);
//        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
//
//        glUniform1f(locTime, time);
//        glUniform3fv(locLightPos, ToFloatArray.convert(cameraLight.getPosition()));
//        glUniformMatrix4fv(locView, false, camera.getViewMatrix().floatArray());
//        glUniformMatrix4fv(locProjection, false, projection.floatArray());
//
//        //texture1.bind(shaderProgram1, "texture1", 0);
//
//        glUniform1i(locTemp, object);
//
//        buffers.draw(GL_TRIANGLES, shaderProgram1);
//
//        if(mode ==1 ) {
//            glUniform1i(locTemp, 2);
//            buffers.draw(GL_TRIANGLES, shaderProgram1);
////            texture1.bind(shaderProgram1, "texture1", 0);
////            viewer.view(texture1, -1, -1, 0.5);
////            viewer.view(renderTarget.getColorTexture(), -1, -0.5, 0.5);
//        }
//
//
//    }
//
//    private void render2() {
//        glUseProgram(shaderProgram2);
//        renderTarget.bind();
//
//        //glBindFramebuffer(GL_FRAMEBUFFER, 0);
//        glClearColor(0, 0.6f, 0, 1);
//        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
//
//        glViewport(0, 0, LwjglWindow.WIDTH, LwjglWindow.HEIGHT);
//
//        glUniformMatrix4fv(locView2, false, camera.getViewMatrix().floatArray());
//        glUniformMatrix4fv(locProjection2, false, projection.floatArray());
//
//        //texture1.bind(shaderProgram2, "texture1", 0);
//        renderTarget.getColorTexture().bind(shaderProgram2, "texture1", 0);
//
//        buffers.draw(GL_TRIANGLES, shaderProgram2);
//    }

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
                    (w != width || h != height)) {
                width = w;
                height = h;
                projection = new Mat4PerspRH(Math.PI / 4, height / (double) width, 0.1, 100.0);
                if (textRenderer != null)
                    textRenderer.resize(width, height);
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
                        if(displayMode == 2)
                            displayMode=0;
                        else{displayMode += 1;}
                    }
                    case GLFW_KEY_2 -> {
                        if(animation == 1)
                            animation=0;
                        else{animation += 1;}
                    }
                    case GLFW_KEY_O -> {
                        if(object == 7)
                            object = 1;
                        else{object += 1;}
                    }
                    case GLFW_KEY_T -> {
                        if(triangleMode == 1) {
                            triangleMode = 0;
//                            buffers = GridFactory.generateGrid(50, 50, triangleMode);
//                            buffers.draw(GL_TRIANGLES, triangleMode);
                        }
                        else{triangleMode += 1;
//                            buffers = GridFactory.generateGrid(50, 50, triangleMode);
//                            buffers.draw(GL_TRIANGLE_STRIP, triangleMode);
                            }
                    }
                    case GLFW_KEY_C -> {
                        if(cameraMode == 1)
                            cameraMode = 0;
                        else{cameraMode += 1;}
                    }
                    case GLFW_KEY_M -> {
                        if(mode == 1)
                            mode = 0;
                        else{mode += 1;}
                    }
                    case GLFW_KEY_X -> {
                        if(lightMode == 3)
                            lightMode = 0;
                        else{lightMode += 1;}
                    }
                }
            }
        }
    };

}
