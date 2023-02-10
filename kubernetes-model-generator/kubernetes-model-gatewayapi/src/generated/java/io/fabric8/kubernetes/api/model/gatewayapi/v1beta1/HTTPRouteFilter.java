
package io.fabric8.kubernetes.api.model.gatewayapi.v1beta1;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectReference;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.PodTemplateSpec;
import io.fabric8.kubernetes.api.model.ResourceRequirements;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.BuildableReference;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "apiVersion",
    "kind",
    "metadata",
    "extensionRef",
    "requestHeaderModifier",
    "requestMirror",
    "requestRedirect",
    "responseHeaderModifier",
    "type",
    "urlRewrite"
})
@ToString
@EqualsAndHashCode
@Setter
@Accessors(prefix = {
    "_",
    ""
})
@Buildable(editableEnabled = false, validationEnabled = false, generateBuilderPackage = false, lazyCollectionInitEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder", refs = {
    @BuildableReference(ObjectMeta.class),
    @BuildableReference(LabelSelector.class),
    @BuildableReference(Container.class),
    @BuildableReference(PodTemplateSpec.class),
    @BuildableReference(ResourceRequirements.class),
    @BuildableReference(IntOrString.class),
    @BuildableReference(ObjectReference.class),
    @BuildableReference(io.fabric8.kubernetes.api.model.LocalObjectReference.class),
    @BuildableReference(PersistentVolumeClaim.class)
})
public class HTTPRouteFilter implements KubernetesResource
{

    @JsonProperty("extensionRef")
    private io.fabric8.kubernetes.api.model.gatewayapi.v1beta1.LocalObjectReference extensionRef;
    @JsonProperty("requestHeaderModifier")
    private HTTPHeaderFilter requestHeaderModifier;
    @JsonProperty("requestMirror")
    private HTTPRequestMirrorFilter requestMirror;
    @JsonProperty("requestRedirect")
    private HTTPRequestRedirectFilter requestRedirect;
    @JsonProperty("responseHeaderModifier")
    private HTTPHeaderFilter responseHeaderModifier;
    @JsonProperty("type")
    private String type;
    @JsonProperty("urlRewrite")
    private HTTPURLRewriteFilter urlRewrite;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public HTTPRouteFilter() {
    }

    /**
     * 
     * @param requestMirror
     * @param requestRedirect
     * @param requestHeaderModifier
     * @param responseHeaderModifier
     * @param type
     * @param urlRewrite
     * @param extensionRef
     */
    public HTTPRouteFilter(io.fabric8.kubernetes.api.model.gatewayapi.v1beta1.LocalObjectReference extensionRef, HTTPHeaderFilter requestHeaderModifier, HTTPRequestMirrorFilter requestMirror, HTTPRequestRedirectFilter requestRedirect, HTTPHeaderFilter responseHeaderModifier, String type, HTTPURLRewriteFilter urlRewrite) {
        super();
        this.extensionRef = extensionRef;
        this.requestHeaderModifier = requestHeaderModifier;
        this.requestMirror = requestMirror;
        this.requestRedirect = requestRedirect;
        this.responseHeaderModifier = responseHeaderModifier;
        this.type = type;
        this.urlRewrite = urlRewrite;
    }

    @JsonProperty("extensionRef")
    public io.fabric8.kubernetes.api.model.gatewayapi.v1beta1.LocalObjectReference getExtensionRef() {
        return extensionRef;
    }

    @JsonProperty("extensionRef")
    public void setExtensionRef(io.fabric8.kubernetes.api.model.gatewayapi.v1beta1.LocalObjectReference extensionRef) {
        this.extensionRef = extensionRef;
    }

    @JsonProperty("requestHeaderModifier")
    public HTTPHeaderFilter getRequestHeaderModifier() {
        return requestHeaderModifier;
    }

    @JsonProperty("requestHeaderModifier")
    public void setRequestHeaderModifier(HTTPHeaderFilter requestHeaderModifier) {
        this.requestHeaderModifier = requestHeaderModifier;
    }

    @JsonProperty("requestMirror")
    public HTTPRequestMirrorFilter getRequestMirror() {
        return requestMirror;
    }

    @JsonProperty("requestMirror")
    public void setRequestMirror(HTTPRequestMirrorFilter requestMirror) {
        this.requestMirror = requestMirror;
    }

    @JsonProperty("requestRedirect")
    public HTTPRequestRedirectFilter getRequestRedirect() {
        return requestRedirect;
    }

    @JsonProperty("requestRedirect")
    public void setRequestRedirect(HTTPRequestRedirectFilter requestRedirect) {
        this.requestRedirect = requestRedirect;
    }

    @JsonProperty("responseHeaderModifier")
    public HTTPHeaderFilter getResponseHeaderModifier() {
        return responseHeaderModifier;
    }

    @JsonProperty("responseHeaderModifier")
    public void setResponseHeaderModifier(HTTPHeaderFilter responseHeaderModifier) {
        this.responseHeaderModifier = responseHeaderModifier;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("urlRewrite")
    public HTTPURLRewriteFilter getUrlRewrite() {
        return urlRewrite;
    }

    @JsonProperty("urlRewrite")
    public void setUrlRewrite(HTTPURLRewriteFilter urlRewrite) {
        this.urlRewrite = urlRewrite;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
