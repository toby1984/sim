package de.codesourcery.sim;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class Main extends JFrame
{
    public static final boolean DEBUG = false;

    private final World world = randomWorld();
    private final MainPanel mainPanel;

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater( () -> new Main() );
    }

    private static World randomWorld()
    {
        final Vec2D tmp = new Vec2D();

        Random r = new Random(0xdeadbeef );

        float mapExtent = Controller.BROADCAST_RADIUS;

        int controllers = 1;
        int robots = 100;
        int factories = 20;
        int depots = 20;

        final World w = new World();

        // add controllers
        final List<Controller> cntrls = new ArrayList<>();
        for ( int i = 0 ; i < controllers ; i++ )
        {
            tmp.randomize( mapExtent, r );
            float min = Math.min( tmp.x, tmp.y );
            if ( min < 0 ) {
                tmp.add( -min,-min);
            }
            final Controller c = new Controller( tmp );
            System.out.println("Controller #"+c.id+" @ "+c.position);
            w.add( c );
            cntrls.add( c );
        }
        final Consumer<Entity> rndLocation = e ->
        {
            int tries = 0;
            do
            {
                final int ctrlIdx = r.nextInt( cntrls.size() );
                final Controller c = cntrls.get( ctrlIdx );
                tmp.randomize( Controller.BROADCAST_RADIUS*0.99f , r );
                tmp.add( c.position );
                e.position.set( tmp );
                tries++;
            } while ( w.intersectsAny( e.position, e.extent ) && tries < 1000);
            if ( tries == 100 ) {
                System.err.println("Failed to position "+e);
            }
        };

        // add robots
        IntStream.range(0,robots).forEach( x ->
        {
            final Robot rob = new Robot( tmp );
            rndLocation.accept( rob );
            w.add( rob );
        } );

        // add factories
        for ( int i = 0 ; i < factories ; i++ )
        {
            final Factory factory;
            if ( r.nextBoolean() ) {
                factory = new Factory( tmp, ItemType.STONE );
                factory.setInput1Type(ItemType.CONCRETE);
            } else {
                factory = new Factory( tmp, ItemType.CONCRETE);
                factory.setInput1Type( ItemType.STONE );
            }
            rndLocation.accept( factory );
            factory.productionTimeSeconds = 1 + r.nextFloat()*3;
            w.add( factory );
        }

        IntStream.range(0,depots).forEach( x ->
        {
            final Depot depot = new Depot( tmp, ItemType.CONCRETE, ItemType.STONE );
            rndLocation.accept( depot );
            w.add( depot );
            w.inventory.create(depot,ItemType.CONCRETE,50);
            w.inventory.create(depot,ItemType.STONE,50);
        } );
        return w;
    }

    public Main()
    {
        super("Test");

        getContentPane().setLayout( new GridBagLayout() );

        // add main panel
        mainPanel = new MainPanel(world);

        GridBagConstraints cnstrs = new GridBagConstraints();
        cnstrs.weightx=1.0;
        cnstrs.weighty=1.0;
        cnstrs.fill = GridBagConstraints.BOTH;
        getContentPane().add( mainPanel, cnstrs );

        // setup frame & tick timer
        setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        setPreferredSize( new Dimension(640,480) );
        pack();
        setLocationRelativeTo( null );
        setVisible( true );

        new Timer( 16, new ActionListener()
        {
            private long lastTick = -1;
            private long frameCounter;

            @Override
            public void actionPerformed(ActionEvent ev)
            {
                long now = System.currentTimeMillis();
                if ( frameCounter > 0 )
                {
                    final float elapsedSeconds = (now-lastTick) / 1000.0f;
                    if ( mainPanel.simulationRunning )
                    {
                        world.tick( elapsedSeconds );
                        long time2 = System.currentTimeMillis();
                        if ( (frameCounter % 60) == 0 ) {
                            System.out.println("Elapsed time: "+(time2-now)+" millis");
                        }
                    }
                    mainPanel.repaint();
                }
                frameCounter++;
                lastTick = now;
            }
        } ).start();
    }
}
