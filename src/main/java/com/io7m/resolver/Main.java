package com.io7m.resolver;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transfer.TransferListener;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.version.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;

public final class Main
{
  private static final Logger LOG = LoggerFactory.getLogger(Main.class);
  public static final String TMP_REPO = "/tmp/repo";

  private Main()
  {

  }

  public static void main(
    final String[] args)
    throws Exception
  {
    final RepositorySystem system = newRepositorySystem();
    final RepositorySystemSession session = newRepositorySystemSession(system);

    {
      final Path path = Paths.get(TMP_REPO);
      Files.walk(path, FileVisitOption.FOLLOW_LINKS)
        .sorted(Comparator.reverseOrder())
        .map(Path::toFile)
        .peek(System.out::println)
        .forEach(File::delete);
    }

    /*
     * It's necessary to construct the artifact string manually, because
     * the Maven Shade plugin will mangle the string if it is specified
     * as a constant.
     *
     * See https://issues.apache.org/jira/browse/MSHADE-156
     */

    final String initial_range =
      new StringBuilder(128)
        .append("com")
        .append(".google")
        .append(".guava")
        .append(":")
        .append("guava")
        .append(":")
        .append("[23.0,)")
        .toString();

    final Version version;

    {
      final Artifact artifact =
        new DefaultArtifact(initial_range);

      final VersionRangeRequest request = new VersionRangeRequest();
      request.setArtifact(artifact);
      request.setRepositories(newRepositories());

      final VersionRangeResult result =
        system.resolveVersionRange(session, request);
      version = result.getHighestVersion();
      LOG.debug("version: {}", version);
    }

    {
      /*
       * It's necessary to construct the artifact string manually, because
       * the Maven Shade plugin will mangle the string if it is specified
       * as a constant.
       *
       * See https://issues.apache.org/jira/browse/MSHADE-156
       */

      final String current_artifact =
        new StringBuilder(128)
          .append("com")
          .append(".google")
          .append(".guava")
          .append(":")
          .append("guava")
          .append(":")
          .append(version)
          .toString();

      final Artifact artifact = new DefaultArtifact(current_artifact);
      final ArtifactRequest request = new ArtifactRequest();
      request.setArtifact(artifact);
      request.setRepositories(newRepositories());

      final ArtifactResult result = system.resolveArtifact(session, request);
      LOG.debug("result: {}", result.getArtifact().getFile());
    }
  }

  private static List<RemoteRepository> newRepositories()
  {
    return List.of(newCentralRepository());
  }

  private static RemoteRepository newCentralRepository()
  {
    return new RemoteRepository.Builder(
      "central",
      "default",
      "https://repo.maven.apache.org/maven2/")
      .build();
  }

  private static RepositorySystemSession newRepositorySystemSession(
    final RepositorySystem system)
  {
    final DefaultRepositorySystemSession session =
      MavenRepositorySystemUtils.newSession();
    final LocalRepository local_repos =
      new LocalRepository(TMP_REPO);

    session.setLocalRepositoryManager(
      system.newLocalRepositoryManager(session, local_repos));

    session.setTransferListener(new LoggingTransferListener());
    return session;
  }

  private static RepositorySystem newRepositorySystem()
  {
    final DefaultServiceLocator locator = newServiceLocator();
    locator.addService(
      RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
    locator.addService(
      TransporterFactory.class, HttpTransporterFactory.class);
    locator.setErrorHandler(
      new DefaultServiceLocator.ErrorHandler()
      {
        @Override
        public void serviceCreationFailed(
          final Class<?> type,
          final Class<?> impl,
          final Throwable exception)
        {
          exception.printStackTrace();
        }
      });

    return locator.getService(RepositorySystem.class);
  }

  private static DefaultServiceLocator newServiceLocator()
  {
    return MavenRepositorySystemUtils.newServiceLocator();
  }

  private static class LoggingTransferListener implements TransferListener
  {
    @Override
    public void transferInitiated(final TransferEvent event)
    {
      LOG.debug("transferInitiated: {}", event);
    }

    @Override
    public void transferStarted(final TransferEvent event)
    {
      LOG.debug("transferStarted: {}", event);
    }

    @Override
    public void transferProgressed(final TransferEvent event)
    {
      LOG.debug("transferProgressed: {}", event);

      final long received = event.getTransferredBytes();
      final long expected = event.getResource().getContentLength();
      final double percent = (((double) received) / ((double) expected)) * 100.0;

      LOG.debug("progress: {}/{} ({}%)",
                Long.valueOf(received),
                Long.valueOf(expected),
                String.format("%.2f", Double.valueOf(percent)));
    }

    @Override
    public void transferCorrupted(final TransferEvent event)
    {
      LOG.debug("transferCorrupted: {}", event);
    }

    @Override
    public void transferSucceeded(final TransferEvent event)
    {
      LOG.debug("transferSucceeded: {}", event);
    }

    @Override
    public void transferFailed(final TransferEvent event)
    {
      LOG.debug("transferFailed: {}", event);
    }
  }
}
