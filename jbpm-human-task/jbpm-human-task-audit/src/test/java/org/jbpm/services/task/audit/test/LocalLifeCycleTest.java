/*
 * Copyright 2012 JBoss by Red Hat.
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
package org.jbpm.services.task.audit.test;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.jbpm.services.task.HumanTaskServiceFactory;
import org.jbpm.services.task.audit.TaskAuditServiceFactory;
import org.jbpm.services.task.audit.index.GroupAuditTaskIndex;
import org.jbpm.services.task.audit.index.HistoryAuditTaskIndex;
import org.jbpm.services.task.audit.index.IndexingTaskLifeCycleEventListener;
import org.jbpm.services.task.audit.index.LuceneIndexService;
import org.jbpm.services.task.audit.index.TaskEventIndex;
import org.jbpm.services.task.audit.index.UserAuditTaskIndex;
import org.jbpm.services.task.audit.service.TaskAuditServiceImpl;
import org.jbpm.services.task.lifecycle.listeners.BAMTaskEventListener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.internal.task.api.InternalTaskService;

import bitronix.tm.resource.jdbc.PoolingDataSource;


/**
 *
 *
 */

public class LocalLifeCycleTest extends LifeCycleBaseTest {

	private PoolingDataSource pds;
	private EntityManagerFactory emf;
	private LuceneIndexService indexService;
	
	@Before
	public void setup() {
        pds = setupPoolingDataSource();
		emf = Persistence.createEntityManagerFactory( "org.jbpm.services.task" );

        this.indexService = new LuceneIndexService();
        indexService.addModel(new UserAuditTaskIndex());
        indexService.addModel(new GroupAuditTaskIndex());
        indexService.addModel(new TaskEventIndex());
        indexService.addModel(new HistoryAuditTaskIndex());
        IndexingTaskLifeCycleEventListener listener = new IndexingTaskLifeCycleEventListener(indexService);


        this.taskService = (InternalTaskService) HumanTaskServiceFactory.newTaskServiceConfigurator()
            .entityManagerFactory(emf)
            .listener(listener)
            .listener(new BAMTaskEventListener())
            .getTaskService();

        this.taskAuditService = TaskAuditServiceFactory.
            newTaskAuditServiceConfigurator().
            setTaskService(taskService).
            setIndexService(indexService).
            setMultiThreadIndex(true).
            getTaskAuditService();

	}
	
	@After
	public void clean() {
		if (emf != null) {
			emf.close();
		}
		if (pds != null) {
			pds.close();
		}
	}
}
