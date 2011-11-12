package com.twitter.mesos.scheduler;

import com.twitter.common.util.BuildInfo;
import com.twitter.mesos.gen.comm.ExecutorStatus;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;
import java.util.Queue;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;

/**
 * @author William Farner
 */
public class ExecutorTrackerImplTest {

  private static final String HOST_1 = "host1";

  private static final String GIT_REVISION_1 = "revision1";
  private static final String TIMESTAMP_1 = "timesamp1";
  private static final String GIT_REVISION_2 = "revision2";
  private static final String TIMESTAMP_2 = "timesamp2";

  private IMocksControl control;

  private Queue<String> restartQueue;
  private ExecutorTracker tracker;

  @Before
  @SuppressWarnings("unchecked")
  public void setUp() {
    control = createControl();

    restartQueue = control.createMock(Queue.class);

    Properties props = new Properties();
    props.setProperty(BuildInfo.Key.GIT_REVISION.value, GIT_REVISION_1);
    props.setProperty(BuildInfo.Key.TIMESTAMP.value, TIMESTAMP_1);
    BuildInfo buildInfo = new BuildInfo(props);

    tracker = new ExecutorTrackerImpl(buildInfo, restartQueue);
  }

  @Test
  public void testNoDuplicates() {
    expect(restartQueue.contains(HOST_1)).andReturn(false);
    expect(restartQueue.add(HOST_1)).andReturn(true);
    expect(restartQueue.contains(HOST_1)).andReturn(true);

    control.replay();

    tracker.addStatus(new ExecutorStatus().setHost(HOST_1).setBuildGitRevision(GIT_REVISION_2));
    tracker.addStatus(new ExecutorStatus().setHost(HOST_1).setBuildGitRevision(GIT_REVISION_2));
  }

  @Test
  public void testNoRestartNeeded() {
    control.replay();

    tracker.addStatus(new ExecutorStatus().setHost(HOST_1)
        .setBuildTimestamp(TIMESTAMP_1)
        .setBuildGitRevision(GIT_REVISION_1));
  }

  @Test
  public void testDifferentTimestamp() {
    expect(restartQueue.contains(HOST_1)).andReturn(false);
    expect(restartQueue.add(HOST_1)).andReturn(true);

    control.replay();

    tracker.addStatus(new ExecutorStatus().setHost(HOST_1)
        .setBuildTimestamp(TIMESTAMP_2)
        .setBuildGitRevision(GIT_REVISION_1));
  }
}
