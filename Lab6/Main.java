import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.objectfile.ObjectFile;
import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.image.TextureLoader;
import com.sun.j3d.utils.universe.SimpleUniverse;
import org.javatuples.Pair;

import javax.media.j3d.*;
import javax.swing.*;
import javax.vecmath.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Main extends JFrame implements ActionListener, KeyListener {
    static class PeriodicTransformation{
        public TransformGroup transformGroup;
        public BiConsumer<Transform3D, Double> function;
    }

    private final static String scratModelLocation = "models/scrat.obj";
    private final static String backgroundLocation = "backgrounds/ice_age.jpg";
    private final static float y_axis_rotate_initial = 0.627f;
    private final static float x_axis_rotate_initial = -0.084f;
    private final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    private final HashMap<String, Pair<Float, Boolean>> scrat_pos = new HashMap<>();
    private final Timer timer = new Timer(20, this);
    private final BranchGroup root = new BranchGroup();
    private TransformGroup initTransformGroup;
    private SimpleUniverse universe;
    private Canvas3D myCanvas3D;
    private Label labelInfo;
    private Button go;

    private static double time = 0;
    private static ArrayList<PeriodicTransformation> periodicTransformationList = new ArrayList<>();


    static double triangleFunction(double t, double period){
        return 4*Math.abs(t/period - Math.floor(t/period + 0.5)) - 1;
    }

    public static void main(String[] args) {
        var window = new Main();
        window.setVisible(true);
        window.addKeyListener(window);
    }

    public Main() {
        configureWindow();
        configureCanvas();
        configureUniverse();
        configureNavigation();
        addLightToUniverse();
        configureInitialHashmapOfPositions();
        configureSceneGraph();
        configureFrame();
    }

    private void configureWindow() {
        setTitle("Lab #6");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void configureCanvas() {
        myCanvas3D = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
        myCanvas3D.setDoubleBufferEnable(true);
        myCanvas3D.addKeyListener(this);
        getContentPane().add(myCanvas3D, BorderLayout.CENTER);
    }

    private void configureUniverse() {
        universe = new SimpleUniverse(myCanvas3D);
        universe.getViewingPlatform().setNominalViewingTransform();
    }

    private void configureNavigation() {
        var ob = new OrbitBehavior(myCanvas3D);
        ob.setReverseRotate(true);
        ob.setSchedulingBounds(new BoundingSphere(new Point3d(), Double.MAX_VALUE));
        universe.getViewingPlatform().setViewPlatformBehavior(ob);
    }

    private void configureFrame() {
        go = new Button("  go  ");
        labelInfo = new Label("To start moving Scrat press `go`. Press `backspace` to reset ");
        var panel = new Panel();
        panel.add(labelInfo);
        panel.add(go);
        add("North", panel);
        go.addActionListener(this);
    }

    private void addLightToUniverse() {
        var dirLight = new DirectionalLight(
                new Color3f(Color.WHITE),
                new Vector3f(4.0f, -7.0f, -12.0f)
        );
        dirLight.setInfluencingBounds(new BoundingSphere(new Point3d(), 1000));
        root.addChild(dirLight);
    }

    private static Transform3D transformFromRot(Vector3d eulerRot){
        var res = new Transform3D();
        res.setEuler(eulerRot);
        return res;
    }

    private static Transform3D transformFromTransl(Vector3d transl){
        var res = new Transform3D();
        res.setTranslation(transl);
        return res;
    }

    private static Transform3D rotAround(Vector3d transl, Vector3d eulerRot){
        Transform3D transf = transformFromTransl(transl);

        transf.mul(transformFromRot(eulerRot));
        transl.negate();
        transf.mul(transformFromTransl(transl));

        return transf;
    }

    private void configureSceneGraph() {
        addImageInTheSkyBackground();
        var scrat = getSceneScratFromFile();
        var scratTransformGroup = new TransformGroup();

        initTransformGroup = new TransformGroup();
        setInitialTransformation(initTransformGroup);

        var scratSceneGroup = scrat.getSceneGroup();
        scratSceneGroup.removeChild((Shape3D)scrat.getNamedObjects().get("nut"));
        scratSceneGroup.removeChild((Shape3D)scrat.getNamedObjects().get("left_hand"));
        scratSceneGroup.removeChild((Shape3D)scrat.getNamedObjects().get("right_hand"));
        scratSceneGroup.removeChild((Shape3D)scrat.getNamedObjects().get("tale"));

        initTransformGroup.addChild(scratSceneGroup);
        initTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        initTransformGroup.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);

        scratTransformGroup.addChild(initTransformGroup);
        scratTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        root.addChild(scratTransformGroup);
        addAppearanceForScrat(scrat);

        scratSceneGroup.addChild(
                applyRotationForShape((Shape3D)scrat.getNamedObjects().get("nut"),
                        (transform3D, time) -> {
                    transform3D.set(rotAround(
                            new Vector3d(-0.9,-0.15,0),
                            new Vector3d(0,0,Math.PI*time)
                            ));
                }));

        scratSceneGroup.addChild(
                applyRotationForShape((Shape3D)scrat.getNamedObjects().get("left_hand"),
                        (transform3D,time) -> {
                            transform3D.set(rotAround(
                                    new Vector3d(0.05,0.05,0.11),
                                    new Vector3d(0,triangleFunction(time,1)*Math.PI*0.25,0)
                            ));
                        }));

        scratSceneGroup.addChild(
                applyRotationForShape((Shape3D)scrat.getNamedObjects().get("right_hand"),
                        (transform3D,time) -> {
                            transform3D.set(rotAround(
                                    new Vector3d(0.05,0.05,-0.11),
                                    new Vector3d(0,triangleFunction(time,1)*Math.PI*0.25,0)
                            ));
                        }));

        scratSceneGroup.addChild(
                applyRotationForShape((Shape3D)scrat.getNamedObjects().get("tale"),
                        (transform3D,time) -> {
                            transform3D.set(rotAround(
                                    new Vector3d(0.35,0,0),
                                    new Vector3d(0,0,triangleFunction(time,0.75)*Math.PI*0.25)
                            ));
                        }));

        final var startingTime = System.currentTimeMillis();

        Transform3D tempTransform = new Transform3D();

        new Timer(33, actionEvent -> {
            time = (System.currentTimeMillis() - startingTime)*0.001;

            for(var listener: periodicTransformationList){
                listener.transformGroup.getTransform(tempTransform);
                listener.function.accept(tempTransform, time);
                listener.transformGroup.setTransform(tempTransform);
            }
        }).start();

        root.compile();
        universe.addBranchGraph(root);
    }

    private void configureInitialHashmapOfPositions() {
        scrat_pos.put("x_loc", new Pair<>(0f, true));
        scrat_pos.put("y_loc", new Pair<>(0f, true));
        scrat_pos.put("z_loc", new Pair<>(0f, true));
        scrat_pos.put("scale", new Pair<>(1f, true));
        scrat_pos.put("y_axis_rotate", new Pair<>(y_axis_rotate_initial, true));
        scrat_pos.put("x_axis_rotate", new Pair<>(x_axis_rotate_initial, true));
    }

    private void setInitialTransformation(TransformGroup transformGroup) {
        var transformYAxis = new Transform3D();
        transformYAxis.rotY(y_axis_rotate_initial);
        var transformXAxis = new Transform3D();
        transformXAxis.rotX(x_axis_rotate_initial);
        transformYAxis.mul(transformXAxis);
        transformGroup.setTransform(transformYAxis);
    }

    private Node applyRotationForShape(Node shape, BiConsumer<Transform3D, Double> transFunction) {
        var transformGroup = new TransformGroup();
        transformGroup.addChild(shape);
        transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

        var perTrans = new PeriodicTransformation();

        perTrans.transformGroup = transformGroup;
        perTrans.function = transFunction;

        periodicTransformationList.add(perTrans);

        return transformGroup;
    }

    private void addAppearanceForScrat(Scene scrat) {
        {
            var object1 = (Shape3D) scrat.getNamedObjects().get("left_eye");
            var object2 = (Shape3D) scrat.getNamedObjects().get("right_eye");

            setAppearanceMaterialAsColorForShapes(new Color(255, 255, 255), 3, object1,object2);
        }
        {
            var object1 = (Shape3D) scrat.getNamedObjects().get("left_eye1");
            var object2 = (Shape3D) scrat.getNamedObjects().get("right_eye1");
            var object3 = (Shape3D) scrat.getNamedObjects().get("nose");

            setAppearanceMaterialAsColorForShapes(new Color(0, 0, 0), 3, object1,object2,object3);
        }
        {
            var object1 = (Shape3D) scrat.getNamedObjects().get("objobject05");

            setAppearanceMaterialAsColorForShapes(new Color(255, 0, 0), 3, object1);
        }
        {
            var object1 = (Shape3D) scrat.getNamedObjects().get("tale");
            var object2 = (Shape3D) scrat.getNamedObjects().get("body");
            var object3 = (Shape3D) scrat.getNamedObjects().get("right_hand");
            var object4 = (Shape3D) scrat.getNamedObjects().get("left_hand");

            setAppearanceMaterialAsColorForShapes(new Color(101,67,33), 3,object1,object2,object3,object4);
        }
        {
            var object1 = (Shape3D) scrat.getNamedObjects().get("nut");

            setAppearanceMaterialAsColorForShapes(new Color(150,75,0), 3,object1);
        }

        //System.out.println(Collections.list((Enumeration<String>) scrat.getNamedObjects().keys()).toString());
    }

    private void setAppearanceMaterialAsColorForShapes(Color color, int shininess, Shape3D... shapes) {
        addAppearanceForShapes(appearance -> {
            var colorVector = new Color3f(color);
            appearance.setMaterial(new Material(colorVector, colorVector, colorVector, colorVector, shininess));
        }, shapes);
    }

    private void addAppearanceForShapes(Consumer<Appearance> changeAppearance, Shape3D... shapes) {
        for (var shape : shapes) {
            var appearance = new Appearance();
            changeAppearance.accept(appearance);
            shape.setAppearance(appearance);
        }
    }

    private Scene getSceneScratFromFile() {
        var file = new ObjectFile(ObjectFile.RESIZE);
        file.setFlags(ObjectFile.RESIZE | ObjectFile.TRIANGULATE | ObjectFile.STRIPIFY);
        var inputStream = classLoader.getResourceAsStream(scratModelLocation);
        try {
            if (inputStream == null) {
                throw new IOException("Resource " + scratModelLocation + " not found");
            }
            return file.load(new BufferedReader(new InputStreamReader(inputStream)));
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
            return null;
        }
    }

    private TextureLoader getTextureLoader(String path) {
        var textureResource = classLoader.getResource(path);
        if (textureResource == null) {
            System.err.println("Couldn't find texture: " + path);
            System.exit(1);
        }
        return new TextureLoader(textureResource.getPath(), myCanvas3D);
    }

    private void addImageInTheSkyBackground() {
        var background = new Background(getTextureLoader(backgroundLocation).getImage());
        background.setImageScaleMode(Background.SCALE_FIT_MAX);
        background.setApplicationBounds(new BoundingSphere(new Point3d(), Double.MAX_VALUE));
        background.setCapability(Background.ALLOW_IMAGE_WRITE);
        root.addChild(background);
    }

    private void flyAway() {
        updateScratPosition(() -> {
            var x_loc = scrat_pos.get("x_loc").getValue0();
            scrat_pos.put("x_loc", new Pair<>(x_loc - 0.05f, true));
        });
    }

    private void updateScratPosition(Runnable func) {
        var x_loc = scrat_pos.get("x_loc").getValue0();
        var y_loc = scrat_pos.get("y_loc").getValue0();
        var z_loc = scrat_pos.get("z_loc").getValue0();
        var scale = scrat_pos.get("scale").getValue0();
        var y_axis_rotate = scrat_pos.get("y_axis_rotate").getValue0();
        var x_axis_rotate = scrat_pos.get("x_axis_rotate").getValue0();

        func.run();

        var transform3D = new Transform3D();
        transform3D.setTranslation(new Vector3f(x_loc, y_loc, z_loc));
        transform3D.setScale(scale);
        var transformYAxis = new Transform3D();
        transformYAxis.rotY(y_axis_rotate);
        var transformXAxis = new Transform3D();
        transformXAxis.rotX(x_axis_rotate);
        transform3D.mul(transformXAxis);
        transform3D.mul(transformYAxis);
        initTransformGroup.setTransform(transform3D);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == go) {
            if (timer.isRunning()) {
                timer.stop();
                go.setLabel("  go  ");
                labelInfo.setText("To start moving Scrat press `go`. Press `backspace` to reset ");
                setInitialTransformation(initTransformGroup);
                configureInitialHashmapOfPositions();
            } else {
                timer.start();
                go.setLabel("stop");
                labelInfo.setText("To stop moving Scrat press `stop`. Press `backspace` to reset");
            }
        } else {
            flyAway();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            configureInitialHashmapOfPositions();
            setInitialTransformation(initTransformGroup);
            universe.getViewingPlatform().setNominalViewingTransform();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) { }

    @Override
    public void keyTyped(KeyEvent e) { }
}
