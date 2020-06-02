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
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Main extends JFrame implements ActionListener, KeyListener {
    static class PeriodicTransformation{
        public TransformGroup transformGroup;
        public BiConsumer<Transform3D, Double> function;
    }

    private final static String dominoModelLocation = "models/domino.obj";
    private final static String backgroundLocation = "backgrounds/table.jpg";
    private final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    private Timer timer;
    private final BranchGroup root = new BranchGroup();
    private TransformGroup initTransformGroup;
    private SimpleUniverse universe;
    private Canvas3D myCanvas3D;
    private Label labelInfo;
    private Button go;

    static double startingTime = 0;
    private static double time = 0;
    private static ArrayList<PeriodicTransformation> periodicTransformationList = new ArrayList<>();

    static final double timeToFall = 3;
    static final double dominoWidth = 0.2;
    static final double dominoHeight = 2;
    static final double dominoDistance = 1.8;

    static final int dominoCount = 6;

    static double getNextAngle(){
        return Math.acos(dominoDistance/dominoHeight);
    }

    static double getMaxAngle(){
        return Math.acos(dominoWidth/dominoDistance);
    }

    static double getStartTime(){
        return timeToFall*(getNextAngle())/(getMaxAngle());
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
        configureSceneGraph();
        configureFrame();
    }

    private void configureWindow() {
        setTitle("Lab #5");
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
        labelInfo = new Label("To start moving dominoes press `go`. Press `backspace` to reset ");
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
        var domino = getSceneFromFile();
        var dominoTransformGroup = new TransformGroup();

        initTransformGroup = new TransformGroup();

        var dominoSceneGroup = domino.getSceneGroup();
        dominoSceneGroup.removeChild((Shape3D) domino.getNamedObjects().get("domino_base"));

        initTransformGroup.addChild(dominoSceneGroup);
        initTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        initTransformGroup.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);

        dominoTransformGroup.addChild(initTransformGroup);
        dominoTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        root.addChild(dominoTransformGroup);
        addAppearanceForDomino(domino);

        for(int i=0;i<dominoCount;i++)
        {
            var transformGroup = new TransformGroup();
            transformGroup.addChild(((Shape3D) domino.getNamedObjects().get("domino_base")).cloneTree());
            transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
            dominoSceneGroup.addChild(transformGroup);

            int finalI = i;
            {
                var temp = new Transform3D();
                temp.setTranslation(new Vector3d(0, 0, finalI - dominoCount * 0.5));
                transformGroup.setTransform(temp);
            }
            applyRotationForShape(transformGroup,
                    (transform3D, time) -> {
                        var temp = new Transform3D();
                        temp.setTranslation(new Vector3d(0,0,finalI-dominoCount*0.5));

                        transform3D.set(temp);

                        var dominoTime = time - getStartTime()*finalI;

                        if(dominoTime < 0)
                            dominoTime = 0;
                        else if(dominoTime > timeToFall)
                            dominoTime = timeToFall;

                        var angle = getMaxAngle()*dominoTime/timeToFall;

                        transform3D.mul(rotAround(
                                new Vector3d(0, -1, 0.1),
                                new Vector3d(angle, 0, 0)
                        ));
                    });
        }

        Transform3D tempTransform = new Transform3D();

        timer = new Timer(33, actionEvent -> {
            time = (System.currentTimeMillis() - startingTime)*0.001;

            for(var listener: periodicTransformationList){
                listener.transformGroup.getTransform(tempTransform);
                listener.function.accept(tempTransform, time);
                listener.transformGroup.setTransform(tempTransform);
            }
        });

        root.compile();
        universe.addBranchGraph(root);
    }

    /*private Node applyRotationForShape(Node shape, BiConsumer<Transform3D, Double> transFunction) {
        var transformGroup = new TransformGroup();
        transformGroup.addChild(shape);
        transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

        var perTrans = new PeriodicTransformation();

        perTrans.transformGroup = transformGroup;
        perTrans.function = transFunction;

        periodicTransformationList.add(perTrans);

        return transformGroup;
    }*/

    private void applyRotationForShape(TransformGroup transformGroup, BiConsumer<Transform3D, Double> transFunction) {
        var perTrans = new PeriodicTransformation();

        perTrans.transformGroup = transformGroup;
        perTrans.function = transFunction;

        periodicTransformationList.add(perTrans);
    }

    private void addAppearanceForDomino(Scene scrat) {
        /*setAppearanceMaterialAsColorForShapes(Color.darkGray, 3,
                (Shape3D) scrat.getNamedObjects().get("domino_spots")
        );*/
        setAppearanceMaterialAsColorForShapes(Color.gray, 3,
                (Shape3D) scrat.getNamedObjects().get("domino_base")
        );

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

    private Scene getSceneFromFile() {
        var file = new ObjectFile(ObjectFile.RESIZE);
        file.setFlags(ObjectFile.RESIZE | ObjectFile.TRIANGULATE | ObjectFile.STRIPIFY);
        var inputStream = classLoader.getResourceAsStream(dominoModelLocation);
        try {
            if (inputStream == null) {
                throw new IOException("Resource " + dominoModelLocation + " not found");
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

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == go) {
            if (timer.isRunning()) {
                startingTime = System.currentTimeMillis();

                for(int i=0;i<periodicTransformationList.size();i++){
                    var transGroup = periodicTransformationList.get(i).transformGroup;

                    var temp = new Transform3D();
                    temp.setTranslation(new Vector3d(0,0,i-dominoCount*0.5));

                    transGroup.setTransform(temp);
                }
                timer.stop();
                go.setLabel("start");
                labelInfo.setText("To start moving domino press `start`. Press `backspace` to reset ");
            } else {
                startingTime = System.currentTimeMillis();
                timer.start();
                go.setLabel("stop");
                labelInfo.setText("To stop moving domino press `stop`. Press `backspace` to reset");
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            universe.getViewingPlatform().setNominalViewingTransform();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) { }

    @Override
    public void keyTyped(KeyEvent e) { }
}
