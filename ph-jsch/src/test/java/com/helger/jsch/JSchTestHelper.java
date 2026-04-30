package com.helger.jsch;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;
import org.junit.Assume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.base.rt.NonBlockingProperties;
import com.helger.base.string.StringParser;
import com.helger.jsch.session.DefaultSessionFactory;
import com.jcraft.jsch.JSchException;

public class JSchTestHelper
{
  private static final Logger LOGGER = LoggerFactory.getLogger (JSchTestHelper.class);

  private JSchTestHelper ()
  {}

  @Nullable
  public static NonBlockingProperties loadTestConfig ()
  {
    try (final InputStream aIS = ClassLoader.getSystemResourceAsStream ("configuration.properties"))
    {
      if (aIS != null)
      {
        final NonBlockingProperties aProperties = new NonBlockingProperties ();
        aProperties.load (aIS);
        return aProperties;
      }
      // fall through
    }
    catch (final IOException ex)
    {
      LOGGER.warn ("cant find properties file (tests will be skipped)", ex);
    }
    return null;
  }

  @Nullable
  public static DefaultSessionFactory createSessionFactoryFromConfig ()
  {
    return createSessionFactoryFromConfig (null);
  }

  @Nullable
  public static DefaultSessionFactory createSessionFactoryFromConfig (@Nullable final Consumer <NonBlockingProperties> aPropsHolder)
  {
    final NonBlockingProperties aProperties = loadTestConfig ();
    Assume.assumeNotNull (aProperties);

    final String sUsername = aProperties.getProperty ("scp.out.test.username");
    Assume.assumeNotNull (sUsername);
    final String sHostname = "localhost";
    final int nPort = StringParser.parseInt (aProperties.getProperty ("scp.out.test.port"), -1);
    Assume.assumeTrue (nPort > 0);

    final DefaultSessionFactory aSF = new DefaultSessionFactory (sUsername, sHostname, nPort);
    try
    {
      final String sKnownHosts = aProperties.getProperty ("ssh.knownHosts");
      aSF.setKnownHosts (sKnownHosts);

      final String sPrivateKey = aProperties.getProperty ("ssh.privateKey");
      aSF.setIdentityFromPrivateKey (sPrivateKey);
    }
    catch (final JSchException ex)
    {
      LOGGER.error ("Failed to configure default session, skipping tests", ex);
      Assume.assumeNoException (ex);
    }

    if (aPropsHolder != null)
      aPropsHolder.accept (aProperties);

    return aSF;
  }
}
