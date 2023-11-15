/*
 * Copyright (c) 2013, 2023 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.jersey.jetty;

import jakarta.ws.rs.ProcessingException;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.glassfish.jersey.jetty.internal.LocalizationMessages;
import org.glassfish.jersey.server.ContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.Container;

import java.net.URI;

/**
 * Factory for creating and starting Jetty server handlers. This returns
 * a handle to the started server as {@link Server} instances, which allows
 * the server to be stopped by invoking the {@link Server#stop()} method.
 * <p/>
 * To start the server in HTTPS mode an {@link SslContextFactory} can be provided.
 * This will be used to decrypt and encrypt information sent over the
 * connected TCP socket channel.
 *
 * @author Arul Dhesiaseelan (aruld@acm.org)
 * @author Marek Potociar
 */
public final class JettyHttpContainerFactory {

    private JettyHttpContainerFactory() {
    }

    /**
     * Creates a {@link Server} instance that registers an {@link org.eclipse.jetty.server.Handler}.
     *
     * @param uri uri on which the {@link org.glassfish.jersey.server.ApplicationHandler} will be deployed. Only first path
     *            segment will be used as context path, the rest will be ignored.
     * @return newly created {@link Server}.
     *
     * @throws ProcessingException      in case of any failure when creating a new Jetty {@code Server} instance.
     * @throws IllegalArgumentException if {@code uri} is {@code null}.
     */
    public static Server createServer(final URI uri) throws ProcessingException {
        return createServer(uri, null, null, true);
    }

    /**
     * Creates a {@link Server} instance that registers an {@link org.eclipse.jetty.server.Handler}.
     *
     * @param uri   uri on which the {@link org.glassfish.jersey.server.ApplicationHandler} will be deployed. Only first path
     *              segment will be used as context path, the rest will be ignored.
     * @param start if set to false, server will not get started, which allows to configure the underlying transport
     *              layer, see above for details.
     * @return newly created {@link Server}.
     *
     * @throws ProcessingException      in case of any failure when creating a new Jetty {@code Server} instance.
     * @throws IllegalArgumentException if {@code uri} is {@code null}.
     */
    public static Server createServer(final URI uri, final boolean start) throws ProcessingException {
        return createServer(uri, null, null, start);
    }

    /**
     * Create a {@link Server} that registers an {@link org.eclipse.jetty.server.Handler} that
     * in turn manages all root resource and provider classes declared by the
     * resource configuration.
     * <p/>
     * This implementation defers to the
     * {@link ContainerFactory#createContainer(Class, jakarta.ws.rs.core.Application)} method
     * for creating an Container that manages the root resources.
     *
     * @param uri    the URI to create the http server. The URI scheme must be
     *               equal to "http". The URI user information and host
     *               are ignored If the URI port is not present then port 80 will be
     *               used. The URI path, query and fragment components are ignored.
     * @param config the resource configuration.
     * @return newly created {@link Server}.
     *
     * @throws ProcessingException      in case of any failure when creating a new Jetty {@code Server} instance.
     * @throws IllegalArgumentException if {@code uri} is {@code null}.
     */
    public static Server createServer(final URI uri, final ResourceConfig config)
            throws ProcessingException {

        final JettyHttpContainer container = ContainerFactory.createContainer(JettyHttpContainer.class, config);
        return createServer(uri, null, container, true);
    }

    /**
     * Create a {@link Server} that registers an {@link org.eclipse.jetty.server.Handler} that
     * in turn manages all root resource and provider classes declared by the
     * resource configuration.
     * <p/>
     * This implementation defers to the
     * {@link ContainerFactory#createContainer(Class, jakarta.ws.rs.core.Application)} method
     * for creating an Container that manages the root resources.
     *
     * @param uri           URI on which the Jersey web application will be deployed. Only first path segment will be
     *                      used as context path, the rest will be ignored.
     * @param configuration web application configuration.
     * @param start         if set to false, server will not get started, which allows to configure the underlying
     *                      transport layer, see above for details.
     * @return newly created {@link Server}.
     *
     * @throws ProcessingException      in case of any failure when creating a new Jetty {@code Server} instance.
     * @throws IllegalArgumentException if {@code uri} is {@code null}.
     */
    public static Server createServer(final URI uri, final ResourceConfig configuration, final boolean start)
            throws ProcessingException {
        return createServer(uri, null, ContainerFactory.createContainer(JettyHttpContainer.class, configuration), start);
    }


