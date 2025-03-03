package com.nirima.jenkins.plugins.docker.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import hudson.Extension;
import hudson.model.Action;
import hudson.model.Describable;
import hudson.model.Descriptor;
import io.jenkins.docker.DockerTransientNode;
import io.jenkins.docker.client.DockerAPI;
import java.io.IOException;
import java.io.Serializable;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Created by magnayn on 10/01/2014.
 */
@ExportedBean
public class DockerBuildAction implements Action, Serializable, Describable<DockerBuildAction> {

    private String cloudId;
    private final String containerHost;
    private final String containerId;
    private String inspect;
    private String taggedId;

    public DockerBuildAction(String containerHost, String containerId, String taggedId) {
        this.containerHost = containerHost;
        this.containerId = containerId;
        this.taggedId = taggedId;
    }

    public DockerBuildAction(DockerTransientNode node) {
        final DockerAPI dockerAPI = node.getDockerAPI();
        this.containerHost = dockerAPI.getDockerHost().getUri();
        this.containerId = node.getContainerId();
        this.cloudId = node.getCloudId();
        try {
            final InspectContainerResponse containerDetails;
            try (final DockerClient client = dockerAPI.getClient()) {
                containerDetails = client.inspectContainerCmd(containerId).exec();
            }
            this.inspect = new ObjectMapper()
                    .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                    .enable(SerializationFeature.INDENT_OUTPUT)
                    .writeValueAsString(containerDetails);
        } catch (IOException e) {
            this.inspect = "Failed to capture container inspection data: " + e.getMessage();
        }
    }

    public String getCloudId() {
        return cloudId;
    }

    public String getContainerHost() {
        return containerHost;
    }

    public String getContainerId() {
        return containerId;
    }

    public String getTaggedId() {
        return taggedId;
    }

    public String getInspect() {
        return inspect;
    }

    @Override
    public String getIconFileName() {
        return "symbol-logo-docker plugin-ionicons-api";
    }

    @Override
    public String getDisplayName() {
        return "Built on Docker";
    }

    @Override
    public String getUrlName() {
        return "docker";
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) Jenkins.get().getDescriptorOrDie(getClass());
    }

    /**
     * Just for assisting form related stuff.
     */
    @Extension
    public static class DescriptorImpl extends Descriptor<DockerBuildAction> {
        @Override
        public String getDisplayName() {
            return "Docker";
        }
    }
}
