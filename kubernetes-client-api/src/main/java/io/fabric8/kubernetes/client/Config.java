/*
 * Copyright (C) 2015 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.fabric8.kubernetes.client;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.fabric8.kubernetes.api.model.AuthProviderConfig;
import io.fabric8.kubernetes.api.model.ConfigBuilder;
import io.fabric8.kubernetes.api.model.NamedContext;
import io.fabric8.kubernetes.client.http.TlsVersion;
import io.fabric8.kubernetes.client.internal.CertUtils;
import io.fabric8.kubernetes.client.internal.KubeConfigUtils;
import io.fabric8.kubernetes.client.internal.SSLUtils;
import io.fabric8.kubernetes.client.readiness.Readiness;
import io.fabric8.kubernetes.client.utils.IOHelpers;
import io.fabric8.kubernetes.client.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

@SuppressWarnings({ "LombokGetterMayBeUsed", "LombokSetterMayBeUsed" })
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(allowGetters = true, allowSetters = true)
public class Config {

  private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);

  /**
   * Disables autoconfiguration based on opinionated defaults in a {@link Config} object in the all arguments constructor
   */
  public static final String KUBERNETES_DISABLE_AUTO_CONFIG_SYSTEM_PROPERTY = "kubernetes.disable.autoConfig";
  public static final String KUBERNETES_MASTER_SYSTEM_PROPERTY = "kubernetes.master";
  public static final String KUBERNETES_API_VERSION_SYSTEM_PROPERTY = "kubernetes.api.version";
  public static final String KUBERNETES_TRUST_CERT_SYSTEM_PROPERTY = "kubernetes.trust.certificates";
  public static final String KUBERNETES_DISABLE_HOSTNAME_VERIFICATION_SYSTEM_PROPERTY = "kubernetes.disable.hostname.verification";
  public static final String KUBERNETES_CA_CERTIFICATE_FILE_SYSTEM_PROPERTY = "kubernetes.certs.ca.file";
  public static final String KUBERNETES_CA_CERTIFICATE_DATA_SYSTEM_PROPERTY = "kubernetes.certs.ca.data";
  public static final String KUBERNETES_CLIENT_CERTIFICATE_FILE_SYSTEM_PROPERTY = "kubernetes.certs.client.file";
  public static final String KUBERNETES_CLIENT_CERTIFICATE_DATA_SYSTEM_PROPERTY = "kubernetes.certs.client.data";
  public static final String KUBERNETES_CLIENT_KEY_FILE_SYSTEM_PROPERTY = "kubernetes.certs.client.key.file";
  public static final String KUBERNETES_CLIENT_KEY_DATA_SYSTEM_PROPERTY = "kubernetes.certs.client.key.data";
  public static final String KUBERNETES_CLIENT_KEY_ALGO_SYSTEM_PROPERTY = "kubernetes.certs.client.key.algo";
  public static final String KUBERNETES_CLIENT_KEY_PASSPHRASE_SYSTEM_PROPERTY = "kubernetes.certs.client.key.passphrase";
  public static final String KUBERNETES_AUTH_BASIC_USERNAME_SYSTEM_PROPERTY = "kubernetes.auth.basic.username";
  public static final String KUBERNETES_AUTH_BASIC_PASSWORD_SYSTEM_PROPERTY = "kubernetes.auth.basic.password";
  public static final String KUBERNETES_AUTH_TRYKUBECONFIG_SYSTEM_PROPERTY = "kubernetes.auth.tryKubeConfig";
  public static final String KUBERNETES_AUTH_TRYSERVICEACCOUNT_SYSTEM_PROPERTY = "kubernetes.auth.tryServiceAccount";
  public static final String KUBERNETES_AUTH_SERVICEACCOUNT_TOKEN_FILE_SYSTEM_PROPERTY = "kubernetes.auth.serviceAccount.token";
  public static final String KUBERNETES_OAUTH_TOKEN_SYSTEM_PROPERTY = "kubernetes.auth.token";
  public static final String KUBERNETES_WATCH_RECONNECT_INTERVAL_SYSTEM_PROPERTY = "kubernetes.watch.reconnectInterval";
  public static final String KUBERNETES_WATCH_RECONNECT_LIMIT_SYSTEM_PROPERTY = "kubernetes.watch.reconnectLimit";
  public static final String KUBERNETES_CONNECTION_TIMEOUT_SYSTEM_PROPERTY = "kubernetes.connection.timeout";
  public static final String KUBERNETES_UPLOAD_REQUEST_TIMEOUT_SYSTEM_PROPERTY = "kubernetes.upload.request.timeout";
  public static final String KUBERNETES_REQUEST_TIMEOUT_SYSTEM_PROPERTY = "kubernetes.request.timeout";
  public static final String KUBERNETES_REQUEST_RETRY_BACKOFFLIMIT_SYSTEM_PROPERTY = "kubernetes.request.retry.backoffLimit";
  public static final String KUBERNETES_REQUEST_RETRY_BACKOFFINTERVAL_SYSTEM_PROPERTY = "kubernetes.request.retry.backoffInterval";
  public static final String KUBERNETES_LOGGING_INTERVAL_SYSTEM_PROPERTY = "kubernetes.logging.interval";
  public static final String KUBERNETES_SCALE_TIMEOUT_SYSTEM_PROPERTY = "kubernetes.scale.timeout";
  public static final String KUBERNETES_WEBSOCKET_PING_INTERVAL_SYSTEM_PROPERTY = "kubernetes.websocket.ping.interval";
  public static final String KUBERNETES_MAX_CONCURRENT_REQUESTS = "kubernetes.max.concurrent.requests";
  public static final String KUBERNETES_MAX_CONCURRENT_REQUESTS_PER_HOST = "kubernetes.max.concurrent.requests.per.host";

  public static final String KUBERNETES_IMPERSONATE_USERNAME = "kubernetes.impersonate.username";
  public static final String KUBERNETES_IMPERSONATE_GROUP = "kubernetes.impersonate.group";

  public static final String KUBERNETES_TRUSTSTORE_PASSPHRASE_PROPERTY = "kubernetes.truststore.passphrase";
  public static final String KUBERNETES_TRUSTSTORE_FILE_PROPERTY = "kubernetes.truststore.file";
  public static final String KUBERNETES_KEYSTORE_PASSPHRASE_PROPERTY = "kubernetes.keystore.passphrase";
  public static final String KUBERNETES_KEYSTORE_FILE_PROPERTY = "kubernetes.keystore.file";

  public static final String KUBERNETES_TLS_VERSIONS = "kubernetes.tls.versions";

  public static final String KUBERNETES_TRYNAMESPACE_PATH_SYSTEM_PROPERTY = "kubernetes.tryNamespacePath";
  public static final String KUBERNETES_NAMESPACE_PATH = "/var/run/secrets/kubernetes.io/serviceaccount/namespace";
  public static final String KUBERNETES_NAMESPACE_FILE = "kubenamespace";
  public static final String KUBERNETES_NAMESPACE_SYSTEM_PROPERTY = "kubernetes.namespace";
  public static final String KUBERNETES_KUBECONFIG_FILE = "kubeconfig";
  public static final String KUBERNETES_SERVICE_HOST_PROPERTY = "KUBERNETES_SERVICE_HOST";
  public static final String KUBERNETES_SERVICE_PORT_PROPERTY = "KUBERNETES_SERVICE_PORT";
  public static final String KUBERNETES_SERVICE_ACCOUNT_TOKEN_PATH = "/var/run/secrets/kubernetes.io/serviceaccount/token";
  public static final String KUBERNETES_SERVICE_ACCOUNT_CA_CRT_PATH = "/var/run/secrets/kubernetes.io/serviceaccount/ca.crt";
  public static final String KUBERNETES_HTTP2_DISABLE = "http2.disable";
  public static final String KUBERNETES_HTTP_PROXY = "http.proxy";
  public static final String KUBERNETES_HTTPS_PROXY = "https.proxy";
  public static final String KUBERNETES_ALL_PROXY = "all.proxy";
  public static final String KUBERNETES_NO_PROXY = "no.proxy";
  public static final String KUBERNETES_PROXY_USERNAME = "proxy.username";
  public static final String KUBERNETES_PROXY_PASSWORD = "proxy.password";

  public static final String KUBERNETES_USER_AGENT = "kubernetes.user.agent";

  public static final String DEFAULT_MASTER_URL = "https://kubernetes.default.svc";
  public static final Long DEFAULT_SCALE_TIMEOUT = 10 * 60 * 1000L;
  public static final int DEFAULT_REQUEST_TIMEOUT = 10 * 1000;
  public static final int DEFAULT_LOGGING_INTERVAL = 20 * 1000;
  public static final Long DEFAULT_WEBSOCKET_PING_INTERVAL = 30 * 1000L;

  public static final Integer DEFAULT_MAX_CONCURRENT_REQUESTS = 64;
  public static final Integer DEFAULT_MAX_CONCURRENT_REQUESTS_PER_HOST = 5;

  public static final Integer DEFAULT_REQUEST_RETRY_BACKOFFLIMIT = 10;
  public static final Integer DEFAULT_REQUEST_RETRY_BACKOFFINTERVAL = 100;

  public static final int DEFAULT_UPLOAD_REQUEST_TIMEOUT = 120 * 1000;

  public static final String HTTP_PROTOCOL_PREFIX = "http://";
  public static final String HTTPS_PROTOCOL_PREFIX = "https://";
  public static final String SOCKS5_PROTOCOL_PREFIX = "socks5://";

  private static final int DEFAULT_WATCH_RECONNECT_INTERVAL = 1000;
  private static final int DEFAULT_CONNECTION_TIMEOUT = 10 * 1000;
  private static final String DEFAULT_CLIENT_KEY_PASSPHRASE = "changeit";

  private Boolean trustCerts;
  private Boolean disableHostnameVerification;
  private String masterUrl;
  private String apiVersion;
  private String namespace;
  private Boolean defaultNamespace;
  private String caCertFile;
  private String caCertData;
  private String clientCertFile;
  private String clientCertData;
  private String clientKeyFile;
  private String clientKeyData;
  private String clientKeyAlgo;
  private String clientKeyPassphrase;
  private String trustStoreFile;
  private String trustStorePassphrase;
  private String keyStoreFile;
  private String keyStorePassphrase;
  private AuthProviderConfig authProvider;
  private String username;
  private String password;
  private volatile String oauthToken;
  @JsonIgnore
  private volatile String autoOAuthToken;
  private OAuthTokenProvider oauthTokenProvider;
  private Long websocketPingInterval;
  private Integer connectionTimeout;
  private Integer maxConcurrentRequests;
  private Integer maxConcurrentRequestsPerHost;

  private final RequestConfig requestConfig;

  private List<NamedContext> contexts;
  private NamedContext currentContext = null;

  /**
   * fields not used but needed for builder generation.
   */
  private Integer watchReconnectInterval;
  private Integer watchReconnectLimit;
  private Integer uploadRequestTimeout;
  private Integer requestRetryBackoffLimit;
  private Integer requestRetryBackoffInterval;
  private Integer requestTimeout;
  private Long scaleTimeout;
  private Integer loggingInterval;
  private String impersonateUsername;
  private String[] impersonateGroups;
  private Map<String, List<String>> impersonateExtras;
  /**
   * end of fields not used but needed for builder generation.
   */

  private Boolean http2Disable;
  private String httpProxy;
  private String httpsProxy;
  private String proxyUsername;
  private String proxyPassword;
  private String[] noProxy;
  private String userAgent;
  private TlsVersion[] tlsVersions;

  private Boolean onlyHttpWatches;

  /**
   * custom headers
   */
  private Map<String, String> customHeaders = null;

  private Boolean autoConfigure;

  @JsonIgnore
  protected Map<String, Object> additionalProperties = new HashMap<>();

  protected static boolean disableAutoConfig() {
    return Utils.getSystemPropertyOrEnvVar(KUBERNETES_DISABLE_AUTO_CONFIG_SYSTEM_PROPERTY, false);
  }

  /**
   * Create an empty {@link Config} class without any automatic configuration
   * (i.e. reading system properties/environment variables to set values).
   * <p>
   * The configuration does include the Kubernetes Client default values for timeouts, etc.
   * <p>
   * You can also reuse this object to build your own {@link Config} object
   * without any auto configuration like this:
   *
   * <pre>{@code
   * Config configFromBuilder = new ConfigBuilder(Config.empty())
   *                                // ...
   *                               .build();
   * }</pre>
   *
   * @return a Config object without any automatic configuration.
   */
  public static Config empty() {
    return new Config(false);
  }

  /**
   * Does auto-detection with some opinionated defaults.
   *
   * @param context if null will use current-context
   * @return Config object
   */
  public static Config autoConfigure(String context) {
    final Config config = new Config(false);
    autoConfigure(config, context);
    return config;
  }

  private static void autoConfigure(Config config, String context) {
    final var kubeConfigFiles = findKubeConfigFiles();
    if (!kubeConfigFiles.isEmpty()) {
      final var kubeconfigs = kubeConfigFiles.stream()
          .map(KubeConfigUtils::parseConfig)
          .toArray(io.fabric8.kubernetes.api.model.Config[]::new);
      KubeConfigUtils.merge(config, context, kubeconfigs);
    } else {
      tryServiceAccount(config);
      tryNamespaceFromPath(config);
    }
    postAutoConfigure(config);
    config.autoConfigure = true;
  }

  private static void postAutoConfigure(Config config) {
    configFromSysPropsOrEnvVars(config);

    config.masterUrl = ensureHttps(config.masterUrl, config);
    config.masterUrl = ensureEndsWithSlash(config.masterUrl);
  }

  private static String ensureEndsWithSlash(String masterUrl) {
    if (!masterUrl.endsWith("/")) {
      masterUrl = masterUrl + "/";
    }
    return masterUrl;
  }

  private static String ensureHttps(String masterUrl, Config config) {
    if (!masterUrl.toLowerCase(Locale.ROOT).startsWith(HTTP_PROTOCOL_PREFIX)
        && !masterUrl.toLowerCase(Locale.ROOT).startsWith(HTTPS_PROTOCOL_PREFIX)) {
      masterUrl = (SSLUtils.isHttpsAvailable(config) ? HTTPS_PROTOCOL_PREFIX : HTTP_PROTOCOL_PREFIX) + masterUrl;
    }
    return masterUrl;
  }

  protected Config(boolean autoConfigure) {
    this(null, null, null, null, null,
        null, null, null, null, null,
        null, null, null, null, null,
        null, null, null, null, null,
        null, null, null, null,
        null,
        null, null, null, null,
        null, null, null,
        null,
        null, null, null, null, null,
        null, null, null,
        null, null, null,
        null, null, null, null,
        null, autoConfigure, true);
  }

  @JsonCreator
  public Config(
      @JsonProperty("masterUrl") String masterUrl,
      @JsonProperty("apiVersion") String apiVersion,
      @JsonProperty("namespace") String namespace,
      @JsonProperty("trustCerts") Boolean trustCerts,
      @JsonProperty("disableHostnameVerification") Boolean disableHostnameVerification,
      @JsonProperty("caCertFile") String caCertFile,
      @JsonProperty("caCertData") String caCertData,
      @JsonProperty("clientCertFile") String clientCertFile,
      @JsonProperty("clientCertData") String clientCertData,
      @JsonProperty("clientKeyFile") String clientKeyFile,
      @JsonProperty("clientKeyData") String clientKeyData,
      @JsonProperty("clientKeyAlgo") String clientKeyAlgo,
      @JsonProperty("clientKeyPassphrase") String clientKeyPassphrase,
      @JsonProperty("username") String username,
      @JsonProperty("password") String password,
      @JsonProperty("oauthToken") String oauthToken,
      @JsonProperty("autoOAuthToken") String autoOAuthToken,
      @JsonProperty("watchReconnectInterval") Integer watchReconnectInterval,
      @JsonProperty("watchReconnectLimit") Integer watchReconnectLimit,
      @JsonProperty("connectionTimeout") Integer connectionTimeout,
      @JsonProperty("requestTimeout") Integer requestTimeout,
      @JsonProperty("scaleTimeout") Long scaleTimeout,
      @JsonProperty("loggingInterval") Integer loggingInterval,
      @JsonProperty("maxConcurrentRequests") Integer maxConcurrentRequests,
      @JsonProperty("maxConcurrentRequestsPerHost") Integer maxConcurrentRequestsPerHost,
      @JsonProperty("http2Disable") Boolean http2Disable,
      @JsonProperty("httpProxy") String httpProxy,
      @JsonProperty("httpsProxy") String httpsProxy,
      @JsonProperty("noProxy") String[] noProxy,
      @JsonProperty("userAgent") String userAgent,
      @JsonProperty("tlsVersions") TlsVersion[] tlsVersions,
      @JsonProperty("websocketPingInterval") Long websocketPingInterval,
      @JsonProperty("proxyUsername") String proxyUsername,
      @JsonProperty("proxyPassword") String proxyPassword,
      @JsonProperty("trustStoreFile") String trustStoreFile,
      @JsonProperty("trustStorePassphrase") String trustStorePassphrase,
      @JsonProperty("keyStoreFile") String keyStoreFile,
      @JsonProperty("keyStorePassphrase") String keyStorePassphrase,
      @JsonProperty("impersonateUsername") String impersonateUsername,
      @JsonProperty("impersonateGroups") String[] impersonateGroups,
      @JsonProperty("impersonateExtras") Map<String, List<String>> impersonateExtras,
      @JsonProperty("oauthTokenProvider") OAuthTokenProvider oauthTokenProvider,
      @JsonProperty("customHeaders") Map<String, String> customHeaders,
      @JsonProperty("requestRetryBackoffLimit") Integer requestRetryBackoffLimit,
      @JsonProperty("requestRetryBackoffInterval") Integer requestRetryBackoffInterval,
      @JsonProperty("uploadRequestTimeout") Integer uploadRequestTimeout,
      @JsonProperty("onlyHttpWatches") Boolean onlyHttpWatches,
      @JsonProperty("currentContext") NamedContext currentContext,
      @JsonProperty("contexts") List<NamedContext> contexts,
      @JsonProperty("autoConfigure") Boolean autoConfigure) {
    this(masterUrl, apiVersion, namespace, trustCerts, disableHostnameVerification, caCertFile, caCertData,
        clientCertFile, clientCertData, clientKeyFile, clientKeyData, clientKeyAlgo, clientKeyPassphrase, username,
        password, oauthToken, autoOAuthToken, watchReconnectInterval, watchReconnectLimit, connectionTimeout, requestTimeout,
        scaleTimeout, loggingInterval, maxConcurrentRequests, maxConcurrentRequestsPerHost, http2Disable,
        httpProxy, httpsProxy, noProxy, userAgent, tlsVersions, websocketPingInterval, proxyUsername, proxyPassword,
        trustStoreFile, trustStorePassphrase, keyStoreFile, keyStorePassphrase, impersonateUsername, impersonateGroups,
        impersonateExtras, oauthTokenProvider, customHeaders, requestRetryBackoffLimit, requestRetryBackoffInterval,
        uploadRequestTimeout, onlyHttpWatches, currentContext, contexts, autoConfigure, true);
  }

  /*
   * The Builder is generated in SundrioConfig, if new fields need to be added here, please make sure to add them there too.
   */
  Config(String masterUrl, String apiVersion, String namespace, Boolean trustCerts, Boolean disableHostnameVerification,
      String caCertFile, String caCertData, String clientCertFile, String clientCertData, String clientKeyFile,
      String clientKeyData, String clientKeyAlgo, String clientKeyPassphrase, String username, String password,
      String oauthToken, String autoOAuthToken, Integer watchReconnectInterval, Integer watchReconnectLimit,
      Integer connectionTimeout,
      Integer requestTimeout,
      Long scaleTimeout, Integer loggingInterval, Integer maxConcurrentRequests, Integer maxConcurrentRequestsPerHost,
      Boolean http2Disable, String httpProxy, String httpsProxy, String[] noProxy,
      String userAgent, TlsVersion[] tlsVersions, Long websocketPingInterval, String proxyUsername,
      String proxyPassword, String trustStoreFile, String trustStorePassphrase, String keyStoreFile, String keyStorePassphrase,
      String impersonateUsername, String[] impersonateGroups, Map<String, List<String>> impersonateExtras,
      OAuthTokenProvider oauthTokenProvider, Map<String, String> customHeaders, Integer requestRetryBackoffLimit,
      Integer requestRetryBackoffInterval, Integer uploadRequestTimeout, Boolean onlyHttpWatches, NamedContext currentContext,
      List<NamedContext> contexts, Boolean autoConfigure, Boolean shouldSetDefaultValues) {
    if (Boolean.TRUE.equals(shouldSetDefaultValues)) {
      this.masterUrl = DEFAULT_MASTER_URL;
      this.apiVersion = "v1";
      this.defaultNamespace = true;
      this.trustCerts = false;
      this.disableHostnameVerification = false;
      this.onlyHttpWatches = false;
      this.http2Disable = false;
      this.clientKeyAlgo = "RSA";
      this.clientKeyPassphrase = DEFAULT_CLIENT_KEY_PASSPHRASE;
      this.websocketPingInterval = DEFAULT_WEBSOCKET_PING_INTERVAL;
      this.connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
      this.maxConcurrentRequests = DEFAULT_MAX_CONCURRENT_REQUESTS;
      this.maxConcurrentRequestsPerHost = DEFAULT_MAX_CONCURRENT_REQUESTS_PER_HOST;
      this.contexts = new ArrayList<>();
      this.watchReconnectInterval = DEFAULT_WATCH_RECONNECT_INTERVAL;
      this.watchReconnectLimit = -1;
      this.uploadRequestTimeout = DEFAULT_UPLOAD_REQUEST_TIMEOUT;
      this.requestRetryBackoffInterval = DEFAULT_REQUEST_RETRY_BACKOFFINTERVAL;
      this.requestRetryBackoffLimit = DEFAULT_REQUEST_RETRY_BACKOFFLIMIT;
      this.requestTimeout = DEFAULT_REQUEST_TIMEOUT;
      this.scaleTimeout = DEFAULT_SCALE_TIMEOUT;
      this.loggingInterval = DEFAULT_LOGGING_INTERVAL;
      this.userAgent = "fabric8-kubernetes-client/" + Version.clientVersion();
      this.tlsVersions = new TlsVersion[] { TlsVersion.TLS_1_3, TlsVersion.TLS_1_2 };
      this.requestConfig = new RequestConfig(this.watchReconnectLimit, this.watchReconnectInterval,
          this.requestTimeout, this.scaleTimeout, this.loggingInterval,
          this.requestRetryBackoffLimit, this.requestRetryBackoffInterval, this.uploadRequestTimeout);
    } else {
      this.requestConfig = new RequestConfig(null, null, null, null, null, null, null, null);
    }

    if (Boolean.TRUE.equals(autoConfigure)) {
      autoConfigure(this, null);
    }
    if (Utils.isNotNullOrEmpty(apiVersion)) {
      this.apiVersion = apiVersion;
    }
    if (Utils.isNotNullOrEmpty(masterUrl)) {
      this.masterUrl = masterUrl;
    }
    if (Utils.isNotNullOrEmpty(namespace)) {
      this.namespace = namespace;
    }
    if (Boolean.TRUE.equals(trustCerts)) {
      this.trustCerts = true;
    }
    if (Boolean.TRUE.equals(disableHostnameVerification)) {
      this.disableHostnameVerification = true;
    }
    if (Utils.isNotNullOrEmpty(caCertFile)) {
      this.caCertFile = caCertFile;
    }
    if (Utils.isNotNullOrEmpty(caCertData)) {
      this.caCertData = caCertData;
    }
    if (Utils.isNotNullOrEmpty(clientCertFile)) {
      this.clientCertFile = clientCertFile;
    }
    if (Utils.isNotNullOrEmpty(clientCertData)) {
      this.clientCertData = clientCertData;
    }
    if (Utils.isNotNullOrEmpty(clientKeyFile)) {
      this.clientKeyFile = clientKeyFile;
    }
    if (Utils.isNotNullOrEmpty(clientKeyData)) {
      this.clientKeyData = clientKeyData;
    }
    if (Utils.isNotNullOrEmpty(clientKeyAlgo)) {
      this.clientKeyAlgo = clientKeyAlgo;
    }
    if (Utils.isNotNullOrEmpty(clientKeyPassphrase)) {
      this.clientKeyPassphrase = clientKeyPassphrase;
    }
    if (Utils.isNotNullOrEmpty(username)) {
      this.username = username;
    }
    if (Utils.isNotNullOrEmpty(password)) {
      this.password = password;
    }
    if (Utils.isNotNullOrEmpty(oauthToken)) {
      this.oauthToken = oauthToken;
    }
    if (websocketPingInterval != null) {
      this.websocketPingInterval = websocketPingInterval;
    }
    if (connectionTimeout != null) {
      this.connectionTimeout = connectionTimeout;
    }
    if (watchReconnectLimit != null) {
      setWatchReconnectLimit(watchReconnectLimit);
    }
    if (watchReconnectInterval != null) {
      setWatchReconnectInterval(watchReconnectInterval);
    }
    if (requestTimeout != null) {
      setRequestTimeout(requestTimeout);
    }
    if (scaleTimeout != null) {
      setScaleTimeout(scaleTimeout);
    }
    if (loggingInterval != null) {
      setLoggingInterval(loggingInterval);
    }
    if (requestRetryBackoffLimit != null) {
      setRequestRetryBackoffLimit(requestRetryBackoffLimit);
    }
    if (requestRetryBackoffInterval != null) {
      setRequestRetryBackoffInterval(requestRetryBackoffInterval);
    }
    if (uploadRequestTimeout != null) {
      setUploadRequestTimeout(uploadRequestTimeout);
    }
    if (Utils.isNotNullOrEmpty(impersonateUsername)) {
      setImpersonateUsername(impersonateUsername);
    }
    if (Utils.isNotNullOrEmpty(impersonateGroups)) {
      setImpersonateGroups(impersonateGroups);
    }
    if (Utils.isNotNullOrEmpty(impersonateExtras)) {
      setImpersonateExtras(impersonateExtras);
    }
    if (http2Disable != null) {
      this.http2Disable = http2Disable;
    }
    if (Utils.isNotNullOrEmpty(httpProxy)) {
      this.httpProxy = httpProxy;
    }
    if (Utils.isNotNullOrEmpty(httpsProxy)) {
      this.httpsProxy = httpsProxy;
    }
    if (Utils.isNotNullOrEmpty(noProxy)) {
      this.noProxy = noProxy;
    }
    if (Utils.isNotNullOrEmpty(proxyUsername)) {
      this.proxyUsername = proxyUsername;
    }
    if (Utils.isNotNullOrEmpty(proxyPassword)) {
      this.proxyPassword = proxyPassword;
    }
    if (Utils.isNotNullOrEmpty(userAgent)) {
      this.userAgent = userAgent;
    }
    if (tlsVersions != null && tlsVersions.length > 0) {
      this.tlsVersions = tlsVersions;
    }
    if (Utils.isNotNullOrEmpty(trustStoreFile)) {
      this.trustStoreFile = trustStoreFile;
    }
    if (Utils.isNotNullOrEmpty(trustStorePassphrase)) {
      this.trustStorePassphrase = trustStorePassphrase;
    }
    if (Utils.isNotNullOrEmpty(keyStoreFile)) {
      this.keyStoreFile = keyStoreFile;
    }
    if (Utils.isNotNullOrEmpty(keyStorePassphrase)) {
      this.keyStorePassphrase = keyStorePassphrase;
    }
    if (maxConcurrentRequests != null) {
      this.maxConcurrentRequests = maxConcurrentRequests;
    }
    if (maxConcurrentRequestsPerHost != null) {
      this.maxConcurrentRequestsPerHost = maxConcurrentRequestsPerHost;
    }
    if (Utils.isNotNullOrEmpty(autoOAuthToken)) {
      this.autoOAuthToken = autoOAuthToken;
    }
    if (contexts != null && !contexts.isEmpty()) {
      this.contexts = contexts;
    }
    if (Utils.isNotNull(currentContext)) {
      this.currentContext = currentContext;
    }
    if (Utils.isNotNullOrEmpty(this.masterUrl)) {
      this.masterUrl = ensureEndsWithSlash(ensureHttps(this.masterUrl, this));
    }
    this.autoConfigure = autoConfigure;
    this.oauthTokenProvider = oauthTokenProvider;
    this.customHeaders = customHeaders;
    this.onlyHttpWatches = onlyHttpWatches;
  }

  public static void configFromSysPropsOrEnvVars(Config config) {
    config.setTrustCerts(Utils.getSystemPropertyOrEnvVar(KUBERNETES_TRUST_CERT_SYSTEM_PROPERTY, config.isTrustCerts()));
    config.setDisableHostnameVerification(Utils.getSystemPropertyOrEnvVar(
        KUBERNETES_DISABLE_HOSTNAME_VERIFICATION_SYSTEM_PROPERTY, config.isDisableHostnameVerification()));
    config.setMasterUrl(Utils.getSystemPropertyOrEnvVar(KUBERNETES_MASTER_SYSTEM_PROPERTY, config.getMasterUrl()));
    config.setApiVersion(Utils.getSystemPropertyOrEnvVar(KUBERNETES_API_VERSION_SYSTEM_PROPERTY, config.getApiVersion()));
    config.setNamespace(Utils.getSystemPropertyOrEnvVar(KUBERNETES_NAMESPACE_SYSTEM_PROPERTY, config.getNamespace()));
    config
        .setCaCertFile(Utils.getSystemPropertyOrEnvVar(KUBERNETES_CA_CERTIFICATE_FILE_SYSTEM_PROPERTY, config.getCaCertFile()));
    config
        .setCaCertData(Utils.getSystemPropertyOrEnvVar(KUBERNETES_CA_CERTIFICATE_DATA_SYSTEM_PROPERTY, config.getCaCertData()));
    config.setClientCertFile(
        Utils.getSystemPropertyOrEnvVar(KUBERNETES_CLIENT_CERTIFICATE_FILE_SYSTEM_PROPERTY, config.getClientCertFile()));
    config.setClientCertData(
        Utils.getSystemPropertyOrEnvVar(KUBERNETES_CLIENT_CERTIFICATE_DATA_SYSTEM_PROPERTY, config.getClientCertData()));
    config.setClientKeyFile(
        Utils.getSystemPropertyOrEnvVar(KUBERNETES_CLIENT_KEY_FILE_SYSTEM_PROPERTY, config.getClientKeyFile()));
    config.setClientKeyData(
        Utils.getSystemPropertyOrEnvVar(KUBERNETES_CLIENT_KEY_DATA_SYSTEM_PROPERTY, config.getClientKeyData()));
    config.setClientKeyAlgo(getKeyAlgorithm(config.getClientKeyFile(), config.getClientKeyData()));
    config.setClientKeyPassphrase(Utils.getSystemPropertyOrEnvVar(KUBERNETES_CLIENT_KEY_PASSPHRASE_SYSTEM_PROPERTY,
        config.getClientKeyPassphrase()));
    config.setUserAgent(Utils.getSystemPropertyOrEnvVar(KUBERNETES_USER_AGENT, config.getUserAgent()));

    config.setTrustStorePassphrase(
        Utils.getSystemPropertyOrEnvVar(KUBERNETES_TRUSTSTORE_PASSPHRASE_PROPERTY, config.getTrustStorePassphrase()));
    config.setTrustStoreFile(Utils.getSystemPropertyOrEnvVar(KUBERNETES_TRUSTSTORE_FILE_PROPERTY, config.getTrustStoreFile()));
    config.setKeyStorePassphrase(
        Utils.getSystemPropertyOrEnvVar(KUBERNETES_KEYSTORE_PASSPHRASE_PROPERTY, config.getKeyStorePassphrase()));
    config.setKeyStoreFile(Utils.getSystemPropertyOrEnvVar(KUBERNETES_KEYSTORE_FILE_PROPERTY, config.getKeyStoreFile()));

    config
        .setAutoOAuthToken(Utils.getSystemPropertyOrEnvVar(KUBERNETES_OAUTH_TOKEN_SYSTEM_PROPERTY, config.getAutoOAuthToken()));
    config.setUsername(Utils.getSystemPropertyOrEnvVar(KUBERNETES_AUTH_BASIC_USERNAME_SYSTEM_PROPERTY, config.getUsername()));
    config.setPassword(Utils.getSystemPropertyOrEnvVar(KUBERNETES_AUTH_BASIC_PASSWORD_SYSTEM_PROPERTY, config.getPassword()));

    config.setImpersonateUsername(
        Utils.getSystemPropertyOrEnvVar(KUBERNETES_IMPERSONATE_USERNAME, config.getImpersonateUsername()));

    String configuredImpersonateGroups = Utils.getSystemPropertyOrEnvVar(KUBERNETES_IMPERSONATE_GROUP, Arrays
        .stream(Optional.ofNullable(config.getImpersonateGroups()).orElse(new String[0])).collect(Collectors.joining(",")));
    if (Utils.isNotNullOrEmpty(configuredImpersonateGroups)) {
      config.setImpersonateGroups(configuredImpersonateGroups.split(","));
    }

    String configuredWatchReconnectInterval = Utils
        .getSystemPropertyOrEnvVar(KUBERNETES_WATCH_RECONNECT_INTERVAL_SYSTEM_PROPERTY);
    if (configuredWatchReconnectInterval != null) {
      config.setWatchReconnectInterval(Integer.parseInt(configuredWatchReconnectInterval));
    }

    String configuredWatchReconnectLimit = Utils.getSystemPropertyOrEnvVar(KUBERNETES_WATCH_RECONNECT_LIMIT_SYSTEM_PROPERTY);
    if (configuredWatchReconnectLimit != null) {
      config.setWatchReconnectLimit(Integer.parseInt(configuredWatchReconnectLimit));
    }

    String configuredScaleTimeout = Utils.getSystemPropertyOrEnvVar(KUBERNETES_SCALE_TIMEOUT_SYSTEM_PROPERTY,
        String.valueOf(DEFAULT_SCALE_TIMEOUT));
    if (configuredScaleTimeout != null) {
      config.setScaleTimeout(Long.parseLong(configuredScaleTimeout));
    }

    String configuredLoggingInterval = Utils.getSystemPropertyOrEnvVar(KUBERNETES_LOGGING_INTERVAL_SYSTEM_PROPERTY,
        String.valueOf(DEFAULT_LOGGING_INTERVAL));
    if (configuredLoggingInterval != null) {
      config.setLoggingInterval(Integer.parseInt(configuredLoggingInterval));
    }

    config.setConnectionTimeout(
        Utils.getSystemPropertyOrEnvVar(KUBERNETES_CONNECTION_TIMEOUT_SYSTEM_PROPERTY, config.getConnectionTimeout()));
    config.setUploadRequestTimeout(
        Utils.getSystemPropertyOrEnvVar(KUBERNETES_UPLOAD_REQUEST_TIMEOUT_SYSTEM_PROPERTY, config.getUploadRequestTimeout()));
    config.setRequestTimeout(
        Utils.getSystemPropertyOrEnvVar(KUBERNETES_REQUEST_TIMEOUT_SYSTEM_PROPERTY, config.getRequestTimeout()));
    config.setRequestRetryBackoffLimit(Utils.getSystemPropertyOrEnvVar(KUBERNETES_REQUEST_RETRY_BACKOFFLIMIT_SYSTEM_PROPERTY,
        config.getRequestRetryBackoffLimit()));
    config.setRequestRetryBackoffInterval(Utils.getSystemPropertyOrEnvVar(
        KUBERNETES_REQUEST_RETRY_BACKOFFINTERVAL_SYSTEM_PROPERTY, config.getRequestRetryBackoffInterval()));

    String configuredWebsocketPingInterval = Utils.getSystemPropertyOrEnvVar(KUBERNETES_WEBSOCKET_PING_INTERVAL_SYSTEM_PROPERTY,
        String.valueOf(config.getWebsocketPingInterval()));
    if (configuredWebsocketPingInterval != null) {
      config.setWebsocketPingInterval(Long.parseLong(configuredWebsocketPingInterval));
    }

    String configuredMaxConcurrentRequests = Utils.getSystemPropertyOrEnvVar(KUBERNETES_MAX_CONCURRENT_REQUESTS,
        String.valueOf(config.getMaxConcurrentRequests()));
    if (configuredMaxConcurrentRequests != null) {
      config.setMaxConcurrentRequests(Integer.parseInt(configuredMaxConcurrentRequests));
    }

    String configuredMaxConcurrentReqeustsPerHost = Utils.getSystemPropertyOrEnvVar(KUBERNETES_MAX_CONCURRENT_REQUESTS_PER_HOST,
        String.valueOf(config.getMaxConcurrentRequestsPerHost()));
    if (configuredMaxConcurrentReqeustsPerHost != null) {
      config.setMaxConcurrentRequestsPerHost(Integer.parseInt(configuredMaxConcurrentReqeustsPerHost));
    }

    config.setHttp2Disable(Utils.getSystemPropertyOrEnvVar(KUBERNETES_HTTP2_DISABLE, config.isHttp2Disable()));

    // Only set http(s) proxy fields if they're not set. This is done in order to align behavior of
    // KubernetesClient with kubectl / client-go . Please see https://github.com/fabric8io/kubernetes-client/issues/6150
    // Precedence is given to proxy-url read from kubeconfig .
    if (Utils.isNullOrEmpty(config.getHttpProxy())) {
      config.setHttpProxy(Utils.getSystemPropertyOrEnvVar(KUBERNETES_ALL_PROXY, config.getHttpProxy()));
      config.setHttpProxy(Utils.getSystemPropertyOrEnvVar(KUBERNETES_HTTP_PROXY, config.getHttpProxy()));
    }
    if (Utils.isNullOrEmpty(config.getHttpsProxy())) {
      config.setHttpsProxy(Utils.getSystemPropertyOrEnvVar(KUBERNETES_ALL_PROXY, config.getHttpsProxy()));
      config.setHttpsProxy(Utils.getSystemPropertyOrEnvVar(KUBERNETES_HTTPS_PROXY, config.getHttpsProxy()));
    }

    config.setProxyUsername(Utils.getSystemPropertyOrEnvVar(KUBERNETES_PROXY_USERNAME, config.getProxyUsername()));
    config.setProxyPassword(Utils.getSystemPropertyOrEnvVar(KUBERNETES_PROXY_PASSWORD, config.getProxyPassword()));

    String noProxyVar = Utils.getSystemPropertyOrEnvVar(KUBERNETES_NO_PROXY);
    if (noProxyVar != null) {
      config.setNoProxy(noProxyVar.split(","));
    }

    String tlsVersionsVar = Utils.getSystemPropertyOrEnvVar(KUBERNETES_TLS_VERSIONS);
    if (tlsVersionsVar != null && !tlsVersionsVar.isEmpty()) {
      String[] tlsVersionsSplit = tlsVersionsVar.split(",");
      TlsVersion[] tlsVersions = new TlsVersion[tlsVersionsSplit.length];
      for (int i = 0; i < tlsVersionsSplit.length; i++) {
        tlsVersions[i] = TlsVersion.forJavaName(tlsVersionsSplit[i]);
      }
      config.setTlsVersions(tlsVersions);
    }
  }

  private static boolean tryServiceAccount(Config config) {
    LOGGER.debug("Trying to configure client from service account...");
    String masterHost = Utils.getSystemPropertyOrEnvVar(KUBERNETES_SERVICE_HOST_PROPERTY, (String) null);
    String masterPort = Utils.getSystemPropertyOrEnvVar(KUBERNETES_SERVICE_PORT_PROPERTY, (String) null);
    String caCertPath = Utils.getSystemPropertyOrEnvVar(KUBERNETES_CA_CERTIFICATE_FILE_SYSTEM_PROPERTY,
        KUBERNETES_SERVICE_ACCOUNT_CA_CRT_PATH);

    if (masterHost != null && masterPort != null) {
      String hostPort = joinHostPort(masterHost, masterPort);
      LOGGER.debug("Found service account host and port: {}", hostPort);
      config.setMasterUrl("https://" + hostPort);
    }
    if (Utils.getSystemPropertyOrEnvVar(KUBERNETES_AUTH_TRYSERVICEACCOUNT_SYSTEM_PROPERTY, true)) {
      boolean serviceAccountCaCertExists = Files.isRegularFile(new File(caCertPath).toPath());
      if (serviceAccountCaCertExists) {
        LOGGER.debug("Found service account ca cert at: [{}}].", caCertPath);
        config.setCaCertFile(caCertPath);
      } else {
        LOGGER.debug("Did not find service account ca cert at: [{}}].", caCertPath);
      }

      File saTokenPathFile = findServiceAccountTokenFile();
      if (saTokenPathFile != null) {
        String saTokenPathLocation = saTokenPathFile.getAbsolutePath();
        try {
          String serviceTokenCandidate = new String(Files.readAllBytes(saTokenPathFile.toPath()));
          LOGGER.debug("Found service account token at: [{}].", saTokenPathLocation);
          config.setAutoOAuthToken(serviceTokenCandidate);
          return true;
        } catch (IOException e) {
          // No service account token available...
          LOGGER.warn("Error reading service account token from: [{}]. Ignoring.", saTokenPathLocation);
        }
      }
    }
    return false;
  }

  private static File findServiceAccountTokenFile() {
    File saTokenPathFile;
    String saTokenPath = Utils.getSystemPropertyOrEnvVar(KUBERNETES_AUTH_SERVICEACCOUNT_TOKEN_FILE_SYSTEM_PROPERTY);
    if (saTokenPath != null) {
      // if user has set the service account token system property, we use it.
      saTokenPathFile = new File(saTokenPath);
    } else {
      // otherwise, let's try the default location iif the location exists.
      saTokenPathFile = new File(KUBERNETES_SERVICE_ACCOUNT_TOKEN_PATH);
      if (!saTokenPathFile.exists()) {
        saTokenPathFile = null;
        LOGGER.debug("Could not find the service account token at the default location: [{}]. Ignoring.",
            KUBERNETES_SERVICE_ACCOUNT_TOKEN_PATH);
      }
    }

    return saTokenPathFile;
  }

  private static String joinHostPort(String host, String port) {
    if (host.indexOf(':') >= 0) {
      // Host is an IPv6
      return "[" + host + "]:" + port;
    }
    return host + ":" + port;
  }

  public static Config fromKubeconfig(String kubeconfigContents) {
    return fromKubeconfig(null, KubeConfigUtils.parseConfigFromString(kubeconfigContents));
  }

  public static Config fromKubeconfig(File kubeconfigFile) {
    return fromKubeconfig(null, kubeconfigFile);
  }

  public static Config fromKubeconfig(String context, File kubeconfig) {
    return fromKubeconfig(context, KubeConfigUtils.parseConfig(kubeconfig));
  }

  private static Config fromKubeconfig(String context, io.fabric8.kubernetes.api.model.Config... kubeconfigs) {
    final Config ret = Config.empty();
    KubeConfigUtils.merge(ret, context, kubeconfigs);
    return ret;
  }

  // Note: kubeconfigPath is optional (see note on loadFromKubeConfig)
  /**
   * @deprecated use {@link #fromKubeconfig(File)} instead
   */
  @Deprecated(since = "7.0.0", forRemoval = true)
  public static Config fromKubeconfig(String context, String kubeconfigContents, String kubeconfigPath) {
    // we allow passing context along here, since downstream accepts it
    Config config = new Config(false);
    if (Utils.isNullOrEmpty(kubeconfigContents)) {
      throw new KubernetesClientException("Could not create Config from kubeconfig");
    }
    final var kubeconfig = KubeConfigUtils.parseConfigFromString(kubeconfigContents);
    if (kubeconfigPath != null) {
      // TODO: temp workaround until the method is removed (marked for removal in 7.0.0)
      kubeconfig.setAdditionalProperty("KUBERNETES_CONFIG_FILE_KEY", new File(kubeconfigPath));
    }
    KubeConfigUtils.merge(config, context, kubeconfig);
    if (!disableAutoConfig()) {
      postAutoConfigure(config);
    }
    return config;
  }

  /**
   * Refresh the config from file / env sources.
   * Any values that the user have programmatically set will be lost.
   *
   * @return this Config instance with the refreshed values (if applicable)
   */
  public Config refresh() {
    final String currentContextName = getCurrentContext() != null ? getCurrentContext().getName() : null;
    if (Utils.isNotNullOrEmpty(oauthToken)) {
      return this;
    }
    if (autoConfigure) {
      return Config.autoConfigure(currentContextName);
    }
    // Only possible if the Config was created using Config.fromKubeconfig, otherwise autoConfigure would have been called
    if (getFile() != null) {
      if (loadKubeConfigContents(getFile()) == null) {
        return this; // loadKubeConfigContents will have logged an exception
      }
      final var refreshedConfig = Config.fromKubeconfig(currentContextName, getFile());
      if (!disableAutoConfig()) {
        postAutoConfigure(refreshedConfig);
      }
      return refreshedConfig;
    }
    // nothing to refresh - the Config values were directly supplied
    return this;
  }

  private static Collection<File> findKubeConfigFiles() {
    LOGGER.debug("Trying to configure client from Kubernetes config...");
    if (!Utils.getSystemPropertyOrEnvVar(KUBERNETES_AUTH_TRYKUBECONFIG_SYSTEM_PROPERTY, true)) {
      return Collections.emptyList();
    }
    return getKubeconfigFilenames().stream()
        .map(File::new)
        .filter(f -> {
          if (!f.isFile()) {
            LOGGER.debug("Did not find Kubernetes config at: [{}]. Ignoring.", f.getPath());
            return false;
          }
          return true;
        })
        .filter(f -> Utils.isNotNullOrEmpty(loadKubeConfigContents(f)))
        .collect(Collectors.toList());
  }

  public static Collection<String> getKubeconfigFilenames() {
    final var valueOrDefault = Utils.getSystemPropertyOrEnvVar(KUBERNETES_KUBECONFIG_FILE,
        new File(getHomeDir(), ".kube" + File.separator + "config").toString());
    // if system property/env var contains multiple files take the first one based on the environment
    // we are running in (eg. : for Linux, ; for Windows)
    return Arrays.asList(valueOrDefault.split(File.pathSeparator));
  }

  private static String loadKubeConfigContents(File kubeConfigFile) {
    try (FileReader reader = new FileReader(kubeConfigFile)) {
      return IOHelpers.readFully(reader);
    } catch (IOException e) {
      LOGGER.error("Could not load Kubernetes config file from {}", kubeConfigFile.getPath(), e);
      return null;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static final class ExecCredential {
    public String kind;
    public String apiVersion;
    public ExecCredentialSpec spec;
    public ExecCredentialStatus status;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static final class ExecCredentialSpec {
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static final class ExecCredentialStatus {
    public String token;
    public String clientCertificateData;
    public String clientKeyData;
    // TODO expirationTimestamp
  }

  private static boolean tryNamespaceFromPath(Config config) {
    LOGGER.debug("Trying to configure client namespace from Kubernetes service account namespace path...");
    if (Utils.getSystemPropertyOrEnvVar(KUBERNETES_TRYNAMESPACE_PATH_SYSTEM_PROPERTY, true)) {
      String serviceAccountNamespace = Utils.getSystemPropertyOrEnvVar(KUBERNETES_NAMESPACE_FILE, KUBERNETES_NAMESPACE_PATH);
      boolean serviceAccountNamespaceExists = Files.isRegularFile(new File(serviceAccountNamespace).toPath());
      if (serviceAccountNamespaceExists) {
        LOGGER.debug("Found service account namespace at: [{}].", serviceAccountNamespace);
        try {
          String namespace = new String(Files.readAllBytes(new File(serviceAccountNamespace).toPath()));
          config.setNamespace(namespace.replace(System.lineSeparator(), ""));
          return true;
        } catch (IOException e) {
          LOGGER.error("Error reading service account namespace from: [" + serviceAccountNamespace + "].", e);
        }
      } else {
        LOGGER.debug("Did not find service account namespace at: [{}]. Ignoring.", serviceAccountNamespace);
      }
    }
    return false;
  }

  private static String getHomeDir() {
    return getHomeDir(Config::isDirectoryAndExists, Config::getSystemEnvVariable);
  }

  private static boolean isDirectoryAndExists(String filePath) {
    File f = new File(filePath);
    return f.exists() && f.isDirectory();
  }

  private static String getSystemEnvVariable(String envVariableName) {
    return System.getenv(envVariableName);
  }

  protected static String getHomeDir(Predicate<String> directoryExists, UnaryOperator<String> getEnvVar) {
    String home = getEnvVar.apply("HOME");
    if (home != null && !home.isEmpty() && directoryExists.test(home)) {
      return home;
    }
    String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
    if (osName.startsWith("win")) {
      String homeDrive = getEnvVar.apply("HOMEDRIVE");
      String homePath = getEnvVar.apply("HOMEPATH");
      if (homeDrive != null && !homeDrive.isEmpty() && homePath != null && !homePath.isEmpty()) {
        String homeDir = homeDrive + homePath;
        if (directoryExists.test(homeDir)) {
          return homeDir;
        }
      }
      String userProfile = getEnvVar.apply("USERPROFILE");
      if (userProfile != null && !userProfile.isEmpty() && directoryExists.test(userProfile)) {
        return userProfile;
      }
    }

    // Fall back to user.home should never really get here
    return System.getProperty("user.home", ".");
  }

  public static String getKeyAlgorithm(InputStream inputStream) throws IOException {
    try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
      String line, algorithm = null;

      while ((line = bufferedReader.readLine()) != null) {
        if (line.contains("BEGIN EC PRIVATE KEY"))
          algorithm = "EC";
        else if (line.contains("BEGIN RSA PRIVATE KEY")) {
          algorithm = "RSA";
        }
      }
      return algorithm;
    }
  }

  public static String getKeyAlgorithm(String clientKeyFile, String clientKeyData) {
    // Check if any system property is set
    if (Utils.getSystemPropertyOrEnvVar(KUBERNETES_CLIENT_KEY_ALGO_SYSTEM_PROPERTY) != null) {
      return Utils.getSystemPropertyOrEnvVar(KUBERNETES_CLIENT_KEY_ALGO_SYSTEM_PROPERTY);
    }

    // Detect algorithm
    try {
      if (clientKeyData != null || clientKeyFile != null) {
        ByteArrayInputStream keyInputStream = CertUtils.getInputStreamFromDataOrFile(clientKeyData, clientKeyFile);
        return getKeyAlgorithm(keyInputStream);
      }
    } catch (IOException exception) {
      LOGGER.debug("Failure in determining private key algorithm type, defaulting to RSA {}", exception.getMessage());
    }
    return null;
  }

  @JsonProperty("oauthToken")
  public String getOauthToken() {
    return oauthToken;
  }

  public void setOauthToken(String oauthToken) {
    this.oauthToken = oauthToken;
  }

  @JsonProperty("password")
  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  @JsonProperty("username")
  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  @JsonProperty("impersonateUsername")
  public String getImpersonateUsername() {
    return getRequestConfig().getImpersonateUsername();
  }

  public void setImpersonateUsername(String impersonateUsername) {
    this.requestConfig.setImpersonateUsername(impersonateUsername);
  }

  @JsonProperty("impersonateGroups")
  public String[] getImpersonateGroups() {
    return getRequestConfig().getImpersonateGroups();
  }

  public void setImpersonateGroups(String... impersonateGroup) {
    this.requestConfig.setImpersonateGroups(impersonateGroup);
  }

  @JsonProperty("impersonateExtras")
  public Map<String, List<String>> getImpersonateExtras() {
    return getRequestConfig().getImpersonateExtras();
  }

  public void setImpersonateExtras(Map<String, List<String>> impersonateExtras) {
    this.requestConfig.setImpersonateExtras(impersonateExtras);
  }

  @JsonProperty("clientKeyPassphrase")
  public String getClientKeyPassphrase() {
    return clientKeyPassphrase;
  }

  public void setClientKeyPassphrase(String clientKeyPassphrase) {
    this.clientKeyPassphrase = clientKeyPassphrase;
  }

  @JsonProperty("clientKeyAlgo")
  public String getClientKeyAlgo() {
    return clientKeyAlgo;
  }

  public void setClientKeyAlgo(String clientKeyAlgo) {
    this.clientKeyAlgo = clientKeyAlgo;
  }

  @JsonProperty("clientKeyData")
  public String getClientKeyData() {
    return clientKeyData;
  }

  public void setClientKeyData(String clientKeyData) {
    this.clientKeyData = clientKeyData;
  }

  @JsonProperty("clientKeyFile")
  public String getClientKeyFile() {
    return clientKeyFile;
  }

  public void setClientKeyFile(String clientKeyFile) {
    this.clientKeyFile = clientKeyFile;
  }

  @JsonProperty("clientCertData")
  public String getClientCertData() {
    return clientCertData;
  }

  public void setClientCertData(String clientCertData) {
    this.clientCertData = clientCertData;
  }

  @JsonProperty("clientCertFile")
  public String getClientCertFile() {
    return clientCertFile;
  }

  public void setClientCertFile(String clientCertFile) {
    this.clientCertFile = clientCertFile;
  }

  @JsonProperty("caCertData")
  public String getCaCertData() {
    return caCertData;
  }

  public void setCaCertData(String caCertData) {
    this.caCertData = caCertData;
  }

  @JsonProperty("caCertFile")
  public String getCaCertFile() {
    return caCertFile;
  }

  public void setCaCertFile(String caCertFile) {
    this.caCertFile = caCertFile;
  }

  @JsonProperty("apiVersion")
  public String getApiVersion() {
    return apiVersion;
  }

  public void setApiVersion(String apiVersion) {
    this.apiVersion = apiVersion;
  }

  @JsonProperty("masterUrl")
  public String getMasterUrl() {
    return masterUrl;
  }

  public void setMasterUrl(String masterUrl) {
    //We set the masterUrl because it's needed by ensureHttps
    this.masterUrl = masterUrl;
    this.masterUrl = ensureEndsWithSlash(ensureHttps(masterUrl, this));
  }

  @JsonProperty("trustCerts")
  public boolean isTrustCerts() {
    return Optional.ofNullable(trustCerts).orElse(false);
  }

  Boolean getTrustCerts() {
    return trustCerts;
  }

  public void setTrustCerts(boolean trustCerts) {
    this.trustCerts = trustCerts;
  }

  @JsonProperty("disableHostnameVerification")
  public boolean isDisableHostnameVerification() {
    return Optional.ofNullable(disableHostnameVerification).orElse(false);
  }

  Boolean getDisableHostnameVerification() {
    return disableHostnameVerification;
  }

  public void setDisableHostnameVerification(boolean disableHostnameVerification) {
    this.disableHostnameVerification = disableHostnameVerification;
  }

  @JsonProperty("watchReconnectInterval")
  public Integer getWatchReconnectInterval() {
    return requestConfig.getWatchReconnectInterval();
  }

  public void setWatchReconnectInterval(Integer watchReconnectInterval) {
    this.requestConfig.setWatchReconnectInterval(watchReconnectInterval);
  }

  @JsonProperty("watchReconnectLimit")
  public Integer getWatchReconnectLimit() {
    return getRequestConfig().getWatchReconnectLimit();
  }

  public void setWatchReconnectLimit(Integer watchReconnectLimit) {
    this.requestConfig.setWatchReconnectLimit(watchReconnectLimit);
  }

  public static ConfigBuilder builder() {
    return new ConfigBuilder();
  }

  @JsonProperty("connectionTimeout")
  public Integer getConnectionTimeout() {
    return connectionTimeout;
  }

  public void setConnectionTimeout(Integer connectionTimeout) {
    this.connectionTimeout = connectionTimeout;
  }

  @JsonProperty("uploadRequestTimeout")
  public Integer getUploadRequestTimeout() {
    return getRequestConfig().getUploadRequestTimeout();
  }

  public void setUploadRequestTimeout(Integer requestTimeout) {
    this.requestConfig.setUploadRequestTimeout(requestTimeout);
  }

  @JsonProperty("requestTimeout")
  public Integer getRequestTimeout() {
    return getRequestConfig().getRequestTimeout();
  }

  public void setRequestTimeout(Integer requestTimeout) {
    this.requestConfig.setRequestTimeout(requestTimeout);
  }

  @JsonProperty("requestRetryBackoffLimit")
  public Integer getRequestRetryBackoffLimit() {
    return getRequestConfig().getRequestRetryBackoffLimit();
  }

  public void setRequestRetryBackoffLimit(Integer requestRetryBackoffLimit) {
    requestConfig.setRequestRetryBackoffLimit(requestRetryBackoffLimit);
  }

  @JsonProperty("requestRetryBackoffInterval")
  public Integer getRequestRetryBackoffInterval() {
    return getRequestConfig().getRequestRetryBackoffInterval();
  }

  public void setRequestRetryBackoffInterval(Integer requestRetryBackoffInterval) {
    requestConfig.setRequestRetryBackoffInterval(requestRetryBackoffInterval);
  }

  @JsonProperty("scaleTimeout")
  public Long getScaleTimeout() {
    return getRequestConfig().getScaleTimeout();
  }

  public void setScaleTimeout(Long scaleTimeout) {
    this.requestConfig.setScaleTimeout(scaleTimeout);
  }

  @JsonProperty("loggingInterval")
  public Integer getLoggingInterval() {
    return getRequestConfig().getLoggingInterval();
  }

  public void setLoggingInterval(Integer loggingInterval) {
    this.requestConfig.setLoggingInterval(loggingInterval);
  }

  @JsonProperty("http2Disable")
  public boolean isHttp2Disable() {
    return Optional.ofNullable(http2Disable).orElse(false);
  }

  Boolean getHttp2Disable() {
    return http2Disable;
  }

  public void setHttp2Disable(Boolean http2Disable) {
    this.http2Disable = http2Disable;
  }

  public void setHttpProxy(String httpProxy) {
    this.httpProxy = httpProxy;
  }

  @JsonProperty("httpProxy")
  public String getHttpProxy() {
    return httpProxy;
  }

  public void setHttpsProxy(String httpsProxy) {
    this.httpsProxy = httpsProxy;
  }

  @JsonProperty("httpsProxy")
  public String getHttpsProxy() {
    return httpsProxy;
  }

  public void setNoProxy(String[] noProxy) {
    this.noProxy = noProxy;
  }

  @JsonProperty("noProxy")
  public String[] getNoProxy() {
    return noProxy;
  }

  @JsonProperty("namespace")
  public String getNamespace() {
    return namespace;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  @JsonProperty("defaultNamespace")
  public boolean isDefaultNamespace() {
    return Optional.ofNullable(defaultNamespace).orElse(true);
  }

  public void setDefaultNamespace(boolean defaultNamespace) {
    this.defaultNamespace = defaultNamespace;
  }

  @JsonProperty("userAgent")
  public String getUserAgent() {
    return userAgent;
  }

  public void setUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }

  @JsonProperty("tlsVersions")
  public TlsVersion[] getTlsVersions() {
    return tlsVersions;
  }

  public void setTlsVersions(TlsVersion[] tlsVersions) {
    this.tlsVersions = tlsVersions;
  }

  @JsonProperty("websocketPingInterval")
  public Long getWebsocketPingInterval() {
    return websocketPingInterval;
  }

  public void setWebsocketPingInterval(Long websocketPingInterval) {
    this.websocketPingInterval = websocketPingInterval;
  }

  public Integer getMaxConcurrentRequests() {
    return maxConcurrentRequests;
  }

  public void setMaxConcurrentRequests(Integer maxConcurrentRequests) {
    this.maxConcurrentRequests = maxConcurrentRequests;
  }

  public Integer getMaxConcurrentRequestsPerHost() {
    return maxConcurrentRequestsPerHost;
  }

  public void setMaxConcurrentRequestsPerHost(Integer maxConcurrentRequestsPerHost) {
    this.maxConcurrentRequestsPerHost = maxConcurrentRequestsPerHost;
  }

  @JsonProperty("proxyUsername")
  public String getProxyUsername() {
    return proxyUsername;
  }

  public void setProxyUsername(String proxyUsername) {
    this.proxyUsername = proxyUsername;
  }

  @JsonProperty("proxyPassword")
  public String getProxyPassword() {
    return proxyPassword;
  }

  public void setProxyPassword(String proxyPassword) {
    this.proxyPassword = proxyPassword;
  }

  public RequestConfig getRequestConfig() {
    return this.requestConfig;
  }

  public void setTrustStorePassphrase(String trustStorePassphrase) {
    this.trustStorePassphrase = trustStorePassphrase;
  }

  @JsonProperty("trustStorePassphrase")
  public String getTrustStorePassphrase() {
    return trustStorePassphrase;
  }

  public void setKeyStorePassphrase(String keyStorePassphrase) {
    this.keyStorePassphrase = keyStorePassphrase;
  }

  @JsonProperty("keyStorePassphrase")
  public String getKeyStorePassphrase() {
    return keyStorePassphrase;
  }

  public void setTrustStoreFile(String trustStoreFile) {
    this.trustStoreFile = trustStoreFile;
  }

  @JsonProperty("trustStoreFile")
  public String getTrustStoreFile() {
    return trustStoreFile;
  }

  public void setKeyStoreFile(String keyStoreFile) {
    this.keyStoreFile = keyStoreFile;
  }

  @JsonProperty("keyStoreFile")
  public String getKeyStoreFile() {
    return keyStoreFile;
  }

  @JsonIgnore
  public OAuthTokenProvider getOauthTokenProvider() {
    return this.oauthTokenProvider;
  }

  public void setOauthTokenProvider(OAuthTokenProvider oauthTokenProvider) {
    this.oauthTokenProvider = oauthTokenProvider;
  }

  @JsonProperty("customHeaders")
  public Map<String, String> getCustomHeaders() {
    return customHeaders;
  }

  public void setCustomHeaders(Map<String, String> customHeaders) {
    this.customHeaders = customHeaders;
  }

  public Boolean getAutoConfigure() {
    return autoConfigure;
  }

  /**
   * Returns all the {@link NamedContext}s that exist in the kube config
   *
   * @return all the contexts
   *
   * @see NamedContext
   */
  public List<NamedContext> getContexts() {
    return contexts;
  }

  public void setContexts(List<NamedContext> contexts) {
    this.contexts = contexts;
  }

  /**
   * Returns the current context that's defined in the kube config. Returns {@code null} if there's none
   *
   * @return the current context
   *
   * @see NamedContext
   */
  public NamedContext getCurrentContext() {
    return currentContext;
  }

  public void setCurrentContext(NamedContext context) {
    this.currentContext = context;
  }

  /**
   * Returns the path to the file that contains the context from which this configuration was loaded from.
   * <p>
   * Returns {@code null} if no file was used.
   *
   * @return the path to the kubeconfig file.
   */
  public File getFile() {
    return KubeConfigUtils.getFileFromContext(getCurrentContext());
  }

  @JsonIgnore
  public Readiness getReadiness() {
    return Readiness.getInstance();
  }

  public void setAuthProvider(AuthProviderConfig authProvider) {
    this.authProvider = authProvider;
  }

  public AuthProviderConfig getAuthProvider() {
    return authProvider;
  }

  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
  }

  public String getAutoOAuthToken() {
    return autoOAuthToken;
  }

  public void setAutoOAuthToken(String autoOAuthToken) {
    this.autoOAuthToken = autoOAuthToken;
  }

  public boolean isOnlyHttpWatches() {
    return Optional.ofNullable(onlyHttpWatches).orElse(false);
  }

  public void setOnlyHttpWatches(boolean onlyHttpWatches) {
    this.onlyHttpWatches = onlyHttpWatches;
  }

}
