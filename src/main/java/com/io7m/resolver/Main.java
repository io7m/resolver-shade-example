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
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.version.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class Main
{
  private static final Logger LOG = LoggerFactory.getLogger(Main.class);

  private Main()
  {

  }

  public static void main(
    final String[] args)
    throws Exception
  {
    final RepositorySystem system = newRepositorySystem();
    final RepositorySystemSession session = newRepositorySystemSession(system);

    final Artifact artifact =
      new DefaultArtifact(
        "com.io7m.junreachable:com.io7m.junreachable:[2.0.0,)");

    final VersionRangeRequest request = new VersionRangeRequest();
    request.setArtifact(artifact);
    request.setRepositories(newRepositories());

    final VersionRangeResult result =
      system.resolveVersionRange(session, request);
    final Version newest =
      result.getHighestVersion();

    LOG.debug("version: {}", newest);
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
      new LocalRepository("/tmp/repo");

    session.setLocalRepositoryManager(
      system.newLocalRepositoryManager(session, local_repos));

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
}
