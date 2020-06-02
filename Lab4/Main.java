import com.sun.j3d.utils.applet.MainFrame;
import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.geometry.Cylinder;
import com.sun.j3d.utils.universe.SimpleUniverse;

import javax.media.j3d.*;
import javax.swing.*;
import javax.vecmath.*;
import java.applet.Applet;
import java.awt.*;

public class Main {
    static class Window extends Applet{
        public Canvas3D c;

        public Window(BranchGroup scene) {
            setLayout(new BorderLayout());
            GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();

            c = new Canvas3D(config);
            add("Center", c);
            c.setDoubleBufferEnable(true);

            SimpleUniverse universe = new SimpleUniverse(c);
            universe.getViewingPlatform().setNominalViewingTransform();
            universe.addBranchGraph(scene);
        }
    }

    private static final TransformGroup pcTransformGroup = new TransformGroup();
    private static final Transform3D pcTransform3D = new Transform3D();
    private static double angleY = 0;

    public static void main(String[] args) {
        Window window = new Window(createScene());
        MainFrame mf = new MainFrame(window, 700, 700);
        mf.run();

        Timer timer = new Timer(33, e -> {
            pcTransform3D.setEuler(new Vector3d(-Math.PI/2,angleY,0));
            pcTransform3D.setScale(0.03);

            Transform3D pcTransform3D2 = new Transform3D();

            pcTransform3D2.setTranslation(new Vector3d(0,-0.5,0));

            pcTransform3D2.mul(pcTransform3D);

            angleY += 0.05;
            pcTransformGroup.setTransform(pcTransform3D2);
        });
        timer.start();
    }

