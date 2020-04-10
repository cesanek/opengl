package main;

import global.AbstractRenderer;
import global.GLCamera;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;


import java.io.IOException;
import java.nio.DoubleBuffer;

import static global.GluUtils.gluLookAt;
import static global.GluUtils.gluPerspective;
import static global.GlutUtils.glutSolidSphere;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;


public class Renderer extends AbstractRenderer {
    private lwjglutils.OGLTexture2D tx1, tx2, tx3;
    private float dx, dy, ox, oy;
    private float px, py, pz;
    private double ex, ey, ez;
    private float zenit, azimut;

    private float trans, deltaTrans = 0;

    private float uhel = 0;
    private float[] modelMatrix = new float[16];

    private boolean mouseButton1 = false;
    private boolean per = true, move = false;
    private int cameraMode;

    private lwjglutils.OGLTexture2D texture;
    private lwjglutils.OGLTexture2D.Viewer textureViewer;
    private GLCamera camera;

    public Renderer() {
        super();

        /*used default glfwWindowSizeCallback see AbstractRenderer*/

        glfwKeyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                    // We will detect this in our rendering loop
                    glfwSetWindowShouldClose(window, true);
                if (action == GLFW_RELEASE) {
                    trans = 0;
                    deltaTrans = 0;
                }

                if (action == GLFW_PRESS) {
                    switch (key) {
                        case GLFW_KEY_P:
                            per = !per;
                            break;
                        case GLFW_KEY_M:
                            move = !move;
                            break;
                        case GLFW_KEY_C:
                            cameraMode++;
                            break;
                        case GLFW_KEY_W:
                        case GLFW_KEY_S:
                        case GLFW_KEY_A:
                        case GLFW_KEY_D:
                            deltaTrans = 0.001f;
                            break;
                    }
                }
                switch (key) {
                    case GLFW_KEY_W:

                        if (cameraMode == 0) {
                            pz -= trans;
                        } else {
                            px += ex * trans;
                            py += ey * trans;
                            pz += ez * trans;
                        }
                        camera.forward(trans);
                        if (deltaTrans < 0.001f)
                            deltaTrans = 0.001f;
                        else
                            deltaTrans *= 1.02;
                        break;

                    case GLFW_KEY_S:
                        if (cameraMode == 0) {
                            pz += trans;
                        } else {
                            px -= ex * trans;
                            py -= ey * trans;
                            pz -= ez * trans;
                        }
                        camera.backward(trans);
                        if (deltaTrans < 0.001f)
                            deltaTrans = 0.001f;
                        else
                            deltaTrans *= 1.02;
                        break;

                    case GLFW_KEY_A:
                        if (cameraMode == 0) {
                            px -= trans;
                        } else {
                            pz -= Math.cos(azimut * Math.PI / 180 - Math.PI / 2) * trans;
                            px += Math.sin(azimut * Math.PI / 180 - Math.PI / 2) * trans;
                        }
                        camera.left(trans);
                        if (deltaTrans < 0.001f)
                            deltaTrans = 0.001f;
                        else
                            deltaTrans *= 1.02;
                        break;

                    case GLFW_KEY_D:
                        if (cameraMode == 0) {
                            px += trans;
                        } else {
                            pz += Math.cos(azimut * Math.PI / 180 - Math.PI / 2) * trans;
                            px -= Math.sin(azimut * Math.PI / 180 - Math.PI / 2) * trans;
                        }
                        camera.right(trans);
                        if (deltaTrans < 0.001f)
                            deltaTrans = 0.001f;
                        else
                            deltaTrans *= 1.02;
                        break;
                }
            }
        };

        glfwMouseButtonCallback = new GLFWMouseButtonCallback() {

            @Override
            public void invoke(long window, int button, int action, int mods) {
                DoubleBuffer xBuffer = BufferUtils.createDoubleBuffer(1);
                DoubleBuffer yBuffer = BufferUtils.createDoubleBuffer(1);
                glfwGetCursorPos(window, xBuffer, yBuffer);
                double x = xBuffer.get(0);
                double y = yBuffer.get(0);

                mouseButton1 = glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_1) == GLFW_PRESS;

                if (button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS) {
                    ox = (float) x;
                    oy = (float) y;
                }
            }

        };

        glfwCursorPosCallback = new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double x, double y) {
                if (mouseButton1) {
                    dx = (float) x - ox;
                    dy = (float) y - oy;
                    ox = (float) x;
                    oy = (float) y;
                    zenit -= dy / width * 180;
                    if (zenit > 90)
                        zenit = 90;
                    if (zenit <= -90)
                        zenit = -90;
                    azimut += dx / height * 180;
                    azimut = azimut % 360;
                    camera.setAzimuth(Math.toRadians(azimut));
                    camera.setZenith(Math.toRadians(zenit));
                    dx = 0;
                    dy = 0;
                }
            }
        };

        glfwScrollCallback = new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double dx, double dy) {
                //do nothing
            }
        };
    }

    @Override
    public void init() {
        super.init();
        glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        textureViewer = new lwjglutils.OGLTexture2D.Viewer();

        glEnable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);
        glFrontFace(GL_CW);
        glPolygonMode(GL_FRONT, GL_FILL);
        glPolygonMode(GL_BACK, GL_FILL);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        System.out.println("Loading textures...");
        try {
            tx1 = new lwjglutils.OGLTexture2D("textures/e952eced5348711e8a305c740399abef.jpg");
            tx2 = new lwjglutils.OGLTexture2D("textures/red-tile-roof-texture.jpg");
            tx3= new lwjglutils.OGLTexture2D("textures/photos.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
     /*  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR); */


        camera = new GLCamera();
    }

    private void drawScene() {

        glMatrixMode(GL_TEXTURE);

        tx1.bind();
        glLoadIdentity();

        glMatrixMode(GL_MODELVIEW);
        glEnable(GL_TEXTURE_2D);


        //  glEnable(GL_TEXTURE_2D);
        // glActiveTexture(GL_TEXTURE1);

        //-------------------------- kostra domu -------------------------//

        // přední stěna

        glBegin(GL_POLYGON);
        glColor3f(1, 1, 1);
        glTexCoord2f(1, 1);
        glVertex3f(8f, -2f, 3.5f);
        glTexCoord2f(1, 0);
        glVertex3f(8f, -2f, 0.5f);
        glTexCoord2f(0, 0);
        glVertex3f(-0.5f, -2f, 0.5f);
        glTexCoord2f(0, 1);
        glVertex3f(-0.5f, -2f, 3.5f);
        glEnd();


        // podlaha domu
        glPushMatrix();
        glBegin(GL_POLYGON);
        glColor3f(1.0f, 1.0f, 1.0f);
        glVertex3f(8, -2, 0.5f);
        glVertex3f(8, 2, 0.5f);
        glVertex3f(-0.5f, 2, 0.5f);
        glVertex3f(-0.5f, -2, 0.5f);
        glEnd();

        //  - pravá stěna
        glBegin(GL_POLYGON);
        glColor3f(1.0f, 1.0f, 1.0f);
        glTexCoord2f(0, 1);
        glVertex3f(8f, -2f, 3.5f);
        glTexCoord2f(1, 1);
        glVertex3f(8f, 2f, 3.5f);
        glTexCoord2f(1, 0);
        glVertex3f(8f, 2f, 0.5f);
        glTexCoord2f(0, 0);
        glVertex3f(8f, -2f, 0.5f);
        glEnd();

        //  -  levá sttana
        glBegin(GL_POLYGON);

        glColor3f(1.0f, 1.0f, 1.0f);
        glTexCoord2f(0, 1);
        glVertex3f(-0.5f, -2f, 3.5f);
        glTexCoord2f(1, 1);
        glVertex3f(-0.5f, 2f, 3.5f);
        glTexCoord2f(1, 0);
        glVertex3f(-0.5f, 2f, 0.5f);
        glTexCoord2f(0, 0);
        glVertex3f(-0.5f, -2f, 0.5f);
        glEnd();


        //  - zadní strana
        glBegin(GL_POLYGON);
        glColor3f(1.0f, 1.0f, 1.0f);
        glTexCoord2f(1, 0);
        glVertex3f(8f, 2f, 0.5f);
        glTexCoord2f(1, 1);
        glVertex3f(8f, 2f, 3.5f);
        glTexCoord2f(0, 1);
        glVertex3f(-0.5f, 2f, 3.5f);
        glTexCoord2f(0, 0);
        glVertex3f(-0.5f, 2f, 0.5f);
        glEnd();

        glDisable(GL_TEXTURE_2D);
        // ------------ konec kostry ---------------------------------------------- //

        //-------------------- střecha domu---------------------//

        tx3.bind();
        glEnable(GL_TEXTURE_2D);


        //levé křídlo střechy
        glBegin(GL_TRIANGLES);
        glColor3f(1f, 1f, 1);
        glTexCoord2f(1, 0);
        glVertex3f(-0.5f, -2f, 3.5f); //bod doteku s kostrou
        glTexCoord2f(0, 0);
        glVertex3f(-0.5f, 2f, 3.5f); // dotek s kostrou
        glTexCoord2f(0.5f, 0.5f);
        glVertex3f(0, 1.0f, 5.5f);
        glEnd();


        //pravé křídlo střechy
        glBegin(GL_TRIANGLES);

        glColor3f(1f, 1f, 1);
        glTexCoord2f(1, 0);
        glVertex3f(8f, -2f, 3.5f);
        glTexCoord2f(0, 0);
        glVertex3f(8f, 2f, 3.5f);
        glTexCoord2f(0.5f, 0.5f);
        glVertex3f(7.5f, 1.0f, 5.5f);
        glEnd();

        glDisable(GL_TEXTURE_2D);


        tx2.bind();
        glEnable(GL_TEXTURE_2D);
        //zadní část střechy
        glBegin(GL_POLYGON);
        glColor3f(1.0f, 1.0f, 1.0f);
        glTexCoord2f(1, 1);
        glVertex3f(7.5f, 1.0f, 5.5f);
        glTexCoord2f(1, 0);
        glVertex3f(8f, 2f, 3.5f);
        glTexCoord2f(0, 0);
        glVertex3f(-0.5f, 2f, 3.5f);
        glTexCoord2f(0, 1);
        glVertex3f(0f, 1f, 5.5f);
        glEnd();

        //přední část střechy
        glBegin(GL_POLYGON);
        glColor3f(1.0f, 1.0f, 1.f);
        glTexCoord2f(1, 1);
        glVertex3f(7.5f, 1.0f, 5.5f);
        glTexCoord2f(1, 0);
        glVertex3f(8f, -2f, 3.5f);
        glTexCoord2f(0, 0);
        glVertex3f(-0.5f, -2f, 3.5f);
        glTexCoord2f(0, 1);
        glVertex3f(0f, 1f, 5.5f);
        glEnd();

        glDisable(GL_TEXTURE_2D);

        //----------------konec střechy--------------------------//

        //----------------------okna---------//

        //levý přední
        glBegin(GL_POLYGON);
        glColor3f(1.0f, 1.0f, 1.0f);
        glVertex3f(1.8f, -2.01f, 2.3f);
        glVertex3f(1.8f, -2.01f, 1.3f);
        glVertex3f(0.8f, -2.01f, 1.3f);
        glVertex3f(0.8f, -2.01f, 2.3f);
        glEnd();


        //pravý přední

        glBegin(GL_POLYGON);
        glColor3f(1.0f, 1.0f, 1.0f);
        glVertex3f(6.7f, -2.01f, 2.3f);
        glVertex3f(6.7f, -2.01f, 1.3f);
        glVertex3f(5.7f, -2.01f, 1.3f);
        glVertex3f(5.7f, -2.01f, 2.3f);
        glEnd();


        // zadní budou mít stejný pozice

        //levý zadní
        glBegin(GL_POLYGON);
        glColor3f(1.0f, 1.0f, 1.0f);
        glVertex3f(1.8f, 2.01f, 2.3f);
        glVertex3f(1.8f, 2.01f, 1.3f);
        glVertex3f(0.8f, 2.01f, 1.3f);
        glVertex3f(0.8f, 2.01f, 2.3f);
        glEnd();

        //pravý přední
        glBegin(GL_POLYGON);
        glColor3f(1.0f, 1.0f, 1.0f);
        glVertex3f(6.7f, 2.01f, 2.3f);
        glVertex3f(6.7f, 2.01f, 1.3f);
        glVertex3f(5.7f, 2.01f, 1.3f);
        glVertex3f(5.7f, 2.01f, 2.3f);
        glEnd();

        //----------------------okna---------//


        //------------------dveře-------------//

        //přední dveře

        glBegin(GL_POLYGON);
        glColor3f(1.0f, 1.0f, 1.0f);
        glVertex3f(3.25f, -2.01f, 2.3f);
        glVertex3f(3.25f, -2.01f, 0.52f);
        glVertex3f(4.25f, -2.01f, 0.52f);
        glVertex3f(4.25f, -2.01f, 2.3f);
        glEnd();

        //klika na dveřích
        glPushMatrix();
        glColor3f(0.5f, 1.0f, 0.0f);
        glTranslatef(4.15f, -2.03f, 1.25f); // posun na nějakou pozici
        glutSolidSphere(0.1, 100, 100); // POČET VRCHOLŮ V HORIZONTÁLNÍM SMĚRU (LAST)  A VERTIKÁLNÍM (LONGS)
        glPopMatrix();


        glPopMatrix();

    }

    @Override
    public void display() {
        glViewport(0, 0, width, height);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glEnable(GL_DEPTH_TEST);
        String text = this.getClass().getName() + ": [lmb] move";

        trans += deltaTrans;


        glMatrixMode(GL_MODELVIEW);


        glLoadIdentity();
        camera.setFirstPerson(false);
        camera.setRadius(30);
        camera.setMatrix();
        text += ", [C]amera: GLCamera 3rd";


        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        if (per)
            gluPerspective(45, width / (float) height, 0.1f, 200.0f);
        else
            glOrtho(-20 * width / (float) height,
                    20 * width / (float) height,
                    -20, 20, 0.1f, 200.0f);

        glMatrixMode(GL_MODELVIEW);

        glPushMatrix();
        if (move) {
            uhel++;
        }

        drawScene();
        glPopMatrix();

        if (per)
            text += ", [P]ersp ";
        else
            text += ", [p]ersp ";

        if (move)
            text += ", Ani[M] ";
        else
            text += ", Ani[m] ";


        String textInfo = String.format("position (%3.1f, %3.1f, %3.1f)", px, py, pz);
        textInfo += String.format(" azimuth %3.1f, zenith %3.1f)", azimut, zenit);
        textInfo += String.format(" trans %3.1f,  delta %3.1f)", trans, deltaTrans);


        textRenderer.clear();
        textRenderer.addStr2D(3, 20, text);
        textRenderer.addStr2D(3, 40, textInfo);

        textRenderer.draw();
    }
}




