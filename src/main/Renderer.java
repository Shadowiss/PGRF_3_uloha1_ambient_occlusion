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

    private int sceneProgram, ssaoProgram, shadeProgram, objProgram;
    private int locSceneView, locSceneProjection, locSceneTemp,
            locSsaoProjection, locShadeView, locSceneTime, locShadeLight, locObjMat, locSceneTime2,
             locLightRotZ1 ,locLightRotZ2 , locScale;

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
    private int ducky = 0;
    private float radians = 0;

    private OGLModelOBJ model;
    Mat4 swapYZ = new Mat4(new double[] {
            1, 0, 0, 0,
            0, 0, 1, 0,
            0, 1, 0, 0,
            0, 0, 0, 1,
    });
    private Mat4RotZ rotZ = new Mat4RotZ(Math.toRadians(radians));
    private Mat4Scale scale = new Mat4Scale(2);

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
        locLightRotZ1 = glGetUniformLocation(sceneProgram, "rotZ");
        locScale = glGetUniformLocation(sceneProgram, "scale");

        ssaoProgram = ShaderUtils.loadProgram("/ssao");
        locSsaoProjection = glGetUniformLocation(ssaoProgram, "projection");

        shadeProgram = ShaderUtils.loadProgram("/shade");
        locShadeView = glGetUniformLocation(shadeProgram, "view");
        locShadeLight = glGetUniformLocation(shadeProgram, "lightMode");
        locSceneTime2 = glGetUniformLocation(shadeProgram, "time");
        locLightRotZ2 = glGetUniformLocation(shadeProgram, "rotZ");

        objProgram = ShaderUtils.loadProgram("/ducky");
        locObjMat = glGetUniformLocation(objProgram, "mat");

        quad = QuadFactory.getQuad();

        camera = new Camera()
                .withPosition(new Vec3D(2, 2, 2)) // pozice pozorovatele
                .withAzimuth(5 / 4f * Math.PI) // otočení do strany o (180+45) stupňů v radiánech
                .withZenith(-1 / 5f * Math.PI); // otočení (90/5) stupňů dolů

        cameraLight = new Camera().withPosition(new Vec3D(-5, -3, -1));




        try {
            texture1 = new OGLTexture2D("./textures/mosaic.jpg");
            model = new OGLModelOBJ("/obj/ducky.obj");
        } catch (IOException e) {
            e.printStackTrace();
        }
        //buffers = model.getBuffers();

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
        if(animation == 1) {
            if (radians > 360) {
                radians = 0;
            }else{
                radians += 0.05;
            }

        }

        setTriangleMode();
        renderScene();
        renderSSAO();
        renderFinal();

        viewer.view(sceneRT.getColorTexture(0), -1, 0, 0.5);
        viewer.view(sceneRT.getColorTexture(1), -1, -0.5, 0.5);
        viewer.view(sceneRT.getColorTexture(2), -1, -1, 0.5);
        viewer.view(sceneRT.getDepthTexture(), -0.5, -1, 0.5);
        viewer.view(ssaoRT.getColorTexture(), -1, 0.5, 0.5);

        textRenderer.addStr2D(LwjglWindow.WIDTH - 190, LwjglWindow.HEIGHT - 3, "PGRF - Jirasek Jiri 2020");
    }

    private void renderScene() {
        switch (ducky){
            case 0 -> glUseProgram(sceneProgram);
            case 1 -> glUseProgram(objProgram);
        }
        //glUseProgram(objProgram);

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

        glUniformMatrix4fv(locObjMat, false,
                ToFloatArray.convert(swapYZ.mul(camera.getViewMatrix()).mul(projection)));
        glUniformMatrix4fv(locLightRotZ1,false, rotZ.floatArray());
        glUniformMatrix4fv(locScale,false, scale.floatArray());

        switch (ducky) {
            case 1 ->{buffers = model.getBuffers();
                buffers.draw(model.getTopology(), objProgram);}
            case 0 ->{

                glUniform1f(locSceneTime, time);
                glUniform1i(locSceneTemp, object);
                setTriangleMode();
                glUniform1i(locSceneTemp, 10);
                setTriangleMode();
                texture1.bind(sceneProgram, "texture1", 0);
                if(mode == 1) {
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
        rotZ = new Mat4RotZ(radians);

        glUniformMatrix4fv(locLightRotZ2,false, rotZ.floatArray());
        glUniform1f(locSceneTime2, time);
        glUniform1i(locShadeLight, lightMode);

        //texture1.bind(shadeProgram, "texture1", 4);
        sceneRT.bindColorTexture(shadeProgram, "positionTexture", 0, 0);
        sceneRT.bindColorTexture(shadeProgram, "normalTexture", 1, 1);
        sceneRT.bindDepthTexture(shadeProgram, "depthTexture", 2);
        ssaoRT.bindColorTexture(shadeProgram, "ssaoTexture", 3, 0);
        sceneRT.bindColorTexture(shadeProgram, "imageTexture", 4,3);


        glUniformMatrix4fv(locShadeView, false, camera.getViewMatrix().floatArray());

        quad.draw(GL_TRIANGLES, shadeProgram);
    }

    private void setTriangleMode(){
        switch (triangleMode){
            case 0 -> {buffers = GridFactory.generateGrid(50, 50, triangleMode);
                buffers.draw(GL_TRIANGLES, sceneProgram);}
            case 1 ->{ buffers = GridFactory.generateGrid(50, 50, triangleMode);
                buffers.draw(GL_TRIANGLE_STRIP, sceneProgram);}
        }
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
                        }
                        else{triangleMode += 1;
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
                        if(lightMode == 4)
                            lightMode = 0;
                        else{lightMode += 1;}
                    }
                    case GLFW_KEY_Z -> {
                        if(ducky == 1)
                            ducky = 0;
                        else{ducky += 1;}
                    }
                }
            }
        }
    };

}
