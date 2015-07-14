package com.playground.osgi.integration.coverage.itests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.configureConsole;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.logLevel;

import java.io.File;

import javax.inject.Inject;

import org.apache.karaf.features.FeaturesService;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.KarafDistributionOption;
import org.ops4j.pax.exam.karaf.options.LogLevelOption;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.options.MavenUrlReference;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.playground.osgi.integration.coverage.service.HelloService;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class KarafITest {

	private static final String KARAF_VERSION = "3.0.0";

	private static final Logger LOG = LoggerFactory.getLogger(KarafITest.class);

	@Inject
	private FeaturesService featuresService;

	@Inject
	private BundleContext bundleContext;

	@Inject
	private HelloService helloService;

	@ProbeBuilder
	public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
		probe.setHeader(Constants.DYNAMICIMPORT_PACKAGE,
				"*,org.apache.felix.service.*;status=provisional");
		return probe;
	}

	@Configuration
	public Option[] config() {
		MavenArtifactUrlReference karafUrl = maven()
				.groupId("org.apache.karaf")
				.artifactId("apache-karaf")
				.version(KARAF_VERSION)
				.type("zip");

		MavenUrlReference karafStandardRepo = maven()
				.groupId("org.apache.karaf.features")
				.artifactId("standard")
				.classifier("features")
				.type("xml")
				.versionAsInProject();

		MavenUrlReference integrationCoverageFeatureRepo = maven()
				.groupId("com.playground.osgi")
				.artifactId("integration-coverage-features")
				.classifier("features")
				.type("xml").versionAsInProject();

		return new Option[] {
				//KarafDistributionOption.debugConfiguration("5005", true),
				karafDistributionConfiguration().frameworkUrl(karafUrl)
					.unpackDirectory(new File("target/exam")).useDeployFolder(false),
				keepRuntimeFolder(),
				KarafDistributionOption.features(karafStandardRepo, "standard"),
				KarafDistributionOption.features(karafStandardRepo, "spring"),
				KarafDistributionOption.features(karafStandardRepo, "spring-dm"),
				KarafDistributionOption.features(integrationCoverageFeatureRepo, "integration-coverage"),
//				mavenBundle().groupId("com.playground.osgi").artifactId("integration-coverage-service").versionAsInProject().start(),
				logLevel(LogLevelOption.LogLevel.INFO),
				configureConsole().ignoreLocalConsole(),
				KarafDistributionOption.editConfigurationFilePut(
						"etc/org.ops4j.pax.web.cfg", "org.osgi.service.http.port", "7050"),
				// Set Jacoco java agent
				CoreOptions.vmOption(System.getProperty("argLine"))
			};
	}

	@Test
	public void check1Provisioning() throws Exception {
		assertTrue(featuresService.isInstalled(featuresService.getFeature("integration-coverage")));
	}

	@Test
	public void check2Bundles() {
		checkBundle("com.playground.osgi.integration-coverage-service");
	}

	@Test
	public void check3Service() {
		for (int i = 0; i < 5; i++) {
			helloService.sayHello();
		}
	}

	private void checkBundle(String symbolicName) {
		LOG.info("Check that bundle " + symbolicName + " is ACTIVE");
		Bundle bundle = getInstalledBundle(symbolicName);
		assertNotNull(bundle);
		assertEquals("Expected bundle " + symbolicName + " to be ACTIVE",
				Bundle.ACTIVE, bundle.getState());
		LOG.info("Bundle " + symbolicName + " is ACTIVE");
	}

	private Bundle getInstalledBundle(String symbolicName) {
		for (Bundle b : bundleContext.getBundles()) {
			if (b.getSymbolicName().equals(symbolicName)) {
				return b;
			}
		}
		for (Bundle b : bundleContext.getBundles()) {
			LOG.error("Bundle: " + b.getSymbolicName());
		}
		throw new RuntimeException("Bundle " + symbolicName + " does not exist");
	}
}
