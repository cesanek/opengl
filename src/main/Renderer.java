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
import java.util.Random;

import static global.GluUtils.gluPerspective;
import static global.GlutUtils.glutSolidSphere;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14C.glBlendFuncSeparate;


public class Renderer extends AbstractRenderer {
    private lwjglutils.OGLTexture2D tx1, tx2, tx3, tx4, tx5, tx6, tx7, tx8, tx9, tx10, tx11, tx12, tx13, tx14, tx15;
    private float dx, dy, ox, oy;
    private float px, py, pz;
    private double ex, ey, ez;
    private float zenit, azimut;

    private double fps;
    private long oldmils;
    private long oldFPSmils;
    private long mils;
    private float step;
    Random random = new Random();

    int plantmove = -5;


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
            tx3 = new lwjglutils.OGLTexture2D("textures/photos.jpg");
            tx4 = new lwjglutils.OGLTexture2D("textures/livingroom.jpg");
            tx5 = new lwjglutils.OGLTexture2D("textures/door.jpg");
            tx6 = new lwjglutils.OGLTexture2D("textures/gold.jpg");
            tx7 = new lwjglutils.OGLTexture2D("textures/beehouse.jpg");
            tx8 = new lwjglutils.OGLTexture2D("textures/beeroof.jpg");
            tx9 = new lwjglutils.OGLTexture2D("textures/drawner.jpg");
            tx10 = new lwjglutils.OGLTexture2D("textures/animal-bee.jpg");
            tx11 = new lwjglutils.OGLTexture2D("textures/grass.jpg");
            tx12 = new lwjglutils.OGLTexture2D("textures/pavement.jpg");
            tx13 = new lwjglutils.OGLTexture2D("textures/plant01.png");
            tx14 = new lwjglutils.OGLTexture2D("textures/garage.jpg");
            tx15 = new lwjglutils.OGLTexture2D("textures/garroof.jpg");
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

