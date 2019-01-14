import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.JFrame;
 
public class Mandelbrot extends JFrame {
 
	private static final long serialVersionUID = 1L;
    private BufferedImage I;
    private final int THREAD_POOL_SIZE = 8;
    private final int NUMBER_OF_TASKS = 480000; // 480000 => 1 pixel = 1 task
    private final int NUMBER_OF_TESTS = 10;
    final double ZOOM = 150;
	final int MAX_ITER = 15000;
    private int taskSize;
    private int width;
 
    
    public Mandelbrot() {
        super("Mandelbrot Set");
        setBounds(100, 100, 800, 600);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        I = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        this.width = getWidth();
        
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        taskSize = getHeight() * getWidth() / NUMBER_OF_TASKS;

        ArrayList<Future<ArrayList<Integer>>> futures = new ArrayList<>();
        
        System.out.println("Thread pool size: " + THREAD_POOL_SIZE);
        System.out.println("Number of tasks: " + NUMBER_OF_TASKS);
        double timeMeasurements[] = new double[NUMBER_OF_TESTS];
        double sum = 0.0;
        
        for (int k = 0; k < NUMBER_OF_TESTS; k++) {
        	
        	long startTime = System.nanoTime();
        	
			for (int i = 0; i < NUMBER_OF_TASKS; i++) {
				Callable<ArrayList<Integer>> task = new MandelBrotPixelCallable(i);
				Future<ArrayList<Integer>> future = executorService.submit(task);
				futures.add(future);
			}
			for (int i = 0; i < NUMBER_OF_TASKS; i++) {
				try {
					ArrayList<Integer> doneCalculations = futures.get(i).get();
					int offset = i * taskSize;
					for (int j = 0; j < taskSize; j++) {
						int x = (j + offset) % width;
						int y = (j + offset) / width;
						I.setRGB(x, y, doneCalculations.get(j));
					}
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			} 
			
			long endTime = System.nanoTime(); 
	        double runTime = (double)(endTime - startTime) / 1_000_000_000.0;
	        System.out.println(k + ". test: " + runTime);
	        timeMeasurements[k] = runTime;
	        sum += runTime;
	        
	        futures.clear();
			
		}
        
        double average = sum / 10;
        sum = 0.0;
        for(int k = 0; k < NUMBER_OF_TESTS; k++) {
        	sum += (Math.pow((timeMeasurements[k] - average), 2));
        }
        double stdDev = Math.sqrt(sum / NUMBER_OF_TESTS);
        System.out.println("Average: " + average);
        System.out.println("Standard deviation: " + stdDev);
        
    }
    
    
    public class MandelBrotPixelCallable implements Callable<ArrayList<Integer>> {
    	
		private int offset;

		public MandelBrotPixelCallable(int i) {
			this.offset = i * taskSize;
		}
    	
    	@Override
    	public ArrayList<Integer> call() {
    		ArrayList<Integer> values = new ArrayList<>();
    		for (int i = 0; i < taskSize; i++) {
    			int x = (i + offset) % width;
            	int y = (i + offset) / width;
    			double zx, zy, cX, cY, tmp;
				zx = zy = 0;
				cX = (x - 400) / ZOOM;
				cY = (y - 300) / ZOOM;
				int iter = MAX_ITER;
				while (zx * zx + zy * zy < 4 && iter > 0) {
					tmp = zx * zx - zy * zy + cX;
					zy = 2.0 * zx * zy + cY;
					zx = tmp;
					iter--;
				}
				values.add(iter | (iter << 8));
			}
    		return values;
    	}
    	
    }
    
    
    @Override
    public void paint(Graphics g) {
        g.drawImage(I, 0, 0, this);
    }
 
    
    public static void main(String[] args) {
        new Mandelbrot().setVisible(true);
    }
}