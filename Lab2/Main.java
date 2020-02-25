package com.company;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.GeneralPath;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

// anim types: 2 - Рух по колу за годинниковою стрілкою, 10 - Масштабування
// frame edge type: JOIN_BEVEL

public class Main extends JPanel implements ActionListener {
    Timer timer;
    // Для анімації повороту
    private double angle = 0;
    // Для анімації масштабування
    private double scale = 1;
    private double time = 0;

    final double delta = 0.05;
    final double rotationScale = 100;

    private static int maxWidth;
    private static int maxHeight;
    public Main() {
// Таймер генеруватиме подію що 10 мс
        timer = new Timer(10, this);
        timer.start();
    }
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D)g;

        // Встановлюємо кольори
        g2d.setBackground(Color.orange);
        //g2d.setColor(Color.YELLOW);
        g2d.clearRect(0, 0, maxWidth+1, maxHeight+1);

        // Встановлюємо параметри рендерингу
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);

        g2d.translate(maxWidth/2, maxHeight/2);
        {
            BasicStroke bs = new BasicStroke(10, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
            g2d.setStroke(bs);
            g2d.setColor(Color.white);
            g2d.drawRect(-230, -200, 480, 430);
        }
        g2d.translate(Math.cos(angle)*rotationScale, Math.sin(angle)*rotationScale);

        g2d.scale(scale, scale);

        {
            GradientPaint gp = new GradientPaint(5, 10, Color.cyan, 5, -90, Color.blue, false);
            g2d.setPaint(gp);

            GeneralPath cartBase = new GeneralPath();
            cartBase.moveTo(51-160,46-130);
            cartBase.lineTo(110-160,100-130);
            cartBase.lineTo(245-160,100-130);
            cartBase.lineTo(245-160,160-130);
            cartBase.lineTo(51-160,160-130);
            cartBase.closePath();

            g2d.fill(cartBase);
        }
        {
            BasicStroke bs = new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
            g2d.setStroke(bs);
            g2d.setColor(Color.black);
            g2d.drawLine(90-160,220-130,196-160,160-130);
        }
        {
            BasicStroke bs = new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
            g2d.setStroke(bs);
            g2d.setColor(Color.black);
            g2d.drawLine(206-160,220-130,100-160,160-130);
        }
        {
            g2d.setColor(new Color(148,0,211));
            g2d.fillOval(90-24 -160,220-24 -130,24*2,24*2);
        }
        {
            g2d.setColor(new Color(148,0,211));
            g2d.fillOval(206-24 -160,220-24 -130,24*2,24*2);
        }
        {
            BasicStroke bs = new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
            g2d.setStroke(bs);
            g2d.setColor(Color.black);
            g2d.drawLine(245 -160,100 -130, 285 -160,60 -130);
        }
    }
    public static void main(String[] args) {

        JFrame frame = new JFrame("Приклад анімації");
        frame.add(new Main());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        Dimension size = frame.getSize();
        Insets insets = frame.getInsets();
        maxWidth = size.width - insets.left - insets.right - 1;
        maxHeight = size.height - insets.top - insets.bottom - 1;
    }
    // Цей метод буде викликано щоразу, як спрацює таймер
    public void actionPerformed(ActionEvent e) {
        time += delta;

        angle = time;
        scale = Math.cos(time/1)*0.4 + 0.5;

        repaint();
    }

    static double triangleWave(double time){
        return 2*Math.abs(time - Math.floor(time + 0.5));
    }
}