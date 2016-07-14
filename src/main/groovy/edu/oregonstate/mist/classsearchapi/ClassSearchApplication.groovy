package edu.oregonstate.mist.classsearchapi

import edu.oregonstate.mist.api.Configuration
import edu.oregonstate.mist.api.Resource
import edu.oregonstate.mist.api.InfoResource
import edu.oregonstate.mist.api.AuthenticatedUser
import edu.oregonstate.mist.api.BasicAuthenticator
import edu.oregonstate.mist.classsearchapi.dao.ClassSearchDAO
import edu.oregonstate.mist.classsearchapi.resources.ClassSearchResource
import io.dropwizard.Application
import io.dropwizard.client.HttpClientBuilder
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import io.dropwizard.auth.AuthFactory
import io.dropwizard.auth.basic.BasicAuthFactory
import org.apache.http.client.HttpClient

/**
 * Main application class.
 */
class ClassSearchApplication extends Application<ClassSearchConfiguration> {
    /**
     * Initializes application bootstrap.
     *
     * @param bootstrap
     */
    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {}

    /**
     * Parses command-line arguments and runs the application.
     *
     * @param configuration
     * @param environment
     */
    @Override
    public void run(ClassSearchConfiguration configuration, Environment environment) {
        Resource.loadProperties('resource.properties')
        environment.jersey().register(new InfoResource())

        HttpClient httpClient = new HttpClientBuilder(environment)
                .using(configuration.getHttpClientConfiguration())
                .build("example-http-client")

        final ClassSearchDAO classSearchDAO = new ClassSearchDAO(configuration.classSearch, httpClient)
        def classSearchResource = new ClassSearchResource(classSearchDAO)
        classSearchResource.setEndpointUri(configuration.getApi().getEndpointUri())
        environment.jersey().register(classSearchResource)

        environment.jersey().register(
                AuthFactory.binder(
                        new BasicAuthFactory<AuthenticatedUser>(
                                new BasicAuthenticator(configuration.getCredentialsList()),
                                'SkeletonApplication',
                                AuthenticatedUser.class)))
        //@todo: healthchecks need to be implemented still!!!
    }

    /**
     * Instantiates the application class with command-line arguments.
     *
     * @param arguments
     * @throws Exception
     */
    public static void main(String[] arguments) throws Exception {
        new ClassSearchApplication().run(arguments)
    }
}