    public static BranchGroup createScene() {
        BranchGroup root = new BranchGroup();

        // add group of scene's objects section start
        pcTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        pcTransformGroup.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
        pcTransformGroup.setTransform(pcTransform3D);
        root.addChild(pcTransformGroup);

        {
            Appearance baseApper = new Appearance();
            baseApper.setColoringAttributes(new ColoringAttributes(new Color3f(Color.DARK_GRAY), ColoringAttributes.SHADE_GOURAUD));

            Appearance keyApper = new Appearance();
            keyApper.setColoringAttributes(new ColoringAttributes(new Color3f(Color.GRAY), ColoringAttributes.SHADE_GOURAUD));

            Appearance powerKeyApper = new Appearance();
            powerKeyApper.setColoringAttributes(new ColoringAttributes(new Color3f(Color.RED), ColoringAttributes.SHADE_GOURAUD));

            Appearance screenApper = new Appearance();
            screenApper.setColoringAttributes(new ColoringAttributes(new Color3f(Color.blue), ColoringAttributes.SHADE_GOURAUD));

            // base
            pcTransformGroup.addChild(buildCube(
                    new Vector3f(10f,16f,1f),
                    new Vector3d(0,0,-1),
                    new Vector3d(0,0,0),
                    baseApper
                    ));

            //screen base
            pcTransformGroup.addChild(buildCube(
                    new Vector3f(10f,16f,0.25f),
                    new Vector3d(-10.75,0,10),
                    new Vector3d(0,Math.PI/2,0),
                    baseApper
            ));

            pcTransformGroup.addChild(buildCube(
                    new Vector3f(10f,1f,0.25f),
                    new Vector3d(-10.25,-15,10),
                    new Vector3d(0,Math.PI/2,0),
                    baseApper
            ));

            // frames
            pcTransformGroup.addChild(buildCube(
                    new Vector3f(10f,1f,0.25f),
                    new Vector3d(-10.25,15,10),
                    new Vector3d(0,Math.PI/2,0),
                    baseApper
            ));

            pcTransformGroup.addChild(buildCube(
                    new Vector3f(0.25f,14f,1f),
                    new Vector3d(-10.25,0,19),
                    new Vector3d(0,0,0),
                    baseApper
            ));

            pcTransformGroup.addChild(buildCube(
                    new Vector3f(0.25f,14f,1f),
                    new Vector3d(-10.25,0,1),
                    new Vector3d(0,0,0),
                    baseApper
            ));

            // screen
            pcTransformGroup.addChild(buildCube(
                    new Vector3f(8f,14f,0.0001f),
                    new Vector3d(-10.25,0,10),
                    new Vector3d(0,Math.PI/2,0),
                    screenApper
            ));

            // power key
            pcTransformGroup.addChild(buildCube(
                    new Vector3f(1f,1f,0.5f),
                    new Vector3d(-8,0,0.5),
                    new Vector3d(0,0,0),
                    powerKeyApper
            ));

            //keys
            for(int y = 0; y < 4; y++) {
                for (int x = ((y == 3) ? 3 : 0); x < 6; x++) {
                    pcTransformGroup.addChild(buildCube(
                            new Vector3f(1f, 1f, 0.5f),
                            new Vector3d(-5 + 2.5*y, 2.5*x, 0.5),
                            new Vector3d(0, 0, 0),
                            keyApper
                    ));

                    pcTransformGroup.addChild(buildCube(
                            new Vector3f(1f, 1f, 0.5f),
                            new Vector3d(-5+2.5*y, -2.5*x, 0.5),
                            new Vector3d(0, 0, 0),
                            keyApper
                    ));
                }
            }
            // space key
            pcTransformGroup.addChild(buildCube(
                    new Vector3f(1f,6f,0.5f),
                    new Vector3d(2.5,0,0.5),
                    new Vector3d(0,0,0),
                    keyApper
            ));

            // touchpad
            pcTransformGroup.addChild(buildCube(
                    new Vector3f(2f,4f,0.25f),
                    new Vector3d(6,0,0.25),
                    new Vector3d(0,0,0),
                    keyApper
            ));

            // touchpad key
            pcTransformGroup.addChild(buildCube(
                    new Vector3f(0.5f,1.75f,0.4f),
                    new Vector3d(9,-2.25,0.4),
                    new Vector3d(0,0,0),
                    keyApper
            ));

            // touchpad key
            pcTransformGroup.addChild(buildCube(
                    new Vector3f(0.5f,1.75f,0.4f),
                    new Vector3d(9,2.25,0.4),
                    new Vector3d(0,0,0),
                    keyApper
            ));

            pcTransformGroup.addChild(buildCylinder(
                    new Vector2f(1f,3f),
                    new Vector3d(-10,-10,0),
                    new Vector3d(0,0,0),
                    keyApper
            ));

            pcTransformGroup.addChild(buildCylinder(
                    new Vector2f(1f,3f),
                    new Vector3d(-10,10,0),
                    new Vector3d(0,0,0),
                    keyApper
            ));

        }
        // add group of scene's objects section end

        // light section start
        BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0),10);

        Color sunLightColor = new Color(200, 255, 253);
        DirectionalLight lightDirect = new DirectionalLight(new Color3f(sunLightColor), new Vector3f(4.0f, -7.0f, -12.0f));
        lightDirect.setInfluencingBounds(bounds);
        root.addChild(lightDirect);

        AmbientLight ambientLightNode = new AmbientLight(new Color3f(new Color(255, 226, 142)));
        ambientLightNode.setInfluencingBounds(bounds);
        root.addChild(ambientLightNode);
        // light section end

        return root;
    }

    public static Node buildCube(Vector3f scale, Vector3d pos, Vector3d rot, Appearance appearance) {
        Box box = new Box(scale.x, scale.y, scale.z, Box.GENERATE_TEXTURE_COORDS, appearance);
        TransformGroup cubeTransform = new TransformGroup();

        Transform3D transformRot = new Transform3D();
        transformRot.setEuler(rot);

        Transform3D transformLoc = new Transform3D();
        transformLoc.setTranslation(pos);

        Transform3D transform = new Transform3D();
        transform.mul(transformLoc,transformRot);

        cubeTransform.setTransform(transform);
        cubeTransform.addChild(box);

        return cubeTransform;
    }

    public static Node buildCylinder(Vector2f scale, Vector3d pos, Vector3d rot, Appearance appearance) {
        Cylinder box = new Cylinder(scale.x, scale.y, Box.GENERATE_TEXTURE_COORDS, appearance);
        TransformGroup cubeTransform = new TransformGroup();

        Transform3D transformRot = new Transform3D();
        transformRot.setEuler(rot);

        Transform3D transformLoc = new Transform3D();
        transformLoc.setTranslation(pos);

        Transform3D transform = new Transform3D();
        transform.mul(transformLoc,transformRot);

        cubeTransform.setTransform(transform);
        cubeTransform.addChild(box);

        return cubeTransform;
    }
}