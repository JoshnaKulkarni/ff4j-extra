package org.ff4j.console;

/*
 * #%L
 * ff4j-console
 * %%
 * Copyright (C) 2013 - 2014 Ff4J
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

/**
 * Constants to be used in the application.
 *
 * @author <a href="mailto:cedrick.lunven@gmail.com">Cedrick LUNVEN</a>
 */
public interface ApplicationConstants {

    // Content of Page
    String PAGE_TITLE = "title";

    String PAGE_SUBTITLE = "subtitle";

    // ----- VIEWS ------------

    String VIEW_HOME = "home";

    String VIEW_FEATURES = "features";

    String VIEW_STATS = "stats";

    String VIEW_SETTINGS = "settings";

    // ---- Roles ----

    String ROLE_USER = "ROLE_USER";

    String ROLE_ADMIN = "‚ROLE_ADMIN";

    // ---- Sessions Beans ----

    String SESSION_HOMEBEAN = "HOME";

    // ----- Model Attributes ------

    String ATTR_HOMEBEAN = "homebean";

    String ATTR_ENVBEAN = "envbean";

    // ---- Form Params ----

    String FORM_PARAM_ENVIRONMENT = "env";

    String MODE_HTTP = "http";

    String MODE_JMX = "jmx";

}
