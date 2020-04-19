package ch.zhaw.prog2.mandelbrot;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This  application uses several threads to compute an image "in the background".
 *
 * As rows of pixels in the image are computed, they are copied to the screen.
 * (The image is a small piece of the famous Mandelbrot set, which
 * is used just because it takes some time to compute.  There is no need
 * to understand what the image means.)  The user starts the computation by
 * clicking a "Start" button.  A pop-up menu allows the user to select the
 * number of threads to be used.  The specified number of threads is created
 * and each thread is assigned a region in the image.  The threads are run
 * at lower priority, which will make sure that the GUI thread will get a
 * chance to run to repaint the display as necessary.
 */
public class MandelbrotExecutor {

    /**
     * This Wrapper Class is only required to allow IDEs to start the FX-Applications
     */
    public static void main(String[] args) {
        Application.launch(MandelbrotApplication.class, args);
    }


    public static class MandelbrotApplication extends Application {

        private volatile boolean running;  // used to signal the thread to abort

        private Button startButton; // button the user can click to start or abort the thread
        private ComboBox<String> threadCountSelect;  // for specifying the number of threads to be used

        private Canvas canvas;      // the canvas where the image is displayed
        private GraphicsContext g;  // the graphics context for drawing on the canvas

        private Color[] palette;    // the color palette, containing the colors of the spectrum

        int width, height;          // the size of the canvas

        private Thread[] workers;   // the threads that compute the image
        private int tasksRemaining; // How many tasks/threads are still running resp. need to be processed

        private long startTime;     // used to calculate the runtime for the calculation

        ExecutorService executorService;

        /**
         * Set up the GUI and event handling.  The canvas will be 1200-by-1000 pixels,
         * if that fits comfortably on the screen; otherwise, size will be reduced to fit.
         * This method also makes the color palette, containing colors in spectral order.
         */
        public void start(Stage stage) {
            palette = new Color[256];
            for (int i = 0; i < 256; i++) {
                palette[i] = Color.hsb(360 * (i / 256.0), 1, 1);
            }
            int screenWidth = (int) Screen.getPrimary().getVisualBounds().getWidth();
            int screenHeight = (int) Screen.getPrimary().getVisualBounds().getHeight();
            width = Math.min(1200, screenWidth - 50);
            height = Math.min(1000, screenHeight - 120);

            canvas = new Canvas(width, height);
            g = canvas.getGraphicsContext2D();
            g.setFill(Color.LIGHTGRAY);
            g.fillRect(0, 0, width, height);
            startButton = new Button("Start!");
            startButton.setOnAction(e -> startOrStopCalculation());
            int maxThreads = 2 * Runtime.getRuntime().availableProcessors();
            threadCountSelect = new ComboBox<>();
            threadCountSelect.setEditable(false);
            for (int i = 1; i <= maxThreads; i++) {
                threadCountSelect.getItems().add("Use " + i + " threads.");
            }
            threadCountSelect.getSelectionModel().select(0);
            HBox bottom = new HBox(8, startButton, threadCountSelect);
            bottom.setStyle("-fx-padding: 6px; -fx-border-color:black; -fx-border-width: 2px 0 0 0");
            bottom.setAlignment(Pos.CENTER);
            BorderPane root = new BorderPane(canvas);
            root.setBottom(bottom);
            root.setStyle("-fx-border-color:black; -fx-border-width: 2px");
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Mandelbrot");
            stage.setResizable(false);
            stage.show();
        }



        /**
         * This method is called when the user clicks the start button.
         * If no computation is currently running, it starts as many new
         * threads as the user has specified, and assigns a different part
         * of the image to each thread.  The threads are run at lower
         * priority than the event-handling thread, in order to keep the
         * GUI responsive.  If a computation is in progress when this
         * method is called, running is set to false as a signal to stop
         * all of the threads.
         */
        private void startOrStopCalculation() {
            if (running) {
                startButton.setDisable(true); // will be re-enabled when all threads have stopped
                // (prevent user from trying to stop threads that are already stopping)
                stopTasks();
            } else {
                startButton.setText("Abort"); // change name while computation is in progress
                threadCountSelect.setDisable(true); // will be re-enabled when all threads finish
                g.setFill(Color.LIGHTGRAY);  // fill canvas with gray
                g.fillRect(0, 0, width, height);
                int threadCount = threadCountSelect.getSelectionModel().getSelectedIndex() + 1;
                startTasks(threadCount);
            }
        }

        /**
         * This method is called by each task/thread when it terminates.  We keep track
         * of the number of tasks/threads that have terminated, so that when they have
         * all finished, we can put the program into the correct state, such as
         * changing the name of the button to "Start Again" and re-enabling the
         * pop-up menu.
         */
        private synchronized void taskFinished() {
            tasksRemaining--;
            if (tasksRemaining == 0) { // all threads have finished
                Platform.runLater(() -> {
                    // Make sure state is correct when threads end.
                    startButton.setText("Start Again");
                    startButton.setDisable(false);
                    threadCountSelect.setDisable(false);
                });
                stopTasks();
            }
        }

