package OCRModule;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import edu.dei.gp.jpa.Song;

import edu.dei.gp.core.OCRBackendIntegrator;

// TODO: Auto-generated Javadoc
/**
 * The Class ThreadPool.
 */
public class ThreadPool implements Runnable  {

	private int corePoolsize = 4;
	private int maximumPoolSize = 10;
	private long keepAliveTime = 1;
	private TimeUnit unit = TimeUnit.DAYS;
	private BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(corePoolsize);

	private ThreadPoolExecutor pool;
	
	public OCRBackendIntegrator ocrBackendIntegrator;

	/**
	 * Instantiates a new thread pool.
	 */
	public ThreadPool() {
		this.pool = new ThreadPoolExecutor(	corePoolsize, 
				maximumPoolSize, 
				keepAliveTime, 
				unit, 
				workQueue);
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		ThreadPool Run = new ThreadPool();
	}

	@Override
	public void run(){
		Song music;

		// 1 - instanciacao da classe para tratamento das mensagens + EJB para o youtube
		ocrBackendIntegrator = new OCRBackendIntegrator();

		// adds a Shutdown Hook to ensure the Connection is terminated when the app is closed
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				ocrBackendIntegrator.close();
			}
		}) {
		});

		while(true){
			music = ocrBackendIntegrator.receiveMessage();
			if(music!=null){
				pool.execute(new MainModule(music));
			}
		}
	}
}
