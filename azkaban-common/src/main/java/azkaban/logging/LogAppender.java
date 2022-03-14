package azkaban.logging;

import azkaban.ServiceProvider;
import azkaban.executor.ExecutorManagerException;
import azkaban.utils.Props;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;

public class LogAppender extends FileAppender {

  public
  LogAppender() {
    Props props = ServiceProvider.SERVICE_PROVIDER.getInstance(Props.class);
    System.out.println(props);
  }

  public
  LogAppender(Layout layout, String filename, boolean append)
      throws IOException {
    super(layout, filename, append);
    Props props = ServiceProvider.SERVICE_PROVIDER.getInstance(Props.class);
    System.out.println(props);
  }

  public
  LogAppender(Layout layout, String flowId, int execId, int attempt, File logDir)
      throws IOException {
    this(layout, getTemporaryLocalFile(logDir, flowId, execId, attempt).getAbsolutePath(), false);
    Props props = ServiceProvider.SERVICE_PROVIDER.getInstance(Props.class);
    System.out.println(props);
  }

  public
  LogAppender(Layout layout, String filename, boolean append, boolean bufferedIO,
      int bufferSize) throws IOException {
    super(layout, filename, append, bufferedIO, bufferSize);

    Props props = ServiceProvider.SERVICE_PROVIDER.getInstance(Props.class);
    System.out.println(props);
  }

  public static File getTemporaryLocalFile(File logDir, String flowId, int execId, int attempt) {
    return new File(logDir, "_flow." + flowId + "." + execId + "." + attempt + ".log");
  }

  @Override
  public synchronized void doAppend(LoggingEvent event) {
    Props props = ServiceProvider.SERVICE_PROVIDER.getInstance(Props.class);
    System.out.println(props);
    super.doAppend(event);
  }

  @Override
  public synchronized void close() {
//    try {
//      this.executorLoader.uploadLogFile(this.execId, "", 0, this.logFile);
//    } catch (final ExecutorManagerException e) {
//      e.printStackTrace();
//    }
    super.close();
  }
}
