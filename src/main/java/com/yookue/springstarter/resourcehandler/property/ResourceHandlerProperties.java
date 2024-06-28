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

package com.yookue.springstarter.resourcehandler.property;


import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import com.yookue.commonplexus.javaseutil.constant.CharVariantConst;
import com.yookue.commonplexus.javaseutil.constant.StringVariantConst;
import com.yookue.commonplexus.springutil.constant.AntPathConst;
import com.yookue.springstarter.resourcehandler.config.ResourceHandlerAutoConfiguration;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * Properties for resource handler
 *
 * @author David Hsing
 */
@ConfigurationProperties(prefix = ResourceHandlerAutoConfiguration.PROPERTIES_PREFIX)
@Getter
@Setter
@ToString
public class ResourceHandlerProperties implements Serializable {
    /**
     * Indicates whether to enable this starter or not
     * <p>
     * Default is {@code true}
     */
    private Boolean enabled = true;

    private final ResourceMapping resourceMapping = new ResourceMapping();
    private final VirtualRoot virtualRoot = new VirtualRoot();


    /**
     * Properties for resource mapping
     *
     * @author David Hsing
     */
    @Getter
    @Setter
    @ToString
    public static class ResourceMapping implements Serializable {
        /**
         * The resource mappings with version suffix for each resource files
         * <p>
         * For example, a file named "foobar.txt" under the "resources/static/" folder, will be mapped to request url "/static/foobar-${version}.txt"
         */
        private final Map<String, String> versional = new LinkedHashMap<>();

        /**
         * The resource mappings with original names for each resource files
         * <p>
         * For example, a file named "foobar.txt" under the "resources/static/" folder, will be mapped to request url "/static/foobar.txt"
         */
        private final Map<String, String> original = new LinkedHashMap<>();

        /**
         * Enable default versional mappings for {@code resourceMapping}, default is {@code true}
         * <p>
         * Versional mappings includes "/asset/**", "/static/**"
         */
        private Boolean addDefaultVersional = true;

        /**
         * Enable default original mappings for {@code resourceMapping}
         * <p>
         * Original mappings includes "/storage/**"
         */
        private Boolean addDefaultOriginal;
    }


    /**
     * Properties for virtual root
     *
     * @author David Hsing
     */
    @Getter
    @Setter
    @ToString
    public static class VirtualRoot implements Serializable {
        /**
         * Enable virtual root folder named "vroot"
         * <p>
         * This will add the virtual root mapping "/vroot/**" to the {@code original} resource mapping
         * <p>
         * For example, a file named "robots.txt" under the "resources/vroot" folder, then the redirected request url is "/robots.txt"
         */
        private Boolean enabled;

        /**
         * The virtual root folder path
         */
        private String resource = StringVariantConst.CLASSPATH_COLON + AntPathConst.SLASH_VROOT + CharVariantConst.SLASH;
    }
}