    private void drawBeeHouse() {

        glMatrixMode(GL_TEXTURE);
        glLoadIdentity();

        glMatrixMode(GL_MODELVIEW);
        glTranslatef(-5, -10f, 1.5f); //prvotní posun krychlí na danou pozici
        glEnable(GL_TEXTURE_2D);


        tx7.bind();
        for (int i = 0; i < 2; i++) {
            glBegin(GL_QUADS);

            // vytvoření více stejných objektů

            glTranslatef(0, 0, 1f); // tak aby byly krychle na sobě
            glPushMatrix();
            // zadní strana krychle
            glColor3f(1.0f, 1.0f, 1);
            glTexCoord2f(1, 0);
            glVertex3f(1.0f, 1.0f, -1.0f + i);
            glTexCoord2f(0, 0);
            glVertex3f(-1.0f, 1.0f, -1.0f + i);
            glTexCoord2f(0, 1);
            glVertex3f(-1.0f, 1.0f, 1.0f + i);
            glTexCoord2f(1, 1);
            glVertex3f(1.0f, 1.0f, 1.0f + i);
            glDisable(GL_TEXTURE_2D);


            //přední hrana krychle
            // glColor3f(1.0f, 0.5f, 0.0f+i);
            glTexCoord2f(1, 1);
            glVertex3f(1.0f, -1.0f, 1.0f + i);
            glTexCoord2f(0, 1);
            glVertex3f(-1.0f, -1.0f, 1.0f + i);
            glTexCoord2f(0, 0);
            glVertex3f(-1.0f, -1.0f, -1.0f + i);
            glTexCoord2f(1, 0);
            glVertex3f(1.0f, -1.0f, -1.0f + i);
            glDisable(GL_TEXTURE_2D);


            // spodní hrana taky nebude potřeba
        /*
           // glColor3f(1.0f, 1.0f, 0.0f + i);
            glTexCoord2f(1,0);
            glVertex3f(1.0f, -1.0f, -1.0f + i);
            glTexCoord2f(0,0);
            glVertex3f(-1.0f, -1.0f, -1.0f + i);
            glTexCoord2f(0,1);
            glVertex3f(-1.0f, 1.0f, -1.0f + i);
            glTexCoord2f(1,1);
            glVertex3f(1.0f, 1.0f, -1.0f + i);


         */


            // levá boční
            // glColor3f(0.0f, 0.0f, 1.0f + i);
            glTexCoord2f(1, 1);
            glVertex3f(-1.0f, 1.0f, 1.0f + i);
            glTexCoord2f(1, 0);
            glVertex3f(-1.0f, 1.0f, -1.0f + i);
            glTexCoord2f(0, 0);
            glVertex3f(-1.0f, -1.0f, -1.0f + i);
            glTexCoord2f(0, 1);
            glVertex3f(-1.0f, -1.0f, 1.0f + i);


            // pravá boční

            //glColor3f(1.0f, 0.0f, 1.0f + i);
            glTexCoord2f(1, 0);
            glVertex3f(1.0f, 1.0f, -1.0f + i);
            glTexCoord2f(1, 1);
            glVertex3f(1.0f, 1.0f, 1.0f + i);
            glTexCoord2f(0, 1);
            glVertex3f(1.0f, -1.0f, 1.0f + i);
            glTexCoord2f(0, 0);
            glVertex3f(1.0f, -1.0f, -1.0f + i);


        }
        glEnd();
        glPopMatrix();


        glDisable(GL_TEXTURE_2D);

        glTranslatef(-5, -10f, 1.5f);


        //střecha domečku pro včely

        glEnable(GL_TEXTURE_2D);
        tx8.bind();

        glBegin(GL_TRIANGLE_STRIP);
        //glColor3f(1,-1,1);
        glTexCoord2f(0, 0);
        glVertex3f(1, 1, 3.0f - 1);
        glTexCoord2f(1, 0);
        glVertex3f(1, -1, 3.0f - 1);
        glTexCoord2f(0.5f, 0.5f);
        glVertex3f(0.0f, 0.0f, 4.0f - 1);
        glTexCoord2f(0, 0);
        glVertex3f(-1, 1, 3.0f - 1);
        glTexCoord2f(1, 0);
        glVertex3f(-1, -1, 3.0f - 1);
        glTexCoord2f(0.5f, 0.5f);
        glVertex3f(0.0f, 0.0f, 4.0f - 1);
        glTexCoord2f(0, 0);
        glVertex3f(1, 1, 3.0f - 1);
        glTexCoord2f(1, 0);
        glVertex3f(-1, 1, 3.0f - 1);
        glTexCoord2f(0.5f, 0.5f);
        glVertex3f(0.0f, 0.0f, 4.0f - 1);
        glTexCoord2f(0, 0);
        glVertex3f(-1, -1, 3.0f - 1);
        glTexCoord2f(1, 0);
        glVertex3f(1, -1, 3.0f - 1);
        glTexCoord2f(0.5f, 0.5f);
        glVertex3f(0.0f, 0.0f, 4.0f - 1);


        glEnd();
        glDisable(GL_TEXTURE_2D);

        glEnable(GL_TEXTURE_2D);
        tx9.bind();
        // glTranslatef(-5, -10f, 0.5f);
        glBegin(GL_POLYGON);
        glColor3f(1, 1, 1);


        glTexCoord2f(1, 1);
        glVertex3f(1.0f, -1.01f, 1.0f);
        glTexCoord2f(0, 1);
        glVertex3f(-1.0f, -1.01f, 1.0f);
        glTexCoord2f(0, 0);
        glVertex3f(-1.0f, -1.01f, -1.0f);
        glTexCoord2f(1, 0);
        glVertex3f(1.0f, -1.01f, -1.0f);


        glEnd();

    }

    private void bee() {


        float a = 0.11f, b = 0.21f, c = 0.11f; // vstupní hodnoty určující jak velká včela(trup) bude
        int slic = 30; // slic + ui určují jak moc objektů "výpně" bude tvořit ovál včely, něco stejného jako slices and stack u gluSpere
        int ui = 25;
//15 10
        float t = (float) ((Math.PI) / slic);
        float s = (float) (Math.PI / ui);

        glMatrixMode(GL_TEXTURE);
        glLoadIdentity();
        uhel++;
        if (uhel == 15) {
            uhel = 0;
        }


        glMatrixMode(GL_MODELVIEW);


        glEnable(GL_TEXTURE_2D);
        glRotatef((float) fps, 100, 100, 100);
        glTranslatef(-1, -1f, 3f);
        tx10.bind();
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

        glBegin(GL_TRIANGLE_STRIP);
        glPushMatrix();
        // cyklus na vytvoření Elypsy
        for (float i = (float) (-(Math.PI) / 2); i <= (Math.PI / 2) + .0001; i += t) {
            for (float j = (float) -(Math.PI); j <= (Math.PI) + .0001; j += s) {

                glTexCoord2f(0, 0);
                glVertex3f((float) (a * Math.cos(i) * Math.cos(s)), (float) (b * Math.cos(i) * Math.sin(s)), (float) (c * Math.sin(i)));
                glTexCoord2f(1, 1);

                glVertex3f((float) (a * Math.cos(i + t) * Math.cos(j)), (float) (b * Math.cos(i + t) * Math.sin(j)), (float) (c * Math.sin(i + t)));
            }


        }


        glFlush(); // vykonání předchozích kroků
        glEnd();
        glPopMatrix();
        glDisable(GL_TEXTURE_2D);


        glEnable(GL_TEXTURE_2D);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR); //aplikování textury na 3d povrh
        glPushMatrix();
        glTranslatef(0, 0.25f, 0);
        glColor3f(0f, 0f, 0f);
        glutSolidSphere(0.08, 100, 50); // POČET VRCHOLŮ V HORIZONTÁLNÍM SMĚRU (LAST)  A VERTIKÁLNÍM (LONGS)
        glPopMatrix();