        /**
         * This method is called from the computation threads when one row of pixels needs
         * to be added to the image.
         *
         * @param row  the row of pixels whose colors are to be set
         */
        private void drawOneRow(ImageRow row) {
            for (int x = 0; x < row.pixels.length; x++) {
                // Color an individual pixel by filling in a 1-by-1 pixel rectangle.
                g.setFill(row.pixels[x]);
                g.fillRect(x, row.rowNumber, 1, 1);
            }
        }

        /**
         * This method starts as many new threads as the user has specified,
         * and assigns a different part of the image to each thread.
         * The threads are run at lower priority than the event-handling thread,
         * in order to keep the GUI responsive.
         *
         * @param threadCount number of thread to start to run the tasks
         */
        private void startTasks(int threadCount) {
            System.out.println("Starting calculation using " + threadCount + " threads.");
            workers = new Thread[height];
            executorService = Executors.newFixedThreadPool(threadCount);
            //int rowsPerThread;  // How many rows of pixels should each thread compute?
            //rowsPerThread = height / threadCount;
            running = true;  // Set the signal before starting the threads!
            //tasksRemaining = threadCount;  // Records how many of the threads are still running
            tasksRemaining = height;
            startTime = System.currentTimeMillis();
            //for (int i = 0; i < threadCount; i++) {
            for (int i = 0; i < height; i++) {
                // first row computed by thread number i
                //int startRow = rowsPerThread * i;

                // last row computed by thread number i
                // (we have to make sure that the endRow for the last thread is the bottom row of the image)
                //int endRow =(i == threadCount-1)? height-1 : rowsPerThread*(i+1)-1;

                // Create and start a thread to compute the rows of the image from startRow to endRow.
                //workers[i] = new Thread(new MandelbrotTask(startRow, endRow));
                workers[i] = new Thread(new MandelbrotTask(i));

                try {
                    workers[i].setPriority(Thread.currentThread().getPriority() - 1);
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
                //workers[i].start();
                executorService.submit(workers[i]);
            }
        }

        private void stopTasks() {
            running = false;  // signal the threads to stop
            workers = null;
            long duration = System.currentTimeMillis()-startTime;
            System.out.println("Finished calculation after " + duration + "ms");
            executorService.shutdown();
        }

        /**
         * This is a container class which holds the data for one row to be drawn on the canvas.
         * No getter and setters. Just use direct access to the fields.
         */
        private static class ImageRow {
            final int rowNumber;
            final Color[] pixels;

            private ImageRow(int rowNumber, int width) {
                this.rowNumber = rowNumber;
                this.pixels = new Color[width];
            }
        }


        /**
         * This class defines the thread that does the computation.
         * The run method computes the image one pixel at a time.
         * After computing the colors for each row of pixels, the colors are
         * copied into the image, and the part of the display that shows that
         * row is repainted.
         * All modifications to the GUI are made using Platform.runLater().
         * (Since the thread runs in the background, at lower priority than
         * the event-handling thread, the event-handling thread wakes up
         * immediately to repaint the display.)
         */
        private class MandelbrotTask implements Runnable {
            // these values define the area and depth of the Mandelbrot graphic
            // we keep them local to allow to extend the function to
            // select the area and depth dynamically.
            private final double xmin, xmax, ymin, ymax, dx, dy;
            private final int maxIterations;
            // this tasks calculates the following range of rows
            //private final int startRow, endRow;
            private final int row;

            //MandelbrotTask(int startRow, int endRow) {
            MandelbrotTask(int row) {
                //this.startRow = startRow;
                //this.endRow = endRow;
                this.row = row;
                xmin = -1.6744096740931858;
                xmax = -1.674409674093473;
                ymin = 4.716540768697223E-5;
                ymax = 4.716540790246652E-5;
                dx = (xmax - xmin) / (width - 1);
                dy = (ymax - ymin) / (height - 1);
                maxIterations = 10000;
            }

            public void run() {
                try {
                    /*for (int row = startRow; row <= endRow; row++) {
                        // Compute one row of pixels.
                        calculateRow(row);
                        // Check for the signal to immediately abort the computation.
                        if (!running) return;
                    }
                    */
                    // Compute one row of pixels.
                    calculateRow(row);
                    // Check for the signal to immediately abort the computation.
                    if (!running) return;
                } finally {
                    // Make sure this is called when the thread finishes for any reason.
                    taskFinished();
                }
            }

            private void calculateRow(int row) {
                final ImageRow imageRow = new ImageRow(row, width);
                double x;
                double y = ymax - dy * row;

                for (int col = 0; col < width; col++) {
                    x = xmin + dx * col;
                    int count = 0;
                    double xx = x;
                    double yy = y;
                    while (count < maxIterations && (xx * xx + yy * yy) < 4) {
                        count++;
                        double newxx = xx * xx - yy * yy + x;
                        yy = 2 * xx * yy + y;
                        xx = newxx;
                    }
                    // select color based on count of iterations
                    imageRow.pixels[col] = (count != maxIterations)?
                        palette[count % palette.length] : Color.BLACK;
                    // Check for the signal to immediately abort the computation.
                    if (!running) return;
                }
                // Schedule to draw the image row in the UI thread (runLater)
                Platform.runLater(() -> drawOneRow(imageRow));
            }
        } // end MandelbrotTask

    } // end MandelbrotApplication

} // end Mandelbrot
