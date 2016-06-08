package org.ff4j.web.embedded;

import static org.ff4j.web.WebConstants.BUFFER_SIZE;
import static org.ff4j.web.WebConstants.DESCRIPTION;
import static org.ff4j.web.WebConstants.FEATID;
import static org.ff4j.web.WebConstants.GROUPNAME;
import static org.ff4j.web.WebConstants.PERMISSION;
import static org.ff4j.web.WebConstants.PERMISSION_RESTRICTED;
import static org.ff4j.web.WebConstants.PREFIX_CHECKBOX;
import static org.ff4j.web.WebConstants.STRATEGY;
import static org.ff4j.web.WebConstants.STRATEGY_INIT;

/*
 * #%L
 * ff4j-web
 * %%
 * Copyright (C) 2013 - 2015 FF4J
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ff4j.FF4j;
import org.ff4j.conf.XmlConfig;
import org.ff4j.conf.XmlParser;
import org.ff4j.core.Feature;
import org.ff4j.core.FeatureStore;
import org.ff4j.core.FlippingStrategy;
import org.ff4j.property.Property;
import org.ff4j.property.store.PropertyStore;
import org.ff4j.property.util.PropertyFactory;
import org.ff4j.utils.Util;
import org.ff4j.web.WebConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ConsoleOperations {
    
    /** Logger for this class. */
    private static Logger LOGGER = LoggerFactory.getLogger(ConsoleOperations.class);
    
    private ConsoleOperations() {}
    
    /**
     * User action to create a new Feature.
     * 
     * @param req
     *            http request containing operation parameters
     */
    public static void createFeature(FF4j ff4j, HttpServletRequest req) {
        // uid
        final String featureId = req.getParameter(FEATID);
        if (featureId != null && !featureId.isEmpty()) {
            Feature fp = new Feature(featureId, false);

            // Description
            final String featureDesc = req.getParameter(DESCRIPTION);
            if (null != featureDesc && !featureDesc.isEmpty()) {
                fp.setDescription(featureDesc);
            }

            // GroupName
            final String groupName = req.getParameter(GROUPNAME);
            if (null != groupName && !groupName.isEmpty()) {
                fp.setGroup(groupName);
            }

            // Strategy
            final String strategy = req.getParameter(STRATEGY);
            if (null != strategy && !strategy.isEmpty()) {
                try {
                    Class<?> strategyClass = Class.forName(strategy);
                    FlippingStrategy fstrategy = (FlippingStrategy) strategyClass.newInstance();

                    final String strategyParams = req.getParameter(STRATEGY_INIT);
                    if (null != strategyParams && !strategyParams.isEmpty()) {
                        Map<String, String> initParams = new HashMap<String, String>();
                        String[] params = strategyParams.split(";");
                        for (String currentP : params) {
                            String[] cur = currentP.split("=");
                            if (cur.length < 2) {
                                throw new IllegalArgumentException("Invalid Syntax : param1=val1,val2;param2=val3,val4");
                            }
                            initParams.put(cur[0], cur[1]);
                        }
                        fstrategy.init(featureId, initParams);
                    }
                    fp.setFlippingStrategy(fstrategy);

                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException("Cannot find strategy class", e);
                } catch (InstantiationException e) {
                    throw new IllegalArgumentException("Cannot instantiate strategy", e);
                } catch (IllegalAccessException e) {
                    throw new IllegalArgumentException("Cannot instantiate : no public constructor", e);
                }
            }

            // Permissions
            final String permission = req.getParameter(PERMISSION);
            if (null != permission && PERMISSION_RESTRICTED.equals(permission)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> parameters = req.getParameterMap();
                Set<String> permissions = new HashSet<String>();
                for (String key : parameters.keySet()) {
                    if (key.startsWith(PREFIX_CHECKBOX)) {
                        permissions.add(key.replace(PREFIX_CHECKBOX, ""));
                    }
                }
                fp.setPermissions(permissions);
            }

            // Creation
            ff4j.getFeatureStore().create(fp);
            LOGGER.info(featureId + " has been created");
        }
    }
    
    /**
     * Sample Element should be updated like name, description, value
     * @param ff4j
     * @param req
     */
    public static void updateProperty(FF4j ff4j, HttpServletRequest req) {
        String name         = req.getParameter("name");
        String type         = req.getParameter("pType");
        String description  = req.getParameter("desc");
        String value        = req.getParameter("pValue");
        String featureUid   = req.getParameter(WebConstants.FEATURE_UID);
        
        Property<?> ap = PropertyFactory.createProperty(name, type, value);
        ap.setDescription(description);
        
        // Update Property for a Feature
        if (Util.hasLength(featureUid) && ff4j.getFeatureStore().exist(featureUid)) {
           Feature relfeat = ff4j.getFeatureStore().read(featureUid);
           relfeat.addProperty(ap);
           ff4j.getFeatureStore().update(relfeat);
        
        // Update Property standalone
        } else if (ff4j.getPropertiesStore().existProperty(name)) {
           Property<?> old = ff4j.getPropertiesStore().readProperty(name);
           if (old.getType().equalsIgnoreCase(type)) {
               old.setDescription(description);
               old.setValueFromString(value);
               ff4j.getPropertiesStore().updateProperty(old);
           } else {
               ff4j.getPropertiesStore().updateProperty(ap);
           }
        }
    }
    
    /**
     * Create new property in store.
     *
     * @param ff4j
     *      current ff4j instance.
     * @param req
     *      current http request
     */
    public static void createProperty(FF4j ff4j, HttpServletRequest req) {
        String name         = req.getParameter("name");
        String type         = req.getParameter("pType");
        String description  = req.getParameter("desc");
        String value        = req.getParameter("pValue");
        String featureUid   = req.getParameter(WebConstants.FEATURE_UID);
        
        Property<?> ap = PropertyFactory.createProperty(name, type, value);
        ap.setDescription(description);
        
        // Create Property for a Feature
        if (Util.hasLength(featureUid) && ff4j.getFeatureStore().exist(featureUid)) {
            Feature relfeat = ff4j.getFeatureStore().read(featureUid);
            relfeat.addProperty(ap);
            ff4j.getFeatureStore().update(relfeat);
        } else {
            ff4j.getPropertiesStore().createProperty(ap);
        }
    }
    
   
    /**
     * User action to import Features from a properties files.
     * 
     * @param in
     *            inpustream from configuration file
     * @throws IOException
     *             Error raised if the configuration cannot be read
     */
    public static void importFile(FF4j ff4j, InputStream in) 
    throws IOException {
        
        FeatureStore store = ff4j.getFeatureStore();
        XmlConfig xmlConfig = new XmlParser().parseConfigurationFile(in);
        Map<String, Feature> mapsOfFeat = xmlConfig.getFeatures();
        for (Entry<String, Feature> feature : mapsOfFeat.entrySet()) {
            if (store.exist(feature.getKey())) {
                store.update(feature.getValue());
            } else {
                store.create(feature.getValue());
            }
        }
        LOGGER.info(mapsOfFeat.size() + " features have been imported.");
        
        PropertyStore pstore = ff4j.getPropertiesStore();
        Map<String, Property<?>> mapsOfProperties = xmlConfig.getProperties();
        for (Entry<String, Property<?>> p : mapsOfProperties.entrySet()) {
            if (pstore.existProperty(p.getKey())) {
                pstore.updateProperty(p.getValue());
            } else {
                pstore.createProperty(p.getValue());
            }
        }
        LOGGER.info(mapsOfProperties.size() + " properties have been imported.");
    }
    
    /**
     * Build Http response when invoking export features.
     * 
     * @param res
     *            http response
     * @throws IOException
     *             error when building response
     */
    public static void exportFile(FF4j ff4j, HttpServletResponse res) throws IOException {
        Map<String, Feature> features = ff4j.getFeatureStore().readAll();
        InputStream in = new XmlParser().exportFeatures(features);
        ServletOutputStream sos = null;
        try {
            sos = res.getOutputStream();
            res.setContentType("text/xml");
            res.setHeader("Content-Disposition", "attachment; filename=\"ff4j.xml\"");
            // res.setContentLength()
            byte[] bbuf = new byte[BUFFER_SIZE];
            int length = 0;
            while ((in != null) && (length != -1)) {
                length = in.read(bbuf);
                sos.write(bbuf, 0, length);
            }
            LOGGER.info(features.size() + " features have been exported.");
        } finally {
            if (in != null) {
                in.close();
            }
            if (sos != null) {
                sos.flush();
                sos.close();
            }
        }
    }
}