    /**
     * Create a {@link Server} that registers an {@link org.eclipse.jetty.server.Handler} that
     * in turn manages all root resource and provider classes declared by the
     * resource configuration.
     *
     * @param uri           the URI to create the http server. The URI scheme must be
     *                      equal to "https". The URI user information and host
     *                      are ignored If the URI port is not present then port 143 will be
     *                      used. The URI path, query and fragment components are ignored.
     * @param config        the resource configuration.
     * @param parentContext DI provider specific context with application's registered bindings.
     * @param start         if set to false, server will not get started, this allows end users to set
     *                      additional properties on the underlying listener.
     * @return newly created {@link Server}.
     *
     * @throws ProcessingException      in case of any failure when creating a new Jetty {@code Server} instance.
     * @throws IllegalArgumentException if {@code uri} is {@code null}.
     * @see JettyHttpContainer
     * @since 2.12
     */
    public static Server createServer(final URI uri, final ResourceConfig config, final boolean start,
                                      final Object parentContext) {
        return createServer(uri, null, new JettyHttpContainer(config, parentContext), start);
    }


    /**
     * Create a {@link Server} that registers an {@link org.eclipse.jetty.server.Handler} that
     * in turn manages all root resource and provider classes declared by the
     * resource configuration.
     *
     * @param uri           the URI to create the http server. The URI scheme must be
     *                      equal to "https". The URI user information and host
     *                      are ignored If the URI port is not present then port 143 will be
     *                      used. The URI path, query and fragment components are ignored.
     * @param config        the resource configuration.
     * @param parentContext DI provider specific context with application's registered bindings.
     * @return newly created {@link Server}.
     *
     * @throws ProcessingException      in case of any failure when creating a new Jetty {@code Server} instance.
     * @throws IllegalArgumentException if {@code uri} is {@code null}.
     * @see JettyHttpContainer
     * @since 2.12
     */
    public static Server createServer(final URI uri, final ResourceConfig config, final Object parentContext) {
        return createServer(uri, null, new JettyHttpContainer(config, parentContext), true);
    }

    /**
     * Create a {@link Server} that registers an {@link org.eclipse.jetty.server.Handler} that
     * in turn manages all root resource and provider classes declared by the
     * resource configuration.
     * <p/>
     * This implementation defers to the
     * {@link ContainerFactory#createContainer(Class, jakarta.ws.rs.core.Application)} method
     * for creating an Container that manages the root resources.
     *
     * @param uri               the URI to create the http server. The URI scheme must be
     *                          equal to {@code https}. The URI user information and host
     *                          are ignored. If the URI port is not present then port
     *                          {@value Container#DEFAULT_HTTPS_PORT} will be
     *                          used. The URI path, query and fragment components are ignored.
     * @param sslContextFactory this is the SSL context factory used to configure SSL connector
     * @param config            the resource configuration.
     * @return newly created {@link Server}.
     *
     * @throws ProcessingException      in case of any failure when creating a new Jetty {@code Server} instance.
     * @throws IllegalArgumentException if {@code uri} is {@code null}.
     */
    public static Server createServer(final URI uri, final SslContextFactory.Server sslContextFactory,
                                      final ResourceConfig config)
            throws ProcessingException {
        final JettyHttpContainer container = ContainerFactory.createContainer(JettyHttpContainer.class, config);
        return createServer(uri, sslContextFactory, container, true);
    }

    /**
     * Create a {@link Server} that registers an {@link org.eclipse.jetty.server.Handler} that
     * in turn manages all root resource and provider classes found by searching the
     * classes referenced in the java classpath.
     *
     * @param uri               the URI to create the http server. The URI scheme must be
     *                          equal to {@code https}. The URI user information and host
     *                          are ignored. If the URI port is not present then port
     *                          {@value Container#DEFAULT_HTTPS_PORT} will be
     *                          used. The URI path, query and fragment components are ignored.
     * @param sslContextFactory this is the SSL context factory used to configure SSL connector
     * @param handler           the container that handles all HTTP requests
     * @param start             if set to false, server will not get started, this allows end users to set
     *                          additional properties on the underlying listener.
     * @return newly created {@link Server}.
     *
     * @throws ProcessingException      in case of any failure when creating a new Jetty {@code Server} instance.
     * @throws IllegalArgumentException if {@code uri} is {@code null}.
     * @see JettyHttpContainer
     */
    public static Server createServer(final URI uri,
                                      final SslContextFactory.Server sslContextFactory,
                                      final JettyHttpContainer handler,
                                      final boolean start) {
        throw new ProcessingException(LocalizationMessages.NOT_SUPPORTED());
    }
}