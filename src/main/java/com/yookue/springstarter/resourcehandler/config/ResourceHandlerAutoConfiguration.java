/*
 * Copyright (c) 2022 Yookue Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yookue.springstarter.resourcehandler.config;


import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.config.annotation.ResourceChainRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.yookue.commonplexus.javaseutil.constant.CharVariantConst;
import com.yookue.commonplexus.javaseutil.constant.StringVariantConst;
import com.yookue.commonplexus.javaseutil.util.FilenamePlainWraps;
import com.yookue.commonplexus.javaseutil.util.MapPlainWraps;
import com.yookue.commonplexus.javaseutil.util.StringUtilsWraps;
import com.yookue.commonplexus.springutil.constant.AntPathConst;
import com.yookue.commonplexus.springutil.util.ResourceConfigWraps;
import com.yookue.springstarter.resourcehandler.property.ResourceHandlerProperties;
import lombok.RequiredArgsConstructor;


/**
 * Configuration for resource handlers and mappings
 *
 * @author David Hsing
 * @reference "http://blog.csdn.net/isea533/article/details/50412212"
 * @reference "http://blog.csdn.net/catoop/article/details/50501706"
 * @see org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration
 * @see org.springframework.web.servlet.handler.SimpleUrlHandlerMapping
 * @see org.springframework.web.servlet.config.annotation.ResourceChainRegistration
 * @see org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = ResourceHandlerAutoConfiguration.PROPERTIES_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@AutoConfigureAfter(value = WebMvcAutoConfiguration.class)
@RequiredArgsConstructor
@EnableConfigurationProperties(value = ResourceHandlerProperties.class)
@SuppressWarnings({"JavadocDeclaration", "JavadocLinkAsPlainText"})
public class ResourceHandlerAutoConfiguration implements InitializingBean, WebMvcConfigurer {
    public static final String PROPERTIES_PREFIX = "spring.resource-handler";    // $NON-NLS-1$
    private final ApplicationContext applicationContext;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private final WebProperties webProperties;

    private final ResourceHandlerProperties handlerProperties;

    @Override
    public void afterPropertiesSet() {
        Map<String, String> versional = handlerProperties.getResourceMapping().getVersional();
        Map<String, String> original = handlerProperties.getResourceMapping().getOriginal();
        if (BooleanUtils.isTrue(handlerProperties.getResourceMapping().getAddDefaultVersional())) {
            versional.put(AntPathConst.SLASH_ASSET_STARS, StringVariantConst.CLASSPATH_COLON + AntPathConst.SLASH_ASSET + CharVariantConst.SLASH);
            versional.put(AntPathConst.SLASH_STATIC_STARS, StringVariantConst.CLASSPATH_COLON + AntPathConst.SLASH_STATIC + CharVariantConst.SLASH);
        }
        if (BooleanUtils.isTrue(handlerProperties.getResourceMapping().getAddDefaultOriginal())) {
            original.put(AntPathConst.SLASH_STORAGE_STARS, StringVariantConst.CLASSPATH_COLON + AntPathConst.SLASH_STORAGE + CharVariantConst.SLASH);
        }
        String resource = handlerProperties.getVirtualRoot().getResource();
        if (BooleanUtils.isTrue(handlerProperties.getVirtualRoot().getEnabled()) && StringUtils.isNotBlank(resource)) {
            original.put(AntPathConst.SLASH_VROOT_STARS, resource);
        }
    }

    @Override
    public void addResourceHandlers(@Nonnull ResourceHandlerRegistry registry) {
        configMappings(registry, handlerProperties.getResourceMapping().getVersional(), true);
        configMappings(registry, handlerProperties.getResourceMapping().getOriginal(), false);
    }

    @Override
    public void addViewControllers(@Nonnull ViewControllerRegistry registry) {
        if (BooleanUtils.isFalse(handlerProperties.getVirtualRoot().getEnabled()) || StringUtils.isBlank(handlerProperties.getVirtualRoot().getResource())) {
            return;
        }
        Set<String> filenames = detectFilenames();
        if (CollectionUtils.isEmpty(filenames)) {
            return;
        }
        for (String filename : filenames) {
            String visitPath = CharVariantConst.SLASH + filename;
            String targetPath = AntPathConst.SLASH_VROOT + CharVariantConst.SLASH + filename;
            registry.addRedirectViewController(visitPath, targetPath);
        }
    }

    private void configMappings(@Nonnull ResourceHandlerRegistry registry, @Nullable Map<String, String> mappings, boolean chain) {
        if (MapPlainWraps.isEmpty(mappings)) {
            return;
        }
        for (Map.Entry<String, String> entry : mappings.entrySet()) {
            if (StringUtils.isAnyBlank(entry.getKey(), entry.getValue()) || registry.hasMappingForPattern(entry.getKey())) {
                continue;
            }
            ResourceHandlerRegistration handlerRegistration = registry.addResourceHandler(entry.getKey()).addResourceLocations(entry.getValue());
            ResourceConfigWraps.configResourceCache(webProperties.getResources(), handlerRegistration);
            if (!chain) {
                continue;
            }
            ResourceChainRegistration chainRegistration = handlerRegistration.resourceChain(webProperties.getResources().getChain().isCache());
            ResourceConfigWraps.configResourceChain(webProperties.getResources().getChain(), chainRegistration);
        }
    }

    @Nullable
    private Set<String> detectFilenames() {
        Set<String> result = new HashSet<>();
        String pattern = StringUtilsWraps.appendIfMissing(FilenamePlainWraps.removeEndSlashes(handlerProperties.getVirtualRoot().getResource()), AntPathConst.SLASH_STAR);
        try {
            Resource[] resources = applicationContext.getResources(StringVariantConst.CLASSPATH_STAR_COLON + pattern);
            if (ArrayUtils.isEmpty(resources)) {
                return null;
            }
            for (Resource resource : resources) {
                if (resource.exists() && resource.isFile()) {
                    result.add(resource.getFilename());
                }
            }
        } catch (Exception ignored) {
        }
        return CollectionUtils.isEmpty(result) ? null : result;
    }
}
