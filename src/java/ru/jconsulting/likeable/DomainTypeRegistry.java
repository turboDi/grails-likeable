/* Copyright 2014 Dmitry Borisov
 *
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
 */
package ru.jconsulting.likeable;

import grails.util.GrailsNameUtils;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DomainTypeRegistry implements InitializingBean {

    private List<Class> domainClassList;

    private Map<String, Class> type2ClassMap;

    @Override
    public void afterPropertiesSet() throws Exception {
        type2ClassMap = new ConcurrentHashMap<String, Class>(domainClassList.size());
        for (Class clazz : domainClassList) {
            type2ClassMap.put(GrailsNameUtils.getPropertyName(clazz), clazz);
        }
    }

    public void setDomainClassList(List<Class> domainClassList) {
        this.domainClassList = domainClassList;
    }

    public Class getClassByType(String type) {
        if (type2ClassMap.containsKey(type)) {
            return type2ClassMap.get(type);
        } else {
            throw new LikeException(String.format("Type '%s' is not a Likeable type", type));
        }
    }
}