        glDisable(GL_TEXTURE_2D);


        // křídla

        glRotatef(uhel, 0, 10, 0);

        glTranslatef(-0.4f, -0.9f, 0);
        glBegin(GL_TRIANGLES);
        glColor3f(1, 1, 1);
        glVertex3f(0.3f, 1.0f, 0);
        glColor3f(1, 1, 1);
        glVertex3f(0f, 0.4f, 0);
        glColor3f(1, 1, 1);
        glVertex3f(0f, 0.8f, 0);

        glEnd();

        glBegin(GL_TRIANGLES);
        glColor3f(1, 1, 1);
        glVertex3f(0.5f, 1.0f, 0);
        glColor3f(1, 1, 1);
        glVertex3f(0.9f, 0.4f, 0);
        glColor3f(1, 1, 1);
        glVertex3f(0.9f, 0.8f, 0);

        glEnd();


    }


    private void drawHouse() {

        glMatrixMode(GL_TEXTURE);


        glLoadIdentity();

        glMatrixMode(GL_MODELVIEW);
        glEnable(GL_TEXTURE_2D);


        //- žaluzie

        tx4.bind();
        glEnable(GL_TEXTURE_2D);

        // pravý přední okno
        glBegin(GL_POLYGON);
        glColor3f(1, 1, 1);
        glTexCoord2f(1, 0.5f);
        glVertex3f(1.8f, -2.005f, 2.3f);
        glTexCoord2f(1, 0);
        glVertex3f(1.8f, -2.005f, 1.3f);
        glTexCoord2f(0, 0);
        glVertex3f(0.8f, -2.005f, 1.3f);
        glTexCoord2f(0, 0.5f);
        glVertex3f(0.8f, -2.005f, 2.3f);
        glEnd();

        glBegin(GL_POLYGON);
        glColor3f(1.0f, 1.0f, 1.0f);
        glTexCoord2f(1, 0.5f);
        glVertex3f(6.7f, -2.005f, 2.3f);
        glTexCoord2f(1, 0);
        glVertex3f(6.7f, -2.005f, 1.3f);
        glTexCoord2f(0, 0);
        glVertex3f(5.7f, -2.005f, 1.3f);
        glTexCoord2f(0, 0.5f);
        glVertex3f(5.7f, -2.005f, 2.3f);
        glEnd();

        glBegin(GL_POLYGON);
        glColor3f(1.0f, 1.0f, 1.0f);
        glTexCoord2f(1, 0.5f);
        glVertex3f(1.8f, 2.005f, 2.3f);
        glTexCoord2f(1, 0);
        glVertex3f(1.8f, 2.005f, 1.3f);
        glTexCoord2f(0, 0);
        glVertex3f(0.8f, 2.005f, 1.3f);
        glTexCoord2f(0, 0.5f);
        glVertex3f(0.8f, 2.005f, 2.3f);
        glEnd();

        glBegin(GL_POLYGON);
        glColor3f(1.0f, 1.0f, 1.0f);
        glTexCoord2f(1, 0.5f);
        glVertex3f(6.7f, 2.005f, 2.3f);
        glTexCoord2f(1, 0);
        glVertex3f(6.7f, 2.005f, 1.3f);
        glTexCoord2f(0, 0);
        glVertex3f(5.7f, 2.005f, 1.3f);
        glTexCoord2f(0, 0.5f);
        glVertex3f(5.7f, 2.005f, 2.3f);
        glEnd();


        //-------------------------- kostra domu -------------------------//


        // přední stěna


        tx1.bind();
        glEnable(GL_TEXTURE_2D);
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
        glEnable(GL_BLEND); //"průhlednost oken"
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO); //řešení pomocí alfa kanálu
        glBegin(GL_POLYGON);
        glColor4f(1, 1, 1, 0.5f);
        //glColor4f(0.69f, 0.80f, 0.92f, 0.5f); // definováno na světle modrou a alfa kanál hlavně
        glVertex3f(1.8f, -2.01f, 2.3f);
        glVertex3f(1.8f, -2.01f, 1.3f);
        glColor4f(0.0f, 0.0f, 0.6f, 0.6f);
        glVertex3f(0.8f, -2.01f, 1.3f);
        glVertex3f(0.8f, -2.01f, 2.3f);
        glEnd();


        //pravý přední


        glBegin(GL_POLYGON);
        //glColor3f(1.0f, 1.0f, 1.0f);
        glColor4f(0.69f, 0.80f, 0.92f, 0.5f);
        glVertex3f(6.7f, -2.01f, 2.3f);
        glVertex3f(6.7f, -2.01f, 1.3f);
        glColor4f(0.0f, 0.0f, 0.6f, 0.6f);
        glVertex3f(5.7f, -2.01f, 1.3f);
        glVertex3f(5.7f, -2.01f, 2.3f);
        glEnd();


        // zadní budou mít stejný pozice

        //levý zadní
        glBegin(GL_POLYGON);
        //glColor3f(1.0f, 1.0f, 1.0f);
        glColor4f(0.69f, 0.80f, 0.92f, 0.5f);
        glVertex3f(1.8f, 2.01f, 2.3f);
        glVertex3f(1.8f, 2.01f, 1.3f);
        glColor4f(0.0f, 0.0f, 0.6f, 0.6f);
        glVertex3f(0.8f, 2.01f, 1.3f);
        glVertex3f(0.8f, 2.01f, 2.3f);
        glEnd();

        //pravý přední
        glBegin(GL_POLYGON);
        //glColor3f(1.0f, 1.0f, 1.0f);
        glColor4f(0.69f, 0.80f, 0.92f, 0.5f);
        glVertex3f(6.7f, 2.01f, 2.3f);
        glVertex3f(6.7f, 2.01f, 1.3f);
        glColor4f(0.0f, 0.0f, 0.6f, 0.6f);
        glVertex3f(5.7f, 2.01f, 1.3f);
        glVertex3f(5.7f, 2.01f, 2.3f);
        glEnd();
        glDisable(GL_BLEND);

        //glDisable(GL_TEXTURE_2D);

        //----------------------okna---------//


        //------------------dveře-------------//

        //přední dveře

        tx5.bind();
        glEnable(GL_TEXTURE_2D);
        glBegin(GL_POLYGON);
        glColor3f(1.0f, 1.0f, 1.0f);
        glTexCoord2f(0, 1);
        glVertex3f(3.25f, -2.01f, 2.3f);
        glTexCoord2f(0, 0);
        glVertex3f(3.25f, -2.01f, 0.52f);
        glTexCoord2f(1, 0);
        glVertex3f(4.25f, -2.01f, 0.52f);
        glTexCoord2f(1, 1);
        glVertex3f(4.25f, -2.01f, 2.3f);
        glEnd();
        glDisable(GL_TEXTURE_2D);


        tx6.bind();
        glEnable(GL_TEXTURE_2D);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR); //aplikování textury na 3d povrh
        //klika na dveřích
        glPushMatrix();
        glTexCoord3f(1, 1, 1); //aplikace
        glColor3f(1f, 1.0f, 1.0f);
        glTranslatef(4.15f, -2.03f, 1.25f); // posun na nějakou pozici
        glutSolidSphere(0.05, 100, 100); // POČET VRCHOLŮ V HORIZONTÁLNÍM SMĚRU (LAST)  A VERTIKÁLNÍM (LONGS)
        glPopMatrix();
        glDisable(GL_TEXTURE_2D);


        glPopMatrix();

    }

    //----------------------TVORABA POVRCHU "ZEMĚ" --------------------------------//

    private void ground() {


        // ---------------základní trávník --- ----------------//
        glMatrixMode(GL_TEXTURE);

        glLoadIdentity();

        glMatrixMode(GL_MODELVIEW);


        glEnable(GL_TEXTURE_2D);


        tx11.bind();
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glBegin(GL_POLYGON);
        glColor3f(1, 1, 1);
        glTexCoord2f(1, 1);
        glVertex3f(200, 200, -1.0f);
        glTexCoord2f(0, 1);
        glColor3f(1, 1, 1);
        glVertex3f(-200, 200, -1.0f);
        glColor3f(1, 1, 1);
        glTexCoord2f(0, 0);
        glVertex3f(-200, -200, -1.0f);
        glColor3f(1, 1, 1);
        glTexCoord2f(1, 0);
        glVertex3f(200, -200, -1.0f);
        glEnd();
        glDisable(GL_TEXTURE_2D);

        //-- příjezdová cesta ----/

        glPushMatrix();
        glTranslatef(20, 5, 0);
        int pocetKostek = 15;
        int countFlowers = 5;

// for cyklus na vytvoření dlažby pro 2 kola po 15 kostkách

        glEnable(GL_TEXTURE_2D);
        tx12.bind();
        for (int i = 0; i < pocetKostek; i++) {
            for (int j = 0; j < 2; j++) {


                glBegin(GL_QUADS);

                glColor3f(1, 1, 1);
                glTexCoord2f(0, 0);
                glVertex3f(0 + j * 3, 0f - (1.2f * i), -0.99f);
                glColor3f(1, 1, 1);
                glTexCoord2f(1, 0);
                glVertex3f(1 + j * 3, 0f - (1.2f * i), -0.99f);
                glColor3f(1, 1, 1);
                glTexCoord2f(1, 1);
                glVertex3f(1 + j * 3, 1f - (1.2f * i), -0.99f);
                glColor3f(1, 1, 1);
                glTexCoord2f(0, 1);
                glVertex3f(0 + j * 3, 1f - (1.2f * i), -0.99f);
            }
        }

        glEnd();
        glPopMatrix();
        glDisable(GL_TEXTURE_2D);


        //---- travnaté plochy --- //

        glEnable(GL_TEXTURE_2D);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);   //-- souží k tomu aby tam kde se nenachází pixely trávy byla brava pozadí--//
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glEnable(GL_BLEND);  // průhlednost - její zapnutí
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA); // nastavení alfa kanálu
        tx13.bind();

        glPushMatrix();
        glBegin(GL_QUADS); // čtverec ze předu

        for (int i = 0; i < countFlowers; i++) {


            for (int j = 0; j < countFlowers; j++) {
                glTexCoord2f(1, 1);
                glVertex3f(5 + (i * plantmove), 5 + (j * plantmove), -1.0f);
                glTexCoord2f(1, 0);
                glVertex3f(5 + (i * plantmove), 5 + (j * plantmove), 0);
                glTexCoord2f(0, 0);
                glVertex3f(6 + (i * plantmove), 5 + (j * plantmove), 0);
                glTexCoord2f(0, 1);
                glVertex3f(6 + (i * plantmove), 5 + (j * plantmove), -1.0f);


                glBegin(GL_QUADS); // boční čtverec
                //glColor3f(1,1,1);
                glTexCoord2f(0, 1);
                glVertex3f(5.5f + (i * plantmove), 5.5f + (j * plantmove), -1.0f);
                glTexCoord2f(0, 0);
                glVertex3f(5.5f + (i * plantmove), 5.5f + (j * plantmove), 0);
                glTexCoord2f(1, 0);
                glVertex3f(5.5f + (i * plantmove), 4.5f + (j * plantmove), 0);
                glTexCoord2f(1, 1);
                glVertex3f(5.5f + (i * plantmove), 4.5f + (j * plantmove), -1.0f);


            }


        }
        glEnd();
        glPopMatrix();
        glDisable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        // ------------------------- KONEC TRAVNATÉ PLOCHY---------------------------------//
    }


    @Override
    public void display() {
        glViewport(0, 0, width, height);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glEnable(GL_DEPTH_TEST);

        // vypocet fps, nastaveni rychlosti otaceni podle rychlosti prekresleni
        mils = System.currentTimeMillis();
        // System.out.println(mils);
        if ((mils - oldFPSmils) > 300) {
            fps = 1000 / (double) (mils - oldmils + 1);
            oldFPSmils = mils;
        }
        String textInfo;

        //System.out.println(fps);
        float speed = 10; // pocet stupnu rotace za vterinu
        step = speed * (mils - oldmils) / 1000.0f; // krok za jedno
        oldmils = mils;


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


        text = String.format("FPS (%3.1f,)", fps);
        textInfo = String.format(" azimuth %3.1f, zenith %3.1f)", azimut, zenit);
        textInfo += String.format(" trans %3.1f,  delta %3.1f)", trans, deltaTrans);


        textRenderer.clear();
        textRenderer.addStr2D(3, 20, text);
        textRenderer.addStr2D(3, 40, textInfo);

        textRenderer.draw();
    }

    private void drawScene() {
        drawHouse();
        drawBeeHouse();
        ground();
        garage();
        bee();


    }

    private void garage() {
        glMatrixMode(GL_TEXTURE);

        glLoadIdentity();

        glMatrixMode(GL_MODELVIEW);


        glEnable(GL_TEXTURE_2D);

        tx14.bind();
        glPushMatrix();
        glTranslatef(22, 10, 1);
        glScaled(3, 2, 2);
        glBegin(GL_QUADS);

        // zadní strana krychle
        glColor3f(1.0f, 1.0f, 1);
        glTexCoord2f(1, 0);
        glVertex3f(1.0f, 1.0f, -1.0f);
        glTexCoord2f(0, 0);
        glVertex3f(-1.0f, 1.0f, -1.0f);
        glTexCoord2f(0, 1);
        glVertex3f(-1.0f, 1.0f, 1.0f);
        glTexCoord2f(1, 1);
        glVertex3f(1.0f, 1.0f, 1.0f);



        //přední hrana krychle
        // glColor3f(1.0f, 0.5f, 0.0f+i);
        glTexCoord2f(1, 1);
        glVertex3f(1.0f, -1.0f, 1.0f);
        glTexCoord2f(0, 1);
        glVertex3f(-1.0f, -1.0f, 1.0f);
        glTexCoord2f(0, 0);
        glVertex3f(-1.0f, -1.0f, -1.0f);
        glTexCoord2f(1, 0);
        glVertex3f(1.0f, -1.0f, -1.0f);


        // levá boční
        glTexCoord2f(1, 1);
        glVertex3f(-1.0f, 1.0f, 1.0f);
        glTexCoord2f(1, 0);
        glVertex3f(-1.0f, 1.0f, -1.0f);
        glTexCoord2f(0, 0);
        glVertex3f(-1.0f, -1.0f, -1.0f);
        glTexCoord2f(0, 1);
        glVertex3f(-1.0f, -1.0f, 1.0f);


        // pravá boční
        glTexCoord2f(1, 0);
        glVertex3f(1.0f, 1.0f, -1.0f );
        glTexCoord2f(1, 1);
        glVertex3f(1.0f, 1.0f, 1.0f );
        glTexCoord2f(0, 1);
        glVertex3f(1.0f, -1.0f, 1.0f );
        glTexCoord2f(0, 0);
        glVertex3f(1.0f, -1.0f, -1.0f );



        glDisable(GL_TEXTURE_2D);


        glEnd();


        glEnable(GL_TEXTURE_2D);
        tx15.bind();

        //střecha garáže , nešla mi aplikovat textura proto změna a aplikování pomocí nového GL_QUADS
        glBegin(GL_QUADS);
        glTexCoord2f(1,0);
        glVertex3f(1.0f, -1.5f, 1.0f );
        glTexCoord2f(0,0);
        glVertex3f(-1.0f, -1.5f, 1.0f );
        glTexCoord2f(0,1);
        glVertex3f(-1.0f, 1.0f, 1.0f );
        glTexCoord2f(1,1);
        glVertex3f(1.0f, 1.0f, 1.0f );


        //pravý plech - nenanesení textury pouze ponechání barevného odstínu ( působí jako jeden celek plechu střechy) / lepší řešení než aplikace textury

        glVertex3f(1.001f, -1.5f, 1.01f );

        glVertex3f(1.001f, -1.5f, 0.8f );

        glVertex3f(1.001f, 1.0f, 0.8f );

        glVertex3f(1.001f, 1.0f, 1.01f );

        //levý plech

        glVertex3f(-1.001f, -1.5f, 1.01f );

        glVertex3f(-1.001f, -1.5f, 0.8f );

        glVertex3f(-1.001f, 1.0f, 0.8f );

        glVertex3f(-1.001f, 1.0f, 1.01f );



        glEnd();
        glPopMatrix();
        glDisable(GL_TEXTURE_2D);


    }


}




